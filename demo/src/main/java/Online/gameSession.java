package Online;

import java.io.IOException;
import java.util.UUID;

import Control.GameController;
import Factory.GameFactory;
import Factory.GameFactory.GameType;
import Model.*;

public class gameSession implements Runnable {
    private String sessionId;
    private playerHandler player1; // X player
    private playerHandler player2; // O player
    private GameController gameController;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private Server server;
    
    public gameSession(playerHandler player1, playerHandler player2, GameType gameType, Server server) {
        this.sessionId = UUID.randomUUID().toString();
        this.player1 = player1;
        this.player2 = player2;
        this.server = server;
        
        // Create players
        Player p1 = player1.getPlayer();
        Player p2 = player2.getPlayer();
        
        // Set symbols
        p1.setSymbol(Symbol.X);
        p2.setSymbol(Symbol.O);
        
        // Create game controller
        this.gameController = new GameController(p1, p2, gameType);
        
        // Set players' game session
        player1.setGameSession(this);
        player2.setGameSession(this);
    }
    
    @Override
    public void run() {
        try {
            startGame();
            
            // Session thread mainly monitors game state
            while (!gameOver) {
                try {
                    Thread.sleep(1000);
                    // Could check for timeouts here
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error in game session: " + e.getMessage());
        }
    }
    
    public void startGame() {
        try {
            gameStarted = true;
            
            // Tell players the game is starting
            String gameTypeStr = gameController.getGame() instanceof Game ? "STANDARD" : "ULTIMATE";
            player1.sendMessage("GAME_START:" + gameTypeStr + ":X:" + player2.getPlayerName());
            player2.sendMessage("GAME_START:" + gameTypeStr + ":O:" + player1.getPlayerName());
            
            // Send initial board state
            sendBoardState();
            
            // X player goes first
            player1.sendMessage("YOUR_TURN:true");
            player2.sendMessage("YOUR_TURN:false");
            
        } catch (IOException e) {
            System.err.println("Error starting game: " + e.getMessage());
        }
    }
    
    public void handleMove(playerHandler player, int row, int col) {
        try {
            // Verify it's this player's turn
            Player currentPlayer = gameController.getGame().getCurrentPlayer();
            boolean isPlayer1Turn = currentPlayer == player1.getPlayer();
            
            if ((player == player1 && !isPlayer1Turn) || (player == player2 && isPlayer1Turn)) {
                player.sendMessage("ERROR:NotYourTurn");
                return;
            }
            
            // Try to make the move
            if (gameController.handleMove(row, col)) {
                // Broadcast the move to both players
                String symbol = isPlayer1Turn ? "X" : "O";
                broadcast("MOVE:" + row + ":" + col + ":" + symbol);
                
                // Update board state
                sendBoardState();
                
                // Check if game is over
                if (gameController.getGame().isGameOver()) {
                    handleGameOver();
                } else {
                    // Switch turns
                    Player newCurrentPlayer = gameController.getGame().getCurrentPlayer();
                    boolean isNowPlayer1Turn = newCurrentPlayer == player1.getPlayer();
                    player1.sendMessage("YOUR_TURN:" + isNowPlayer1Turn);
                    player2.sendMessage("YOUR_TURN:" + !isNowPlayer1Turn);
                }
            } else {
                player.sendMessage("ERROR:InvalidMove");
            }
        } catch (IOException e) {
            System.err.println("Error handling move: " + e.getMessage());
        }
    }
    
    private void sendBoardState() throws IOException {
        BoardGame board = gameController.getGame().getBoard();
        Cell[][] cells = board.getCells();
        
        StringBuilder boardState = new StringBuilder("BOARD:");
        
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                Symbol symbol = cells[i][j].getSymbol();
                boardState.append(i).append(",")
                         .append(j).append(",")
                         .append(symbol == null ? "EMPTY" : symbol.toString())
                         .append(";");
            }
        }
        
        broadcast(boardState.toString());
    }
    
    private void handleGameOver() throws IOException {
        gameOver = true;
        
        Player winner = gameController.getGame().getWinner();
        if (winner != null) {
            // Someone won
            if (winner == player1.getPlayer()) {
                player1.sendMessage("GAME_OVER:WIN");
                player2.sendMessage("GAME_OVER:LOSE");
            } else {
                player1.sendMessage("GAME_OVER:LOSE");
                player2.sendMessage("GAME_OVER:WIN");
            }
        } else {
            // Draw
            broadcast("GAME_OVER:DRAW");
        }
        
        // Notify server to end this session
        server.endGameSession(sessionId);
    }
    
    public void handleDisconnect(playerHandler player) {
        if (gameOver) return;
        
        gameOver = true;
        
        try {
            // Notify the other player
            if (player == player1 && player2 != null) {
                player2.sendMessage("OPPONENT_DISCONNECTED");
            } else if (player == player2 && player1 != null) {
                player1.sendMessage("OPPONENT_DISCONNECTED");
            }
            
            // End the session
            server.endGameSession(sessionId);
        } catch (IOException e) {
            System.err.println("Error handling disconnect: " + e.getMessage());
        }
    }
    
    public void handleChat(playerHandler from, String message) {
        try {
            String chatMessage = "CHAT:" + from.getPlayerName() + ":" + message;
            broadcast(chatMessage);
        } catch (IOException e) {
            System.err.println("Error sending chat: " + e.getMessage());
        }
    }
    
    private void broadcast(String message) throws IOException {
        player1.sendMessage(message);
        player2.sendMessage(message);
    }
}
