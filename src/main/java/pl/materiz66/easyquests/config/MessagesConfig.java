package pl.materiz66.easyquests.config;

import org.bukkit.configuration.file.FileConfiguration;

public class MessagesConfig {
    private String prefix;
    private String noPermission;
    private String questStarted;
    private String questCompleted;
    private String questProgress;

    /**
     * Mapuje wartości wiadomości na pola klasy.
     */
    public void load(FileConfiguration config) {
        this.prefix = config.getString("prefix", "<dark_gray>[<gradient:gold:yellow>EasyQuest</gradient>]<gray> ");
        this.noPermission = config.getString("no-permission", "<red>Nie masz uprawnien do uzycia tej komendy.");
        this.questStarted = config.getString("quest-started", "<green>Rozpoczales nowe zadanie: <yellow>%quest%</yellow>!");
        this.questCompleted = config.getString("quest-completed", "<green>Gratulacje! Ukonczyles zadanie <gold>%quest%</gold>.");
        this.questProgress = config.getString("quest-progress", "<gray>Postep: <yellow>%objective%</yellow> (<gold>%progress%/%target%</gold>)");
    }

    public String getPrefix() { return prefix; }
    public String getNoPermission() { return noPermission; }
    public String getQuestStarted() { return questStarted; }
    public String getQuestCompleted() { return questCompleted; }
    public String getQuestProgress() { return questProgress; }
}