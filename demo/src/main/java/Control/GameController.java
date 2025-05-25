package Control;

import Factory.GameFactory;
import Factory.GameFactory.GameType;
import Model.*;
/**
 * @author Le Duc Thanh Kim 
 * This class is designed to controll a game
 * It acts as mediator between the model and the view layer
 * handling game logic operations and state in game
 */
public class GameController {
    private GameInterface game;
    
    public GameController(Player player1, Player player2, GameType gameType) {
        game = GameFactory.createGame(gameType, player1, player2);
    }
    /**
     * Handle players move at specific pos
     * Validates the move and updates the game state
     * @param row The row index where the move is attempted
     * @param col The column index where the move is attempted
     * T
     */
    public boolean handleMove(int row, int col) {
        GameInterface game = getGame();
        // Print debug info before making move
        System.out.println("Attempting move at row=" + row + ", col=" + col + 
                          " for player " + game.getCurrentPlayer().getName() + 
                          " with symbol " + game.getCurrentPlayer().getSymbol());
        
        boolean moveSuccess = game.playTurn(row, col);
        
        if (moveSuccess) {
            System.out.println("Move successful. Game over? " + game.isGameOver());
            if (game.isGameOver()) {
                Player winner = game.getWinner();
                System.out.println("Winner: " + (winner != null ? winner.getName() : "Draw"));
            }
        } else {
            System.out.println("Move failed");
        }
        
        return moveSuccess;
    }
    
    public GameInterface getGame() {
        return game;
    }

    public void setGame(GameInterface game) {
        this.game = game;
    }
    /**
     * Handles the end-game state when player wins
     * Sets the game as over and records who wins the game
     * @param player
     */
    public void handleWin(Player player) {
        // Only end the game, don't update database
        game.setGameOver(true);
        game.setWinner(player);

    }
}
