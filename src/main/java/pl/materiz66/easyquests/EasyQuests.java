package pl.materiz66.easyquests;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.materiz66.easyquests.commands.EasyQuestsCommand;
import pl.materiz66.easyquests.config.ConfigManager;
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
import java.util.List;

public final class EasyQuests extends JavaPlugin {
    private QuestManager questManager;
    private ConfigManager configManager;
    private BossBarManager bossBarManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        this.questManager = new QuestManager(this);
        this.configManager = new ConfigManager(this);
        this.bossBarManager = new BossBarManager(this);
        this.playerDataManager = new PlayerDataManager(this);

        this.configManager.loadConfigs();

        if (getCommand("easyquests") != null) {
            getCommand("easyquests").setExecutor(new EasyQuestsCommand(this));
        }

        getServer().getPluginManager().registerEvents(new MenuClickListener(this), this);
        getServer().getPluginManager().registerEvents(new QuestProgressionListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EasyQuestsPlaceholderExpansion(this).register();
            getLogger().info("Zarejestrowano zmienne PlaceholderAPI dla EasyQuests.");
        }

        getLogger().info("EasyQuests 1.5.0 został pomyślnie włączony!");
    }

    @Override
    public void onDisable() {
        if (bossBarManager != null) {
            bossBarManager.clearAll();
        }
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        getLogger().info("EasyQuests został wyłączony.");
    }

    /**
     * Aktywuje wybrane przez gracza zadanie w GUI (maksymalnie jedno aktywne)
     */
    public void handleQuestActivation(Player player, Quest quest) {
        PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
        if (data == null) return;

        // 1. Zabezpieczenie: Czy zadanie jest już ukończone
        if (data.isCompleted(quest.getId())) {
            player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&cTo zadanie zostalo juz ukonczone!"));
            return;
        }

        // 2. Zabezpieczenie: Czy etap nie jest zablokowany
        List<Quest> categoryQuests = quest.getCategory().getQuests();
        int index = categoryQuests.indexOf(quest);
        if (index > 0) {
            Quest prevQuest = categoryQuests.get(index - 1);
            if (!data.isCompleted(prevQuest.getId())) {
                player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&cTo zadanie jest zablokowane! Musisz ukonczyc poprzednie etapy."));
                if (getConfig().getBoolean("gui.sounds.enabled", true)) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
                return;
            }
        }

        // 3. Zabezpieczenie: Czy zadanie jest już aktywne
        if (quest.getId().equals(data.getActiveQuestId())) {
            player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&cTo zadanie jest juz Twoja aktywnoscia!"));
            return;
        }

        String prefix = getConfigManager().getSettingsConfig().getPrefix();
        String questName = quest.getDisplayName() != null ? quest.getDisplayName() : quest.getId();

        // Sprawdzenie, czy gracz zmienia poprzednio aktywne zadanie
        String oldActiveId = data.getActiveQuestId();
        if (oldActiveId != null) {
            Quest oldQuest = questManager.getQuest(oldActiveId);
            String oldQuestName = oldQuest != null ? (oldQuest.getDisplayName() != null ? oldQuest.getDisplayName() : oldQuest.getId()) : "poprzednie";
            player.sendMessage(ColorUtil.formatLegacy(prefix + "&aZmieniono aktywne zadanie z &f" + oldQuestName + " &ana &f" + questName + "&a!"));
        } else {
            player.sendMessage(ColorUtil.formatLegacy(prefix + "&aUaktywniono zadanie: &f" + questName));
        }

        // Sukces: Zapisujemy nowe aktywne zadanie w pamięci RAM sesji gracza
        data.setActiveQuestId(quest.getId());

        if (getConfig().getBoolean("gui.sounds.enabled", true)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        }

        // Aktualizacja graficzna pasków postępu
        int currentProgress = data.getQuestProgress(quest.getId());
        bossBarManager.updateBossBar(player, quest, currentProgress);

        // Odświeżenie GUI (re-open) w celu uaktualnienia lore ikon ("zmień zadanie")
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

        int newProgress = currentProgress + progressGained;
        if (newProgress > target) newProgress = target;

        data.setQuestProgress(quest.getId(), newProgress);

        char barChar = getConfig().getString("gui.progress-bar.char", "■").charAt(0);
        String compColor = getConfig().getString("gui.progress-bar.color-completed", "&#55ff55");
        String uncompColor = getConfig().getString("gui.progress-bar.color-uncompleted", "&#ff5555");
        String progressBar = ColorUtil.getProgressBar(newProgress, target, 10, barChar, compColor, uncompColor);

        // Wysyłanie paska ActionBar
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

        // Warunek ukończenia zadania
        if (newProgress >= target) {
            data.completeQuest(quest.getId());
            bossBarManager.removeBossBar(player);
            player.sendActionBar(ColorUtil.format(" ")); // Czyszczenie ActionBar

            if (getConfig().getBoolean("gui.sounds.enabled", true)) {
                Sound sound = ValidationUtil.getSafeSound(getConfig().getString("gui.sounds.quest-unlocked", "BLOCK_NOTE_BLOCK_PLING"), Sound.BLOCK_NOTE_BLOCK_PLING);
                player.playSound(player.getLocation(), sound, 1.0f, 1.2f);
            }

            // Powiadomienie o ukończeniu
            String rawCompletedMsg = getConfigManager().getSettingsConfig().getMsgQuestCompleted();
            String completedMsg = rawCompletedMsg.replace("{quest}", quest.getDisplayName() != null ? quest.getDisplayName() : quest.getId());
            player.sendMessage(ColorUtil.formatLegacy(completedMsg));

            // Wykonanie nagród
            for (String command : quest.getRewards()) {
                String cmd = command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }

            // Automatyczny postęp do kolejnych zadań w tej samej kategorii lub przejście kategorii
            QuestCategory currentCategory = quest.getCategory();
            List<Quest> categoryQuests = currentCategory.getQuests();
            int currentIndex = categoryQuests.indexOf(quest);

            if (currentIndex < categoryQuests.size() - 1) {
                // PRZYPADEK A: Istnieje kolejne zadanie w tej samej kategorii -> automatyczna aktywacja
                Quest nextQuest = categoryQuests.get(currentIndex + 1);
                data.setActiveQuestId(nextQuest.getId());

                String nextQuestName = nextQuest.getDisplayName() != null ? nextQuest.getDisplayName() : nextQuest.getId();
                player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&aAutomatycznie aktywowano kolejny etap: &f" + nextQuestName));

                // Natychmiastowe uaktywnienie BossBara dla nowego poziomu
                int nextProgress = data.getQuestProgress(nextQuest.getId());
                bossBarManager.updateBossBar(player, nextQuest, nextProgress);
            } else {
                // PRZYPADEK B: Ukończono całą kategorię -> przejście do kolejnej kategorii (LinkedHashMap)
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
                    // Gracz ukończył absolutnie wszystko
                    player.sendMessage(ColorUtil.formatLegacy(getConfigManager().getSettingsConfig().getPrefix() + "&6&lGRATULACJE! &aUkonczyles absolutnie wszystkie kategorie zadan na serwerze!"));
                }
            }
        }
    }

    /**
     * Wyszukuje kolejną kategorię z LinkedHashMap zachowując kolejność ładowania
     */
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
}