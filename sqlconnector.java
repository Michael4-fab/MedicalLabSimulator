import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class sqlconnector {

    private static final String dbUrl = "jdbc:mysql://localhost:3306/MedicalLabSimulator";
    private static final String username = "root";
    private static final String password = "5550555@Cc";

    public static Connection connect() {
        try {
            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection
            Connection conn = DriverManager.getConnection(dbUrl, username, password);
            System.out.println("✅ Connected to MySQL!");
            return conn; // ✅ Return the connection, not null

        } catch (SQLException e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            System.out.println("❌ JDBC driver not found: " + e.getMessage());
            return null;
        }
    }
}
