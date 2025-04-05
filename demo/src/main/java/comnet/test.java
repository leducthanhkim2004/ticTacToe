package comnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
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
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    public void start(Stage primaryStage) {
        connectToServer();

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
                button.setOnAction(e -> {
                    handleButtonClick(row, col);
                    sendMove(row, col, currentPlayer);
                });
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

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345); // server must be running on this port
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start thread to receive messages
            Thread listener = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        String[] parts = line.split(",");
                        int row = Integer.parseInt(parts[0]);
                        int col = Integer.parseInt(parts[1]);
                        String symbol = parts[2];

                        Platform.runLater(() -> {
                            buttons[row][col].setText(symbol);
                            buttons[row][col].setDisable(true);
                            if (checkWin()) {
                                statusLabel.setText("Player " + symbol + " wins!");
                                disableAllButtons();
                            } else if (isBoardFull()) {
                                statusLabel.setText("It's a draw!");
                                disableAllButtons();
                            } else {
                                currentPlayer = (symbol.equals("X")) ? 'O' : 'X';
                                statusLabel.setText("Current Turn: Player " + currentPlayer);
                            }
                        });
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            listener.setDaemon(true);
            listener.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMove(int row, int col, char player) {
        if (out != null) {
            out.println(row + "," + col + "," + player);
        }
    }

    private void handleButtonClick(int row, int col) {
        Button button = buttons[row][col];
        if (button.getText().isEmpty()) {
            button.setText(String.valueOf(currentPlayer));
            button.setDisable(true);
        }
    }

    private boolean checkWin() {
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(buttons[i][1].getText()) &&
                buttons[i][0].getText().equals(buttons[i][2].getText()) &&
                !buttons[i][0].getText().isEmpty()) {
                return true;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (buttons[0][i].getText().equals(buttons[1][i].getText()) &&
                buttons[0][i].getText().equals(buttons[2][i].getText()) &&
                !buttons[0][i].getText().isEmpty()) {
                return true;
            }
        }
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
