package pl.materiz66.easyquests.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.quest.Quest;

public class PlayerJoinQuitListener implements Listener {
    private final EasyQuestPlugin plugin;

    public PlayerJoinQuitListener(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getDatabaseService().loadPlayerProgress(player.getUniqueId()).thenAccept(progress -> {

            // Sprawdzanie i aktywacja pierwszego zadania dla każdej kategorii z osobna (jeśli gracz nie ma w niej postępu)
            for (String categoryId : plugin.getConfigManager().getSettings().getCategories().keySet()) {
                var categoryQuests = plugin.getQuestManager().getQuestsByCategory(categoryId);
                if (!categoryQuests.isEmpty()) {
                    boolean hasProgress = false;
                    for (Quest q : categoryQuests) {
                        if (progress.getActiveQuestsProgress().containsKey(q.getId()) || progress.getCompletedQuests().containsKey(q.getId())) {
                            hasProgress = true;
                            break;
                        }
                    }
                    if (!hasProgress) {
                        progress.getActiveQuestsProgress().put(categoryQuests.get(0).getId(), 0);
                    }
                }
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