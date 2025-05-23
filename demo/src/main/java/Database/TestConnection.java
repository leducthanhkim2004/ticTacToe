/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 * Test class to validate database connectivity
 * @author leduc
 */
public class TestConnection {
    // Define your database URL, username, and password
    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String DATABASE = "TicTacToe";
    private static final String FULL_URL = URL + DATABASE + "?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    /**
     * Establish and return a database connection
     * @return Connection object or null if connection failed
     */
    public static Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(FULL_URL, USER, PASSWORD);
            System.out.println("✓ Connected to MySQL server successfully!");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
        return connection;
    }
    
    /**
     * Run a comprehensive database connection test
     * @return true if all tests pass, false otherwise
     */
    public static boolean runConnectionTest() {
        boolean success = false;
        Connection conn = null;
        Statement stmt = null;
        
        try {
            System.out.println("\n==== MYSQL CONNECTION TEST ====\n");
            
            // Step 1: Connect to MySQL server (without database)
            System.out.println("Step 1: Connecting to MySQL server...");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✓ Connected to MySQL server!");
            
            // Step 2: Get database metadata to verify connection
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("\n----- Server Information -----");
            System.out.println("Database: " + metaData.getDatabaseProductName());
            System.out.println("Version: " + metaData.getDatabaseProductVersion());
            System.out.println("JDBC Driver: " + metaData.getDriverName() + " " + metaData.getDriverVersion());
            
            // Step 3: Create database if it doesn't exist
            System.out.println("\nStep 2: Creating/verifying database...");
            stmt = conn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE);
            System.out.println("✓ Database '" + DATABASE + "' is ready");
            
            // Close and reopen connection to specific database
            conn.close();
            conn = DriverManager.getConnection(FULL_URL, USER, PASSWORD);
            System.out.println("✓ Connected to '" + DATABASE + "' database");
            
            // Step 4: Create a test table
            System.out.println("\nStep 3: Testing database write access...");
            stmt = conn.createStatement();
            String createTableSQL = 
                "CREATE TABLE IF NOT EXISTS connection_test (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "message VARCHAR(100), " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            stmt.executeUpdate(createTableSQL);
            System.out.println("✓ Test table created/verified");
            
            // Step 5: Insert test data
            String testMessage = "Connection test at " + new Date();
            String insertSQL = "INSERT INTO connection_test (message) VALUES ('" + testMessage + "')";
            int rowsAffected = stmt.executeUpdate(insertSQL);
            System.out.println("✓ Inserted " + rowsAffected + " test record");
            
            // Step 6: Read test data back
            System.out.println("\nStep 4: Testing database read access...");
            ResultSet rs = stmt.executeQuery("SELECT * FROM connection_test ORDER BY id DESC LIMIT 1");
            
            if (rs.next()) {
                int id = rs.getInt("id");
                String message = rs.getString("message");
                String timestamp = rs.getTimestamp("timestamp").toString();
                
                System.out.println("----- Retrieved Test Record -----");
                System.out.println("ID: " + id);
                System.out.println("Message: " + message);
                System.out.println("Timestamp: " + timestamp);
                
                // Verify the message matches what we inserted
                if (message.equals(testMessage)) {
                    System.out.println("✓ Data integrity verified");
                } else {
                    System.out.println("❌ Data integrity check failed");
                    return false;
                }
            } else {
                System.out.println("❌ Failed to retrieve test record");
                return false;
            }
            
            // If we get here, all tests passed
            System.out.println("\n==== CONNECTION TEST SUMMARY ====");
            System.out.println("✓ MySQL server connection: SUCCESS");
            System.out.println("✓ Database creation/access: SUCCESS");
            System.out.println("✓ Table creation: SUCCESS");
            System.out.println("✓ Data write (INSERT): SUCCESS");
            System.out.println("✓ Data read (SELECT): SUCCESS");
            System.out.println("\n✅ ALL TESTS PASSED - Your database connection is working correctly!");
            
            success = true;
            
        } catch (SQLException e) {
            System.err.println("\n❌ CONNECTION TEST FAILED");
            System.err.println("Error: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        } finally {
            // Clean up resources
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
                System.out.println("\nDatabase resources closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return success;
    }
    
    /**
     * Test database connection with diagnostic information
     */
    public static void main(String[] args) {
        System.out.println("Starting database connection test...");
        
        boolean testResult = runConnectionTest();
        
        if (testResult) {
            System.out.println("\nYour application is ready to use the database!");
        } else {
            System.err.println("\nDatabase connection test failed. Please fix the issues before proceeding.");
            
            // Print troubleshooting information
            System.out.println("\n==== TROUBLESHOOTING TIPS ====");
            System.out.println("1. Verify MySQL server is running");
            System.out.println("2. Check username and password");
            System.out.println("3. Make sure MySQL port (3306) is not blocked by firewall");
            System.out.println("4. Check MySQL connector is properly added to your project");
            System.out.println("5. Ensure MySQL server allows remote connections (if not on localhost)");
        }
    }
}
