package comnet;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class test extends Application {

    private char currentPlayer = 'X'; // 'X' starts the game
    private Button[][] buttons = new Button[3][3];
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button button = new Button();
                button.setMinSize(100, 100);
                button.setStyle("-fx-font-size: 24;");
                int row = i;
                int col = j;
                button.setOnAction(e -> handleButtonClick(row, col));
                buttons[row][col] = button;
                gridPane.add(button, col, row);
            }
        }

        statusLabel = new Label("Current Turn: Player " + currentPlayer);
        VBox root = new VBox(10, statusLabel, gridPane);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tic Tac Toe");
        primaryStage.show();
    }

    private void handleButtonClick(int row, int col) {
        Button button = buttons[row][col];
        if (button.getText().isEmpty()) {
            button.setText(String.valueOf(currentPlayer));
            if (checkWin()) {
                statusLabel.setText("Player " + currentPlayer + " wins!");
                disableAllButtons();
            } else if (isBoardFull()) {
                statusLabel.setText("It's a draw!");
                disableAllButtons();
            } else {
                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                statusLabel.setText("Current Turn: Player " + currentPlayer);
            }
        }
    }

    private boolean checkWin() {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(buttons[i][1].getText()) &&
                    buttons[i][0].getText().equals(buttons[i][2].getText()) &&
                    !buttons[i][0].getText().isEmpty()) {
                return true;
            }
        }

        // Check columns
        for (int i = 0; i < 3; i++) {
            if (buttons[0][i].getText().equals(buttons[1][i].getText()) &&
                    buttons[0][i].getText().equals(buttons[2][i].getText()) &&
                    !buttons[0][i].getText().isEmpty()) {
                return true;
            }
        }

        // Check diagonals
        if (buttons[0][0].getText().equals(buttons[1][1].getText()) &&
                buttons[0][0].getText().equals(buttons[2][2].getText()) &&
                !buttons[0][0].getText().isEmpty()) {
            return true;
        }

        if (buttons[0][2].getText().equals(buttons[1][1].getText()) &&
                buttons[0][2].getText().equals(buttons[2][0].getText()) &&
                !buttons[0][2].getText().isEmpty()) {
            return true;
        }

        return false;
    }

    private boolean isBoardFull() {
        for (Button[] row : buttons) {
            for (Button button : row) {
                if (button.getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void disableAllButtons() {
        for (Button[] row : buttons) {
            for (Button button : row) {
                button.setDisable(true);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
