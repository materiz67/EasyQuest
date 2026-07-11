package pl.materiz66.easyquests.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Type-safe wrapper wiadomości z pliku messages.yml.
 * Wszystkie wiadomości obsługują format MiniMessage.
 * Placeholdery używają formatu {klucz}, zastępowane przez MessageService.
 */
public class MessagesConfig {

    // Ogólne
    private String prefix;
    private String noPermission;
    private String playerOnly;
    private String playerOffline;
    private String invalidUsage;

    // Postęp zadań
    private String questStarted;
    private String questCompleted;
    private String questProgress;
    private String questLocked;
    private String allCompleted;
    private String alreadyStarted;

    // /quest status
    private String statusHeader;
    private String statusTitle;
    private String statusLine;
    private String statusNoActive;
    private String statusFooter;

    // /quest help
    private String helpHeader;
    private String helpTitle;
    private String helpQuest;
    private String helpQuestStatus;
    private String helpQuestHelp;
    private String helpFooter;

    // Admin
    private String reloadSuccess;
    private String resetSuccess;
    private String resetNotify;
    private String giveSuccess;
    private String giveNotify;
    private String categoryNotFound;
    private String noProgressData;

    public void load(FileConfiguration config) {
        // Ogólne
        prefix = config.getString("prefix", "<dark_gray>[<gradient:gold:yellow>EasyQuests</gradient>]<gray> ");
        noPermission = config.getString("no-permission", "<red>Nie masz uprawnień do użycia tej komendy.");
        playerOnly = config.getString("player-only", "<red>Ta komenda może być wykonana tylko przez gracza.");
        playerOffline = config.getString("player-offline", "<red>Gracz <yellow>{player}</yellow> jest offline.");
        invalidUsage = config.getString("invalid-usage", "<red>Nieprawidłowe użycie. Wpisz <yellow>/quest help</yellow>.");

        // Postęp zadań
        questStarted = config.getString("quest-started", "<green>✔ Rozpocząłeś nowe zadanie: <yellow>{quest}</yellow>!");
        questCompleted = config.getString("quest-completed", "<gold>★ Gratulacje! Ukończyłeś zadanie <green>{quest}</green>!");
        questProgress = config.getString("quest-progress", "<gray>Postęp: <yellow>{objective}</yellow> (<gold>{progress}/{target}</gold>)");
        questLocked = config.getString("quest-locked", "<red>✖ To zadanie jest zablokowane. Ukończ najpierw poprzednie!");
        allCompleted = config.getString("quest-all-completed", "<green>✔ Ukończyłeś już wszystkie zadania w tej kategorii!");
        alreadyStarted = config.getString("already-started", "<yellow>⚠ To zadanie jest już aktywne.");

        // /quest status
        statusHeader = config.getString("status-header", "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        statusTitle = config.getString("status-title", "<gradient:gold:yellow>  Twoje Aktywne Zadania</gradient>");
        statusLine = config.getString("status-line", " <gray>▸ <yellow>{quest} <dark_gray>| <gold>{category} <gray>➔ <aqua>{progress}<gray>/<aqua>{target}");
        statusNoActive = config.getString("status-no-active", " <gray>Nie masz żadnych aktywnych zadań.");
        statusFooter = config.getString("status-footer", "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // /quest help
        helpHeader = config.getString("help-header", "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        helpTitle = config.getString("help-title", "<gradient:gold:yellow>  EasyQuests – Pomoc</gradient>");
        helpQuest = config.getString("help-quest", " <yellow>/quest <gray>— Otwiera menu kategorii zadań.");
        helpQuestStatus = config.getString("help-quest-status", " <yellow>/quest status <gray>— Pokazuje aktywne zadania.");
        helpQuestHelp = config.getString("help-quest-help", " <yellow>/quest help <gray>— Wyświetla tę listę.");
        helpFooter = config.getString("help-footer", "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Admin
        reloadSuccess = config.getString("reload-success", "<green>✔ Konfiguracja oraz zadania zostały pomyślnie przeładowane!");
        resetSuccess = config.getString("reset-success", "<green>✔ Zresetowano postęp zadań dla gracza <yellow>{player}</yellow>.");
        resetNotify = config.getString("reset-notify", "<yellow>⚠ Twój postęp zadań został zresetowany przez administratora.");
        giveSuccess = config.getString("give-success", "<green>✔ Nadano graczowi <yellow>{player}</yellow> start kategorii <gold>{category}</gold>.");
        giveNotify = config.getString("give-notify", "<yellow>⚠ Administrator nadał Ci start w kategorii <gold>{category}</gold>.");
        categoryNotFound = config.getString("category-not-found", "<red>Nie znaleziono kategorii o ID: <yellow>{category}</yellow>.");
        noProgressData = config.getString("no-progress-data", "<red>Błąd: Brak danych gracza w pamięci podręcznej.");
    }

    // --- Gettery ---
    public String getPrefix() { return prefix; }
    public String getNoPermission() { return noPermission; }
    public String getPlayerOnly() { return playerOnly; }
    public String getPlayerOffline() { return playerOffline; }
    public String getInvalidUsage() { return invalidUsage; }

    public String getQuestStarted() { return questStarted; }
    public String getQuestCompleted() { return questCompleted; }
    public String getQuestProgress() { return questProgress; }
    public String getQuestLocked() { return questLocked; }
    public String getAllCompleted() { return allCompleted; }
    public String getAlreadyStarted() { return alreadyStarted; }

    public String getStatusHeader() { return statusHeader; }
    public String getStatusTitle() { return statusTitle; }
    public String getStatusLine() { return statusLine; }
    public String getStatusNoActive() { return statusNoActive; }
    public String getStatusFooter() { return statusFooter; }

    public String getHelpHeader() { return helpHeader; }
    public String getHelpTitle() { return helpTitle; }
    public String getHelpQuest() { return helpQuest; }
    public String getHelpQuestStatus() { return helpQuestStatus; }
    public String getHelpQuestHelp() { return helpQuestHelp; }
    public String getHelpFooter() { return helpFooter; }

    public String getReloadSuccess() { return reloadSuccess; }
    public String getResetSuccess() { return resetSuccess; }
    public String getResetNotify() { return resetNotify; }
    public String getGiveSuccess() { return giveSuccess; }
    public String getGiveNotify() { return giveNotify; }
    public String getCategoryNotFound() { return categoryNotFound; }
    public String getNoProgressData() { return noProgressData; }
}