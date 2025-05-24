package Online;

import java.io.*;
import java.net.Socket;
import Model.Player;
import Model.Symbol;
import Factory.GameFactory.GameType;

public class playerHandler implements Runnable {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean connected = true;
    private String playerName;
    private Player player;
    private GameType selectedGameType;
    private gameSession currentGame;

    public playerHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Error creating player handler: " + e.getMessage());
            connected = false;
        }
    }

    @Override
    public void run() {
        while (connected) {
            try {
                String message = in.readUTF();
                processMessage(message);
            } catch (IOException e) {
                System.err.println("Error reading from client: " + e.getMessage());
                connected = false;
            }
        }
        
        // Clean up when disconnected
        if (currentGame != null) {
            currentGame.handlePlayerDisconnect(this);
        } else {
            server.removeFromWaitingList(this);
        }
        
        closeConnection();
    }
    
    public void processMessage(String message) {
        if (message.startsWith("REGISTER:")) {
            String username = message.substring("REGISTER:".length());
            
            // Check if this username is already logged in
            if (server.isPlayerActive(username)) {
                // Send an error message that this account is already logged in
                sendMessage("LOGIN_ERROR:ALREADY_LOGGED_IN");
                return; // Don't proceed with registration
            }
            
            // Register player with server
            playerName = username;
            
            // Get player information from database if they're not a guest
            if (!username.startsWith("Guest")) {
                player = Database.UserDatabase.getPlayer(username);
            } else {
                // Create a temporary player object for guest
                player = new Player(username, Symbol.X);
            }
            
            // Register with server's active players list
            server.registerPlayer(username, this);
            
            // Confirm registration
            sendMessage("CONNECTED:" + username);
        } else if (message.startsWith("SELECT_GAME:")) {
            String gameTypeStr = message.substring("SELECT_GAME:".length());
            GameType gameType = GameType.valueOf(gameTypeStr);
            selectedGameType = gameType;
            
            // Add to waiting list for this game type
            server.addToWaitingList(this, gameType);
            
            // Confirm selection
            sendMessage("GAME_SELECTED:" + gameTypeStr);
        } else if (message.startsWith("MOVE:")) {
            // Handle move in current game
            if (currentGame != null) {
                currentGame.handleMove(this, message.substring("MOVE:".length()));
            }
        } else if (message.startsWith("CHAT:")) {
            // Handle chat message
            handleChat(message.substring("CHAT:".length()));
        } else if (message.equals("QUIT")) {
            // Handle player quitting
            connected = false;
        } else if (message.startsWith("REQUEST_STATS")) {
            // Handle request for updated player stats
            sendUpdatedPlayerStats();
        }
    }
    
    public void setCurrentGame(gameSession game) {
        this.currentGame = game;
    }
    
    public void endGame() {
        this.currentGame = null;
    }

    public void sendMessage(String message) {
        try {
            if (connected && out != null) {
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + e.getMessage());
            connected = false;
        }
    }
    
    private void handleChat(String message) {
        if (currentGame != null) {
            currentGame.handleChat(this, message);
        }
    }
    
    private void closeConnection() {
        try {
            // Remove from active players first
            if (playerName != null) {
                server.removeActivePlayer(playerName);
            }
            
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public GameType getSelectedGameType() {
        return selectedGameType;
    }

    private void sendUpdatedPlayerStats() {
        if (player != null && playerName != null && !playerName.startsWith("Guest")) {
            try {
                // Get fresh player data from database
                Player updatedPlayer = Database.UserDatabase.getPlayer(playerName);
                
                if (updatedPlayer != null) {
                    // Send player stats back to client
                    sendMessage("PLAYER_STATS:" + 
                               updatedPlayer.getGame_Win() + ":" + 
                               updatedPlayer.getGame_Lose() + ":" +
                               updatedPlayer.getGame_Draw() + ":" +
                               updatedPlayer.getScore());
                }
            } catch (Exception e) {
                System.err.println("Error sending updated player stats: " + e.getMessage());
            }
        }
    }
}
