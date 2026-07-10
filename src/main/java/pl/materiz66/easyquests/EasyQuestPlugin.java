package pl.materiz66.easyquests;

import org.bukkit.plugin.java.JavaPlugin;
import pl.materiz66.easyquests.api.EasyQuestAPI;
import pl.materiz66.easyquests.command.AdminCommand;
import pl.materiz66.easyquests.command.QuestCommand;
import pl.materiz66.easyquests.config.ConfigManager;
import pl.materiz66.easyquests.database.DatabaseService;
import pl.materiz66.easyquests.listener.InventoryClickListener;
import pl.materiz66.easyquests.listener.PlayerJoinQuitListener;
import pl.materiz66.easyquests.listener.QuestObjectiveListener;
import pl.materiz66.easyquests.quest.QuestManager;
import pl.materiz66.easyquests.service.MessageService;
import pl.materiz66.easyquests.user.UserCacheManager;

public final class EasyQuestPlugin extends JavaPlugin {
    private ConfigManager configManager;
    private DatabaseService databaseService;
    private UserCacheManager cacheManager;
    private MessageService messageService;
    private QuestManager questManager;

    @Override
    public void onEnable() {
        // 1. Inicjalizacja parsera tekstów (MiniMessage i PlaceholderAPI)
        this.messageService = new MessageService();

        // 2. Ładowanie i koordynacja konfiguracji (Type-safe config)
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        // 3. Inicjalizacja systemu pamięci RAM (Cache)
        this.cacheManager = new UserCacheManager();

        // 4. Inicjalizacja i wczytanie bazy zadań (Quest Engine)
        this.questManager = new QuestManager(this);
        this.questManager.loadQuests();

        // 5. Inicjalizacja asynchronicznej bazy danych SQL
        this.databaseService = new DatabaseService();
        boolean useMySQL = configManager.getSettings().isUseMySQL();
        databaseService.initialize(
                getDataFolder(),
                useMySQL,
                configManager.getSettings().getHost(),
                configManager.getSettings().getPort(),
                configManager.getSettings().getDatabase(),
                configManager.getSettings().getUsername(),
                configManager.getSettings().getPassword()
        );

        // 6. Integracja i wystawienie publicznego API dla innych deweloperów
        EasyQuestAPI.setPlugin(this);

        // 7. Rejestracja słuchaczy zdarzeń (Listeners)
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new QuestObjectiveListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);

        // 8. Rejestracja komend systemowych
        if (getCommand("quest") != null) {
            getCommand("quest").setExecutor(new QuestCommand(this));
        }

        AdminCommand adminCommand = new AdminCommand(this);
        if (getCommand("easyquest") != null) {
            getCommand("easyquest").setExecutor(adminCommand);
            getCommand("easyquest").setTabCompleter(adminCommand);
        }

        getLogger().info("EasyQuests 2.0 pomyslnie zainicjalizowany!");
    }

    @Override
    public void onDisable() {
        // Zabezpieczenie danych wszystkich graczy online przed wyłączeniem / przeładowaniem serwera
        if (cacheManager != null && databaseService != null) {
            getLogger().info("Zapisywanie danych graczy przed wylaczeniem...");
            // Wykonujemy synchronicznie na głównym wątku podczas disable, by zapobiec przerwaniu zapisu przez JVM
            getServer().getOnlinePlayers().forEach(player -> {
                var progress = cacheManager.getCachedProgress(player.getUniqueId());
                if (progress != null) {
                    try {
                        databaseService.savePlayerProgress(progress).get(); // Blokuje wątek do ukończenia operacji zapisu
                    } catch (Exception e) {
                        getLogger().severe("Blad podczas synchronicznego zapisu danych dla: " + player.getName());
                        e.printStackTrace();
                    }
                }
            });
        }

        // Zamknięcie połączenia HikariDataSource
        if (databaseService != null) {
            databaseService.close();
        }
        getLogger().info("EasyQuests 2.0 wylaczony.");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseService getDatabaseService() { return databaseService; }
    public UserCacheManager getCacheManager() { return cacheManager; }
    public MessageService getMessageService() { return messageService; }
    public QuestManager getQuestManager() { return questManager; }

    // Pomocnicza klasa dla ładowania YAML wiadomości w komendach
    public org.bukkit.configuration.file.FileConfiguration getMessagesYaml() {
        return configManager.getMessagesYaml();
    }

    // Metoda do bezpiecznego przeładowania wiadomości i konfiguracji
    public void reloadMessages() {
        if (configManager != null) {
            configManager.reloadAll();
        }
    }
}