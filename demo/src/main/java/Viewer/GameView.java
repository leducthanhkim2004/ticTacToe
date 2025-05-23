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

        Player player1 = new Player("Player 1", Symbol.X);
        Player player2 = new Player("Player 2", Symbol.O);

        gameController = new GameController(player1, player2, gameType);

        String gameTitle = (gameType == GameType.STANDARD) ? "Tic Tac Toe" : "Ultimate Tic Tac Toe";
        stage.setTitle(gameTitle);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f9f9f9;");

        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        statusLabel = new Label("Player 1's turn (X)");
        statusLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        statusLabel.setPadding(new Insets(10, 10, 10, 10));

        GridPane boardGrid = createBoardGrid();

        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(10));
        centerBox.getChildren().addAll(statusLabel, boardGrid);

        root.setCenter(centerBox);

        int sceneSize = (gameType == GameType.STANDARD) ? 400 : 700;
        Scene scene = new Scene(root, sceneSize, sceneSize);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu gameMenu = new Menu("Game");
        MenuItem newGameItem = new MenuItem("New Game");
        newGameItem.setOnAction(e -> restartGame());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> primaryStage.close());
        gameMenu.getItems().addAll(newGameItem, exitItem);

        Menu modeMenu = new Menu("Mode");
        MenuItem standardItem = new MenuItem("Standard (3x3)");
        standardItem.setOnAction(e -> switchGameMode(GameType.STANDARD));
        MenuItem ultimateItem = new MenuItem("Ultimate (9x9)");
        ultimateItem.setOnAction(e -> switchGameMode(GameType.ULTIMATE));
        modeMenu.getItems().addAll(standardItem, ultimateItem);

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
                if (gameType == GameType.STANDARD) {
                    button.setPrefSize(100, 100);
                }
                button.setFont(Font.font("Arial", FontWeight.BOLD, 24));
                button.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 10; -fx-background-radius: 10;");

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
        if (gameController.getGame().isGameOver()) return;

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
                    if (symbol == Symbol.X) {
                        buttons[row][col].setStyle("-fx-text-fill: #0077cc; -fx-background-color: #e6f0ff; -fx-border-radius: 10; -fx-background-radius: 10;");
                    } else {
                        buttons[row][col].setStyle("-fx-text-fill: #cc0000; -fx-background-color: #ffe6e6; -fx-border-radius: 10; -fx-background-radius: 10;");
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
        // Placeholder
    }

    private void disableAllButtons() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setDisable(true);
            }
        }
    }

    private void restartGame() {
        Player player1 = new Player("Player 1", Symbol.X);
        Player player2 = new Player("Player 2", Symbol.O);

        gameController = new GameController(player1, player2, gameType);

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setText("");
                buttons[row][col].setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 10; -fx-background-radius: 10;");
                buttons[row][col].setDisable(false);
            }
        }

        statusLabel.setText("Player 1's turn (X)");
    }

    private void switchGameMode(GameType newGameType) {
        gameType = newGameType;
        boardSize = (gameType == GameType.STANDARD) ? 3 : 9;
        primaryStage.close();
        Stage newStage = new Stage();
        this.primaryStage = newStage;
        start(newStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
