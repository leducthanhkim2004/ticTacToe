package Control;

import Model.*;

public class GameController {
    private Game game;
  

    public GameController(Player player1, Player player2, int row, int column) {
       
        game = new Game(player1, player2,row,column);
    }

    public boolean handleMove(int rowIndex, int columnIndex) {
        return game.playTurn(rowIndex, columnIndex);
    }

    public Game getGame() {
        return game;
    }
}
