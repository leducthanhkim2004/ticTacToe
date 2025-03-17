package Viewer;

import Model.*;
import Control.GameController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GameViewer extends Application {
    private GameController gameController;
    private Label messageLabel;
    private Button[][] buttons = new Button[3][3];
    
    @Override
    public void start(Stage stage) {
        // Initialize the game and players
        Player player1 = new Player("Player 1", Symbol.X);
        Player player2 = new Player("Player 2", Symbol.O);
        
        gameController = new GameController(player1, player2,3,3);

        stage.setTitle("Tic Tac Toe");

        messageLabel = new Label("Current Player: Player 1 (X)");
        messageLabel.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
        messageLabel.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(javafx.geometry.Pos.CENTER);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                final int row = i;
                final int column = j;
                buttons[i][j] = new Button();
                buttons[i][j].setPrefSize(100, 100);
                buttons[i][j].setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
                buttons[i][j].setOnAction(e -> handleButtonClick(row, column));
                gridPane.add(buttons[i][j], j, i);
            }
        }

        VBox vbox = new VBox(10, messageLabel, gridPane);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.setAlignment(javafx.geometry.Pos.CENTER);

        // Show scene
        Scene scene = new Scene(vbox, 400, 450);
        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.setMinHeight(450);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void handleButtonClick(int row, int column) {
        if (gameController.handleMove(row, column)) {
            Board board = gameController.getGame().getBoard();
            buttons[row][column].setText(board.getCells()[row][column].getSymbol().toString());
            if (gameController.getGame().isGameOver()) {
                disableBoard();
                messageLabel.setText("Winner: " + gameController.getGame().getCurrentPlayer().getName());
            } else {
                messageLabel.setText("Current Player: " + gameController.getGame().getCurrentPlayer().getName());
            }
        }
    }

    private void disableBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setDisable(true);
            }
        }
    }
}