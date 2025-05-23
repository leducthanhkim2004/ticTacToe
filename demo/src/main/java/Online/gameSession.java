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
    private GameType gameType;
    
    public gameSession(playerHandler player1, playerHandler player2, GameType gameType, Server server) {
        this.sessionId = UUID.randomUUID().toString();
        this.player1 = player1;
        this.player2 = player2;
        this.server = server;
        this.gameType = gameType;
        
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
            e.printStackTrace();
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
            if (parts.length < 2) {
                player.sendMessage("ERROR:InvalidMoveFormat");
                return;
            }
            
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            
            System.out.println("\n=== NEW MOVE ===");
            System.out.println("Player " + player.getPlayer().getName() + " with symbol " + 
                              player.getPlayer().getSymbol() + " moving at [" + row + ", " + col + "]");
            
            // Verify it's this player's turn
            Player currentPlayer = gameController.getGame().getCurrentPlayer();
            if (currentPlayer == null) {
                player.sendMessage("ERROR:GameNotInitialized");
                return;
            }
            
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
                
                // Debug the board state
                debugBoardState();
                
                // Update board state
                sendBoardState();
                
                // Check win condition manually after the move
                if (gameController.getGame().getBoard().checkWin(currentPlayer)) {
                    System.out.println("Win detected for " + currentPlayer.getName() + 
                                     " with symbol " + currentPlayer.getSymbol());
                    
                    // Set game over and winner in the game model
                    setGameOver(currentPlayer);
                    
                    // Process the game over state
                    handleGameOver();
                    return;
                } else if (gameController.getGame().getBoard().isBoardFull()) {
                    System.out.println("Board is full - game is a draw");
                    
                    // Set game over with no winner (draw)
                    setGameOver(null);
                    
                    // Process the game over state
                    handleGameOver();
                    return;
                }
                
                // If no win detected, switch turns
                switchCurrentPlayer();
                
                // Get the new current player and update clients
                Player newCurrentPlayer = gameController.getGame().getCurrentPlayer();
                boolean isNowPlayer1Turn = newCurrentPlayer == player1.getPlayer();
                player1.sendMessage("YOUR_TURN:" + isNowPlayer1Turn);
                player2.sendMessage("YOUR_TURN:" + !isNowPlayer1Turn);
            } else {
                player.sendMessage("ERROR:InvalidMove");
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            player.sendMessage("ERROR:InvalidMoveFormat");
            System.err.println("Invalid move format: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            player.sendMessage("ERROR:ServerError");
            System.err.println("Unexpected error processing move: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the game as over and records the winner
     */
    private void setGameOver(Player winner) {
        // Mark the game as over
        gameOver = true;
        
        // Set the winner in the game model
        GameInterface game = gameController.getGame();
        
        try {
            // Use reflection to invoke setGameOver method if it exists
            java.lang.reflect.Method setGameOverMethod = 
                game.getClass().getMethod("setGameOver", boolean.class);
            setGameOverMethod.invoke(game, true);
            
            if (winner != null) {
                java.lang.reflect.Method setWinnerMethod = 
                    game.getClass().getMethod("setWinner", Player.class);
                setWinnerMethod.invoke(game, winner);
            }
        } catch (Exception e) {
            // If reflection fails, use the GameController's method if available
            try {
                java.lang.reflect.Method endGameMethod = 
                    gameController.getClass().getMethod("endGame", Player.class);
                endGameMethod.invoke(gameController, winner);
            } catch (Exception ex) {
                System.err.println("Could not set game over state: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Switches to the other player's turn
     */
    private void switchCurrentPlayer() {
        try {
            // Try to use the game's built-in switch player method
            GameInterface game = gameController.getGame();
            
            // Try using reflection to call switchPlayer
            try {
                java.lang.reflect.Method switchPlayerMethod = 
                    game.getClass().getMethod("switchPlayer");
                switchPlayerMethod.invoke(game);
                return;
            } catch (Exception e) {
                // Method might be protected or have different name
                System.err.println("Could not call switchPlayer via reflection: " + e.getMessage());
            }
            
            // Fallback: manually set current player
            Player currentPlayer = game.getCurrentPlayer();
            Player player1Model = player1.getPlayer();
            Player player2Model = player2.getPlayer();
            
            // Determine which player is next
            Player nextPlayer = (currentPlayer == player1Model) ? player2Model : player1Model;
            
            // Try to set the current player via reflection
            try {
                java.lang.reflect.Method setCurrentPlayerMethod = 
                    game.getClass().getMethod("setCurrentPlayer", Player.class);
                setCurrentPlayerMethod.invoke(game, nextPlayer);
            } catch (Exception e) {
                System.err.println("Could not set current player: " + e.getMessage());
                
                // Last resort: check if GameController has a method to switch players
                try {
                    java.lang.reflect.Method switchPlayerMethod = 
                        gameController.getClass().getMethod("switchCurrentPlayer");
                    switchPlayerMethod.invoke(gameController);
                } catch (Exception ex) {
                    System.err.println("All player switching methods failed: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error during player switch: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Prints the current board state to console for debugging
     */
    private void debugBoardState() {
        try {
            BoardGame board = gameController.getGame().getBoard();
            Cell[][] cells = board.getCells();
            
            System.out.println("Current board state:");
            for (int i = 0; i < cells.length; i++) {
                StringBuilder row = new StringBuilder("Row " + i + ": ");
                for (int j = 0; j < cells[i].length; j++) {
                    Symbol symbol = cells[i][j].getSymbol();
                    row.append("[").append(symbol == null ? "-" : symbol).append("] ");
                }
                System.out.println(row.toString());
            }
        } catch (Exception e) {
            System.err.println("Error printing board state: " + e.getMessage());
        }
    }
    
    /**
     * Sends the current board state to all players
     */
    private void sendBoardState() {
        try {
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
        } catch (Exception e) {
            System.err.println("Error sending board state: " + e.getMessage());
        }
    }
    
    /**
     * Handles the game over state
     */
    private void handleGameOver() {
        try {
            GameInterface game = gameController.getGame();
            Player winner = game.getWinner();
            
            // Debug the game state
            System.out.println("Game over! Final state:");
            debugBoardState();
            
            if (winner != null) {
                // Someone won
                System.out.println("Winner: " + winner.getName() + " with symbol " + winner.getSymbol());
                
                if (winner == player1.getPlayer()) {
                    player1.sendMessage("GAME_OVER:WIN");
                    player2.sendMessage("GAME_OVER:LOSE");
                } else {
                    player1.sendMessage("GAME_OVER:LOSE");
                    player2.sendMessage("GAME_OVER:WIN");
                }
            } else {
                // Draw
                System.out.println("Game ended in a draw");
                broadcast("GAME_OVER:DRAW");
            }
            
            // Update player stats in database if they're not guests
            updatePlayerStats(winner);
            
            // Notify server to end this session
            server.endGameSession(sessionId);
        } catch (Exception e) {
            System.err.println("Error handling game over: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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
                    e.printStackTrace();
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
                        // IMPORTANT CHANGE: Use recordWinLoss instead of recordGameResult
                        // to avoid duplicate updates from the old method
                        Database.UserDatabase.recordWinLoss(
                            otherPlayer.getName(), 
                            disconnectedPlayer.getName()
                        );
                        
                        System.out.println("Recording WIN for " + otherPlayer.getName() + 
                                          " due to " + disconnectedPlayer.getName() + " disconnecting");
                    } catch (Exception e) {
                        System.err.println("Error updating stats for disconnect: " + e.getMessage());
                        e.printStackTrace();
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