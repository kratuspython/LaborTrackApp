import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class LaborTrackDBConnector {
    static Connection connect = null;
    public static Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC"); //connect to my DB browser SQLite
    
            String databasePath = "jdbc:sqlite:src/Database/labortrack.db"; //path to my database
    
            connect = DriverManager.getConnection(databasePath);
    
            System.out.println("Connected to Database!");
        }catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
        catch (SQLException e) {
            System.out.println("Error Connecting to the Database: " + e.getMessage());
        }
        return connect;
    }
}
