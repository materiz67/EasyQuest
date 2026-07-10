package pl.materiz66.easyquests.database;

import pl.materiz66.easyquests.user.QuestProgress;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseService {
    private HikariDatabase mysqlDb;
    private SQLiteDatabase sqliteDb;
    private boolean useMySQL;

    public void initialize(File dataFolder, boolean useMySQL, String host, int port, String database, String user, String password) {
        this.useMySQL = useMySQL;
        if (useMySQL) {
            this.mysqlDb = new HikariDatabase();
            this.mysqlDb.connect(host, port, database, user, password);
        } else {
            this.sqliteDb = new SQLiteDatabase();
            this.sqliteDb.connect(dataFolder);
        }
        createTables();
    }

    private Connection getConnection() throws SQLException {
        return useMySQL ? mysqlDb.getConnection() : sqliteDb.getConnection();
    }

    private void createTables() {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS eq_player_quests (" +
                             "uuid VARCHAR(36) NOT NULL, " +
                             "quest_id VARCHAR(64) NOT NULL, " +
                             "progress INT DEFAULT 0, " +
                             "completed TINYINT DEFAULT 0, " +
                             "PRIMARY KEY (uuid, quest_id))")) {
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<QuestProgress> loadPlayerProgress(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            QuestProgress progress = new QuestProgress(uuid);
            String query = "SELECT quest_id, progress, completed FROM eq_player_quests WHERE uuid = ?";

            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String questId = rs.getString("quest_id");
                        int prog = rs.getInt("progress");
                        boolean completed = rs.getBoolean("completed");

                        if (completed) {
                            progress.getCompletedQuests().put(questId, true);
                        } else {
                            progress.getActiveQuestsProgress().put(questId, prog);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return progress;
        });
    }

    public CompletableFuture<Void> savePlayerProgress(QuestProgress progress) {
        return CompletableFuture.runAsync(() -> {
            String upsert = "REPLACE INTO eq_player_quests (uuid, quest_id, progress, completed) VALUES (?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(upsert)) {

                conn.setAutoCommit(false);
                for (var entry : progress.getActiveQuestsProgress().entrySet()) {
                    ps.setString(1, progress.getPlayerUuid().toString());
                    ps.setString(2, entry.getKey());
                    ps.setInt(3, entry.getValue());
                    ps.setBoolean(4, false);
                    ps.addBatch();
                }
                for (var entry : progress.getCompletedQuests().keySet()) {
                    ps.setString(1, progress.getPlayerUuid().toString());
                    ps.setString(2, entry);
                    ps.setInt(3, 0);
                    ps.setBoolean(4, true);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void close() {
        if (useMySQL && mysqlDb != null) {
            mysqlDb.close();
        } else if (sqliteDb != null) {
            sqliteDb.close();
        }
    }
}