package Model;

public interface BoardGame {


    public boolean isBoardFull();
    public boolean checkWin(Player player);
    public boolean isDiagonalWin(Player player);
    public boolean isHorizontalWin(Player player);  
    public boolean isVerticalWin(Player player);
    public boolean makeMove(int row, int column, Player currentPlayer);
    public Cell[][] getCells();

}
