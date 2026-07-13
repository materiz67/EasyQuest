package pl.materiz66.easyquests.config;

import org.bukkit.configuration.file.FileConfiguration;
import pl.materiz66.easyquests.EasyQuests;

public class ConfigManager {
    private final EasyQuests plugin;
    private final SettingsConfig settingsConfig;

    public ConfigManager(EasyQuests plugin) {
        this.plugin = plugin;
        this.settingsConfig = new SettingsConfig(plugin);
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        settingsConfig.load(config);

        plugin.getQuestManager().load();
    }

    public SettingsConfig getSettingsConfig() {
        return settingsConfig;
    }
}