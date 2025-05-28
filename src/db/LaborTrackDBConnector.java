package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LaborTrackDBConnector {
    public static Connection connect() {
        Connection connect = null;
        try {
            Class.forName("org.sqlite.JDBC");
            String databasePath = "jdbc:sqlite:database/labortrack.db";
            connect = DriverManager.getConnection(databasePath);
            System.out.println("Connected to Database!");
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error Connecting to the Database: " + e.getMessage());
        }
        return connect;
    }
}