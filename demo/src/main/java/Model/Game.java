package Model;

public class Game extends AbstractGame {
    private Board board;

    public Game(Player player1, Player player2) {
        super(player1, player2);
        this.board = new Board(); // 3x3 board
    }

    public boolean playTurn(int row, int column) {
        if (gameOver || !board.makeMove(row, column, currentPlayer)) {
            return false;
        }
        if (board.checkWin(currentPlayer)) {
            gameOver = true;
            winner = currentPlayer; // Set the winner!
            return true;
        } 
        else if (board.isBoardFull()) {
            gameOver = true;
            // winner remains null for a draw
            return true;
        }
        else {
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

    protected void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }
    
    // Add these methods to satisfy the interface requirements
    @Override
    public void setGameOver(boolean isGameOver) {
        this.gameOver = isGameOver;
    }
    
    @Override
    public void setWinner(Player winner) {
        this.winner = winner;
    }
}