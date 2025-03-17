package Model;

public class Game {
    private Board board;
    private Player currentPlayer;
    private Player player1;
    private Player player2;
    private boolean gameOver;
    private int row;
    private int col;

    public Game(Player player1, Player player2,int row,int col) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;
        this.board = new Board(row,col);
        this.gameOver = false;
    }

    public boolean playTurn(int row, int column) {
        if (gameOver || !board.makeMove(row, column, currentPlayer)) {
            return false;
        }
        if (board.checkWin(currentPlayer)) {
            gameOver = true;
        } else {
            switchPlayer();
        }
        return true;
    }

    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }
}