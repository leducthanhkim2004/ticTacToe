package Model;

import java.util.TimerTask;

public abstract class AbstractGame implements GameInterface {
    protected BoardGame board;
    protected Player currentPlayer;
    protected Player player1;
    protected Player player2;
    protected boolean gameOver;
    protected Player winner;
    private boolean gameStarted; // Add this line
    private TimerTask currentTimerTask; // Add this line

    public AbstractGame(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;
        this.gameOver = false;
        this.gameStarted = false; // Initialize here
    }

    @Override
    public boolean playTurn(int row, int column) {
        if (gameOver || !board.makeMove(row, column, currentPlayer)) {
            return false;
        }
        
        System.out.println("Checking win condition for " + currentPlayer.getName() + 
                          " after move at row=" + row + ", col=" + column);
        
        if (board.checkWin(currentPlayer)) {
            System.out.println("Win condition detected for " + currentPlayer.getName());
            gameOver = true;
            winner = currentPlayer;
            return true;
        } else if (board.isBoardFull()) {
            System.out.println("Board is full - game is a draw");
            gameOver = true;
            winner = null; // Draw
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

    @Override
    public void endGame(Player winner) {
        gameStarted = false;
        gameOver = true;
        
        // Cancel any active timer
        if (currentTimerTask != null) {
            currentTimerTask.cancel();
            currentTimerTask = null;
        }
        
        // Remove any database calls from here
        // Only log the result
        System.out.println("Game ended. Winner: " + 
                           (winner != null ? winner.getName() : "Draw"));
    }

    protected void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }
}