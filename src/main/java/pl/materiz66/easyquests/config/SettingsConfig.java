package pl.materiz66.easyquests.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import pl.materiz66.easyquests.EasyQuests;

import java.util.List;

public class SettingsConfig {
    private final EasyQuests plugin;
    private String prefix;

    private QuestIconTemplate lockedTemplate;
    private QuestIconTemplate unlockedTemplate; // Nowe pole
    private QuestIconTemplate activeTemplate;
    private QuestIconTemplate completedTemplate;

    private String msgQuestUnlocked;
    private String msgQuestCompleted;
    private String msgQuestProgress;
    private String msgCannotStartLocked;
    private String msgNoPermission;
    private String msgReloadSuccess;

    public SettingsConfig(EasyQuests plugin) {
        this.plugin = plugin;
    }

    public void load(FileConfiguration config) {
        this.prefix = config.getString("prefix", "&#ff9900&lEasyQuests &#ffcc00» ");

        if (config.isConfigurationSection("gui.quest-icons")) {
            this.lockedTemplate = QuestIconTemplate.fromConfig(config.getConfigurationSection("gui.quest-icons.locked"));
            this.unlockedTemplate = QuestIconTemplate.fromConfig(config.getConfigurationSection("gui.quest-icons.unlocked"));
            this.activeTemplate = QuestIconTemplate.fromConfig(config.getConfigurationSection("gui.quest-icons.active"));
            this.completedTemplate = QuestIconTemplate.fromConfig(config.getConfigurationSection("gui.quest-icons.completed"));
        } else {
            this.lockedTemplate = new QuestIconTemplate(Material.BARRIER, "<red>✖ Zablokowane: {quest}", List.of("<gray>Musisz ukończyć poprzednie etapy tej kategorii."));
            this.unlockedTemplate = new QuestIconTemplate(null, "<yellow>☘ Dostępne: {quest}", List.of("<gray>Kliknij, aby aktywować!"));
            this.activeTemplate = new QuestIconTemplate(null, "<yellow>⚒ W trakcie: {quest}", List.of("<gray>Cel: {target_name}", "<gray>Postęp: <gold>{progress}/{target_amount}</gold>"));
            this.completedTemplate = new QuestIconTemplate(Material.EMERALD, "<green>✔ Ukończono: {quest}", List.of("<gray>To zadanie zostało wykonane!"));
        }

        this.msgQuestUnlocked = config.getString("messages.quest-unlocked", "{prefix}&aOdblokowano nowe zadanie: &f{quest}&a!");
        this.msgQuestCompleted = config.getString("messages.quest-completed", "{prefix}&aGratulacje! Ukończyłeś zadanie: &f{quest}&a!");
        this.msgQuestProgress = config.getString("messages.quest-progress", "{prefix}&ePostęp zadania &f{quest}&e: &6{progress}/{target_amount} &7({percent}%)");
        this.msgCannotStartLocked = config.getString("messages.cannot-start-locked", "{prefix}&cTo zadanie jest zablokowane! Musisz ukończyć poprzednie etapy.");
        this.msgNoPermission = config.getString("messages.no-permission", "{prefix}&cNie posiadasz uprawnień do wykonania tej akcji.");
        this.msgReloadSuccess = config.getString("messages.reload-success", "{prefix}&aPomyślnie przeładowano konfigurację i kategorie zadań!");
    }

    public String getPrefix() { return prefix; }
    public QuestIconTemplate getLockedTemplate() { return lockedTemplate; }
    public QuestIconTemplate getUnlockedTemplate() { return unlockedTemplate; } // Getter
    public QuestIconTemplate getActiveTemplate() { return activeTemplate; }
    public QuestIconTemplate getCompletedTemplate() { return completedTemplate; }

    public String getMsgQuestUnlocked() { return msgQuestUnlocked.replace("{prefix}", prefix); }
    public String getMsgQuestCompleted() { return msgQuestCompleted.replace("{prefix}", prefix); }
    public String getMsgQuestProgress() { return msgQuestProgress.replace("{prefix}", prefix); }
    public String getMsgCannotStartLocked() { return msgCannotStartLocked.replace("{prefix}", prefix); }
    public String getMsgNoPermission() { return msgNoPermission.replace("{prefix}", prefix); }
    public String getMsgReloadSuccess() { return msgReloadSuccess.replace("{prefix}", prefix); }
}