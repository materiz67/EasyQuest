package pl.materiz66.easyquests.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.PlayerData;
import pl.materiz66.easyquests.util.ColorUtil;

import java.util.List;

public class EasyQuestsPlaceholderExpansion extends PlaceholderExpansion {
    private final EasyQuests plugin;

    public EasyQuestsPlaceholderExpansion(EasyQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() { return "materiz66"; }

    @Override
    public @NotNull String getIdentifier() { return "easyquests"; }

    @Override
    public @NotNull String getVersion() { return "1.5.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        Quest activeQuest = getPlayerActiveQuest(player);

        // Zmienna warunkowa specjalnie pod integrację z CustomNameplates
        if (params.equalsIgnoreCase("has_active")) {
            return activeQuest != null ? "true" : "false";
        }

        // Jeśli gracz nie posiada aktywnego zadania, zwracamy puste wartości.
        // Dzięki temu CustomNameplates/TAB/Scoreboard nie wyświetli pustych ramek ani napisów "Brak zadania".
        if (activeQuest == null) {
            return "";
        }

        int progress = getPlayerProgress(player, activeQuest);
        int target = activeQuest.getObjective().getAmount();

        switch (params.toLowerCase()) {
            case "active_quest":
                return activeQuest.getDisplayName() != null ? activeQuest.getDisplayName() : activeQuest.getId();
            case "progress":
                return String.valueOf(progress);
            case "target":
                return String.valueOf(target);
            case "percent":
                return String.valueOf((int) (((double) progress / target) * 100));
            case "progress_bar":
                char barChar = plugin.getConfig().getString("gui.progress-bar.char", "■").charAt(0);
                String compColor = plugin.getConfig().getString("gui.progress-bar.color-completed", "&#55ff55");
                String uncompColor = plugin.getConfig().getString("gui.progress-bar.color-uncompleted", "&#ff5555");
                return ColorUtil.getProgressBar(progress, target, 10, barChar, compColor, uncompColor);
        }

        return null;
    }

    /**
     * Wyszukuje rzeczywiste aktywne zadanie gracza ze wszystkich załadowanych kategorii
     */
    private Quest getPlayerActiveQuest(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return null;

        for (Quest quest : plugin.getQuestManager().getQuests().values()) {
            if (isQuestActive(player, quest)) {
                return quest;
            }
        }
        return null;
    }

    private boolean isQuestActive(Player player, Quest quest) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return false;

        if (data.isCompleted(quest.getId())) return false;

        List<Quest> categoryQuests = quest.getCategory().getQuests();
        int index = categoryQuests.indexOf(quest);
        if (index == 0) return true;

        Quest prevQuest = categoryQuests.get(index - 1);
        return data.isCompleted(prevQuest.getId());
    }

    private int getPlayerProgress(Player player, Quest quest) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return 0;
        return data.getQuestProgress(quest.getId());
    }
}