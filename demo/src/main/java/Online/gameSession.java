package Online;

import java.util.UUID;

import Control.GameController;
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
    private GameType gameType; // Add this field to the class
    
    public gameSession(playerHandler player1, playerHandler player2, GameType gameType, Server server) {
        this.sessionId = UUID.randomUUID().toString();
        this.player1 = player1;
        this.player2 = player2;
        this.server = server;
        this.gameType = gameType; // Save the game type
        
        // Create players
        Player p1 = player1.getPlayer();
        Player p2 = player2.getPlayer();
        
        // Set symbols
        p1.setSymbol(Symbol.X);
        p2.setSymbol(Symbol.O);
        
        // Create game controller
        this.gameController = new GameController(p1, p2, gameType);
        
        // Set players' game session
        player1.setCurrentGame(this);
        player2.setCurrentGame(this);
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
        gameStarted = true;
        
        // Tell players the game is starting using the saved game type
        String gameTypeStr = gameType.toString();
        player1.sendMessage("GAME_START:" + gameTypeStr + ":X:" + player2.getPlayerName());
        player2.sendMessage("GAME_START:" + gameTypeStr + ":O:" + player1.getPlayerName());
        
        // Send initial board state
        sendBoardState();
        
        // X player goes first
        player1.sendMessage("YOUR_TURN:true");
        player2.sendMessage("YOUR_TURN:false");
    }
    
    public void handleMove(playerHandler player, String moveData) {
        // Parse move data
        try {
            String[] parts = moveData.split(":");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            
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
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            player.sendMessage("ERROR:InvalidMoveFormat");
        }
    }
    
    private void sendBoardState() {
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
    
    private void handleGameOver() {
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
        
        // Update player stats in database if they're not guests
        updatePlayerStats(winner);
        
        // Notify server to end this session
        server.endGameSession(sessionId);
    }
    
    // This should be in your gameSession.java file, not in UserDatabase.java
    private void updatePlayerStats(Player winner) {
        // Only update stats if players are not guests
        Player p1 = player1.getPlayer();
        Player p2 = player2.getPlayer();
        
        if (p1 != null && p2 != null) {
            if (p1.getName() != null && !p1.getName().startsWith("Guest") && 
                p2.getName() != null && !p2.getName().startsWith("Guest")) {
                
                try {
                    if (winner == null) {
                        // For a draw, we pass a special flag to indicate this
                        System.out.println("Recording DRAW between " + p1.getName() + " and " + p2.getName());
                        // IMPORTANT: Use a different method for draw to avoid double-counting
                        Database.UserDatabase.recordDraw(p1.getName(), p2.getName());
                    } else if (winner == p1) {
                        // Clear win/loss case
                        System.out.println("Recording WIN for " + p1.getName() + " against " + p2.getName());
                        // IMPORTANT: We're skipping the callback to LoginView to avoid double-counting
                        Database.UserDatabase.recordWinLoss(p1.getName(), p2.getName());
                    } else if (winner == p2) {
                        // Clear win/loss case
                        System.out.println("Recording WIN for " + p2.getName() + " against " + p1.getName());
                        // IMPORTANT: We're skipping the callback to LoginView to avoid double-counting
                        Database.UserDatabase.recordWinLoss(p2.getName(), p1.getName());
                    }
                } catch (Exception e) {
                    System.err.println("Error updating stats: " + e.getMessage());
                }
            }
        }
    }
    
    public void handlePlayerDisconnect(playerHandler player) {
        if (gameOver) return;
        
        gameOver = true;
        
        // If game was in progress, count as a loss for disconnected player
        if (gameStarted) {
            Player disconnectedPlayer = player.getPlayer();
            Player otherPlayer = (player == player1) ? player2.getPlayer() : player1.getPlayer();
            
            // Update stats if not guests
            if (disconnectedPlayer != null && otherPlayer != null) {
                if (disconnectedPlayer.getName() != null && !disconnectedPlayer.getName().startsWith("Guest") && 
                    otherPlayer.getName() != null && !otherPlayer.getName().startsWith("Guest")) {
                    
                    try {
                        // Record as a win for the player who stayed
                        Database.UserDatabase.recordGameResult(
                            otherPlayer.getName(), 
                            disconnectedPlayer.getName()
                        );
                    } catch (Exception e) {
                        System.err.println("Error updating stats for disconnect: " + e.getMessage());
                    }
                }
            }
        }
        
        // Notify the other player
        if (player == player1 && player2 != null) {
            player2.sendMessage("OPPONENT_DISCONNECTED");
            player2.endGame();
        } else if (player == player2 && player1 != null) {
            player1.sendMessage("OPPONENT_DISCONNECTED");
            player1.endGame();
        }
        
        // End the session
        server.endGameSession(sessionId);
    }
    
    public void handleChat(playerHandler from, String message) {
        String chatMessage = "CHAT:" + from.getPlayerName() + ":" + message;
        broadcast(chatMessage);
    }
    
    private void broadcast(String message) {
        if (player1 != null) player1.sendMessage(message);
        if (player2 != null) player2.sendMessage(message);
    }
    
    public String getSessionId() {
        return sessionId;
    }
}
