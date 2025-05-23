package Database;

public class SimpleTest {
    public static void main(String[] args) {
        System.out.println("Simple test program is running!");
        
        try {
            // Try to load the MySQL driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL Driver loaded successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found!");
            System.err.println("Error: " + e.getMessage());
        }
        
        System.out.println("Simple test completed.");
    }
}