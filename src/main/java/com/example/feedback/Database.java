
package com.example.feedback;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;

public class Database {

    private static final Path DB_FOLDER = Paths.get(
            System.getProperty("user.home"), "feedback-app-data");

    private static final Path DB_FILE = DB_FOLDER.resolve("feedback.db");

    private static final String URL =
            "jdbc:sqlite:" + DB_FILE.toAbsolutePath();
        public static void initialize() throws IOException, SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        Files.createDirectories(DB_FOLDER);

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS feedback (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reference_id TEXT NOT NULL UNIQUE, " +
                "name TEXT NOT NULL, " +
                "email TEXT NOT NULL, " +
                "feedback TEXT NOT NULL, " +
                "submitted_at TEXT NOT NULL)"
            );
        }
    }

    public static void save(String referenceId, String name,
                            String email, String feedback)
            throws SQLException {

        String sql = "INSERT INTO feedback " +
                "(reference_id, name, email, feedback, submitted_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, referenceId);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.setString(4, feedback);
            statement.setString(5, Timestamp.from(Instant.now()).toString());

            statement.executeUpdate();
        }
    }
}