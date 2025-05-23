package Viewer;

import Database.UserDatabase;
import Model.Player;
import Model.Symbol;
import Factory.GameFactory.GameType;
import comnet.App;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private VBox gameSelectionPanel;
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
        loginPanel = new VBox(15);
        loginPanel.setPadding(new Insets(30));
        loginPanel.setAlignment(Pos.CENTER);
        loginPanel.setMaxWidth(400);
        
        Label headerLabel = new Label("Sign In");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(300);
        
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(300);
        
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(300);
        loginButton.setStyle("-fx-background-color: #0077B6; -fx-text-fill: white;");
        
        Button guestButton = new Button("Play as Guest");
        guestButton.setPrefWidth(300);
        guestButton.setStyle("-fx-background-color: #90E0EF; -fx-text-fill: #03045E;");
        
        Hyperlink registerLink = new Hyperlink("Don't have an account? Sign up");
        
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        
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
        registerPanel = new VBox(15);
        registerPanel.setPadding(new Insets(30));
        registerPanel.setAlignment(Pos.CENTER);
        registerPanel.setMaxWidth(400);
        
        Label headerLabel = new Label("Create Account");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setMaxWidth(300);
        
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        passwordField.setMaxWidth(300);
        
        Label confirmPasswordLabel = new Label("Confirm Password:");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setMaxWidth(300);
        
        Button registerButton = new Button("Create Account");
        registerButton.setPrefWidth(300);
        registerButton.setStyle("-fx-background-color: #0077B6; -fx-text-fill: white;");
        
        Hyperlink loginLink = new Hyperlink("Already have an account? Sign in");
        
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        
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
        gameSelectionPanel = new VBox(20);
        gameSelectionPanel.setPadding(new Insets(30));
        gameSelectionPanel.setAlignment(Pos.CENTER);
        
        String welcomeText = loggedInPlayer.getName().equals("Guest") ? 
            "Playing as Guest" : 
            "Welcome, " + loggedInPlayer.getName();
        
        Label welcomeLabel = new Label(welcomeText);
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        if (!loggedInPlayer.getName().equals("Guest")) {
            // Show player stats
            Label statsLabel = new Label(String.format(
                "Score: %d | Wins: %d | Losses: %d | Draws: %d",
                loggedInPlayer.getScore(),
                loggedInPlayer.getGame_Win(),
                loggedInPlayer.getGame_Lose(),
                loggedInPlayer.getGame_Draw()
            ));
            statsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            gameSelectionPanel.getChildren().addAll(welcomeLabel, statsLabel);
        } else {
            gameSelectionPanel.getChildren().add(welcomeLabel);
        }
        
        Label offlineLabel = new Label("Offline Play");
        offlineLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Button standardButton = createGameButton("Standard Tic Tac Toe (3x3)", "#90E0EF");
        standardButton.setOnAction(e -> startLocalGame(GameType.STANDARD));
        
        Button ultimateButton = createGameButton("Ultimate Tic Tac Toe (9x9)", "#90E0EF");
        ultimateButton.setOnAction(e -> startLocalGame(GameType.ULTIMATE));
        
        Label onlineLabel = new Label("Online Play");
        onlineLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        onlineLabel.setPadding(new Insets(10, 0, 0, 0));
        
        Button onlineButton = createGameButton("Play Online", "#48CAE4");
        onlineButton.setOnAction(e -> startOnlineGame());
        
        Button leaderboardButton = createGameButton("View Leaderboard", "#ADE8F4");
        leaderboardButton.setOnAction(e -> showLeaderboard());
        
        Button logoutButton = new Button("Logout");
        logoutButton.setPrefWidth(300);
        logoutButton.setStyle("-fx-background-color: #DDDDDD;");
        logoutButton.setOnAction(e -> logout());
        
        gameSelectionPanel.getChildren().addAll(
            offlineLabel,
            standardButton,
            ultimateButton,
            onlineLabel,
            onlineButton,
            leaderboardButton,
            logoutButton
        );
    }
    
    private Button createGameButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(300);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #03045E;");
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
    
    private void startLocalGame(GameType gameType) {
        try {
            Stage gameStage = new Stage();
            GameView gameView = new GameView(gameType);
            
            // Set the player information before starting game
            gameView.setPlayer1(loggedInPlayer);
            gameView.setPlayer2(new Player("Player 2", Symbol.O));
            
            // Set callback for game over to update player stats
            gameView.setGameOverCallback(this::updatePlayerStats);
            
            gameView.start(gameStage);
        } catch (Exception e) {
            showErrorAlert("Error", "Failed to start the game: " + e.getMessage());
            e.printStackTrace();
        }
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