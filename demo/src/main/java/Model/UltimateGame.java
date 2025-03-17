package Model;

public class UltimateGame {
    private Player currentPlayer;
    private Player player1;
    private Player player2;
    private UltimateBoard board;
    private boolean gameOver;

    public UltimateGame(Player player1, Player player2, int rows, int cols) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;
        this.board = new UltimateBoard(rows, cols);
        this.gameOver = false;
    }

    public boolean makeMove(int row, int col) {
        if (board.makeMove(row, col, currentPlayer)) {
            if (board.checkWin(currentPlayer)) {
                gameOver = true;
            } else {
                switchPlayer();
            }
            return true;
        }
        return false;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public UltimateBoard getBoard() {
        return board;
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }
}
