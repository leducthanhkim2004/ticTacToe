package comnet;


import Viewer.LoginView;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.DialogPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class App extends Application {
    private Stage primaryStage;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Set stage properties
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setTitle("TicTacToe Game");
        
        // Launch the login/signup view
        try {
            LoginView loginView = new LoginView();
            loginView.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
            showMainMenu();
        }
    }
    
    private void showMainMenu() {
        primaryStage.setTitle("TicTacToe Game");

        // Create a simple background
        BackgroundFill backgroundFill = new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);

        BorderPane root = new BorderPane();
        root.setBackground(background);
        
        // Add top panel with title
        VBox topPanel = new VBox(15);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(20, 10, 10, 10));
        
        Label titleLabel = new Label("Tic Tac Toe");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.DARKBLUE);
        
        Label subtitleLabel = new Label("Login to play online");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        
        topPanel.getChildren().addAll(titleLabel, subtitleLabel);
        
        // Main button panel
        VBox centerPanel = new VBox(15);
        centerPanel.setAlignment(Pos.CENTER);
        centerPanel.setPadding(new Insets(20));
        
        // Login button
        Button loginButton = createButton("Login / Register", Color.DARKBLUE, Color.WHITE);
        loginButton.setOnAction(e -> {
            try {
                LoginView loginView = new LoginView();
                loginView.start(primaryStage);
            } catch (Exception ex) {
                showError("Could not open login view: " + ex.getMessage());
            }
        });
        
        // Exit button
        Button exitButton = createButton("Exit Game", Color.RED, Color.WHITE);
        exitButton.setOnAction(e -> primaryStage.close());
        
        centerPanel.getChildren().addAll(
            loginButton,
            exitButton
        );
        
        // Bottom panel with version info
        HBox bottomPanel = new HBox();
        bottomPanel.setAlignment(Pos.CENTER_RIGHT);
        bottomPanel.setPadding(new Insets(10));
        
        Label versionLabel = new Label("Version 1.0");
        versionLabel.setFont(Font.font("Arial", 12));
        versionLabel.setTextFill(Color.DARKBLUE);
        
        bottomPanel.getChildren().add(versionLabel);
        
        // Add all panels to the root
        root.setTop(topPanel);
        root.setCenter(centerPanel);
        root.setBottom(bottomPanel);
        
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private Button createButton(String text, Color bgColor, Color textColor) {
        Button button = new Button(text);
        button.setPrefWidth(300);
        button.setPrefHeight(50);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Set colors directly
        button.setStyle(
            "-fx-background-color: " + toRgbCode(bgColor) + ";" +
            "-fx-text-fill: " + toRgbCode(textColor) + ";" +
            "-fx-padding: 10px;"
        );
        
        // Simple shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.gray(0.5));
        shadow.setRadius(5);
        shadow.setOffsetY(3);
        button.setEffect(shadow);
        
        return button;
    }
    
    private String toRgbCode(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Simple alert styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-font-size: 14px;");
        
        alert.showAndWait();
    }
}
