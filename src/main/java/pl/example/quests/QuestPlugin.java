package pl.example.quests;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class QuestPlugin extends JavaPlugin {
    private QuestManager questManager;
    private ActionBarNotification actionBarNotification;
    private MessageManager messageManager;
    private VaultHook vaultHook;

    @Override
    public void onEnable() {
        // Zapisuje domyślny plik config.yml jeśli nie istnieje w folderze pluginu
        saveDefaultConfig();

        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        this.messageManager = new MessageManager(this);
        this.actionBarNotification = new ActionBarNotification(this);
        this.vaultHook = new VaultHook();

        if (vaultHook.setupEconomy()) {
            getLogger().info("Pomyslnie polaczono z systemem ekonomii Vault!");
        } else {
            getLogger().warning("Nie znaleziono kompatybilnego pluginu ekonomicznego Vault. Nagrody pieniezne zostana zablokowane.");
        }

        this.questManager = new QuestManager(this);
        this.questManager.loadQuests();

        getServer().getPluginManager().registerEvents(new QuestListener(this), this);

        // Rejestracja komendy oraz jej silnika podpowiedzi TabCompleter
        QuestCommand questCommand = new QuestCommand(this);
        getCommand("questy").setExecutor(questCommand);
        getCommand("questy").setTabCompleter(questCommand);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new QuestPlaceholderExpansion(this).register();
            getLogger().info("Zarejestrowano rozszerzenie PlaceholderAPI.");
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            questManager.loadPlayerData(p);
        }

        getLogger().info("Zaladowano profesjonalny plugin EasyQuests!");
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            questManager.saveAndUnloadPlayerData(p.getUniqueId());
        }
        getLogger().info("Wylaczono plugin EasyQuests.");
    }

    public QuestManager getQuestManager() { return questManager; }
    public ActionBarNotification getActionBarNotification() { return actionBarNotification; }
    public MessageManager getMessageManager() { return messageManager; }
    public VaultHook getVaultHook() { return vaultHook; }
}