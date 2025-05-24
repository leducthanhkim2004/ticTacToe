package Viewer;

import Model.Player;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import Database.UserDatabase;

public class LeaderboardView extends Application {
    
    private TableView<Player> leaderboardTable;
    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("TicTacToe - Leaderboard");
        
        // Create the root layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F5F5;"); // Light gray background
        
        // Create header
        VBox headerBox = createHeader();
        root.setTop(headerBox);
        
        // Create content (leaderboard table)
        VBox contentBox = createContent();
        root.setCenter(contentBox);
        
        // Create footer with back button
        HBox footerBox = createFooter();
        root.setBottom(footerBox);
        
        // Create scene and show
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Load leaderboard data
        loadLeaderboardData();
    }
    
    private VBox createHeader() {
        VBox headerBox = new VBox();
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(20, 0, 15, 0));
        headerBox.setStyle("-fx-background-color: #66CCCC;"); // Turquoise blue
        
        Label titleLabel = new Label("Tic Tac Toe");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        titleLabel.setTextFill(Color.web("#333333"));
        
        headerBox.getChildren().add(titleLabel);
        return headerBox;
    }
    
    private VBox createContent() {
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(25));
        contentBox.setAlignment(Pos.CENTER);
        
        // Create title for leaderboard
        Label leaderboardLabel = new Label("Top 5 Players");
        leaderboardLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        leaderboardLabel.setTextFill(Color.web("#333333"));
        
        // Create the table
        leaderboardTable = new TableView<>();
        leaderboardTable.setPrefHeight(300); // Fixed height
        
        // Define columns
        TableColumn<Player, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setPrefWidth(120);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Player, Integer> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setPrefWidth(80);
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        
        TableColumn<Player, Integer> winsColumn = new TableColumn<>("Wins");
        winsColumn.setPrefWidth(80);
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("game_Win"));
        
        TableColumn<Player, Integer> lossesColumn = new TableColumn<>("Losses");
        lossesColumn.setPrefWidth(80);
        lossesColumn.setCellValueFactory(new PropertyValueFactory<>("game_Lose"));
        
        TableColumn<Player, Integer> drawsColumn = new TableColumn<>("Draws");
        drawsColumn.setPrefWidth(80);
        drawsColumn.setCellValueFactory(new PropertyValueFactory<>("game_Draw"));
        
        // Add columns to table
        leaderboardTable.getColumns().addAll(nameColumn, scoreColumn, winsColumn, lossesColumn, drawsColumn);
        
        contentBox.getChildren().addAll(leaderboardLabel, leaderboardTable);
        return contentBox;
    }
    
    private HBox createFooter() {
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(15, 0, 25, 0));
        
        // Create back button
        Button backButton = new Button("Back");
        backButton.setPrefWidth(150);
        backButton.setPrefHeight(40);
        backButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        backButton.setStyle("-fx-background-color: #66CCCC; -fx-text-fill: white;");
        
        backButton.setOnAction(e -> {
            // Navigate back to the main menu or previous screen
            try {
                new LoginView().start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        footerBox.getChildren().add(backButton);
        return footerBox;
    }
    
    private void loadLeaderboardData() {
        // Load top 5 players from database
        ObservableList<Player> players = FXCollections.observableArrayList();
        
        try {
            players.addAll(UserDatabase.getTopPlayers(5));
        } catch (Exception e) {
            System.err.println("Error loading leaderboard data: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Set table items
        leaderboardTable.setItems(players);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}