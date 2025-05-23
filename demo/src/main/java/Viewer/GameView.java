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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class GameView extends Application {
    private GameController gameController;
    private GameType gameType;
    private Label statusLabel;
    private Button[][] buttons;
    private int boardSize;
    private Stage primaryStage;
    
    // Player tracking fields
    private Player player1;
    private Player player2;
    private Consumer<Player.GameResult> gameOverCallback;
    
    // Score display
    private Label player1ScoreLabel;
    private Label player2ScoreLabel;

    public GameView() {
        this(GameType.STANDARD);
    }

    public GameView(GameType gameType) {
        this.gameType = gameType;
        this.boardSize = (gameType == GameType.STANDARD) ? 3 : 9;
    }
    
    /**
     * Set player 1 (typically the human/logged-in player)
     */
    public void setPlayer1(Player player1) {
        this.player1 = player1;
        
        // If symbol not set, assign X by default
        if (player1.getSymbol() == null) {
            player1.setSymbol(Symbol.X);
        }
    }
    
    /**
     * Set player 2 (typically the opponent/computer)
     */
    public void setPlayer2(Player player2) {
        this.player2 = player2;
        
        // If symbol not set, assign O by default
        if (player2.getSymbol() == null) {
            player2.setSymbol(Symbol.O);
        }
    }
    
    /**
     * Set callback to be called when the game is over
     * Used to update player stats in the database
     */
    public void setGameOverCallback(Consumer<Player.GameResult> callback) {
        this.gameOverCallback = callback;
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Use provided players or create default ones
        if (player1 == null) {
            player1 = new Player("Player 1", Symbol.X);
        }
        if (player2 == null) {
            player2 = new Player("Player 2", Symbol.O);
        }

        gameController = new GameController(player1, player2, gameType);

        String gameTitle = (gameType == GameType.STANDARD) ? "Tic Tac Toe" : "Ultimate Tic Tac Toe";
        stage.setTitle(gameTitle);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f9f9f9;");

        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // Create player stats display
        HBox playersBox = createPlayersBox();
        
        // Status label shows whose turn it is
        statusLabel = new Label(player1.getName() + "'s turn (" + player1.getSymbol() + ")");
        statusLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        statusLabel.setPadding(new Insets(10, 10, 10, 10));

        GridPane boardGrid = createBoardGrid();

        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(10));
        centerBox.getChildren().addAll(playersBox, statusLabel, boardGrid);

        root.setCenter(centerBox);

        int sceneSize = (gameType == GameType.STANDARD) ? 450 : 700;
        Scene scene = new Scene(root, sceneSize, sceneSize);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
    
    private HBox createPlayersBox() {
        // Create player score displays
        VBox player1Box = createPlayerInfoBox(player1);
        VBox player2Box = createPlayerInfoBox(player2);
        
        HBox playersBox = new HBox(50);
        playersBox.setAlignment(Pos.CENTER);
        playersBox.setPadding(new Insets(10));
        playersBox.getChildren().addAll(player1Box, player2Box);
        
        return playersBox;
    }
    
    private VBox createPlayerInfoBox(Player player) {
        Label nameLabel = new Label(player.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Label symbolLabel = new Label("Symbol: " + player.getSymbol());
        
        Label scoreLabel = new Label("Score: " + player.getScore());
        
        // Store score labels to update them later
        if (player == player1) {
            player1ScoreLabel = scoreLabel;
        } else {
            player2ScoreLabel = scoreLabel;
        }
        
        VBox playerBox = new VBox(5);
        playerBox.setAlignment(Pos.CENTER);
        playerBox.getChildren().addAll(nameLabel, symbolLabel, scoreLabel);
        
        // Style based on player symbol
        String backgroundColor = (player.getSymbol() == Symbol.X) ? "#e6f0ff" : "#ffe6e6";
        String textColor = (player.getSymbol() == Symbol.X) ? "#0077cc" : "#cc0000";
        playerBox.setStyle("-fx-background-color: " + backgroundColor + "; " +
                          "-fx-border-color: " + textColor + "; " +
                          "-fx-border-radius: 5; " +
                          "-fx-background-radius: 5; " +
                          "-fx-padding: 10;");
        
        return playerBox;
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
                
                // Call the callback if provided
                if (gameOverCallback != null) {
                    // Determine which player won and notify accordingly
                    if (winner.getName().equals(player1.getName())) {
                        gameOverCallback.accept(Player.GameResult.WIN);
                        // Update UI score
                        player1.setScore(player1.getScore() + 3);
                        player1ScoreLabel.setText("Score: " + player1.getScore());
                    } else {
                        gameOverCallback.accept(Player.GameResult.LOSE);
                        // Update UI score
                        player2.setScore(player2.getScore() + 3);
                        player2ScoreLabel.setText("Score: " + player2.getScore());
                    }
                }
            } else {
                statusLabel.setText("Game over! It's a draw!");
                
                // Call the callback for draw if provided
                if (gameOverCallback != null) {
                    gameOverCallback.accept(Player.GameResult.DRAW);
                    // Update UI scores
                    player1.setScore(player1.getScore() + 1);
                    player2.setScore(player2.getScore() + 1);
                    player1ScoreLabel.setText("Score: " + player1.getScore());
                    player2ScoreLabel.setText("Score: " + player2.getScore());
                }
            }
            disableAllButtons();
            
            // Add a play again button
            addPlayAgainButton();
        } else {
            Player currentPlayer = game.getCurrentPlayer();
            statusLabel.setText(currentPlayer.getName() + "'s turn (" + currentPlayer.getSymbol() + ")");
        }
    }
    
    private void addPlayAgainButton() {
        Button playAgainButton = new Button("Play Again");
        playAgainButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        playAgainButton.setPrefWidth(150);
        playAgainButton.setOnAction(e -> restartGame());
        
        HBox buttonBox = new HBox(playAgainButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        // Add the button to the scene
        VBox centerBox = (VBox) ((BorderPane) primaryStage.getScene().getRoot()).getCenter();
        if (centerBox.getChildren().size() > 3) {
            centerBox.getChildren().remove(3); // Remove existing button if there
        }
        centerBox.getChildren().add(buttonBox);
    }

    private void disableAllButtons() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setDisable(true);
            }
        }
    }

    private void restartGame() {
        // Keep the same players but restart the game
        gameController = new GameController(player1, player2, gameType);

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setText("");
                buttons[row][col].setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 10; -fx-background-radius: 10;");
                buttons[row][col].setDisable(false);
            }
        }

        statusLabel.setText(player1.getName() + "'s turn (" + player1.getSymbol() + ")");
        
        // Remove play again button if present
        VBox centerBox = (VBox) ((BorderPane) primaryStage.getScene().getRoot()).getCenter();
        if (centerBox.getChildren().size() > 3) {
            centerBox.getChildren().remove(3);
        }
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
