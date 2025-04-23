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
    
    @Override
    public void start(Stage primaryStage) {
        // Main layout
        BorderPane root = new BorderPane();
        
        // Top section
        VBox topBox = new VBox(10);
        statusLabel = new Label("Connecting to server...");
        statusLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        statusLabel.setPadding(new Insets(10));
        topBox.getChildren().add(statusLabel);
        
        // Center section with game board
        GridPane boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(5);
        boardGrid.setVgap(5);
        
        buttons = new Button[9][9]; // Maximum size for Ultimate
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                Button button = new Button();
                button.setPrefSize(60, 60);
                button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                
                final int r = row;
                final int c = col;
                button.setOnAction(e -> handleButtonClick(r, c));
                
                buttons[row][col] = button;
                boardGrid.add(button, col, row);
                
                // Initially hide all buttons
                button.setVisible(false);
            }
        }
        
        // Right side chat panel
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
        
        // Add all components to the main layout
        root.setTop(topBox);
        root.setCenter(boardGrid);
        root.setRight(chatBox);
        
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
    
    private void connectToServer() {
        // Run in a background thread
        new Thread(() -> {
            try {
                // Connect to server (change host/port as needed)
                socket = new Socket("localhost", 1234);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                
                connected = true;
                
                // Send player registration with random name
                String playerName = "Player" + new Random().nextInt(1000);
                out.writeUTF("REGISTER:" + playerName);
                
                Platform.runLater(() -> 
                    statusLabel.setText("Connected as " + playerName + ". Waiting for opponent..."));
                
                // Start listening for server messages
                listenForMessages();
                
            } catch (IOException e) {
                Platform.runLater(() -> 
                    statusLabel.setText("Could not connect to server: " + e.getMessage()));
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
        System.out.println("Received: " + message);
        String[] parts = message.split(":");
        String command = parts[0];
        
        Platform.runLater(() -> {
            switch (command) {
                case "CONNECTED":
                    statusLabel.setText("Connected as " + parts[1] + ". Waiting for opponent...");
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
            
            statusLabel.setText("Game started! You are " + symbolStr + 
                                ". Playing against: " + opponentName);
            
            // Configure visible buttons based on board size
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    boolean visible = row < boardSize && col < boardSize;
                    buttons[row][col].setVisible(visible);
                    
                    // Adjust button size
                    if (visible) {
                        int buttonSize = boardSize == 3 ? 100 : 60;
                        buttons[row][col].setPrefSize(buttonSize, buttonSize);
                        buttons[row][col].setText("");
                    }
                }
            }
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
            
            buttons[row][col].setText(symbol);
            String color = symbol.equals("X") ? "blue" : "red";
            buttons[row][col].setStyle("-fx-text-fill: " + color + ";");
        }
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
        }
        disableAllButtons();
    }
    
    private void handleButtonClick(int row, int col) {
        if (!isMyTurn) {
            statusLabel.setText("Not your turn!");
            return;
        }
        
        if (row >= boardSize || col >= boardSize || !buttons[row][col].getText().isEmpty()) {
            return;
        }
        
        try {
            // Send move to server
            out.writeUTF("MOVE:" + row + ":" + col);
            
            // Disable further moves until server confirms
            isMyTurn = false;
            statusLabel.setText("Waiting for opponent...");
            
        } catch (IOException e) {
            statusLabel.setText("Error sending move: " + e.getMessage());
        }
    }
    
    private void sendChat() {
        String message = chatField.getText().trim();
        if (message.isEmpty()) return;
        
        try {
            out.writeUTF("CHAT:" + message);
            chatField.clear();
        } catch (IOException e) {
            statusLabel.setText("Error sending chat: " + e.getMessage());
        }
    }
    
    private void disableAllButtons() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col].setDisable(true);
            }
        }
    }
    
    private void disconnect() {
        connected = false;
        try {
            if (out != null) {
                out.writeUTF("QUIT");
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}