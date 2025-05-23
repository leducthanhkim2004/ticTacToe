package Viewer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import Model.*;
import Factory.GameFactory.GameType;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class OnlineGameView extends Application {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean connected = false;

    private Label statusLabel;
    private Button[][] buttons;
    private TextField chatField;
    private TextArea chatArea;
    private int boardSize = 3;
    private boolean isMyTurn = false;
    private Symbol mySymbol;
    private Stage primaryStage;
    private BorderPane root;
    private GridPane boardGrid;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        root = new BorderPane();
        BackgroundFill backgroundFill = new BackgroundFill(
    new javafx.scene.paint.LinearGradient(
        0, 0, 1, 1, true,
        javafx.scene.paint.CycleMethod.NO_CYCLE,
        new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#ffecd2")),
        new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#fcb69f"))
    ),
    CornerRadii.EMPTY,
    Insets.EMPTY
);
root.setBackground(new Background(backgroundFill));

        VBox topBox = new VBox(10);
        statusLabel = new Label("Connecting to server...");
        statusLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        statusLabel.setPadding(new Insets(10));
        statusLabel.setStyle("-fx-background-color: linear-gradient(to right, #f6d365, #fda085); -fx-text-fill: #2e2e2e; -fx-border-color: #f7b733; -fx-border-width: 2px; -fx-border-radius: 10; -fx-background-radius: 10;");
        topBox.getChildren().add(statusLabel);

        VBox gameSelectionBox = createGameSelectionPane();

        root.setTop(topBox);
        root.setCenter(gameSelectionBox);

        Scene scene = new Scene(root, 700, 600);
        primaryStage.setTitle("Online Tic Tac Toe");
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();

        primaryStage.setOnCloseRequest(e -> disconnect());
    }

    private VBox createGameSelectionPane() {
        VBox selectionBox = new VBox(20);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(50));

        Label titleLabel = new Label("Select Game Type");
        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: #333333;");

        Button standardButton = new Button("Standard Tic Tac Toe (3x3)");
        styleGameButton(standardButton);
        standardButton.setOnAction(e -> selectGameType(GameType.STANDARD));

        Button ultimateButton = new Button("Ultimate Tic Tac Toe (9x9)");
        styleGameButton(ultimateButton);
        ultimateButton.setOnAction(e -> selectGameType(GameType.ULTIMATE));

        selectionBox.getChildren().addAll(titleLabel, standardButton, ultimateButton);
        return selectionBox;
    }

    private void styleGameButton(Button button) {
        button.setPrefWidth(250);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle("-fx-background-color: #90e0ef; -fx-text-fill: #03045e; -fx-background-radius: 10;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #48cae4; -fx-text-fill: #03045e; -fx-background-radius: 10;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #90e0ef; -fx-text-fill: #03045e; -fx-background-radius: 10;"));
    }

    private GridPane createGameBoard(int size) {
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(5);
        boardGrid.setVgap(5);

        buttons = new Button[size][size];
        int buttonSize = size == 3 ? 100 : 60;

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Button button = new Button();
                button.setPrefSize(buttonSize, buttonSize);
                button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                button.setStyle("-fx-background-color: #caf0f8;");
                final int r = row;
                final int c = col;
                button.setOnAction(e -> handleButtonClick(r, c));
                buttons[row][col] = button;
                boardGrid.add(button, col, row);
            }
        }
        return boardGrid;
    }

    private void selectGameType(GameType gameType) {
        try {
            if (connected) {
                out.writeUTF("SELECT_GAME:" + gameType.name());
                boardSize = gameType == GameType.STANDARD ? 3 : 9;
                statusLabel.setText("Selected " + gameType + " game. Waiting for opponent...");
                setupGameInterface();
            }
        } catch (IOException e) {
            statusLabel.setText("Error selecting game: " + e.getMessage());
        }
    }

    private void setupGameInterface() {
        GridPane boardGrid = createGameBoard(boardSize);

        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(10));
        chatBox.setPrefWidth(200);
        chatBox.setStyle("-fx-background-color: #edf6f9; -fx-border-color: #90e0ef; -fx-border-radius: 5;");

        Label chatLabel = new Label("Chat");
        chatLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        chatLabel.setStyle("-fx-text-fill: #023e8a;");

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(300);

        HBox chatInputBox = new HBox(5);
        chatField = new TextField();
        chatField.setPrefWidth(150);
        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #48cae4; -fx-text-fill: white;");
        sendButton.setOnAction(e -> sendChat());
        chatInputBox.getChildren().addAll(chatField, sendButton);

        chatBox.getChildren().addAll(chatLabel, chatArea, chatInputBox);

        Platform.runLater(() -> {
            root.setCenter(boardGrid);
            root.setRight(chatBox);
        });
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 1234);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                connected = true;
                String playerName = "Player" + new Random().nextInt(1000);
                out.writeUTF("REGISTER:" + playerName);
                Platform.runLater(() -> statusLabel.setText("Connected as " + playerName + ". Select game type."));
                listenForMessages();
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Could not connect to server: " + e.getMessage()));
            }
        }).start();
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                while (connected) {
                    String message = in.readUTF();
                    handleServerMessage(message);
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Connection to server lost");
                    disableAllButtons();
                });
                connected = false;
            }
        }).start();
    }

    private void handleServerMessage(String message) {
        String[] parts = message.split(":");
        String command = parts[0];

        Platform.runLater(() -> {
            switch (command) {
                case "CONNECTED":
                    statusLabel.setText("Connected as " + parts[1] + ". Select game type.");
                    break;
                case "GAME_SELECTED":
                    statusLabel.setText(parts[1] + " game selected. Waiting for opponent...");
                    break;
                case "GAME_START":
                    handleGameStart(parts);
                    break;
                case "BOARD":
                    updateBoard(parts[1]);
                    break;
                case "YOUR_TURN":
                    isMyTurn = Boolean.parseBoolean(parts[1]);
                    statusLabel.setText(isMyTurn ? "Your turn" : "Opponent's turn");
                    break;
                case "MOVE":
                    handleMoveUpdate(parts);
                    break;
                case "GAME_OVER":
                    handleGameOver(parts[1]);
                    break;
                case "OPPONENT_DISCONNECTED":
                    statusLabel.setText("Your opponent disconnected");
                    disableAllButtons();
                    break;
                case "CHAT":
                    if (parts.length >= 3) {
                        chatArea.appendText(parts[1] + ": " + parts[2] + "\n");
                    }
                    break;
                case "ERROR":
                    statusLabel.setText("Error: " + parts[1]);
                    break;
            }
        });
    }

    private void handleGameStart(String[] parts) {
        if (parts.length >= 4) {
            boardSize = parts[1].equals("ULTIMATE") ? 9 : 3;
            mySymbol = parts[2].equals("X") ? Symbol.X : Symbol.O;
            setupGameInterface();
            statusLabel.setText("Game started! You are " + parts[2] + ". Playing against: " + parts[3]);
        }
    }

    private void updateBoard(String boardData) {
        String[] cells = boardData.split(";");
        for (String cell : cells) {
            String[] cellData = cell.split(",");
            if (cellData.length >= 3) {
                int row = Integer.parseInt(cellData[0]);
                int col = Integer.parseInt(cellData[1]);
                String symbolStr = cellData[2];
                if (row < boardSize && col < boardSize) {
                    if (!symbolStr.equals("EMPTY")) {
                        buttons[row][col].setText(symbolStr);
                        buttons[row][col].setStyle("-fx-text-fill: " + (symbolStr.equals("X") ? "#0077b6" : "#d00000") + "; -fx-background-color: #ade8f4;");
                    } else {
                        buttons[row][col].setText("");
                        buttons[row][col].setStyle("-fx-background-color: #caf0f8;");
                    }
                }
            }
        }
    }

    private void handleMoveUpdate(String[] parts) {
        if (parts.length >= 4) {
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);
            String symbol = parts[3];
            if (row < boardSize && col < boardSize) {
                buttons[row][col].setText(symbol);
                buttons[row][col].setStyle("-fx-text-fill: " + (symbol.equals("X") ? "#0077b6" : "#d00000") + "; -fx-background-color: #ade8f4;");
            }
        }
    }

    private void handleButtonClick(int row, int col) {
        if (isMyTurn && connected && (buttons[row][col].getText() == null || buttons[row][col].getText().isEmpty())) {
            try {
                out.writeUTF("MOVE:" + row + ":" + col);
                buttons[row][col].setText(mySymbol.toString());
                buttons[row][col].setStyle("-fx-text-fill: " + (mySymbol == Symbol.X ? "#0077b6" : "#d00000") + "; -fx-background-color: #ade8f4;");
                isMyTurn = false;
                statusLabel.setText("Waiting for opponent's move...");
            } catch (IOException e) {
                statusLabel.setText("Error sending move: " + e.getMessage());
            }
        } else {
            statusLabel.setText("Not your turn yet!");
        }
    }

    private void sendChat() {
        if (connected && !chatField.getText().trim().isEmpty()) {
            try {
                String message = chatField.getText().trim();
                out.writeUTF("CHAT:" + message);
                chatField.clear();
                chatArea.appendText("Me: " + message + "\n");
            } catch (IOException e) {
                statusLabel.setText("Error sending chat: " + e.getMessage());
            }
        }
    }

    private void disconnect() {
        try {
            connected = false;
            if (out != null) {
                out.writeUTF("QUIT");
                out.flush();
            }
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    private void disableAllButtons() {
        if (buttons == null) return;
        Platform.runLater(() -> {
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    if (buttons[row][col] != null) buttons[row][col].setDisable(true);
                }
            }
        });
    }

    private void handleGameOver(String result) {
        switch (result) {
            case "WIN":
                statusLabel.setText("Game over - You won!");
                break;
            case "LOSE":
                statusLabel.setText("Game over - You lost");
                break;
            case "DRAW":
                statusLabel.setText("Game over - It's a draw!");
                break;
            default:
                statusLabel.setText("Game over");
        }

        disableAllButtons();

        Button playAgainBtn = new Button("Play Again");
        playAgainBtn.setStyle("-fx-background-color: #06d6a0; -fx-text-fill: white;");
        playAgainBtn.setOnAction(e -> setupSelectionInterface());

        HBox buttonsBox = new HBox(10, playAgainBtn);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox gameOverBox = new VBox(10, buttonsBox);
        gameOverBox.setAlignment(Pos.BOTTOM_CENTER);

        Platform.runLater(() -> root.setBottom(gameOverBox));
    }

    private void setupSelectionInterface() {
        Platform.runLater(() -> {
            root.setCenter(createGameSelectionPane());
            root.setRight(null);
            root.setBottom(null);
            statusLabel.setText("Select a game type");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
