package Viewer;

import javafx.animation.RotateTransition;
import javafx.animation.Interpolator;
import javafx.util.Duration;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.scene.control.ProgressBar;
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

    // NOTE: Using player consistently throughout the code
    private Player player; // This is YOUR player
    private Player opponentPlayer; // This is the opponent
    private Consumer<Player.GameResult> gameOverCallback;
    private Label opponentLabel;
    private Label playerInfoLabel;
    private String mySymbolStr;

    // Add these fields to the class
    private Label timerLabel;
    private ProgressBar timerProgressBar;
    private int timeLeft = 60; // 60 seconds per turn

    // Add this constant at the top of your OnlineGameView class
    private static final int TURN_TIME_LIMIT = 60; // 60 seconds, same as in gameSession

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

        // Create timer display with better visibility
        HBox timerBox = new HBox(10);
        timerBox.setAlignment(Pos.CENTER);
        timerBox.setPadding(new Insets(10));
        timerBox.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 10px; -fx-border-color: #CCCCCC; -fx-border-radius: 10px; -fx-border-width: 1px;");
        
        // Add a clock icon for visual appeal
        Label clockIcon = new Label("⏱");
        clockIcon.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Make the timer text larger and more visible
        timerLabel = new Label("60s");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        timerLabel.setStyle("-fx-text-fill: #2E7D32;");
        
        timerProgressBar = new ProgressBar(1.0);
        timerProgressBar.setPrefWidth(200);
        timerProgressBar.setPrefHeight(20); // Make progress bar taller
        timerProgressBar.setStyle("-fx-accent: #4CAF50;"); // Green progress bar
        
        timerBox.getChildren().addAll(clockIcon, timerProgressBar, timerLabel);
        
        // Create a top game info bar that will contain the timer
        HBox gameTopBar = new HBox(20);
        gameTopBar.setAlignment(Pos.CENTER);
        gameTopBar.setPadding(new Insets(10, 10, 5, 10));
        gameTopBar.getChildren().add(timerBox);
        
        // Create the main content that includes the board and top bar
        VBox centerContent = new VBox(10);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(gameTopBar, boardGrid);

        Platform.runLater(() -> {
            root.setCenter(centerContent); // Set the combined content instead of just the board
            root.setRight(chatBox);
        });
        
        // If this is called after game start, update the timer display immediately
        if (timerLabel != null) {
            timerLabel.setText("60s");
            timerLabel.setStyle("-fx-text-fill: #2E7D32;");
        }
        if (timerProgressBar != null) {
            timerProgressBar.setProgress(1.0);
            timerProgressBar.setStyle("-fx-accent: #4CAF50;");
        }
    }

    private VBox createChatPanel() {
        VBox chatBox = new VBox(10);
        chatBox.setPrefWidth(250);
        chatBox.setMaxWidth(250);
        chatBox.setMinWidth(200);
        chatBox.setPadding(new Insets(15));
        chatBox.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 10px;");
        
        // Add shadow to chat panel
        DropShadow chatShadow = new DropShadow();
        chatShadow.setColor(Color.color(0, 0, 0, 0.5));
        chatShadow.setRadius(10);
        chatShadow.setOffsetY(4);
        chatBox.setEffect(chatShadow);
        
        Label chatLabel = new Label("Chat");
        chatLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        chatLabel.setStyle("-fx-text-fill: #303F9F;");
        
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(500);
        chatArea.setStyle("-fx-control-inner-background: #f5f5f5; -fx-background-radius: 8px;");
        
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        
        chatField = new TextField();
        chatField.setPrefWidth(170);
        chatField.setPromptText("Type a message...");
        chatField.setStyle("-fx-background-radius: 20px;");
        chatField.setOnAction(e -> sendChat());
        
        Button sendButton = new Button("Send");
        sendButton.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        sendButton.setStyle("-fx-background-color: #3F51B5; -fx-text-fill: white; -fx-background-radius: 20px;");
        sendButton.setOnAction(e -> sendChat());
        
        inputBox.getChildren().addAll(chatField, sendButton);
        
        chatBox.getChildren().addAll(chatLabel, chatArea, inputBox);
        return chatBox;
    }

    private void sendChat() {
        try {
            if (connected && chatField.getText() != null && !chatField.getText().trim().isEmpty()) {
                String message = chatField.getText().trim();
                out.writeUTF("CHAT:" + message);
                chatField.clear();
                // Don't add the message to chat area here - wait for server echo
            }
        } catch (IOException e) {
            statusLabel.setText("Error sending chat: " + e.getMessage());
            statusLabel.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-background-radius: 20px;");
        }
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
                
                // If player is null, create it with temporary Symbol.X that will be replaced later
                if (player == null) {
                    player = new Player(playerName, Symbol.X); // Temporary symbol
                }
                
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
        Platform.runLater(() -> {
            try {
                System.out.println("Received: " + message);
                
                // Add this new case to handle timer stop messages
                if (message.equals("TIMER_STOP")) {
                    // Hide or reset the timer when game is over
                    if (timerLabel != null) {
                        timerLabel.setVisible(false);
                    }
                    if (timerProgressBar != null) {
                        timerProgressBar.setVisible(false);
                    }
                    return;
                }
                
                // Handle player stats message
                if (message.startsWith("PLAYER_STATS:")) {
                    String[] statParts = message.substring("PLAYER_STATS:".length()).split(":");
                    if (statParts.length >= 4) {
                        // Update local player object with server data
                        int wins = Integer.parseInt(statParts[0]);
                        int losses = Integer.parseInt(statParts[1]);
                        int draws = Integer.parseInt(statParts[2]);
                        int score = Integer.parseInt(statParts[3]);
                        
                        // Only update if values actually changed
                        boolean updated = false;
                        if (player.getGame_Win() != wins) {
                            player.setGame_Win(wins);
                            updated = true;
                        }
                        if (player.getGame_Lose() != losses) {
                            player.setGame_Lose(losses);
                            updated = true;
                        }
                        if (player.getGame_Draw() != draws) {
                            player.setGame_Draw(draws);
                            updated = true;
                        }
                        if (player.getScore() != score) {
                            player.setScore(score);
                            updated = true;
                        }
                        
                        // Update the UI with new stats if anything changed
                        if (updated) {
                            updatePlayerInfoDisplay();
                        }
                    }
                    return;
                }
                
                // Handle timer message
                else if (message.startsWith("TIMER:")) {
                    try {
                        String[] timerParts = message.substring("TIMER:".length()).split(":");
                        timeLeft = Integer.parseInt(timerParts[0]);
                        boolean isMyTimer = timerParts.length > 1 && Boolean.parseBoolean(timerParts[1]);
                        
                        Platform.runLater(() -> {
                            // Update label
                            timerLabel.setText(timeLeft + "s");
                            
                            // IMPORTANT FIX: Use explicit casting to double before division
                            double progress = (double)timeLeft / (double)TURN_TIME_LIMIT;
                            timerProgressBar.setProgress(progress);
                            
                            // Make sure both timerLabel and timerProgressBar are visible
                            timerLabel.setVisible(true);
                            timerProgressBar.setVisible(true);
                            
                            // Update turn label based on whose timer it is
                            try {
                                Label turnLabel = (Label)((VBox)timerProgressBar.getParent().getParent()).getChildren().get(0);
                                if (isMyTimer) {
                                    turnLabel.setText("Your turn");
                                    turnLabel.setStyle("-fx-text-fill: #2E7D32;");
                                } else {
                                    turnLabel.setText("Opponent's turn");
                                    turnLabel.setStyle("-fx-text-fill: #3F51B5;");
                                }
                            } catch (Exception e) {
                                System.err.println("Error updating turn label: " + e.getMessage());
                            }
                            
                            // Change colors based on time remaining
                            if (timeLeft <= 10) {
                                // Red for urgent (less than 10 seconds)
                                timerLabel.setStyle("-fx-text-fill: #D32F2F;");
                                timerProgressBar.setStyle("-fx-accent: #F44336;");
                            } else if (timeLeft <= 20) {
                                // Yellow for warning (less than 20 seconds)
                                timerLabel.setStyle("-fx-text-fill: #FF8F00;");
                                timerProgressBar.setStyle("-fx-accent: #FFC107;");
                            } else {
                                // Green for plenty of time
                                timerLabel.setStyle("-fx-text-fill: #2E7D32;");
                                timerProgressBar.setStyle("-fx-accent: #4CAF50;");
                            }
                        });
                    } catch (Exception e) {
                        System.err.println("Error processing timer message: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return;
                }
                    
                // Handle game over without updating database
                else if (message.startsWith("GAME_OVER:")) {
                    String result = message.substring("GAME_OVER:".length());
                    
                    switch (result) {
                        case "WIN":
                            // You won - update UI only (server will update DB)
                            statusLabel.setText("You won the game!");
                            disableAllButtons();
                            showGameOverDialog(true, false);
                            break;
                        case "LOSE":
                            // You lost - update UI only (server will update DB)
                            statusLabel.setText("You lost the game.");
                            disableAllButtons();
                            showGameOverDialog(false, false);
                            break;
                        case "DRAW":
                            // Draw - update UI only (server will update DB)
                            statusLabel.setText("Game ended in a draw.");
                            disableAllButtons();
                            showGameOverDialog(false, true);
                            break;
                        case "WIN_BY_TIMEOUT":
                            statusLabel.setText("You won! Opponent ran out of time.");
                            disableAllButtons();
                            showGameOverDialog(true, true);
                            break;
                        case "LOSE_BY_TIMEOUT":
                            statusLabel.setText("You lost! You ran out of time.");
                            disableAllButtons();
                            showGameOverDialog(false, true);
                            break;
                    }
                    return;
                }
                
                // Split the message for processing
                String[] parts = message.split(":", 3); // Limit to 3 parts for chat messages
                String command = parts[0];
                
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
                    case "OPPONENT_DISCONNECTED":
                        statusLabel.setText("Your opponent disconnected");
                        disableAllButtons();
                        break;
                    case "CHAT":
                        if (parts.length >= 3) {
                            String sender = parts[1];
                            String chatMessage = parts[2];
                            
                            // Display chat message with proper formatting
                            displayChatMessage(sender, chatMessage);
                        }
                        break;
                    case "ERROR":
                        statusLabel.setText("Error: " + parts[1]);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error processing server message: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Fix the handleGameStart method
    private void handleGameStart(String[] parts) {
        if (parts.length >= 4) {
            boardSize = parts[1].equals("ULTIMATE") ? 9 : 3;
            mySymbolStr = parts[2];
            setupGameInterface();

            // Initialize players if not already done
            if (player == null) {
                // Use proper constructor - player should already be set via setPlayer method
                // Just set the symbol for the existing player
                Symbol symbol = Symbol.valueOf(mySymbolStr);
                player.setSymbol(symbol);
            } else {
                // Just update the symbol if player already exists
                player.setSymbol(Symbol.valueOf(mySymbolStr));
            }
            
            // Set opponent with correct constructor
            String opponentName = parts[3];
            Symbol opponentSymbol = mySymbolStr.equals("X") ? Symbol.O : Symbol.X;
            
            if (opponentPlayer == null) {
                // Since we know Player constructor requires Symbol
                opponentPlayer = new Player(opponentName, opponentSymbol);
            } else {
                opponentPlayer.setName(opponentName);
                opponentPlayer.setSymbol(opponentSymbol);
            }
            
            // Setup player info display
            setupPlayerInfoDisplay();
            
            statusLabel.setText("Game started! You are " + parts[2] + ". Playing against: " + opponentName);
        }
        
        // Set up the game interface with board and chat
        setupGameInterface();
        
        // Make sure timerLabel and timerProgressBar are initialized and visible
        if (timerLabel != null) {
            timerLabel.setText("60s");
            timerLabel.setStyle("-fx-text-fill: #2E7D32;");
        }
        if (timerProgressBar != null) {
            timerProgressBar.setProgress(1.0);
            timerProgressBar.setStyle("-fx-accent: #4CAF50;");
        }
        
        // Update turn label based on who goes first (X always goes first)
        Platform.runLater(() -> {
            try {
                Label turnLabel = (Label)((VBox)timerProgressBar.getParent().getParent()).getChildren().get(0);
                boolean amIX = "X".equals(mySymbolStr);
                if (amIX) {
                    turnLabel.setText("Your turn");
                    turnLabel.setStyle("-fx-text-fill: #2E7D32;");
                } else {
                    turnLabel.setText("Opponent's turn");
                    turnLabel.setStyle("-fx-text-fill: #3F51B5;");
                }
            } catch (Exception e) {
                System.err.println("Error updating turn label: " + e.getMessage());
            }
        });
    }

    // Add a simple refresh button to the player info panel
    private void setupPlayerInfoDisplay() {
        // Create player info panel if it doesn't exist
        if (gameInfoPanel == null) {
            gameInfoPanel = new VBox(10);
            gameInfoPanel.setPadding(new Insets(15));
            gameInfoPanel.setAlignment(Pos.CENTER);
            gameInfoPanel.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-background-radius: 10px;");
            
            // Create opponent label
            opponentLabel = new Label("Opponent: " + (opponentPlayer != null ? opponentPlayer.getName() : "Waiting..."));
            opponentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            
            // Create player info label with default values if needed
            int wins = player != null ? player.getGame_Win() : 0;
            int losses = player != null ? player.getGame_Lose() : 0;
            int draws = player != null ? player.getGame_Draw() : 0;
            int score = player != null ? player.getScore() : 0;
            
            playerInfoLabel = new Label(String.format(
                "Wins: %d | Losses: %d | Draws: %d | Score: %d", 
                wins, losses, draws, score
            ));
            playerInfoLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            
            // Simple refresh button
            Button refreshButton = new Button("⟳");
            refreshButton.setPrefSize(24, 24);
            refreshButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-background-radius: 50%;");
            refreshButton.setOnAction(e -> {
                // Simple visual feedback
                playerInfoLabel.setText("Refreshing stats...");
                
                // Request updated stats
                try {
                    if (connected) {
                        out.writeUTF("REQUEST_STATS");
                    }
                } catch (IOException ex) {
                    System.err.println("Error requesting stats: " + ex.getMessage());
                }
            });
            
            // Create an HBox to hold the player info and refresh button
            HBox infoBox = new HBox(10);
            infoBox.setAlignment(Pos.CENTER);
            infoBox.getChildren().addAll(playerInfoLabel, refreshButton);
            
            gameInfoPanel.getChildren().addAll(opponentLabel, infoBox);
            
            // Add to the top section
            VBox topBox = (VBox) root.getTop();
            if (topBox != null) {
                topBox.getChildren().add(gameInfoPanel);
            }
        } else {
            // Update existing labels
            if (opponentLabel != null) {
                opponentLabel.setText("Opponent: " + (opponentPlayer != null ? opponentPlayer.getName() : "Waiting..."));
            }
            updatePlayerInfoDisplay();
        }
    }

    private void refreshPlayerStats() {
        if (player != null && !player.getName().startsWith("Guest") && connected) {
            try {
                // Find the refresh button in its parent container
                Button refreshButton = null;
                for (Node node : ((HBox) playerInfoLabel.getParent()).getChildren()) {
                    if (node instanceof Button && ((Button) node).getTooltip() != null &&
                        ((Button) node).getTooltip().getText().equals("Refresh stats")) {
                        refreshButton = (Button) node;
                        break;
                    }
                }
                
                // Create and play rotation animation
                if (refreshButton != null) {
                    // Create rotate transition
                    RotateTransition rotateTransition = new RotateTransition(Duration.seconds(0.8), refreshButton);
                    rotateTransition.setByAngle(360);
                    rotateTransition.setCycleCount(1);
                    rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
                    rotateTransition.play();
                }
                
                // Request updated stats from server
                out.writeUTF("REQUEST_STATS");
                
                // Show loading animation
                playerInfoLabel.setText("Refreshing stats...");
                
            } catch (IOException e) {
                System.err.println("Error requesting stats: " + e.getMessage());
            }
        }
    }

    private void updatePlayerInfoDisplay() {
        if (playerInfoLabel != null && player != null) {
            playerInfoLabel.setText(String.format(
                "Wins: %d | Losses: %d | Draws: %d | Score: %d", 
                player.getGame_Win(), 
                player.getGame_Lose(), 
                player.getGame_Draw(), 
                player.getScore()
            ));
        }
    }
    
    private void showGameOverDialog(boolean isWin, boolean isTimeout) {
        String title = isWin ? "You Won!" : "Game Over";
        String message = isWin ? 
            (isTimeout ? "Your opponent ran out of time!" : "Congratulations, you won the game!") :
            (isTimeout ? "You ran out of time!" : "You lost this game. Better luck next time!");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-font-size: 14px;");
        
        alert.showAndWait();
    }

    private void showReturnToLobbyButton() {
        Platform.runLater(() -> {
            // Reset timer display
            if (timerLabel != null) {
                timerLabel.setText("60s");
            }
            if (timerProgressBar != null) {
                timerProgressBar.setProgress(1.0);
            }
            
            Button returnButton = new Button("Return to Lobby");
            returnButton.setPrefWidth(200);
            returnButton.setPrefHeight(40);
            returnButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            returnButton.setStyle("-fx-background-color: #3F51B5; -fx-text-fill: white; -fx-background-radius: 8px;");
            
            // Add shadow effect
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.color(0, 0, 0, 0.4));
            shadow.setRadius(5);
            shadow.setOffsetY(2);
            returnButton.setEffect(shadow);
            
            returnButton.setOnAction(e -> {
                // Return to game selection screen
                root.setCenter(createGameSelectionPane());
                root.setRight(null);
                statusLabel.setText("Select a game type to play again");
            });
            
            // Add button at the bottom of the board
            BorderPane boardContainer = new BorderPane();
            boardContainer.setCenter(boardGrid);
            
            VBox bottomBox = new VBox(15);
            bottomBox.setAlignment(Pos.CENTER);
            bottomBox.setPadding(new Insets(15, 0, 0, 0));
            bottomBox.getChildren().add(returnButton);
            
            boardContainer.setBottom(bottomBox);
            
            root.setCenter(boardContainer);
        });
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

    // Add these missing methods
    private void handleButtonClick(int row, int col) {
        if (isMyTurn && connected) {
            try {
                // Send move to server
                out.writeUTF("MOVE:" + row + ":" + col);
                
                // Disable button until server confirms move
                buttons[row][col].setDisable(true);
                
                // Update status
                statusLabel.setText("Move sent. Waiting for confirmation...");
            } catch (IOException e) {
                statusLabel.setText("Error sending move: " + e.getMessage());
            }
        }
    }

    private void handleMoveUpdate(String[] parts) {
        if (parts.length >= 4) {
            int row = Integer.parseInt(parts[1]);
            int col = Integer.parseInt(parts[2]);
            String symbol = parts[3];
            
            // Update button with the move
            Button button = buttons[row][col];
            button.setText(symbol);
            button.setDisable(true);
            
            // Style based on symbol
            if (symbol.equals("X")) {
                button.setStyle("-fx-text-fill: #0284C7; -fx-background-color: #E0F2FE; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-border-color: #0284C7; -fx-border-width: 1px;");
            } else {
                button.setStyle("-fx-text-fill: #DC2626; -fx-background-color: #FEE2E2; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-border-color: #DC2626; -fx-border-width: 1px;");
            }
        }
    }
    
    private void updateBoard(String boardState) {
        if (boardState == null || boardState.isEmpty()) return;
        
        // Format: 0,0,X;0,1,O;...
        String[] cells = boardState.split(";");
        
        for (String cell : cells) {
            String[] parts = cell.split(",");
            if (parts.length >= 3) {
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                String symbol = parts[2];
                
                if (row >= 0 && row < boardSize && col >= 0 && col < boardSize) {
                    Button button = buttons[row][col];
                    
                    if (!symbol.equals("EMPTY")) {
                        button.setText(symbol);
                        button.setDisable(true);
                        
                        // Style based on symbol
                        if (symbol.equals("X")) {
                            button.setStyle("-fx-text-fill: #0284C7; -fx-background-color: #E0F2FE; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-border-color: #0284C7; -fx-border-width: 1px;");
                        } else {
                            button.setStyle("-fx-text-fill: #DC2626; -fx-background-color: #FEE2E2; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-border-color: #DC2626; -fx-border-width: 1px;");
                        }
                    }
                }
            }
        }
    }

    // Fix the chat display method to avoid redundancy
    private void displayChatMessage(String sender, String message) {
        Platform.runLater(() -> {
            // Format timestamp once here
            String timestamp = new SimpleDateFormat("[HH:mm]").format(new Date());
            
            // Check if this is my message or opponent's
            if (player != null && sender.equals(player.getName())) {
                chatArea.appendText(timestamp + " You: " + message + "\n");
            } else {
                chatArea.appendText(timestamp + " " + sender + ": " + message + "\n");
            }
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
