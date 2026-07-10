package pl.materiz66.easyquests.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.materiz66.easyquests.EasyQuestPlugin;

public class PlayerJoinQuitListener implements Listener {
    private final EasyQuestPlugin plugin;

    public PlayerJoinQuitListener(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDatabaseService().loadPlayerProgress(player.getUniqueId()).thenAccept(progress -> {
            var quests = plugin.getQuestManager().getQuestsInOrder();
            if (!quests.isEmpty() && progress.getActiveQuestsProgress().isEmpty() && progress.getCompletedQuests().isEmpty()) {
                progress.getActiveQuestsProgress().put(quests.get(0).getId(), 0);
            }
            plugin.getCacheManager().cacheProgress(player.getUniqueId(), progress);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        var progress = plugin.getCacheManager().getCachedProgress(player.getUniqueId());
        if (progress != null) {
            plugin.getDatabaseService().savePlayerProgress(progress).thenRun(() -> {
                plugin.getCacheManager().invalidate(player.getUniqueId());
            });
        }
    }
}