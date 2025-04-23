package Model;

public abstract class AbstractGame implements GameInterface {
    protected BoardGame board;
    protected Player currentPlayer;
    protected Player player1;
    protected Player player2;
    protected boolean gameOver;
    protected Player winner;

    public AbstractGame(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;
        this.gameOver = false;
    }

    @Override
    public boolean playTurn(int row, int column) {
        if (gameOver || !board.makeMove(row, column, currentPlayer)) {
            return false;
        }
        if (board.checkWin(currentPlayer)) {
            gameOver = true;
            winner = currentPlayer;
            return true;
        } else if (board.isBoardFull()) {
            gameOver = true;
            winner=null;
            return true;
        } else {
            switchPlayer();
        }
        return true;
    }

    @Override
    public BoardGame getBoard() {
        return board;
    }

    @Override
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }
    
    @Override
    public Player getWinner() {
        return winner;
    }

    protected void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }
}