package pl.materiz66.easyquests.database;

import pl.materiz66.easyquests.EasyQuests;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {
    private final EasyQuests plugin;
    private Connection connection;

    public DatabaseManager(EasyQuests plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String type = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();

        try {
            if (type.equals("MYSQL")) {
                String host = plugin.getConfig().getString("database.mysql.host", "localhost");
                int port = plugin.getConfig().getInt("database.mysql.port", 3306);
                String database = plugin.getConfig().getString("database.mysql.database", "easyquests");
                String username = plugin.getConfig().getString("database.mysql.username", "root");
                String password = plugin.getConfig().getString("database.mysql.password", "");
                boolean ssl = plugin.getConfig().getBoolean("database.mysql.useSSL", false);

                // Nawiązywanie bezpiecznego połączenia MySQL
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + ssl + "&autoReconnect=true";
                connection = DriverManager.getConnection(url, username, password);
                plugin.getLogger().info("Pomyslnie nawiazano polaczenie z baza danych MySQL!");
            } else if (type.equals("SQLITE")) {
                // Konfiguracja SQLite w pliku lokalnym database.db
                File dbFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("database.sqlite.file", "database.db"));
                if (!dbFile.exists()) {
                    try {
                        dbFile.createNewFile();
                    } catch (Exception ignored) {}
                }
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                connection = DriverManager.getConnection(url);
                plugin.getLogger().info("Pomyslnie nawiazano polaczenie z lokalna baza danych SQLite!");
            } else {
                plugin.getLogger().info("Zapis bazy danych jest wylaczony. Uzywam plikow plaskich YAML.");
                return;
            }

            createTables();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Blad krytyczny podczas inicjalizacji bazy danych!", e);
        }
    }

    /**
     * Tworzy tablice SQL pod zapis danych graczy
     */
    private void createTables() throws SQLException {
        if (connection == null) return;
        try (PreparedStatement stmt1 = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS easyquests_players (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "active_quest_id VARCHAR(64)" +
                        ")");
             PreparedStatement stmt2 = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS easyquests_completed (" +
                             "uuid VARCHAR(36), " +
                             "quest_id VARCHAR(64), " +
                             "PRIMARY KEY (uuid, quest_id)" +
                             ")");
             PreparedStatement stmt3 = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS easyquests_progress (" +
                             "uuid VARCHAR(36), " +
                             "quest_id VARCHAR(64), " +
                             "progress INT, " +
                             "PRIMARY KEY (uuid, quest_id)" +
                             ")")) {
            stmt1.execute();
            stmt2.execute();
            stmt3.execute();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initialize();
            }
        } catch (SQLException e) {
            initialize();
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Nie udalo sie zamknac polaczenia z baza danych.", e);
        }
    }
}