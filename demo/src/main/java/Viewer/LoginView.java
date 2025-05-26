package Viewer;

import Database.UserDatabase;
import Model.Player;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class LoginView extends Application {
    private Stage primaryStage;
    private Player loggedInPlayer = null;
    private BorderPane mainLayout;
    private VBox loginPanel;
    private VBox registerPanel;
    private BorderPane gameSelectionPanel;
    private VBox leaderboardPanel;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("TicTacToe - Login");
        
        // Initialize database connection
        if (!UserDatabase.testConnection()) {
            showErrorAlert("Database Error", "Failed to connect to the database. Please check your connection settings.");
            return;
        }
        
        mainLayout = new BorderPane();
        
        // Create header
        Label titleLabel = new Label("Tic Tac Toe");
        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 30));
        titleLabel.setPadding(new Insets(20, 0, 20, 0));
        
        VBox headerBox = new VBox(titleLabel);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setStyle("-fx-background-color: #48CAE4;");
        mainLayout.setTop(headerBox);
        
        // Create login panel
        createLoginPanel();
        
        // Create register panel
        createRegisterPanel();
        
        // Set initial view to login
        showLoginPanel();
        
        Scene scene = new Scene(mainLayout, 500, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void createLoginPanel() {
        loginPanel = new VBox(20);  // Increased spacing
        loginPanel.setPadding(new Insets(40));  // More padding
        loginPanel.setAlignment(Pos.CENTER);
        loginPanel.setMaxWidth(450);  // Wider panel
        
        // Apply a nice background
        loginPanel.setStyle("-fx-background-color: #e6f7ff; -fx-background-radius: 15;");
        
        // Add drop shadow for depth
        DropShadow panelShadow = new DropShadow();
        panelShadow.setColor(Color.gray(0.4));
        panelShadow.setRadius(15);
        panelShadow.setOffsetY(5);
        loginPanel.setEffect(panelShadow);
        
        // Larger header
        Label headerLabel = new Label("Sign In");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));  // Larger font
        headerLabel.setTextFill(Color.DARKBLUE);
        
        // Username field with larger font
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(350);
        usernameField.setPrefHeight(40);  // Taller field
        usernameField.setFont(Font.font("Arial", 14));  // Larger text
        
        // Password field with larger font
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(350);
        passwordField.setPrefHeight(40);  // Taller field
        passwordField.setFont(Font.font("Arial", 14));  // Larger text
        
        // Attractive login button
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(350);
        loginButton.setPrefHeight(50);  // Taller button
        loginButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));  // Larger text
        
        // Blue button with white text
        loginButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-background-radius: 8;");
        
        // Drop shadow for button
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setColor(Color.gray(0.5));
        buttonShadow.setRadius(5);
        buttonShadow.setOffsetY(3);
        loginButton.setEffect(buttonShadow);
        
        // Guest button with different styling
        Button guestButton = new Button("Play as Guest");
        guestButton.setPrefWidth(350);
        guestButton.setPrefHeight(50);
        guestButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        guestButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 8;");
        guestButton.setEffect(buttonShadow);
        
        // More visible hyperlink
        Hyperlink registerLink = new Hyperlink("Don't have an account? Sign up");
        registerLink.setFont(Font.font("Arial", 16));
        registerLink.setTextFill(Color.BLUE);
        
        // Error message with larger font
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font("Arial", 14));
        
        // More spacing between elements
        VBox.setMargin(loginButton, new Insets(20, 0, 5, 0));
        VBox.setMargin(guestButton, new Insets(5, 0, 15, 0));
        
        // Handle login button click
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter both username and password");
                return;
            }
            
            errorLabel.setText("Logging in...");
            
            // Perform login in background thread
            new Thread(() -> {
                // Force database lookup for each login attempt
                Player player = UserDatabase.loginPlayer(username, password);
                
                Platform.runLater(() -> {
                    if (player != null) {
                        // Important: Set the logged in player to the newly retrieved player
                        loggedInPlayer = player;
                        errorLabel.setText("");
                        
                        // Clear fields after successful login
                        usernameField.clear();
                        passwordField.clear();
                        
                        // Recreate game selection panel with fresh data
                        gameSelectionPanel = null; // Force recreation
                        showGameSelectionPanel();
                    } else {
                        errorLabel.setText("Invalid username or password");
                    }
                });
            }).start();
        });
        
        // Handle play as guest
        guestButton.setOnAction(e -> {
            loggedInPlayer = new Player("Guest", null);
            
            // Clear fields
            usernameField.clear();
            passwordField.clear();
            errorLabel.setText("");
            
            showGameSelectionPanel();
        });
        
        // Handle register link
        registerLink.setOnAction(e -> showRegisterPanel());
        
        loginPanel.getChildren().addAll(
            headerLabel, 
            usernameLabel, usernameField,
            passwordLabel, passwordField,
            errorLabel,
            loginButton,
            guestButton,
            registerLink
        );
    }
    
    private void createRegisterPanel() {
        registerPanel = new VBox(20);  // Increased spacing
        registerPanel.setPadding(new Insets(40));
        registerPanel.setAlignment(Pos.CENTER);
        registerPanel.setMaxWidth(450);  // Wider panel
        
        // Apply a nice background
        registerPanel.setStyle("-fx-background-color: #e6f7ff; -fx-background-radius: 15;");
        
        // Add drop shadow for depth
        DropShadow panelShadow = new DropShadow();
        panelShadow.setColor(Color.gray(0.4));
        panelShadow.setRadius(15);
        panelShadow.setOffsetY(5);
        registerPanel.setEffect(panelShadow);
        
        // Larger header
        Label headerLabel = new Label("Create Account");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        headerLabel.setTextFill(Color.DARKBLUE);
        
        // Username field with larger font
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setMaxWidth(350);
        usernameField.setPrefHeight(40);
        usernameField.setFont(Font.font("Arial", 14));
        
        // Password fields with larger font
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        passwordField.setMaxWidth(350);
        passwordField.setPrefHeight(40);
        passwordField.setFont(Font.font("Arial", 14));
        
        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setMaxWidth(350);
        confirmPasswordField.setPrefHeight(40);
        confirmPasswordField.setFont(Font.font("Arial", 14));
        
        // Create account button
        Button registerButton = new Button("Create Account");
        registerButton.setPrefWidth(350);
        registerButton.setPrefHeight(50);
        registerButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        registerButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 8;");
        
        // Drop shadow for button
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setColor(Color.gray(0.5));
        buttonShadow.setRadius(5);
        buttonShadow.setOffsetY(3);
        registerButton.setEffect(buttonShadow);
        
        // More visible hyperlink
        Hyperlink loginLink = new Hyperlink("Already have an account? Sign in");
        loginLink.setFont(Font.font("Arial", 16));
        loginLink.setTextFill(Color.BLUE);
        
        // Error message with larger font
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font("Arial", 14));
        
        // More spacing between elements
        VBox.setMargin(registerButton, new Insets(15, 0, 10, 0));
        
        // Handle register button click
        registerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill all required fields");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                errorLabel.setText("Passwords do not match");
                return;
            }
            
            errorLabel.setText("Creating account...");
            
            // Perform registration in background thread
            new Thread(() -> {
                boolean success = UserDatabase.registerPlayer(username, password);
                
                Platform.runLater(() -> {
                    if (success) {
                        errorLabel.setTextFill(Color.GREEN);
                        errorLabel.setText("Account created successfully!");
                        
                        // Clear fields
                        usernameField.clear();
                        passwordField.clear();
                        confirmPasswordField.clear();
                        
                        // Switch to login panel after short delay
                        new Thread(() -> {
                            try {
                                Thread.sleep(1500);
                                Platform.runLater(() -> {
                                    // IMPORTANT: Recreate login panel to ensure fresh state
                                    createLoginPanel();
                                    showLoginPanel();
                            });
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                } else {
                    errorLabel.setTextFill(Color.RED);
                    errorLabel.setText("Username already exists or registration failed");
                }
            });
        }).start();
    });
    
    // Handle login link
    loginLink.setOnAction(e -> showLoginPanel());
    
    registerPanel.getChildren().addAll(
        headerLabel,
        usernameLabel, usernameField,
        passwordLabel, passwordField,
        confirmPasswordLabel, confirmPasswordField,
        errorLabel,
        registerButton,
        loginLink
    );
    }
    
    private void createGameSelectionPanel() {
        gameSelectionPanel = new BorderPane();
        
        // Apply a nice background with gradient
        gameSelectionPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #e6f7ff, #cce5ff);");
        
        // Top section with user info and logout
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(20, 30, 10, 30));
        topBar.setAlignment(Pos.CENTER_RIGHT);
        
        // Show logged in user information
        Label userLabel = new Label("Logged in as: " + loggedInPlayer.getName());
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        userLabel.setTextFill(Color.DARKBLUE);
        
        // Spacer to push elements apart
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        logoutButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5;");
        logoutButton.setOnAction(e -> logout());
        
        topBar.getChildren().addAll(userLabel, spacer, logoutButton);
        
        // Center section with title and game options
        VBox centerContent = new VBox(25);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(20, 30, 40, 30));
        
        // Game selection title
        Label titleLabel = new Label("Select Game Mode");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.DARKBLUE);
        
        // Game buttons container
        VBox gameButtons = new VBox(18);
        gameButtons.setAlignment(Pos.CENTER);
        gameButtons.setPadding(new Insets(20, 0, 20, 0));
        
        // Online game button
        Button onlineGameButton = createGameButton("Online Multiplayer", Color.web("#28a745"));
        onlineGameButton.setOnAction(e -> startOnlineGame());
        
        // Stats button
        Button statsButton = createGameButton("View Leaderboard", Color.web("#6f42c1"));
        statsButton.setOnAction(e -> showLeaderboard());
        
        // Add all game buttons
        gameButtons.getChildren().addAll(
            onlineGameButton,
            statsButton
        );
        
        // Add everything to the center content
        centerContent.getChildren().addAll(titleLabel, gameButtons);
        
        // Status bar at bottom
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(10, 20, 10, 20));
        statusBar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");
        
        // Player stats
        Label statsLabel = new Label(String.format("Wins: %d | Losses: %d | Draws: %d | Score: %d", 
            loggedInPlayer.getGame_Win(), 
            loggedInPlayer.getGame_Lose(), 
            loggedInPlayer.getGame_Draw(),
            loggedInPlayer.getScore()));
        statsLabel.setFont(Font.font("Arial", 14));
        statsLabel.setTextFill(Color.GRAY);
        
        statusBar.getChildren().add(statsLabel);
        
        // Add all sections to the main panel
        gameSelectionPanel.setTop(topBar);
        gameSelectionPanel.setCenter(centerContent);
        gameSelectionPanel.setBottom(statusBar);
    }
    
    // Helper method to create consistently styled game buttons
    private Button createGameButton(String text, Color color) {
        Button button = new Button(text);
        button.setPrefWidth(400);
        button.setPrefHeight(60);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        // Convert Color to hex string
        String colorHex = String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
        
        button.setStyle(
            "-fx-background-color: " + colorHex + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;"
        );
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.3));
        shadow.setRadius(10);
        shadow.setOffsetY(5);
        button.setEffect(shadow);
        
        // Add hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: derive(" + colorHex + ", -10%);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10;"
            );
        });
        
        return button;
    }
    
    private void createLeaderboardPanel() {
        leaderboardPanel = new VBox(15);
        leaderboardPanel.setPadding(new Insets(30));
        leaderboardPanel.setAlignment(Pos.CENTER);
        
        Label headerLabel = new Label("Top 5 Players");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        TableView<Player> table = new TableView<>();
        table.setPrefHeight(300);
        
        TableColumn<Player, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName());
        });
        nameColumn.setPrefWidth(150);
        
        TableColumn<Player, Number> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getScore());
        });
        scoreColumn.setPrefWidth(100);
        
        TableColumn<Player, Number> winsColumn = new TableColumn<>("Wins");
        winsColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getGame_Win());
        });
        winsColumn.setPrefWidth(80);
        
        TableColumn<Player, Number> lossesColumn = new TableColumn<>("Losses");
        lossesColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getGame_Lose());
        });
        lossesColumn.setPrefWidth(80);
        
        TableColumn<Player, Number> drawsColumn = new TableColumn<>("Draws");
        drawsColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getGame_Draw());
        });
        drawsColumn.setPrefWidth(80);
        
        table.getColumns().addAll(nameColumn, scoreColumn, winsColumn, lossesColumn, drawsColumn);
        
        // Load top players
        new Thread(() -> {
            List<Player> topPlayers = UserDatabase.getTopPlayers(5);
            Platform.runLater(() -> {
                table.getItems().clear();
                table.getItems().addAll(topPlayers);
            });
        }).start();
        
        Button backButton = new Button("Back");
        backButton.setPrefWidth(200);
        backButton.setStyle("-fx-background-color: #48CAE4; -fx-text-fill: white;");
        backButton.setOnAction(e -> showGameSelectionPanel());
        
        leaderboardPanel.getChildren().addAll(
            headerLabel,
            table,
            backButton
        );
    }
    
    private void showLoginPanel() {
        mainLayout.setCenter(loginPanel);
    }
    
    private void showRegisterPanel() {
        mainLayout.setCenter(registerPanel);
    }
    
    private void showGameSelectionPanel() {
        if (gameSelectionPanel == null) {
            createGameSelectionPanel();
        }
        mainLayout.setCenter(gameSelectionPanel);
        primaryStage.setTitle("TicTacToe - Game Selection");
    }
    
    private void showLeaderboard() {
        if (leaderboardPanel == null) {
            createLeaderboardPanel();
        } else {
            // Refresh the leaderboard data
            new Thread(() -> {
                List<Player> topPlayers = UserDatabase.getTopPlayers(5);
                Platform.runLater(() -> {
                    TableView<Player> table = (TableView<Player>) leaderboardPanel.getChildren().get(1);
                    table.getItems().clear();
                    table.getItems().addAll(topPlayers);
                });
            }).start();
        }
        mainLayout.setCenter(leaderboardPanel);
        primaryStage.setTitle("TicTacToe - Leaderboard");
    }
    
    private void startOnlineGame() {
        try {
            // Pass the logged in player to the online game
            Stage onlineStage = new Stage();
            OnlineGameView onlineView = new OnlineGameView();
            
            // Set the logged in player
            onlineView.setPlayer(loggedInPlayer);
            
            // Set callback for game over to update player stats
            onlineView.setGameOverCallback(this::updatePlayerStats);
            
            onlineView.start(onlineStage);
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to start online game: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updatePlayerStats(Player.GameResult result) {
        if (loggedInPlayer.getName().equals("Guest")) {
            return; // Don't update stats for guest players
        }
        
        // Update player stats based on game result
        switch (result) {
            case WIN:
                loggedInPlayer.setGame_Win(loggedInPlayer.getGame_Win() + 1);
                loggedInPlayer.setScore(loggedInPlayer.getScore() + 3);
                break;
            case LOSE:
                loggedInPlayer.setGame_Lose(loggedInPlayer.getGame_Lose() + 1);
                break;
            case DRAW:
                loggedInPlayer.setGame_Draw(loggedInPlayer.getGame_Draw() + 1);
                loggedInPlayer.setScore(loggedInPlayer.getScore() + 1);
                break;
        }
        
        // Update the database
        new Thread(() -> {
            boolean updated = UserDatabase.updatePlayerStats(loggedInPlayer);
            if (!updated) {
                Platform.runLater(() -> showErrorAlert("Database Error", "Failed to update player statistics."));
            }
        }).start();
        
        // Refresh the game selection panel to show updated stats
        Platform.runLater(() -> {
            createGameSelectionPanel();
            showGameSelectionPanel();
        });
    }
    
    private void logout() {
        // Reset all user-related data
        loggedInPlayer = null;
        
        // Clear any cached login data
        if (loginPanel != null) {
            // Find the username and password fields and clear them
            for (int i = 0; i < loginPanel.getChildren().size(); i++) {
                if (loginPanel.getChildren().get(i) instanceof TextField) {
                    ((TextField) loginPanel.getChildren().get(i)).clear();
                } else if (loginPanel.getChildren().get(i) instanceof PasswordField) {
                    ((PasswordField) loginPanel.getChildren().get(i)).clear();
                }
            }
        }
        
        // Clear any error labels
        for (int i = 0; i < loginPanel.getChildren().size(); i++) {
            if (loginPanel.getChildren().get(i) instanceof Label) {
                Label label = (Label) loginPanel.getChildren().get(i);
                if (label.getTextFill().equals(Color.RED)) {
                    label.setText("");
                }
            }
        }
        
        // Recreate panels to ensure no cached data
        createLoginPanel();
        createRegisterPanel();
        
        // Show login panel
        showLoginPanel();
        primaryStage.setTitle("TicTacToe - Login");
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}