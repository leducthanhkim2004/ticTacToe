package Control;

import Factory.GameFactory;
import Factory.GameFactory.GameType;
import Model.*;

public class GameController {
    private GameInterface game;
    
    public GameController(Player player1, Player player2, GameType gameType) {
        game = GameFactory.createGame(gameType, player1, player2);
    }
    
    public boolean handleMove(int row, int col) {
        return game.playTurn(row, col);
    }
    
    public GameInterface getGame() {
        return game;
    }

    public void setGame(GameInterface game) {
        this.game = game;
    }
    
}
