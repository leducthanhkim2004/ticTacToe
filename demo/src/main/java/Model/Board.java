package Model;


public class Board  implements BoardGame{
    private int row;
    private int col;
    private Cell[][] cells;

    public Board(int row, int col) {
        this.row = row;
        this.col = col;
        cells = new Cell[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                cells[i][j] = new Cell();
            }
        }
    }

    public boolean isBoardFull() {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (cells[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean makeMove(int rowIndex, int columnIndex, Player player) {
        if (rowIndex < 0 || rowIndex >= row || columnIndex < 0 || columnIndex >= col) {
            throw new IllegalArgumentException("Invalid row index or column index");
        }
        if (cells[rowIndex][columnIndex].isEmpty()) {
            cells[rowIndex][columnIndex].setSymbol(player.getSymbol());
            return true;
        }
        return false;
    }
    @Override
    public boolean checkWin(Player player) {
        return isDiagonalWin(player) || isHorizontalWin(player) || isVerticalWin(player);
    }

    public boolean isDiagonalWin(Player player) {
        int countRightDiagonal = 0;
        int countLeftDiagonal = 0;
        for(int i = 0 ; i<3 ; i++){
            // check right diagonal
            if(cells[i][i].getSymbol()==player.getSymbol()){
                countRightDiagonal++;
            }
            //check left diagonal
            if(cells[i][2-i].getSymbol()==player.getSymbol()){
                countLeftDiagonal++;
            }
        }
        return countLeftDiagonal==3 || countRightDiagonal==3;
    }
    @Override
    public boolean isHorizontalWin(Player player) {
        
        for(int i = 0 ; i< 3 ;i++){
          boolean flag =true;
          for(int j  = 0 ; j<3 ;j++){
                if(cells[i][j].getSymbol()!= player.getSymbol()){
                    flag =false;
                    return false;
                }
          }
            if(flag){
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean isVerticalWin(Player player) {
        for(int i = 0 ; i< 3;i++){
            boolean win =true;
            for(int j = 0 ; j<3 ;j++){
                if(cells[j][i].getSymbol()!=player.getSymbol()){
                    win = false;
                    break;
                }
            }
            if(win){
                return true;
            }
        }
        return false;
    }
    public Cell[][] getCells() {
        return cells;
    }
}
