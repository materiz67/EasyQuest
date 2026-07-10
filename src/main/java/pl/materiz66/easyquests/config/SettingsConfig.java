package pl.materiz66.easyquests.config;

import org.bukkit.configuration.file.FileConfiguration;

public class SettingsConfig {
    private boolean useMySQL;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    private String menuTitle;
    private String actionBarProgressFormat;

    /**
     * Mapuje wartości z obiektu FileConfiguration na pola klasy.
     */
    public void load(FileConfiguration config) {
        this.useMySQL = config.getBoolean("database.use-mysql", false);
        this.host = config.getString("database.host", "localhost");
        this.port = config.getInt("database.port", 3306);
        this.database = config.getString("database.database", "minecraft");
        this.username = config.getString("database.username", "root");
        this.password = config.getString("database.password", "password");

        this.menuTitle = config.getString("settings.menu-title", "<gradient:gold:yellow>Droga zadan</gradient>");
        this.actionBarProgressFormat = config.getString("settings.actionbar-progress-format", "<gray>Zadanie: <yellow>%quest% <gray>➔ <gold>%progress%/%target%</gold>");
    }

    public boolean isUseMySQL() { return useMySQL; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabase() { return database; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getMenuTitle() { return menuTitle; }
    public String getActionBarProgressFormat() { return actionBarProgressFormat; }
}