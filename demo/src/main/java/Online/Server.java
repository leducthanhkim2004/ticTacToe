package Online;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.Map;
import java.util.UUID;
import Factory.GameFactory.GameType;

public class Server {
    private static final int PORT = 1234;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(20);
    private static final BlockingQueue<playerHandler> waitingList = new LinkedBlockingQueue<>();
    private static final Map<String, gameSession> activeSessions = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        new Server().start();
    }
    
    public void start() {
        System.out.println("Server starting on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
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
    
    public synchronized void addToWaitingList(playerHandler player) {
        waitingList.add(player);
        System.out.println("Player added to waiting list. Size: " + waitingList.size());
        pairPlayers();
    }
    
    public synchronized void removeFromWaitingList(playerHandler player) {
        waitingList.remove(player);
        System.out.println("Player removed from waiting list. Size: " + waitingList.size());
    }
    
    private synchronized void pairPlayers() {
        // Need at least 2 players to make a match
        if (waitingList.size() >= 2) {
            playerHandler player1 = waitingList.poll();
            playerHandler player2 = waitingList.poll();
            
            // Create and start a game session
            String sessionId = UUID.randomUUID().toString();
            gameSession session = new gameSession(player1, player2, GameType.STANDARD, this);
            activeSessions.put(sessionId, session);
            
            // Start the game session in a new thread
            threadPool.execute(session);
            
            System.out.println("New game session created: " + sessionId);
        }
    }
    
    public void endGameSession(String sessionId) {
        activeSessions.remove(sessionId);
        System.out.println("Game session ended: " + sessionId);
    }
}
