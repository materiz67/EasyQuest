package pl.materiz66.easyquests.user;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.materiz66.easyquests.EasyQuests;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerDataManager {
    private final EasyQuests plugin;
    private final Map<UUID, PlayerData> cache = new HashMap<>();
    private final File dataFolder;

    public PlayerDataManager(EasyQuests plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "userdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public void loadPlayerData(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        PlayerData data = new PlayerData(uuid);

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
            // Odczyt aktywnego zadania z pliku UUID.yml
            if (config.contains("active-quest")) {
                data.setActiveQuestId(config.getString("active-quest"));
            }
        }
        cache.put(uuid, data);
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) return;

        File file = new File(dataFolder, uuid + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("completed", new java.util.ArrayList<>(data.getCompletedQuests()));
        for (Map.Entry<String, Integer> entry : data.getProgress().entrySet()) {
            config.set("progress." + entry.getKey(), entry.getValue());
        }
        // Zapis aktualnie aktywnego zadania
        config.set("active-quest", data.getActiveQuestId());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for UUID: " + uuid, e);
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