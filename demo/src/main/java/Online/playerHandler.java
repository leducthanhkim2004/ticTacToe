package Online;

import java.io.*;
import java.net.Socket;
import Model.*;

public class playerHandler implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Player player;
    private gameSession currentSession;
    private String playerName;
    private boolean connected = true;
    private Server server;
    
    public playerHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
    }
    
    @Override
    public void run() {
        try {
            // First message should be player registration with name
            String registerMessage = in.readUTF();
            handleRegistration(registerMessage);
            
            // Main message loop
            while (connected) {
                String message = in.readUTF();
                processMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Connection lost with player: " + playerName);
        } finally {
            handleDisconnect();
        }
    }
    
    private void handleRegistration(String message) throws IOException {
        // Expected format: REGISTER:playerName
        String[] parts = message.split(":");
        if (parts[0].equals("REGISTER") && parts.length > 1) {
            this.playerName = parts[1];
            this.player = new Player(playerName, null); // Symbol will be assigned later
            sendMessage("CONNECTED:" + playerName);
            
            // Add player to waiting queue
            server.addToWaitingList(this);
        } else {
            sendMessage("ERROR:Invalid registration");
            socket.close();
            connected = false;
        }
    }
    
    private void processMessage(String message) {
        try {
            String[] parts = message.split(":");
            String command = parts[0];
            
            switch (command) {
                case "MOVE":
                    if (currentSession != null && parts.length >= 3) {
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        currentSession.handleMove(this, row, col);
                    }
                    break;
                case "CHAT":
                    if (currentSession != null && parts.length >= 2) {
                        currentSession.handleChat(this, parts[1]);
                    }
                    break;
                case "QUIT":
                    handleDisconnect();
                    break;
                default:
                    sendMessage("ERROR:Unknown command");
            }
        } catch (Exception e) {
            try {
                sendMessage("ERROR:Invalid message format");
            } catch (IOException ex) {
                connected = false;
            }
        }
    }
    
    public void sendMessage(String message) throws IOException {
        if (out != null && connected) {
            out.writeUTF(message);
            out.flush();
        }
    }
    
    private void handleDisconnect() {
        connected = false;
        try {
            if (currentSession != null) {
                currentSession.handleDisconnect(this);
            } else {
                server.removeFromWaitingList(this);
            }
            
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }
    
    public void setGameSession(gameSession session) {
        this.currentSession = session;
    }
    
    public void setSymbol(Symbol symbol) {
        this.player.setSymbol(symbol);
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getPlayerName() {
        return playerName;
    }
}
