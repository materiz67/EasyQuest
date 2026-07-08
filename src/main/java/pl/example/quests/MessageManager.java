package pl.example.quests;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageManager {
    private final QuestPlugin plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public MessageManager(QuestPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        String msg = messagesConfig.getString(path);
        if (msg == null) return "Missing message: " + path;

        String prefix = messagesConfig.getString("prefix", "&8[&e&lQuesty&8] &r");
        // Łączenie prefiksu i wiadomości wraz z obsługą Hex
        return ColorUtil.color(msg.replace("%prefix%", prefix));
    }

    public String getRawMessage(String path) {
        String msg = messagesConfig.getString(path);
        return msg != null ? ColorUtil.color(msg) : "";
    }

    public FileConfiguration getConfig() {
        return messagesConfig;
    }
}