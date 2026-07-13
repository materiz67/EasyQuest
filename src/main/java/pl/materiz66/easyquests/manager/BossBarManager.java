package pl.materiz66.easyquests.manager;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.util.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarManager {
    private final EasyQuests plugin;
    private final Map<UUID, BossBar> activeBars = new HashMap<>();

    public BossBarManager(EasyQuests plugin) {
        this.plugin = plugin;
    }

    public void updateBossBar(Player player, Quest quest, int progress) {
        if (!plugin.getConfig().getBoolean("bossbar.enabled", true)) {
            removeBossBar(player);
            return;
        }

        UUID uuid = player.getUniqueId();
        BossBar bossBar = activeBars.get(uuid);

        int max = quest.getObjective().getAmount();
        double ratio = (double) progress / max;
        if (ratio > 1.0) ratio = 1.0;
        if (ratio < 0.0) ratio = 0.0;

        // Budowanie paska postępu (kwadratów)
        char barChar = plugin.getConfig().getString("gui.progress-bar.char", "■").charAt(0);
        String compColor = plugin.getConfig().getString("gui.progress-bar.color-completed", "&#55ff55");
        String uncompColor = plugin.getConfig().getString("gui.progress-bar.color-uncompleted", "&#ff5555");
        String progressBar = ColorUtil.getProgressBar(progress, max, 10, barChar, compColor, uncompColor);

        // Odczyt formatu tytułu
        String rawTitle = plugin.getConfig().getString("bossbar.title", "&eᴘᴏsᴛᴇᴘ: &f{quest} &7- &6{progress}/{target_amount} &8[{progress_bar}&8]");
        String formattedTitle = ColorUtil.formatLegacy(rawTitle
                .replace("{quest}", quest.getDisplayName() != null ? quest.getDisplayName() : quest.getId())
                .replace("{progress}", String.valueOf(progress))
                .replace("{target_amount}", String.valueOf(max))
                .replace("{progress_bar}", progressBar)
        );

        if (bossBar == null) {
            String colorStr = plugin.getConfig().getString("bossbar.color", "YELLOW");
            String styleStr = plugin.getConfig().getString("bossbar.style", "SEGMENTED_10");

            BarColor color;
            try {
                color = BarColor.valueOf(colorStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                color = BarColor.YELLOW;
            }

            BarStyle style;
            try {
                style = BarStyle.valueOf(styleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                style = BarStyle.SEGMENTED_10;
            }

            bossBar = Bukkit.createBossBar(formattedTitle, color, style);
            bossBar.addPlayer(player);
            activeBars.put(uuid, bossBar);
        } else {
            bossBar.setTitle(formattedTitle);
        }

        bossBar.setProgress(ratio);
        bossBar.setVisible(true);
    }

    public void removeBossBar(Player player) {
        BossBar bossBar = activeBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public void clearAll() {
        for (BossBar bar : activeBars.values()) {
            bar.removeAll();
        }
        activeBars.clear();
    }
}