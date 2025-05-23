package Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Model.Player;
import Model.Symbol;

public class UserDatabase {
    private static final String URL = "jdbc:mysql://localhost:3306/tictactoe";
    private static final String USER = "root";
    private static final String PASSSWORD = "1234";
    
    /**
     * Get a database connection 
     */ 
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSSWORD);
    }
    
    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Failed connection: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Register a new player
     * @param username Player's username
     * @param password Player's password
     * @return true if registration successful, false otherwise
     */
    public static boolean registerPlayer(String username, String password) {
        if (playerExists(username)) {
            return false;
        }
        
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO players (User_Name, User_Pass) VALUES (?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error registering player: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if a player exists
     * @param username Player's username
     * @return true if player exists, false otherwise
     */
    public static boolean playerExists(String username) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT COUNT(*) FROM players WHERE User_Name = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking player existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Login a player
     * @param username Player's username
     * @param password Player's password
     * @return Player object if login successful, null otherwise
     */
    public static Player loginPlayer(String username, String password) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM players WHERE User_Name = ? AND User_Pass = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // Create Player object from database data
                        Player player = new Player(username, null);  // Symbol will be assigned later
                        player.setPassword(password);
                        player.setScore(rs.getInt("Score"));
                        player.setGame_Win(rs.getInt("GameWon"));
                        player.setGame_Draw(rs.getInt("GameDraw"));
                        player.setGame_Lose(rs.getInt("GameLose"));
                        return player;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get a player by username
     * @param username Player's username
     * @return Player object if found, null otherwise
     */
    public static Player getPlayer(String username) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM players WHERE User_Name = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Player player = new Player(username, null);
                        player.setPassword(rs.getString("User_Pass"));
                        player.setScore(rs.getInt("Score"));
                        player.setGame_Win(rs.getInt("GameWon"));
                        player.setGame_Draw(rs.getInt("GameDraw"));
                        player.setGame_Lose(rs.getInt("GameLose"));
                        return player;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting player: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Update player statistics
     * @param player Player object with updated stats
     * @return true if update successful, false otherwise
     */
    public static boolean updatePlayerStats(Player player) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE players SET Score = ?, GameWon = ?, GameLose = ?, GameDraw = ? WHERE User_Name = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, player.getScore());
                pstmt.setInt(2, player.getGame_Win());
                pstmt.setInt(3, player.getGame_Lose());
                pstmt.setInt(4, player.getGame_Draw());
                pstmt.setString(5, player.getName());
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating player stats: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update player password
     * @param username Player's username
     * @param newPassword New password
     * @return true if update successful, false otherwise
     */
    public static boolean updatePassword(String username, String newPassword) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE players SET User_Pass = ? WHERE User_Name = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newPassword);
                pstmt.setString(2, username);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Record game result (DEPRECATED - use recordWinLoss or recordDraw instead)
     * @param winner Winner username (or null for draw)
     * @param loser Loser username (or null for draw)
     * @return true if successful, false otherwise
     */
    public static boolean recordGameResult(String winner, String loser) {
        System.out.println("WARNING: Using deprecated recordGameResult method. Use recordWinLoss or recordDraw instead.");
        
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                if (winner != null && loser != null && !winner.equals(loser)) {
                    // Win/loss scenario
                    return recordWinLoss(winner, loser);
                } else if (winner == null && loser == null) {
                    // No players provided
                    System.err.println("Error: No players provided to record game result");
                    return false;
                } else {
                    // Draw scenario or only one player provided
                    if (winner != null && loser != null) {
                        return recordDraw(winner, loser);
                    } else if (winner != null) {
                        updateDraw(conn, winner);
                        conn.commit();
                        return true;
                    } else if (loser != null) {
                        updateDraw(conn, loser);
                        conn.commit();
                        return true;
                    }
                }
                
                return false;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.err.println("Error recording game result: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Record a win for one player and a loss for another player
     * @param winner Winner username
     * @param loser Loser username
     * @return true if successful, false otherwise
     */
    public static boolean recordWinLoss(String winner, String loser) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Update winner stats
                updateWin(conn, winner);
                // Update loser stats
                updateLoss(conn, loser);
                
                System.out.println("Direct DB update: Win for " + winner + " and loss for " + loser);
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error recording win/loss: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error getting connection: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Record a draw for both players
     * @param player1 First player username
     * @param player2 Second player username
     * @return true if successful, false otherwise
     */
    public static boolean recordDraw(String player1, String player2) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Update draw for player1
                updateDraw(conn, player1);
                // Update draw for player2
                updateDraw(conn, player2);
                
                System.out.println("Direct DB update: Draw for " + player1 + " and " + player2);
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error recording draw: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error getting connection: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update win count for a player
     */
    private static void updateWin(Connection conn, String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new SQLException("Cannot update win for null or empty username");
        }
        
        // First check if player exists
        String checkSql = "SELECT COUNT(*) FROM players WHERE User_Name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new SQLException("Player does not exist: " + username);
                }
            }
        }
        
        String sql = "UPDATE players SET GameWon = GameWon + 1, Score = Score + 3 WHERE User_Name = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int updated = pstmt.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Failed to update win for player: " + username);
            }
            System.out.println("Updated win for " + username);
        }
    }
    
    /**
     * Update loss count for a player
     */
    private static void updateLoss(Connection conn, String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new SQLException("Cannot update loss for null or empty username");
        }
        
        // First check if player exists
        String checkSql = "SELECT COUNT(*) FROM players WHERE User_Name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new SQLException("Player does not exist: " + username);
                }
            }
        }
        
        String sql = "UPDATE players SET GameLose = GameLose + 1 WHERE User_Name = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int updated = pstmt.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Failed to update loss for player: " + username);
            }
            System.out.println("Updated loss for " + username);
        }
    }
    
    /**
     * Update draw count for a player
     */
    private static void updateDraw(Connection conn, String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new SQLException("Cannot update draw for null or empty username");
        }
        
        // First check if player exists
        String checkSql = "SELECT COUNT(*) FROM players WHERE User_Name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new SQLException("Player does not exist: " + username);
                }
            }
        }
        
        String sql = "UPDATE players SET GameDraw = GameDraw + 1, Score = Score + 1 WHERE User_Name = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int updated = pstmt.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Failed to update draw for player: " + username);
            }
            System.out.println("Updated draw for " + username);
        }
    }
    
    /**
     * Get top players by score
     * @param limit Maximum number of players to return
     * @return List of Player objects
     */
    public static List<Player> getTopPlayers(int limit) {
        List<Player> topPlayers = new ArrayList<>();
        
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM players ORDER BY Score DESC LIMIT ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String username = rs.getString("User_Name");
                        Player player = new Player(username, null);
                        player.setScore(rs.getInt("Score"));
                        player.setGame_Win(rs.getInt("GameWon"));
                        player.setGame_Draw(rs.getInt("GameDraw"));
                        player.setGame_Lose(rs.getInt("GameLose"));
                        topPlayers.add(player);
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting top players: " + e.getMessage());
        }
        
        return topPlayers;
    }
    
    /**
     * Delete a player
     * @param username Player's username
     * @return true if deletion successful, false otherwise
     */
    public static boolean deletePlayer(String username) {
        try (Connection conn = getConnection()) {
            String sql = "DELETE FROM players WHERE User_Name = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error deleting player: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get a player's ID
     * @param username Player's username
     * @return Player ID or -1 if not found
     */
    public static int getPlayerId(String username) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT User_ID FROM players WHERE User_Name = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("User_ID");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting player ID: " + e.getMessage());
        }
        
        return -1;
    }
    
    /**
     * Get player by ID
     * @param id Player ID
     * @return Player object if found, null otherwise
     */
    public static Player getPlayerById(int id) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT * FROM players WHERE User_ID = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String username = rs.getString("User_Name");
                        Player player = new Player(username, null);
                        player.setPassword(rs.getString("User_Pass"));
                        player.setScore(rs.getInt("Score"));
                        player.setGame_Win(rs.getInt("GameWon"));
                        player.setGame_Draw(rs.getInt("GameDraw"));
                        player.setGame_Lose(rs.getInt("GameLose"));
                        return player;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting player by ID: " + e.getMessage());
        }
        
        return null;
    }
}
