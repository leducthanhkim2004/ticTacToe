package Model;

public interface GameInterface {
    boolean playTurn(int row, int column);
    BoardGame getBoard();
    Player getCurrentPlayer();
    boolean isGameOver();
    Player getWinner();
}