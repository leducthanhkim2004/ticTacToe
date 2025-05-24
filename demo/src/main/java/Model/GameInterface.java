package Model;

public interface GameInterface {
    boolean playTurn(int row, int column);
    BoardGame getBoard();
    Player getCurrentPlayer();
    boolean isGameOver();
    Player getWinner();
    void endGame(Player winner);
    void setGameOver(boolean isGameOver);
    void setWinner(Player winner);
}