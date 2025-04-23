package Viewer;

import Control.GameController;
import Factory.GameFactory.GameType;
import Model.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GameView extends Application {
    private GameController gameController;
    private GameType gameType;
    private Label statusLabel;
    private Button[][] buttons;
    private int boardSize;
    private Stage primaryStage;

    // Constructor for testing
    public GameView() {
        this(GameType.STANDARD);
    }
    
    public GameView(GameType gameType) {
        this.gameType = gameType;
        this.boardSize = (gameType == GameType.STANDARD) ? 3 : 9;
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        
        // Initialize players
        Player player1 = new Player("Player 1", Symbol.X);
        Player player2 = new Player("Player 2", Symbol.O);
        
        // Initialize controller
        gameController = new GameController(player1, player2, gameType);
        
        // Set the window title
        String gameTitle = (gameType == GameType.STANDARD) ? "Tic Tac Toe" : "Ultimate Tic Tac Toe";
        stage.setTitle(gameTitle);
        
        // Create the layout
        BorderPane root = new BorderPane();
        
        // Add menu bar
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);
        
        // Create status label
        statusLabel = new Label("Player 1's turn (X)");
        statusLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        statusLabel.setPadding(new Insets(10, 10, 10, 10));
        
        // Create the game board
        GridPane boardGrid = createBoardGrid();
        
        // Add components to a VBox for center area
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(10));
        centerBox.getChildren().addAll(statusLabel, boardGrid);
        
        root.setCenter(centerBox);
        
        // Set scene and show stage
        int sceneSize = (gameType == GameType.STANDARD) ? 400 : 700;
        Scene scene = new Scene(root, sceneSize, sceneSize);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // Game menu
        Menu gameMenu = new Menu("Game");
        
        MenuItem newGameItem = new MenuItem("New Game");
        newGameItem.setOnAction(e -> restartGame());
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());
        
        gameMenu.getItems().addAll(newGameItem, exitItem);
        
        // Mode menu
        Menu modeMenu = new Menu("Mode");
        
        MenuItem standardItem = new MenuItem("Standard (3x3)");
        standardItem.setOnAction(e -> switchGameMode(GameType.STANDARD));
        
        MenuItem ultimateItem = new MenuItem("Ultimate (9x9)");
        ultimateItem.setOnAction(e -> switchGameMode(GameType.ULTIMATE));
        
        modeMenu.getItems().addAll(standardItem, ultimateItem);
        
        // Add menus to menubar
        menuBar.getMenus().addAll(gameMenu, modeMenu);
        
        return menuBar;
    }
    
    private GridPane createBoardGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(8);
        grid.setVgap(8);
        
        buttons = new Button[boardSize][boardSize];
        
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Button button = new Button();
                button.setMinSize(50, 50);
                button.setPrefSize(60, 60);
                
                // Add padding to make buttons bigger in standard mode
                if (gameType == GameType.STANDARD) {
                    button.setPrefSize(100, 100);
                }
                
                button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                
                final int finalRow = row;
                final int finalCol = col;
                
                button.setOnAction(e -> handleButtonClick(finalRow, finalCol));
                
                buttons[row][col] = button;
                grid.add(button, col, row);
            }
        }
        
        return grid;
    }
    
    private void handleButtonClick(int row, int col) {
        // If game is over, do nothing
        if (gameController.getGame().isGameOver()) {
            return;
        }
        
        // Try to make a move
        if (gameController.handleMove(row, col)) {
            updateBoard();
            checkGameStatus();
        }
    }
    
    private void updateBoard() {
        BoardGame board = gameController.getGame().getBoard();
        Cell[][] cells = board.getCells();
        
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Symbol symbol = cells[row][col].getSymbol();
                if (symbol != null) {
                    buttons[row][col].setText(symbol.toString());
                    
                    // Set button color based on symbol
                    if (symbol == Symbol.X) {
                        buttons[row][col].setStyle("-fx-text-fill: blue;");
                    } else {
                        buttons[row][col].setStyle("-fx-text-fill: red;");
                    }
                }
            }
        }
    }
    
    private void checkGameStatus() {
        GameInterface game = gameController.getGame();
        
        if (game.isGameOver()) {
            Player winner = game.getWinner();
            if (winner != null) {
                statusLabel.setText("Game over! " + winner.getName() + " (" + winner.getSymbol() + ") wins!");
                highlightWinningCells();
            } else {
                statusLabel.setText("Game over! It's a draw!");
            }
            disableAllButtons();
        } else {
            Player currentPlayer = game.getCurrentPlayer();
            statusLabel.setText(currentPlayer.getName() + "'s turn (" + currentPlayer.getSymbol() + ")");
        }
    }
    
    private void highlightWinningCells() {
        // This would require access to winning cells information from the game model
        // For now, we'll leave this as a placeholder
    }
    
    private void disableAllButtons() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setDisable(true);
            }
        }
    }
    
    private void restartGame() {
        // Create new players
        Player player1 = new Player("Player 1", Symbol.X);
        Player player2 = new Player("Player 2", Symbol.O);
        
        // Create new game controller
        gameController = new GameController(player1, player2, gameType);
        
        // Reset UI
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setText("");
                buttons[row][col].setStyle("");
                buttons[row][col].setDisable(false);
            }
        }
        
        statusLabel.setText("Player 1's turn (X)");
    }
    
    private void switchGameMode(GameType newGameType) {
        // Change game type and restart with new stage
        gameType = newGameType;
        boardSize = (gameType == GameType.STANDARD) ? 3 : 9;
        
        // Close current stage and create new one
        primaryStage.close();
        
        Stage newStage = new Stage();
        this.primaryStage = newStage;
        start(newStage);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}