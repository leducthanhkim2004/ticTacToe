package Online;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import Factory.GameFactory.GameType;

public class Server {
    private static final int PORT = 1234;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(20);
    
    // Separate queues for different game types
    private final Map<GameType, Queue<playerHandler>> waitingLists = new ConcurrentHashMap<>();
    private final Map<String, gameSession> activeSessions = new ConcurrentHashMap<>();
    
    public Server() {
        // Initialize queues for each game type
        waitingLists.put(GameType.STANDARD, new ConcurrentLinkedQueue<>());
        waitingLists.put(GameType.ULTIMATE, new ConcurrentLinkedQueue<>());
    }
    
    public static void main(String[] args) {
        new Server().start();
    }
    
    public void start() {
        System.out.println("Server starting on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started successfully!");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                // Create and start a player handler thread
                playerHandler player = new playerHandler(clientSocket, this);
                threadPool.execute(player);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
    
    public synchronized void addToWaitingList(playerHandler player, GameType gameType) {
        Queue<playerHandler> waitingList = waitingLists.get(gameType);
        waitingList.add(player);
        System.out.println("Player added to " + gameType + " waiting list. Size: " + waitingList.size());
        pairPlayers(gameType);
    }
    
    public synchronized void removeFromWaitingList(playerHandler player) {
        for (Queue<playerHandler> queue : waitingLists.values()) {
            queue.remove(player);
        }
    }
    
    private synchronized void pairPlayers(GameType gameType) {
        Queue<playerHandler> waitingList = waitingLists.get(gameType);
        
        // Need at least 2 players to make a match
        if (waitingList.size() >= 2) {
            playerHandler player1 = waitingList.poll();
            playerHandler player2 = waitingList.poll();
            
            // Create and start a game session
            String sessionId = UUID.randomUUID().toString();
            gameSession session = new gameSession(player1, player2, gameType, this);
            activeSessions.put(sessionId, session);
            
            // Start the game session in a new thread
            threadPool.execute(session);
            
            System.out.println("New " + gameType + " game session created: " + sessionId);
        }
    }
    
    public void endGameSession(String sessionId) {
        activeSessions.remove(sessionId);
        System.out.println("Game session ended: " + sessionId);
    }
}
