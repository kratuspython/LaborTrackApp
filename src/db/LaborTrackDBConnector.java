package db;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class LaborTrackDBConnector {
    /** 
     * Always returns a brand-new Connection pointed at database/labortrack.db,
     * with WAL mode *and* a 5s busy_timeout so writes wait instead of failing.
     */
    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite driver not found", e);
        }

        // build full path to database/labortrack.db
        String root   = Paths.get("").toAbsolutePath().toString();
        String dbPath = Paths.get(root, "database", "labortrack.db").toString();
        String url    = "jdbc:sqlite:" + dbPath + "?busy_timeout=5000";

        Connection c = DriverManager.getConnection(url);

        // enable WAL so you can read while writing
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA journal_mode=WAL;");
        }

        System.out.println("✅ Connected to DB at " + dbPath + " (WAL + busy_timeout)");
        return c;
    }
}
