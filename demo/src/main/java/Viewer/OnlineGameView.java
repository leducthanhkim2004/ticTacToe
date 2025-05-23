package Viewer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import Model.*;
import Factory.GameFactory.GameType;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.function.Consumer;

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
    private VBox gameInfoPanel;

    private Player player;
    private Consumer<Player.GameResult> gameOverCallback;
    private Label opponentLabel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Create a modern gradient background
        Stop[] stops = new Stop[] {
            new Stop(0, Color.web("#1a237e", 0.2)),  // Deep indigo (faded)
            new Stop(1, Color.web("#4a148c", 0.3))   // Deep purple (faded)
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        BackgroundFill backgroundFill = new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);

        root = new BorderPane();
        root.setBackground(background);

        // Create a stylish header
        VBox topBox = new VBox(15);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(20, 20, 10, 20));
        
        Label headerLabel = new Label("Online Tic-Tac-Toe");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        headerLabel.setStyle("-fx-text-fill: #303F9F;");
        
        statusLabel = new Label("Connecting to server...");
        statusLabel.setFont(Font.font("Arial", FontWeight.MEDIUM, 16));
        statusLabel.setPadding(new Insets(10, 15, 10, 15));
        statusLabel.setStyle("-fx-background-color: #E8EAF6; -fx-text-fill: #3F51B5; -fx-background-radius: 20px;");
        
        // Add shadow to status label
        DropShadow statusShadow = new DropShadow();
        statusShadow.setColor(Color.gray(0.5, 0.5));
        statusShadow.setRadius(5);
        statusShadow.setOffsetY(2);
        statusLabel.setEffect(statusShadow);
        
        topBox.getChildren().addAll(headerLabel, statusLabel);

        // Create game selection panel
        VBox gameSelectionBox = createGameSelectionPane();

        root.setTop(topBox);
        root.setCenter(gameSelectionBox);

        Scene scene = new Scene(root, 800, 700);
        primaryStage.setTitle("Online Tic Tac Toe");
        primaryStage.setScene(scene);
        primaryStage.show();

        connectToServer();

        primaryStage.setOnCloseRequest(e -> disconnect());
    }

    private VBox createGameSelectionPane() {
        VBox selectionBox = new VBox(25);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(30, 50, 50, 50));

        Label titleLabel = new Label("Select Game Type");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #303F9F;");
        
        // Player information
        HBox playerInfoBox = new HBox(20);
        playerInfoBox.setAlignment(Pos.CENTER);
        playerInfoBox.setPadding(new Insets(15));
        playerInfoBox.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-background-radius: 10px;");
        
        Label playerNameLabel = new Label("Playing as: " + (player != null ? player.getName() : "Guest"));
        playerNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playerNameLabel.setStyle("-fx-text-fill: #303F9F;");
        
        playerInfoBox.getChildren().add(playerNameLabel);

        // Game type buttons with enhanced styling
        Button standardButton = new Button("Standard Tic Tac Toe (3×3)");
        styleGameButton(standardButton, Color.web("#3F51B5"));
        standardButton.setOnAction(e -> selectGameType(GameType.STANDARD));

        Button ultimateButton = new Button("Ultimate Tic Tac Toe (9×9)");
        styleGameButton(ultimateButton, Color.web("#673AB7"));
        ultimateButton.setOnAction(e -> selectGameType(GameType.ULTIMATE));
        
        // Add exit button
        Button exitButton = new Button("Back to Main Menu");
        exitButton.setPrefWidth(300);
        exitButton.setPrefHeight(45);
        exitButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        exitButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-background-radius: 8;");
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.4));
        shadow.setRadius(8);
        shadow.setOffsetY(3);
        exitButton.setEffect(shadow);
        
        exitButton.setOnAction(e -> {
            disconnect();
            primaryStage.close();
        });

        VBox.setMargin(playerInfoBox, new Insets(0, 0, 20, 0));
        VBox.setMargin(exitButton, new Insets(30, 0, 0, 0));
        
        selectionBox.getChildren().addAll(titleLabel, playerInfoBox, standardButton, ultimateButton, exitButton);
        return selectionBox;
    }

    private void styleGameButton(Button button, Color baseColor) {
        button.setPrefWidth(350);
        button.setPrefHeight(60);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        // Convert Color to hex string
        String colorHex = String.format("#%02X%02X%02X",
            (int)(baseColor.getRed() * 255),
            (int)(baseColor.getGreen() * 255),
            (int)(baseColor.getBlue() * 255));
        
        button.setStyle(
            "-fx-background-color: " + colorHex + ";" + 
            "-fx-text-fill: white;" + 
            "-fx-background-radius: 8px;"
        );
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.4));
        shadow.setRadius(8);
        shadow.setOffsetY(3);
        button.setEffect(shadow);
        
        // Add hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: derive(" + colorHex + ", -10%);" + 
                "-fx-text-fill: white;" + 
                "-fx-background-radius: 8px;"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + colorHex + ";" + 
                "-fx-text-fill: white;" + 
                "-fx-background-radius: 8px;"
            );
        });
    }

    private GridPane createGameBoard(int size) {
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(8);
        boardGrid.setVgap(8);
        boardGrid.setPadding(new Insets(15));
        boardGrid.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 10px;");

        // Add shadow to the game board
        DropShadow boardShadow = new DropShadow();
        boardShadow.setColor(Color.color(0, 0, 0, 0.5));
        boardShadow.setRadius(10);
        boardShadow.setOffsetY(4);
        boardGrid.setEffect(boardShadow);

        buttons = new Button[size][size];
        int buttonSize = size == 3 ? 90 : 55;

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Button button = new Button();
                button.setPrefSize(buttonSize, buttonSize);
                button.setFont(Font.font("Arial", FontWeight.BOLD, size == 3 ? 36 : 24));
                button.setStyle("-fx-background-color: #E8EAF6; -fx-background-radius: 5px; -fx-border-color: #C5CAE9; -fx-border-radius: 5px; -fx-border-width: 1px;");
                
                final int r = row;
                final int c = col;
                button.setOnAction(e -> handleButtonClick(r, c));
                
                // Add hover effect
                button.setOnMouseEntered(e -> {
                    if (isMyTurn && (button.getText() == null || button.getText().isEmpty())) {
                        button.setStyle("-fx-background-color: #D1C4E9; -fx-background-radius: 5px; -fx-border-color: #B39DDB; -fx-border-radius: 5px; -fx-border-width: 1px;");
                    }
                });
                
                button.setOnMouseExited(e -> {
                    if (button.getText() == null || button.getText().isEmpty()) {
                        button.setStyle("-fx-background-color: #E8EAF6; -fx-background-radius: 5px; -fx-border-color: #C5CAE9; -fx-border-radius: 5px; -fx-border-width: 1px;");
                    }
                });
                
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

        VBox chatBox = createChatPanel();

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
                
                // Use the logged-in player's name or generate a random one
                String playerName = (player != null) ? player.getName() : "Player" + new Random().nextInt(1000);
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
                        String sender = parts[1];
        
                        // Reassemble message in case it contained colons
                        StringBuilder messageContent = new StringBuilder();
                        for (int i = 2; i < parts.length; i++) {
                            messageContent.append(parts[i]);
                            if (i < parts.length - 1) messageContent.append(":");
                        }
        
                        // Display with formatting
                        displayChatMessage(sender, messageContent.toString(), false);
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

    private VBox createChatPanel() {
        VBox chatPanel = new VBox(10);
        chatPanel.setPadding(new Insets(15));
        chatPanel.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: #C5CAE9;" +
            "-fx-border-radius: 10px;" +
            "-fx-border-width: 1px;"
        );
        
        // Header with icon
        HBox chatHeader = new HBox(10);
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label chatIconLabel = new Label("💬");
        chatIconLabel.setFont(Font.font("Arial", 18));
        
        Label chatTitle = new Label("Chat");
        chatTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        chatTitle.setStyle("-fx-text-fill: #303F9F;");
        
        chatHeader.getChildren().addAll(chatIconLabel, chatTitle);
        
        // Chat area with better styling
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(250);
        chatArea.setWrapText(true);
        chatArea.setFont(Font.font("Arial", 15));
        chatArea.setStyle(
            "-fx-control-inner-background: #F5F5F5;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-padding: 8px;" +
            "-fx-font-size: 15px;"
        );
        
        // Auto-scroll to bottom when new messages arrive
        chatArea.textProperty().addListener((observable, oldValue, newValue) -> {
            chatArea.setScrollTop(Double.MAX_VALUE);
        });
        
        // Input area with button
        HBox inputBox = new HBox(8);
        inputBox.setAlignment(Pos.CENTER);
        
        chatField = new TextField();
        chatField.setPrefHeight(40);
        chatField.setFont(Font.font("Arial", 15));
        chatField.setPromptText("Type a message...");
        chatField.setStyle(
            "-fx-background-radius: 20px;" +
            "-fx-border-radius: 20px;" +
            "-fx-border-color: #C5CAE9;" +
            "-fx-padding: 8px 15px 8px 15px;" +
            "-fx-font-size: 15px;"
        );
        
        // Make chat field expand to fill available space
        HBox.setHgrow(chatField, Priority.ALWAYS);
        
        Button sendButton = new Button("Send");
        sendButton.setPrefHeight(40);
        sendButton.setPrefWidth(80);
        sendButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        sendButton.setStyle(
            "-fx-background-color: #3F51B5;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20px;" +
            "-fx-cursor: hand;"
        );
        
        // Add hover effect to button
        sendButton.setOnMouseEntered(e -> 
            sendButton.setStyle(
                "-fx-background-color: #303F9F;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 20px;" +
                "-fx-cursor: hand;"
            )
        );
        
        sendButton.setOnMouseExited(e -> 
            sendButton.setStyle(
                "-fx-background-color: #3F51B5;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 20px;" +
                "-fx-cursor: hand;"
            )
        );
        
        // Add action handlers
        chatField.setOnAction(e -> sendChat());
        sendButton.setOnAction(e -> sendChat());
        
        inputBox.getChildren().addAll(chatField, sendButton);
        
        // Add everything to the chat panel
        chatPanel.getChildren().addAll(chatHeader, chatArea, inputBox);
        
        return chatPanel;
    }

    // Improved method to display chat messages with formatting
    private void displayChatMessage(String sender, String message, boolean isCurrentUser) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            
            StringBuilder formattedMsg = new StringBuilder();
            
            if (isCurrentUser) {
                formattedMsg.append("[").append(timestamp).append("] ");
                formattedMsg.append("You: ");
                formattedMsg.append(message).append("\n");
                
                // Use custom styling for your messages
                chatArea.setStyle(
                    "-fx-control-inner-background: #F5F5F5;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-radius: 8px;" +
                    "-fx-border-color: #E0E0E0;" +
                    "-fx-padding: 8px;" +
                    "-fx-font-size: 15px;" +
                    "-fx-highlight-fill: #E8EAF6;" + 
                    "-fx-highlight-text-fill: #3F51B5;"
                );
            } else {
                formattedMsg.append("[").append(timestamp).append("] ");
                formattedMsg.append(sender).append(": ");
                formattedMsg.append(message).append("\n");
            }
            
            chatArea.appendText(formattedMsg.toString());
        });
    }

    // Improved send chat method
    private void sendChat() {
        if (connected && chatField.getText() != null && !chatField.getText().trim().isEmpty()) {
            try {
                String message = chatField.getText().trim();
                out.writeUTF("CHAT:" + message);
                
                // Display in chat area with formatting
                displayChatMessage("You", message, true);
                
                // Clear input field
                chatField.clear();
                
                // Give focus back to chat field
                chatField.requestFocus();
                
            } catch (IOException e) {
                statusLabel.setText("Error sending chat: " + e.getMessage());
                statusLabel.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-background-radius: 20px;");
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
                // REMOVE THE CALLBACK FOR ONLINE GAMES
                // We're already updating the database in gameSession.updatePlayerStats()
                // if (gameOverCallback != null && player != null) {
                //     gameOverCallback.accept(Player.GameResult.WIN);
                // }
                break;
            case "LOSE":
                statusLabel.setText("Game over - You lost");
                // REMOVE THE CALLBACK FOR ONLINE GAMES
                // if (gameOverCallback != null && player != null) {
                //     gameOverCallback.accept(Player.GameResult.LOSE);
                // }
                break;
            case "DRAW":
                statusLabel.setText("Game over - It's a draw!");
                // REMOVE THE CALLBACK FOR ONLINE GAMES
                // if (gameOverCallback != null && player != null) {
                //     gameOverCallback.accept(Player.GameResult.DRAW);
                // }
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

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGameOverCallback(Consumer<Player.GameResult> callback) {
        this.gameOverCallback = callback;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
