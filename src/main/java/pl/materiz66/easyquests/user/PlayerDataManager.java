package pl.materiz66.easyquests.user;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.materiz66.easyquests.EasyQuests;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerDataManager {
    private final EasyQuests plugin;
    private final Map<UUID, PlayerData> cache = new HashMap<>();
    private final File dataFolder;
    private boolean useDatabase;

    public PlayerDataManager(EasyQuests plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "userdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Weryfikuje wybrany w config.yml silnik zapisu danych
     */
    private void checkStorageType() {
        String type = plugin.getConfig().getString("database.type", "YAML").toUpperCase();
        this.useDatabase = type.equals("SQLITE") || type.equals("MYSQL");
    }

    public void loadPlayerData(UUID uuid) {
        checkStorageType();
        PlayerData data = new PlayerData(uuid);

        if (useDatabase) {
            Connection conn = plugin.getDatabaseManager().getConnection();
            if (conn == null) {
                cache.put(uuid, data);
                return;
            }

            // 1. Wczytywanie aktywnej misji
            try (PreparedStatement stmt = conn.prepareStatement("SELECT active_quest_id FROM easyquests_players WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        data.setActiveQuestId(rs.getString("active_quest_id"));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Blad wczytywania aktywnej misji dla " + uuid, e);
            }

            // 2. Wczytywanie ukończonych zadań
            try (PreparedStatement stmt = conn.prepareStatement("SELECT quest_id FROM easyquests_completed WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        data.getCompletedQuests().add(rs.getString("quest_id"));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Blad wczytywania ukonczonych misji dla " + uuid, e);
            }

            // 3. Wczytywanie postępów aktywnych zadań
            try (PreparedStatement stmt = conn.prepareStatement("SELECT quest_id, progress FROM easyquests_progress WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        data.getProgress().put(rs.getString("quest_id"), rs.getInt("progress"));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Blad wczytywania postepu dla " + uuid, e);
            }
        } else {
            // Bezpieczny fallback do zapisu YAML (.yml)
            File file = new File(dataFolder, uuid + ".yml");
            if (file.exists()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                if (config.contains("completed")) {
                    data.getCompletedQuests().addAll(config.getStringList("completed"));
                }
                if (config.isConfigurationSection("progress")) {
                    for (String key : config.getConfigurationSection("progress").getKeys(false)) {
                        data.getProgress().put(key, config.getInt("progress." + key));
                    }
                }
                if (config.contains("active-quest")) {
                    data.setActiveQuestId(config.getString("active-quest"));
                }
            }
        }

        cache.put(uuid, data);
    }

    public void savePlayerData(UUID uuid) {
        checkStorageType();
        PlayerData data = cache.get(uuid);
        if (data == null) return;

        if (useDatabase) {
            Connection conn = plugin.getDatabaseManager().getConnection();
            if (conn == null) return;

            try {
                // 1. Zapis aktywnego zadania (REPLACE INTO jest wspierane przez SQLite i MySQL)
                try (PreparedStatement stmt = conn.prepareStatement("REPLACE INTO easyquests_players (uuid, active_quest_id) VALUES (?, ?)")) {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, data.getActiveQuestId());
                    stmt.executeUpdate();
                }

                // 2. Zapis ukończonych zadań (czyszczenie i zapis wsadowy)
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM easyquests_completed WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();
                }
                if (!data.getCompletedQuests().isEmpty()) {
                    try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO easyquests_completed (uuid, quest_id) VALUES (?, ?)")) {
                        for (String qId : data.getCompletedQuests()) {
                            stmt.setString(1, uuid.toString());
                            stmt.setString(2, qId);
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                // 3. Zapis postępów zadań (czyszczenie i zapis wsadowy)
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM easyquests_progress WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();
                }
                if (!data.getProgress().isEmpty()) {
                    try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO easyquests_progress (uuid, quest_id, progress) VALUES (?, ?, ?)")) {
                        for (Map.Entry<String, Integer> entry : data.getProgress().entrySet()) {
                            stmt.setString(1, uuid.toString());
                            stmt.setString(2, entry.getKey());
                            stmt.setInt(3, entry.getValue());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Blad krytyczny zapisu danych w bazie SQL dla " + uuid, e);
            }
        } else {
            // Zapis YAML (.yml)
            File file = new File(dataFolder, uuid + ".yml");
            FileConfiguration config = new YamlConfiguration();

            config.set("completed", new java.util.ArrayList<>(data.getCompletedQuests()));
            for (Map.Entry<String, Integer> entry : data.getProgress().entrySet()) {
                config.set("progress." + entry.getKey(), entry.getValue());
            }
            config.set("active-quest", data.getActiveQuestId());

            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player data for UUID: " + uuid, e);
            }
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        if (!cache.containsKey(uuid)) {
            loadPlayerData(uuid);
        }
        return cache.get(uuid);
    }

    public void unloadPlayerData(UUID uuid) {
        savePlayerData(uuid);
        cache.remove(uuid);
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet()) {
            savePlayerData(uuid);
        }
    }
}