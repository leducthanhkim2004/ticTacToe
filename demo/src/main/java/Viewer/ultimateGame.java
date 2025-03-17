package Viewer;

import Model.*;
import Control.ultimateGameController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ultimateGame extends Application {
    private ultimateGameController ultimateGameController;
    private Label statusLabel;
    private Button[][] buttons = new Button[9][9];

    @Override
    public void start(Stage stage) {
        // Initialize the game and players
        Player player1 = new Player("Player 1", Symbol.X);
        Player player2 = new Player("Player 2", Symbol.O);
        ultimateGameController = new ultimateGameController(player1, player2);

        stage.setTitle("Ultimate Tic Tac Toe");

        statusLabel = new Label("Current Player: Player 1 (X)");
        Font font = new Font("verdana", 20);
        statusLabel.setFont(font);
        statusLabel.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        // Create grid pane
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(javafx.geometry.Pos.CENTER);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                buttons[i][j] = new Button();
                buttons[i][j].setPrefSize(100, 100);
                buttons[i][j].setFont(Font.font("verdana", 20));
                final int row = i;
                final int col = j;
                buttons[i][j].setOnAction(e -> handleButtonClick(row, col));
                gridPane.add(buttons[i][j], j, i);
            }
        }

        VBox vbox = new VBox(10, statusLabel, gridPane);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.setAlignment(javafx.geometry.Pos.CENTER);

        // Show scene
        Scene scene = new Scene(vbox, 600, 600);
        stage.setScene(scene);
        stage.setMinHeight(600);
        stage.setMinWidth(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void handleButtonClick(int row, int col) {
        if (ultimateGameController.handleMove(row, col)) {
            UltimateBoard board = ultimateGameController.getUltimateGame().getBoard();
            buttons[row][col].setText(board.getCells()[row][col].getSymbol().toString());
            if (ultimateGameController.getUltimateGame().isGameOver()) {
                statusLabel.setText("Winner is " + ultimateGameController.getUltimateGame().getCurrentPlayer().getName());
                disableBoard();
            } else {
                statusLabel.setText("Current Player: " + ultimateGameController.getUltimateGame().getCurrentPlayer().getName());
            }
        }
    }

    private void disableBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                buttons[i][j].setDisable(true);
            }
        }
    }
}
