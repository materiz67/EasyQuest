package pl.materiz66.easyquests;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.materiz66.easyquests.commands.EasyQuestsCommand;
import pl.materiz66.easyquests.config.ConfigManager;
import pl.materiz66.easyquests.database.DatabaseManager; // NOWY IMPORT
import pl.materiz66.easyquests.gui.QuestPathMenu;
import pl.materiz66.easyquests.hook.EasyQuestsPlaceholderExpansion;
import pl.materiz66.easyquests.listeners.MenuClickListener;
import pl.materiz66.easyquests.listeners.QuestProgressionListener;
import pl.materiz66.easyquests.manager.BossBarManager;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.quest.QuestCategory;
import pl.materiz66.easyquests.quest.QuestManager;
import pl.materiz66.easyquests.user.PlayerData;
import pl.materiz66.easyquests.user.PlayerDataManager;
import pl.materiz66.easyquests.util.ColorUtil;
import pl.materiz66.easyquests.util.ValidationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EasyQuests extends JavaPlugin {
    private QuestManager questManager;
    private ConfigManager configManager;
    private BossBarManager bossBarManager;
    private PlayerDataManager playerDataManager;
    private DatabaseManager databaseManager; // DODANE POLE

    @Override
    public void onEnable() {
        this.questManager = new QuestManager(this);
        this.configManager = new ConfigManager(this);
        this.bossBarManager = new BossBarManager(this);
        this.playerDataManager = new PlayerDataManager(this);

        // 1. Inicjalizacja menedżera baz danych (przed załadowaniem konfiguracji)
        this.databaseManager = new DatabaseManager(this);

        this.configManager.loadConfigs();

        // 2. Inicjalizacja połączeń SQL (SQLite / MySQL w zależności od wyboru w config.yml)
        this.databaseManager.initialize();

        EasyQuestsCommand cmdExecutor = new EasyQuestsCommand(this);
        if (getCommand("easyquests") != null) {
            getCommand("easyquests").setExecutor(cmdExecutor);
            getCommand("easyquests").setTabCompleter(cmdExecutor);
        }

        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);
        getServer().getPluginManager().registerEvents(new QuestProgressionListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EasyQuestsPlaceholderExpansion().register();
            getLogger().info("Zarejestrowano zmienne PlaceholderAPI dla EasyQuests.");
        }

        // Automatyczny autozapis co 5 minut
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            playerDataManager.saveAll();
        }, 6000L, 6000L);

        // Sekundowy scheduler do wygaszania HUD
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!isHudVisible(player)) {
                    bossBarManager.removeBossBar(player);
                }
            }
        }, 20L, 20L);

        getLogger().info("EasyQuests 1.6.0 został pomyślnie włączony i połączony z bazą danych!");
    }

    @Override
    public void onDisable() {
        if (bossBarManager != null) {
            bossBarManager.clearAll();
        }
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        // 3. Bezpieczne zamknięcie aktywnego połączenia SQL
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("EasyQuests został wyłączony.");
    }

    // Mapa przechowująca czas (timestamp) ostatniej akcji gracza
    private final Map<UUID, Long> lastActivity = new HashMap<>();

    public void updateActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isHudVisible(Player player) {
        if (!lastActivity.containsKey(player.getUniqueId())) {
            return false;
        }
        long allowedDuration = getConfig().getLong("hud-visible-duration", 5) * 1000L;
        return (System.currentTimeMillis() - lastActivity.get(player.getUniqueId())) < allowedDuration;
    }

    /**
     * Aktywuje wybrane przez gracza zadanie w GUI
     */
    public void handleQuestActivation(Player player, Quest quest) {
        PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
        if (data == null) return;

        if (data.isCompleted(quest.getId())) {
            player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&cTo zadanie zostalo juz ukonczone!"));
            return;
        }

        List<Quest> categoryQuests = quest.getCategory().getQuests();
        int index = categoryQuests.indexOf(quest);
        if (index > 0) {
            Quest prevQuest = categoryQuests.get(index - 1);
            if (!data.isCompleted(prevQuest.getId())) {
                player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&cTo zadanie jest zablokowane! Musisz ukonczyc poprzednie etapy."));
                if (getConfig().getBoolean("gui.sounds.enabled", true)) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
                return;
            }
        }

        if (quest.getId().equals(data.getActiveQuestId())) {
            player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&cTo zadanie jest juz Twoja aktywnoscia!"));
            return;
        }

        String prefix = getConfigManager().getSettingsConfig().getPrefix();
        String questName = quest.getDisplayName() != null ? quest.getDisplayName() : quest.getId();

        String oldActiveId = data.getActiveQuestId();
        if (oldActiveId != null) {
            Quest oldQuest = questManager.getQuest(oldActiveId);
            String oldQuestName = oldQuest != null ? (oldQuest.getDisplayName() != null ? oldQuest.getDisplayName() : oldQuest.getId()) : "poprzednie";
            player.sendMessage(ColorUtil.formatLegacy(prefix + "&aZmieniono aktywne zadanie z &f" + oldQuestName + " &ana &f" + questName + "&a!"));
        } else {
            player.sendMessage(ColorUtil.formatLegacy(prefix + "&aUaktywniono zadanie: &f" + questName));
        }

        data.setActiveQuestId(quest.getId());
        updateActivity(player);

        if (getConfig().getBoolean("gui.sounds.enabled", true)) {
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }

        int currentProgress = data.getQuestProgress(quest.getId());
        bossBarManager.updateBossBar(player, quest, currentProgress);

        new QuestPathMenu(this, quest.getCategory(), player).open();
    }

    /**
     * Zarządza postępem, uaktualnianiem ActionBar/BossBar oraz automatycznym awansem do kolejnych etapów i kategorii
     */
    public void handleProgress(Player player, Quest quest, int progressGained) {
        PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
        if (data == null) return;

        int currentProgress = data.getQuestProgress(quest.getId());
        int target = quest.getObjective().getAmount();

        if (currentProgress >= target) return;

        updateActivity(player);

        int newProgress = currentProgress + progressGained;
        if (newProgress > target) newProgress = target;

        data.setQuestProgress(quest.getId(), newProgress);

        char barChar = getConfig().getString("gui.progress-bar.char", "■").charAt(0);
        String compColor = getConfig().getString("gui.progress-bar.color-completed", "&#55ff55");
        String uncompColor = getConfig().getString("gui.progress-bar.color-uncompleted", "&#ff5555");
        String progressBar = ColorUtil.getProgressBar(newProgress, target, 10, barChar, compColor, uncompColor);

        if (getConfig().getBoolean("actionbar.enabled", true)) {
            String rawFormat = getConfig().getString("actionbar.format", "&eᴀᴋᴛʏᴡɴᴇ ᴢᴀᴅᴀɴɪᴇ: &f{quest} &7- &6{progress}/{target_amount} &8[{progress_bar}&8]");
            String formatted = rawFormat
                    .replace("{quest}", quest.getDisplayName() != null ? quest.getDisplayName() : quest.getId())
                    .replace("{progress}", String.valueOf(newProgress))
                    .replace("{target_amount}", String.valueOf(target))
                    .replace("{progress_bar}", progressBar);

            player.sendActionBar(ColorUtil.format(formatted));
        }

        bossBarManager.updateBossBar(player, quest, newProgress);

        if (newProgress >= target) {
            data.completeQuest(quest.getId());
            bossBarManager.removeBossBar(player);
            player.sendActionBar(ColorUtil.format(" "));

            if (getConfig().getBoolean("gui.sounds.enabled", true)) {
                Sound sound = ValidationUtil.getSafeSound(getConfig().getString("gui.sounds.quest-unlocked", "BLOCK_NOTE_BLOCK_PLING"), Sound.BLOCK_NOTE_BLOCK_PLING);
                player.playSound(player.getLocation(), sound, 1.0f, 1.2f);
            }

            String rawCompletedMsg = getConfigManager().getSettingsConfig().getMsgQuestCompleted();
            String completedMsg = rawCompletedMsg.replace("{quest}", quest.getDisplayName() != null ? quest.getDisplayName() : quest.getId());
            player.sendMessage(ColorUtil.formatLegacy(completedMsg));

            for (String command : quest.getRewards()) {
                String cmd = command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }

            QuestCategory currentCategory = quest.getCategory();
            List<Quest> categoryQuests = currentCategory.getQuests();
            int currentIndex = categoryQuests.indexOf(quest);

            if (currentIndex < categoryQuests.size() - 1) {
                Quest nextQuest = categoryQuests.get(currentIndex + 1);
                data.setActiveQuestId(nextQuest.getId());

                String nextQuestName = nextQuest.getDisplayName() != null ? nextQuest.getDisplayName() : nextQuest.getId();
                player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&aAutomatycznie aktywowano kolejny etap: &f" + nextQuestName));

                int nextProgress = data.getQuestProgress(nextQuest.getId());
                bossBarManager.updateBossBar(player, nextQuest, nextProgress);
            } else {
                player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&6&lGRATULACJE! &aUkonczyles cala sciezke kategorii: &e" + currentCategory.getDisplayName()));

                QuestCategory nextCategory = getNextCategory(currentCategory);
                if (nextCategory != null) {
                    Quest nextCategoryFirstQuest = nextCategory.getQuests().get(0);
                    data.setActiveQuestId(nextCategoryFirstQuest.getId());

                    String nextQuestName = nextCategoryFirstQuest.getDisplayName() != null ? nextCategoryFirstQuest.getDisplayName() : nextCategoryFirstQuest.getId();
                    player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&6Rozpoczynasz nowa kategorie: " + nextCategory.getDisplayName() + "&a! Aktywowano pierwsze zadanie: &f" + nextQuestName));

                    int nextProgress = data.getQuestProgress(nextCategoryFirstQuest.getId());
                    bossBarManager.updateBossBar(player, nextCategoryFirstQuest, nextProgress);
                } else {
                    player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&6&lGRATULACJE! &aUkonczyles absolutnie wszystkie kategorie zadan na serwerze!"));
                }
            }
        }
    }

    private QuestCategory getNextCategory(QuestCategory current) {
        List<QuestCategory> allCategories = new ArrayList<>(getQuestManager().getCategories().values());
        int index = allCategories.indexOf(current);
        if (index >= 0 && index < allCategories.size() - 1) {
            return allCategories.get(index + 1);
        }
        return null;
    }

    public QuestManager getQuestManager() { return questManager; }
    public ConfigManager getConfigManager() { return configManager; }
    public BossBarManager getBossBarManager() { return bossBarManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; } // DODANY GETTER
}