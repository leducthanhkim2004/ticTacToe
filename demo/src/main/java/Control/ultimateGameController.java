package Control;

import Model.*;

public class ultimateGameController {
    private UltimateGame ultimateGame;

    public ultimateGameController(Player player1, Player player2) {
        ultimateGame = new UltimateGame(player1, player2, 9, 9);
    }

    public boolean handleMove(int row, int col) {
        return ultimateGame.makeMove(row, col);
    }

    public UltimateGame getUltimateGame() {
        return ultimateGame;
    }
}
