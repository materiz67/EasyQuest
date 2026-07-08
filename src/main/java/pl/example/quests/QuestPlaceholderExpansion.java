package pl.example.quests;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class QuestPlaceholderExpansion extends PlaceholderExpansion {
    private final QuestPlugin plugin;

    public QuestPlaceholderExpansion(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "ExampleAuthor";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "easyquests";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.2";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // %easyquests_progress_stone_breaker%
        if (params.startsWith("progress_")) {
            String questId = params.substring("progress_".length());
            return String.valueOf(plugin.getQuestManager().getProgress(player.getUniqueId(), questId));
        }

        // %easyquests_completed_stone_breaker%
        if (params.startsWith("completed_")) {
            String questId = params.substring("completed_".length());
            return plugin.getQuestManager().isCompleted(player.getUniqueId(), questId) ? "Tak" : "Nie";
        }

        // %easyquests_target_stone_breaker%
        if (params.startsWith("target_")) {
            String questId = params.substring("target_".length());
            return String.valueOf(plugin.getQuestManager().getQuestTargetAmount(questId));
        }

        // %easyquests_formatted_stone_breaker% -> "42/500"
        if (params.startsWith("formatted_")) {
            String questId = params.substring("formatted_".length());
            int progress = plugin.getQuestManager().getProgress(player.getUniqueId(), questId);
            int target = plugin.getQuestManager().getQuestTargetAmount(questId);
            return progress + "/" + target;
        }

        return null;
    }
}