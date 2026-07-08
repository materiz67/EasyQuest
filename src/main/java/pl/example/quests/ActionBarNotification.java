package pl.example.quests;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ActionBarNotification {
    private final QuestPlugin plugin;

    public ActionBarNotification(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    public void showProgress(Player player, String questName, int current, int max) {
        if (!plugin.getMessageManager().getConfig().getBoolean("actionbar.enabled", true)) {
            return;
        }

        String rawFormat = plugin.getMessageManager().getRawMessage("actionbar.format");
        String progressBar = getProgressBar(current, max);

        String message = rawFormat
                .replace("%quest_name%", questName)
                .replace("%current%", String.valueOf(current))
                .replace("%max%", String.valueOf(max))
                .replace("%progress_bar%", progressBar);

        // Wysyłanie wiadomości bezpośrednio nad ekwipunek podręczny gracza
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    private String getProgressBar(int current, int max) {
        int totalBars = 10;
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);
        int leftBars = totalBars - progressBars;

        StringBuilder sb = new StringBuilder("§a");
        for (int i = 0; i < progressBars; i++) {
            sb.append("|");
        }
        sb.append("§c");
        for (int i = 0; i < leftBars; i++) {
            sb.append("|");
        }
        return sb.toString();
    }
}