package Online;

import java.io.*;
import java.net.Socket;
import Model.Player;
import Factory.GameFactory.GameType;

public class playerHandler implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;
    private String playerName;
    private Player player;
    private gameSession currentGame;
    private boolean inGame = false;
    private GameType selectedGameType;
    
    public playerHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        
        // Initialize default player name (will be updated later)
        this.playerName = "Player-" + socket.getInetAddress().getHostAddress();
        
        try {
            // Initialize streams
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Error initializing streams: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                // Read messages from client
                String message = in.readUTF();
                processMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + playerName);
            // Clean up
            if (inGame && currentGame != null) {
                currentGame.handlePlayerDisconnect(this);
            } else {
                server.removeFromWaitingList(this);
            }
            closeConnection();
        }
    }
    
    private void processMessage(String message) {
        System.out.println("Message from " + playerName + ": " + message);
        
        if (message.startsWith("REGISTER:")) {
            // Handle player registration
            String name = message.substring(9);
            if (name != null && !name.trim().isEmpty()) {
                this.playerName = name;
                
                // Create a Player object now that we have the name
                this.player = new Player(name, null); // Symbol will be assigned later
                
                sendMessage("REGISTERED:" + playerName);
            }
        } else if (message.startsWith("SELECT_GAME:")) {
            // Handle game type selection
            String gameTypeStr = message.substring(12);
            try {
                GameType gameType = GameType.valueOf(gameTypeStr);
                this.selectedGameType = gameType;
                
                // Add player to waiting list for selected game type
                server.addToWaitingList(this, gameType);
                sendMessage("WAITING:" + gameType);
            } catch (IllegalArgumentException e) {
                sendMessage("ERROR:Invalid game type");
            }
        } else if (message.startsWith("MOVE:")) {
            // Handle move during game
            if (inGame && currentGame != null) {
                currentGame.handleMove(this, message.substring(5));
            } else {
                sendMessage("ERROR:Not in game");
            }
        } else if (message.startsWith("CHAT:")) {
            // Handle chat message
            if (inGame && currentGame != null) {
                currentGame.handleChat(this, message.substring(5));
            }
        } else if (message.equals("QUIT")) {
            // Handle player quitting
            if (inGame && currentGame != null) {
                currentGame.handlePlayerDisconnect(this);
            } else {
                server.removeFromWaitingList(this);
            }
            closeConnection();
        }
    }
    
    public void setCurrentGame(gameSession game) {
        this.currentGame = game;
        this.inGame = true;
    }
    
    public void endGame() {
        this.currentGame = null;
        this.inGame = false;
    }
    
    public void sendMessage(String message) {
        try {
            if (out != null && socket != null && !socket.isClosed()) {
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message to " + playerName + ": " + e.getMessage());
            closeConnection();
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
    
    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
