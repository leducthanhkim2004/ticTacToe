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
    private int boardSize = 3; // Default to standard game
    private boolean isMyTurn = false;
    private Symbol mySymbol;
    private Stage primaryStage;
    private BorderPane root;
    private GridPane boardGrid;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Main layout
        root = new BorderPane();
        
        // Top section
        VBox topBox = new VBox(10);
        statusLabel = new Label("Connecting to server...");
        statusLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        statusLabel.setPadding(new Insets(10));
        topBox.getChildren().add(statusLabel);
        
        // Create the game selection pane
        VBox gameSelectionBox = createGameSelectionPane();
        
        // Add components to the root layout
        root.setTop(topBox);
        root.setCenter(gameSelectionBox);
        
        // Set up the scene
        Scene scene = new Scene(root, 700, 600);
        primaryStage.setTitle("Online Tic Tac Toe");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Connect to server when the view is shown
        connectToServer();
        
        // Handle window close
        primaryStage.setOnCloseRequest(e -> disconnect());
    }
    
    private VBox createGameSelectionPane() {
        VBox selectionBox = new VBox(20);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(50));
        
        Label titleLabel = new Label("Select Game Type");
        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        
        Button standardButton = new Button("Standard Tic Tac Toe (3x3)");
        standardButton.setPrefWidth(250);
        standardButton.setOnAction(e -> selectGameType(GameType.STANDARD));
        
        Button ultimateButton = new Button("Ultimate Tic Tac Toe (9x9)");
        ultimateButton.setPrefWidth(250);
        ultimateButton.setOnAction(e -> selectGameType(GameType.ULTIMATE));
        
        selectionBox.getChildren().addAll(titleLabel, standardButton, ultimateButton);
        
        return selectionBox;
    }
    
    private GridPane createGameBoard(int size) {
        // Create or recreate the board grid
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
                // Send game type selection to server
                out.writeUTF("SELECT_GAME:" + gameType.name());
                
                // Update the board size based on game type
                boardSize = gameType == GameType.STANDARD ? 3 : 9;
                
                // Update status
                statusLabel.setText("Selected " + gameType + " game. Waiting for opponent...");
                
                // Switch to game board view
                setupGameInterface();
            }
        } catch (IOException e) {
            statusLabel.setText("Error selecting game: " + e.getMessage());
        }
    }
    
    private void setupGameInterface() {
        // Create game board
        GridPane boardGrid = createGameBoard(boardSize);
        
        // Create chat panel
        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(10));
        chatBox.setPrefWidth(200);
        
        Label chatLabel = new Label("Chat");
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefHeight(300);
        
        HBox chatInputBox = new HBox(5);
        chatField = new TextField();
        chatField.setPrefWidth(150);
        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendChat());
        chatInputBox.getChildren().addAll(chatField, sendButton);
        
        chatBox.getChildren().addAll(chatLabel, chatArea, chatInputBox);
        
        // Update the root layout
        Platform.runLater(() -> {
            root.setCenter(boardGrid);
            root.setRight(chatBox);
        });
    }
    
    private void connectToServer() {
        // Run in a background thread
        new Thread(() -> {
            try {
                // Connect to server (change host/port as needed)
                System.out.println("Attempting to connect to localhost:1234...");
                socket = new Socket("localhost", 1234);
                System.out.println("Connected successfully!");
                
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                
                connected = true;
                
                // Send player registration with random name
                String playerName = "Player" + new Random().nextInt(1000);
                out.writeUTF("REGISTER:" + playerName);
                
                Platform.runLater(() -> 
                    statusLabel.setText("Connected as " + playerName + ". Select game type."));
                
                // Start listening for server messages
                listenForMessages();
                
            } catch (IOException e) {
                Platform.runLater(() -> 
                    statusLabel.setText("Could not connect to server: " + e.getMessage()));
                System.err.println("Connection error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    private void listenForMessages() {
        new Thread(() -> {
            try {
                while (connected) {
                    String message = in.readUTF();
                    System.out.println("Received: " + message);
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
                    
                case "GAME_TYPES":
                    // Server sent available game types
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
                    boolean myTurn = Boolean.parseBoolean(parts[1]);
                    isMyTurn = myTurn;
                    statusLabel.setText(myTurn ? "Your turn" : "Opponent's turn");
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
        // Format: GAME_START:gameType:symbol:opponentName
        if (parts.length >= 4) {
            String gameType = parts[1];
            String symbolStr = parts[2];
            String opponentName = parts[3];
            
            // Set board size based on game type
            boardSize = gameType.equals("ULTIMATE") ? 9 : 3;
            mySymbol = symbolStr.equals("X") ? Symbol.X : Symbol.O;
            
            // Recreate the game board with the correct size
            setupGameInterface();
            
            statusLabel.setText("Game started! You are " + symbolStr + 
                                ". Playing against: " + opponentName);
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
                        
                        // Style based on symbol
                        String color = symbolStr.equals("X") ? "blue" : "red";
                        buttons[row][col].setStyle("-fx-text-fill: " + color + ";");
                    } else {
                        buttons[row][col].setText("");
                        buttons[row][col].setStyle("");
                    }
                }
            }
        }
    }
    
    private void handleMoveUpdate(String[] parts) {
        // Format: MOVE:row:col:symbol
        if (parts.length >= 4) {
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);
            String symbol = parts[3];
            
            if (row < boardSize && col < boardSize) {
                buttons[row][col].setText(symbol);
                String color = symbol.equals("X") ? "blue" : "red";
                buttons[row][col].setStyle("-fx-text-fill: " + color + ";");
            }
        }
    }
    
    private void handleButtonClick(int row, int col) {
        if (isMyTurn && connected) {
            // Don't allow clicking on already filled cells
            if (buttons[row][col].getText() != null && !buttons[row][col].getText().isEmpty()) {
                return;
            }
            
            try {
                // FIXED: Send move to server with correct command name - MOVE instead of MAKE_MOVE
                out.writeUTF("MOVE:" + row + ":" + col);
                
                // Update local board immediately for responsiveness
                buttons[row][col].setText(mySymbol.toString());
                buttons[row][col].setStyle("-fx-text-fill: " + (mySymbol == Symbol.X ? "blue" : "red") + ";");
                
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
                // FIXED: Use CHAT instead of SEND_CHAT to match server expectations
                out.writeUTF("CHAT:" + message);
                chatField.clear();
                
                // Update chat area locally
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
                // FIXED: Use QUIT instead of DISCONNECT to match server expectations
                out.writeUTF("QUIT");
                out.flush();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
        // Removed the Platform.exit() and System.exit(0) calls which could cause unexpected application termination
    }
    
    private void disableAllButtons() {
        if (buttons == null) return;
        
        Platform.runLater(() -> {
            try {
                for (int row = 0; row < boardSize; row++) {
                    for (int col = 0; col < boardSize; col++) {
                        if (row < buttons.length && col < buttons[row].length && buttons[row][col] != null) {
                            buttons[row][col].setDisable(true);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error disabling buttons: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void handleGameOver(String result) {
        try {
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
                    break;
            }
            disableAllButtons();
            
            // Add play again button
            Button playAgainBtn = new Button("Play Again");
            playAgainBtn.setOnAction(e -> {
                // Reset the view to selection mode
                setupSelectionInterface();
            });
            
            HBox buttonsBox = new HBox(10);
            buttonsBox.setAlignment(Pos.CENTER);
            buttonsBox.getChildren().add(playAgainBtn);
            
            VBox gameOverBox = new VBox(10);
            gameOverBox.setAlignment(Pos.BOTTOM_CENTER);
            gameOverBox.getChildren().add(buttonsBox);
            
            // Add game over interface at the bottom
            Platform.runLater(() -> {
                if (root != null) {
                    root.setBottom(gameOverBox);
                }
            });
        } catch (Exception e) {
            System.err.println("Error handling game over: " + e.getMessage());
        }
    }

    private void setupSelectionInterface() {
        Platform.runLater(() -> {
            if (root != null) {
                VBox selectionBox = createGameSelectionPane();
                root.setCenter(selectionBox);
                root.setRight(null);
                root.setBottom(null);
                statusLabel.setText("Select a game type");
            }
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}