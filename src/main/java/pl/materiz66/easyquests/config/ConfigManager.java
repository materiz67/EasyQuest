package pl.materiz66.easyquests.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.materiz66.easyquests.EasyQuestPlugin;

import java.io.File;

public class ConfigManager {
    private final EasyQuestPlugin plugin;

    private final SettingsConfig settings = new SettingsConfig();
    private final MessagesConfig messages = new MessagesConfig();

    private File messagesFile;
    private FileConfiguration messagesYaml;

    public ConfigManager(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Wczytuje i mapuje wszystkie zasoby konfiguracyjne wtyczki.
     */
    public void loadAll() {
        // 1. Przetworzenie pliku config.yml
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        settings.load(plugin.getConfig());

        // 2. Przetworzenie pliku messages.yml
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messagesYaml = YamlConfiguration.loadConfiguration(messagesFile);
        messages.load(messagesYaml);
    }

    /**
     * Metoda do natychmiastowego przeładowania konfiguracji.
     */
    public void reloadAll() {
        loadAll();
    }

    public SettingsConfig getSettings() {
        return settings;
    }

    public MessagesConfig getMessages() {
        return messages;
    }

    public FileConfiguration getMessagesYaml() {
        return messagesYaml;
    }
}