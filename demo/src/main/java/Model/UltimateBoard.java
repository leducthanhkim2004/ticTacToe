package Model;

public class UltimateBoard implements BoardGame {
    private int row;
    private int col;
    private Cell[][] cells;

    // Constructor for the ultimate board
    public UltimateBoard() {
        this.row = 9;
        this.col = 9;
        cells = new Cell[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new Cell();
            }
        }
    }

    @Override
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

    @Override
    public boolean checkWin(Player player) {
        return isDiagonalWin(player) || isHorizontalWin(player) || isVerticalWin(player);
    }

    @Override
    public boolean isDiagonalWin(Player player) {
        // Check right diagonal
        
        return isRightDiagonalWin(player) || isLeftDiagonalWin(player);
    }

    @Override
    public boolean isHorizontalWin(Player player) {
        for (int i = 0; i < row; i++) {
            int countHorizontal = 0;
            for (int j = 0; j < col; j++) {
                if (cells[i][j].getSymbol() == player.getSymbol()) {
                    countHorizontal++;
                    if (countHorizontal == 5) {
                        return true;
                    }
                } else {
                    countHorizontal = 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isVerticalWin(Player player) {
        for (int i = 0; i < col; i++) {
            int countVertical = 0;
            for (int j = 0; j < row; j++) {
                if (cells[j][i].getSymbol() == player.getSymbol()) {
                    countVertical++;
                    if (countVertical == 5) {
                        return true;
                    }
                } else {
                    countVertical = 0;
                }
            }
        }
        return false;
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

    public Cell[][] getCells() {
        return cells;
    }
    private boolean isRightDiagonalWin(Player player){
        int countRightDiagonal= 0 ;
        int i = 0 ;
        while(i<9){
            if(cells[i][i].getSymbol()==player.getSymbol()){
                countRightDiagonal++;
            }
            if(countRightDiagonal==5){
                return true;
            }
            i++;
        }
        return false;
    }
    /*TODO
     * IMPLEMENT THIS MEHTOD
     */
    public boolean isLeftDiagonalWin(Player player){
        int countLeftDiagonal = 0;
        int i  = 8 ;
        int j = 0;
        while(i>0){
            if(cells[j][i].getSymbol()==player.getSymbol()){
                countLeftDiagonal++;
            }
            if(countLeftDiagonal==5){
                return true;
            }
        }
        return false;
    }
}