package Model;

public class UltimateBoard implements BoardGame {
    private int row;
    private int col;
    private Cell[][] cells;

    public UltimateBoard(int row, int col) {
        this.row = row;
        this.col = col;
        cells = new Cell[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
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

    @Override
    public boolean isDiagonalWin(Player player) {
        // Check right diagonal
        int countRightDiagonal = 0;
        for (int i = 0; i < row; i++) {
            if (cells[i][i].getSymbol() == player.getSymbol()) {
                countRightDiagonal++;
                if (countRightDiagonal == 5) {
                    return true;
                }
            } else {
                countRightDiagonal = 0;
            }
        }

        // Check left diagonal
        int countLeftDiagonal = 0;
        for (int i = 0; i < row; i++) {
            if (cells[i][row - 1 - i].getSymbol() == player.getSymbol()) {
                countLeftDiagonal++;
                if (countLeftDiagonal == 5) {
                    return true;
                }
            } else {
                countLeftDiagonal = 0;
            }
        }

        return false;
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

    public Cell[][] getCells() {
        return cells;
    }
}