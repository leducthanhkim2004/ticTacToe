package Online;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Database.UserDatabase;
import Factory.GameFactory.GameType;

public class Server {
    private static final int PORT = 1234;
    private ServerSocket serverSocket;
    private boolean running = false;
    private ExecutorService clientPool;
    private ExecutorService gamePool;   
    
    // Maps to track waiting players by game type
    private Map<GameType, List<playerHandler>> waitingLists = new HashMap<>();
    
    // Map to track active game sessions
    private Map<String, gameSession> activeSessions = new ConcurrentHashMap<>();
    
    // Add a new map to track active player usernames
    private final Map<String, playerHandler> activePlayerUsernames = new ConcurrentHashMap<>();

    public Server() {
        // Initialize waiting lists for each game type
        for (GameType type : GameType.values()) {
            waitingLists.put(type, new ArrayList<>());
        }
        
        // Initialize thread pools
        clientPool = Executors.newCachedThreadPool();
        gamePool = Executors.newCachedThreadPool();
    }
    
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
    
    public void start() {
        try {
            // Test database connection
            if (!UserDatabase.testConnection()) {
                System.err.println("Warning: Could not connect to database. Player stats will not be saved.");
            }
            
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Server started on port " + PORT);
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New connection from: " + clientSocket.getInetAddress().getHostAddress());
                    
                    // Create and start a new player handler
                    playerHandler handler = new playerHandler(clientSocket, this);
                    clientPool.execute(handler);
                    
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }
    
    public synchronized void addToWaitingList(playerHandler player, GameType gameType) {
        waitingLists.get(gameType).add(player);
        System.out.println("Added " + player.getPlayerName() + " to waiting list for " + gameType);
        
        // Try to pair players
        pairPlayers(gameType);
    }
    
    /**
     * Removes a player from the waiting list
     * @param player Player to remove
     */
    public synchronized void removeFromWaitingList(playerHandler player) {
        String username = player.getPlayerName();
        GameType type = player.getSelectedGameType();
        
        if (type != null) {
            waitingLists.get(type).remove(player);
        }
        
        // Also remove from active players
        if (username != null) {
            removeActivePlayer(username);
        }
        
        System.out.println("Player removed from waiting list: " + username);
    }
    
    private synchronized void pairPlayers(GameType gameType) {
        List<playerHandler> waitingList = waitingLists.get(gameType);
        
        if (waitingList.size() >= 2) {
            // Get the first two players in the list
            playerHandler player1 = waitingList.remove(0);
            playerHandler player2 = waitingList.remove(0);
            
            // Create a new game session
            String sessionId = UUID.randomUUID().toString();
            gameSession session = new gameSession(player1, player2, gameType, this);
            
            // Add to active sessions
            activeSessions.put(sessionId, session);
            
            // Start the game session in a new thread
            gamePool.execute(session);
            
            System.out.println("Created new game session: " + sessionId + " for " + 
                              player1.getPlayerName() + " and " + player2.getPlayerName());
        }
    }
    
    public void endGameSession(String sessionId) {
        gameSession session = activeSessions.remove(sessionId);
        if (session != null) {
            System.out.println("Ended game session: " + sessionId);
        }
    }
    
    public void shutdown() {
        running = false;
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        // Shutdown thread pools
        if (clientPool != null) {
            clientPool.shutdownNow();
        }
        
        if (gamePool != null) {
            gamePool.shutdownNow();
        }
        
        System.out.println("Server shutdown complete");
    }
    
    /**
     * Check if a player with the given username is already active
     * @param username Username to check
     * @return true if player is already active, false otherwise
     */
    public synchronized boolean isPlayerActive(String username) {
        return activePlayerUsernames.containsKey(username) && 
               !username.startsWith("Guest"); // Allow multiple guests with different IDs
    }

    /**
     * Register a player as active
     * @param username Player's username
     * @param handler Player's connection handler
     */
    public synchronized void registerPlayer(String username, playerHandler handler) {
        activePlayerUsernames.put(username, handler);
    }

    /**
     * Remove a player from active players
     * @param username Player's username
     */
    public synchronized void removeActivePlayer(String username) {
        activePlayerUsernames.remove(username);
    }
}
