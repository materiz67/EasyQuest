package pl.materiz66.easyquests.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.PlayerData;
import pl.materiz66.easyquests.util.ColorUtil;

public class EasyQuestsPlaceholderExpansion extends PlaceholderExpansion {

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

        EasyQuests plugin;
        try {
            plugin = JavaPlugin.getPlugin(EasyQuests.class);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "";
        }

        if (plugin == null || !plugin.isEnabled()) return "";

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return "";

        String activeQuestId = data.getActiveQuestId();

        // INTEGRACJA: Pokaż BossBar tylko wtedy, gdy zadanie jest aktywne ORAZ gracz mieści się w limicie czasowym po akcji
        if (params.equalsIgnoreCase("has_active")) {
            boolean hasActive = activeQuestId != null;
            boolean shouldShow = plugin.isHudVisible(player);
            return (hasActive && shouldShow) ? "true" : "false";
        }

        if (activeQuestId == null) {
            return "";
        }

        Quest activeQuest = plugin.getQuestManager().getQuest(activeQuestId);
        if (activeQuest == null) return "";

        int progress = data.getQuestProgress(activeQuestId);
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
}