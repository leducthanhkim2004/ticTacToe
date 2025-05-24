package Model;

public class UltimateGame extends AbstractGame {
    
    public UltimateGame(Player player1, Player player2) {
        super(player1, player2);
        this.board = new UltimateBoard(); // 9x9 board
    }

    @Override
    public void setGameOver(boolean isGameOver) {
        this.gameOver = isGameOver;
    }

    @Override
    public void setWinner(Player winner) {
        this.winner = winner;
    }
}