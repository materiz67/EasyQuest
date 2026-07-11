package pl.materiz66.easyquests.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.materiz66.easyquests.EasyQuestPlugin;

import java.io.File;

/**
 * Koordynator konfiguracji pluginu.
 * Wczytuje i zarządza plikami: config.yml, messages.yml.
 * Używa plugin.saveResource() – pliki szablonów są w src/main/resources.
 */
public class ConfigManager {
    private final EasyQuestPlugin plugin;

    private final SettingsConfig settings = new SettingsConfig();
    private final MessagesConfig messages = new MessagesConfig();

    private File messagesFile;
    private FileConfiguration messagesYaml;

    public ConfigManager(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // --- config.yml ---
        // saveDefaultConfig() kopiuje config.yml z jara jeśli plik nie istnieje
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        settings.load(plugin.getConfig());

        // --- messages.yml ---
        messagesFile = new File(dataFolder, "messages.yml");
        if (!messagesFile.exists()) {
            // Kopiuje messages.yml z resources/ do folderu pluginu
            plugin.saveResource("messages.yml", false);
        }
        messagesYaml = YamlConfiguration.loadConfiguration(messagesFile);
        messages.load(messagesYaml);
    }

    public void reloadAll() {
        // Wymuszamy przeładowanie z dysku
        plugin.reloadConfig();
        settings.load(plugin.getConfig());

        if (messagesFile != null && messagesFile.exists()) {
            messagesYaml = YamlConfiguration.loadConfiguration(messagesFile);
            messages.load(messagesYaml);
        }
    }

    // --- Gettery ---
    public SettingsConfig getSettings() { return settings; }
    public MessagesConfig getMessages() { return messages; }
    public GUIConfig getGui() { return settings.getGuiConfig(); }
    public FileConfiguration getMessagesYaml() { return messagesYaml; }
}