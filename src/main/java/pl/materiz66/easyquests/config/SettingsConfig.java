package pl.materiz66.easyquests.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Type-safe wrapper ustawień globalnych z config.yml.
 * Sekcje: database, settings, gui, categories.
 */
public class SettingsConfig {

    // Baza danych
    private boolean useMySQL;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    // Ogólne ustawienia
    private String actionBarProgressFormat;
    private boolean showActionBar;
    private boolean autoStartFirstQuest;

    // GUI – delegowane do osobnej klasy
    private final GUIConfig guiConfig = new GUIConfig();

    // Kategorie (zachowana kolejność z pliku dzięki LinkedHashMap)
    private final Map<String, QuestCategory> categories = new LinkedHashMap<>();

    public void load(FileConfiguration config) {
        // Baza danych
        this.useMySQL = config.getBoolean("database.use-mysql", false);
        this.host = config.getString("database.host", "localhost");
        this.port = config.getInt("database.port", 3306);
        this.database = config.getString("database.database", "minecraft");
        this.username = config.getString("database.username", "root");
        this.password = config.getString("database.password", "password");

        // Ogólne ustawienia
        this.actionBarProgressFormat = config.getString("settings.actionbar-progress-format",
                "<gray>Zadanie: <yellow>%quest% <gray>➔ <gold>%progress%/%target%</gold>");
        this.showActionBar = config.getBoolean("settings.show-actionbar", true);
        this.autoStartFirstQuest = config.getBoolean("settings.auto-start-first-quest", true);

        // Konfiguracja GUI
        guiConfig.load(config);

        // Kategorie – zachowana kolejność z pliku
        categories.clear();
        ConfigurationSection section = config.getConfigurationSection("categories");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key + ".display-name", key);
                String material = section.getString(key + ".material", "BOOK");
                int slot = section.getInt(key + ".slot", 10);
                var lore = section.getStringList(key + ".lore");
                categories.put(key, new QuestCategory(key, name, material, slot, lore));
            }
        }
    }

    // --- Gettery ---
    public boolean isUseMySQL() { return useMySQL; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabase() { return database; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public String getActionBarProgressFormat() { return actionBarProgressFormat; }
    public boolean isShowActionBar() { return showActionBar; }
    public boolean isAutoStartFirstQuest() { return autoStartFirstQuest; }

    public GUIConfig getGuiConfig() { return guiConfig; }
    public Map<String, QuestCategory> getCategories() { return categories; }
}