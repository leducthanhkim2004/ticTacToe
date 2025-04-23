package comnet;

import Viewer.GameView;
import Viewer.OnlineGameView;
import Factory.GameFactory.GameType;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tic Tac Toe");
        
        // Create main menu
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        
        Label titleLabel = new Label("Tic Tac Toe");
        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 24));
        
        Button localStandardButton = new Button("Local Play - Standard (3x3)");
        localStandardButton.setPrefWidth(200);
        localStandardButton.setOnAction(e -> startLocalGame(GameType.STANDARD));
        
        Button localUltimateButton = new Button("Local Play - Ultimate (9x9)");
        localUltimateButton.setPrefWidth(200);
        localUltimateButton.setOnAction(e -> startLocalGame(GameType.ULTIMATE));
        
        Button onlineButton = new Button("Online Play");
        onlineButton.setPrefWidth(200);
        onlineButton.setOnAction(e -> startOnlineGame());
        
        Button exitButton = new Button("Exit");
        exitButton.setPrefWidth(200);
        exitButton.setOnAction(e -> primaryStage.close());
        
        root.getChildren().addAll(
            titleLabel, 
            localStandardButton, 
            localUltimateButton, 
            onlineButton, 
            exitButton
        );
        
        Scene scene = new Scene(root, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
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
}
