package comnet;

import Viewer.GameView;
import Viewer.OnlineGameView;
import Viewer.LoginView;
import Factory.GameFactory.GameType;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class App extends Application {
    private Stage primaryStage;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Set a larger default stage size
        primaryStage.setWidth(600);
        primaryStage.setHeight(700);
        
        // Launch the login/signup view
        try {
            // Create a new instance of LoginView every time
            LoginView loginView = new LoginView();
            loginView.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
            
            // If login view fails to start, show the original main menu as fallback
            showMainMenu();
        }
    }
    
    private void showMainMenu() {
        primaryStage.setTitle("Tic Tac Toe");

        // Create gradient background
        Stop[] stops = new Stop[] {
            new Stop(0, Color.web("#ffecd2")),
            new Stop(1, Color.web("#fcb69f"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        BackgroundFill backgroundFill = new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setBackground(background);

        // Title
        Label titleLabel = new Label("Tic Tac Toe");
        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #333333;");

        // Add login button
        Button loginButton = createStyledButton("Login / Register");
        loginButton.setOnAction(e -> {
            try {
                LoginView loginView = new LoginView();
                loginView.start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                showError("Could not open login view: " + ex.getMessage());
            }
        });

        // Existing buttons
        Button localStandardButton = createStyledButton("Local Play - Standard (3x3)");
        localStandardButton.setOnAction(e -> startLocalGame(GameType.STANDARD));

        Button localUltimateButton = createStyledButton("Local Play - Ultimate (9x9)");
        localUltimateButton.setOnAction(e -> startLocalGame(GameType.ULTIMATE));

        Button onlineButton = createStyledButton("Online Play");
        onlineButton.setOnAction(e -> {
            // Inform user they need to login for online play
            showError("Please login first to access online play features.");
        });

        Button exitButton = createStyledButton("Exit");
        exitButton.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(
            titleLabel,
            loginButton,
            localStandardButton,
            localUltimateButton,
            onlineButton,
            exitButton
        );

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(250);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle("-fx-background-color: #90e0ef; -fx-text-fill: #03045e; -fx-background-radius: 10;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #48cae4; -fx-text-fill: #03045e; -fx-background-radius: 10;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #90e0ef; -fx-text-fill: #03045e; -fx-background-radius: 10;"));
        return button;
    }

    private void startLocalGame(GameType gameType) {
        Stage gameStage = new Stage();
        GameView gameView = new GameView(gameType);
        gameView.start(gameStage);
    }

    private void startOnlineGame() {
        try {
            Stage onlineStage = new Stage();
            OnlineGameView onlineView = new OnlineGameView();
            onlineView.start(onlineStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
