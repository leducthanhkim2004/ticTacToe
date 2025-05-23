package Model;


public class Board  implements BoardGame{
    private int row;
    private int col;
    private Cell[][] cells;

    public Board() {
        this.row = 3;
        this.col = 3;
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
        // Add debugging to see all win check operations
        boolean horizontal = isHorizontalWin(player);
        boolean vertical = isVerticalWin(player);
        boolean diagonal = isDiagonalWin(player);
        
        System.out.println("Check win for " + player.getName() + " with symbol " + player.getSymbol());
        System.out.println("- Horizontal win: " + horizontal);
        System.out.println("- Vertical win: " + vertical);
        System.out.println("- Diagonal win: " + diagonal);
        
        return horizontal || vertical || diagonal;
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
        Symbol playerSymbol = player.getSymbol();
        System.out.println("Checking horizontal win for player " + player.getName() + " with symbol " + playerSymbol);
        
        // Print the board for debugging
        printBoardDebug();
        
        // Check each row
        for(int i = 0; i < 3; i++) {
            // Check if all cells in this row have player's symbol
            if (cells[i][0].getSymbol() == playerSymbol &&
                cells[i][1].getSymbol() == playerSymbol &&
                cells[i][2].getSymbol() == playerSymbol) {
                System.out.println("HORIZONTAL WIN DETECTED for " + player.getName() + 
                                  " in row " + i + " with symbol " + playerSymbol);
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

    // Helper method to print the board state for debugging
    private void printBoardDebug() {
        System.out.println("Current board state:");
        for (int i = 0; i < 3; i++) {
            System.out.print("Row " + i + ": ");
            for (int j = 0; j < 3; j++) {
                Symbol symbol = cells[i][j].getSymbol();
                System.out.print("[" + (symbol == null ? "-" : symbol) + "] ");
            }
            System.out.println();
        }
    }
}
