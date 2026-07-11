package pl.materiz66.easyquests.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.api.event.QuestCompleteEvent;
import pl.materiz66.easyquests.api.event.QuestStartEvent;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.QuestProgress;

import java.util.ArrayList;
import java.util.List;

/**
 * Nasłuchuje zdarzeń gry i aktualizuje postęp zadań gracza.
 * Obsługuje typy celów: MINE_BLOCKS, KILL_MOBS, COLLECT_ITEMS, CRAFT_ITEMS.
 */
public class QuestObjectiveListener implements Listener {
    private final EasyQuestPlugin plugin;

    public QuestObjectiveListener(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        handleProgress(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            handleProgress(killer, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            handleProgress(player, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            handleProgress(player, event);
        }
    }

    private void handleProgress(Player player, Event event) {
        QuestProgress progress = plugin.getCacheManager().getCachedProgress(player.getUniqueId());
        if (progress == null) return;

        // Bezpiecznik: aktywuj pierwsze zadanie każdej kategorii jeśli gracz nie ma żadnego postępu
        if (plugin.getConfigManager().getSettings().isAutoStartFirstQuest()) {
            for (String categoryId : plugin.getConfigManager().getSettings().getCategories().keySet()) {
                List<Quest> categoryQuests = plugin.getQuestManager().getQuestsByCategory(categoryId);
                if (!categoryQuests.isEmpty()) {
                    boolean hasAnyProgress = categoryQuests.stream().anyMatch(q ->
                            progress.getActiveQuestsProgress().containsKey(q.getId())
                            || progress.getCompletedQuests().containsKey(q.getId()));
                    if (!hasAnyProgress) {
                        progress.getActiveQuestsProgress().put(categoryQuests.get(0).getId(), 0);
                    }
                }
            }
        }

        // Iteruj po KOPII kluczy, aby uniknąć ConcurrentModificationException
        List<String> activeQuestIds = new ArrayList<>(progress.getActiveQuestsProgress().keySet());

        for (String questId : activeQuestIds) {
            Quest quest = plugin.getQuestManager().getQuestById(questId);
            if (quest == null) continue;

            if (quest.getObjective().checkProgress(player, event)) {
                int currentProg = progress.getActiveQuestsProgress().getOrDefault(questId, 0) + 1;
                int target = quest.getObjective().getAmount();

                if (currentProg >= target) {
                    // Zadanie ukończone!
                    progress.getActiveQuestsProgress().remove(questId);
                    progress.getCompletedQuests().put(questId, true);

                    Bukkit.getPluginManager().callEvent(new QuestCompleteEvent(player, quest));

                    String completedMsg = plugin.getConfigManager().getMessages().getPrefix()
                            + plugin.getConfigManager().getMessages().getQuestCompleted()
                                .replace("{quest}", quest.getDisplayName());
                    plugin.getMessageService().sendMessage(player, completedMsg);

                    // Wykonaj komendy nagród
                    for (String cmd : quest.getRewards()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                cmd.replace("%player%", player.getName()));
                    }

                    // Aktywuj następne zadanie w tej kategorii
                    activateNextQuest(player, progress, quest);

                } else {
                    // Aktualizuj postęp
                    progress.getActiveQuestsProgress().put(questId, currentProg);

                    // ActionBar (jeśli włączony w configu)
                    if (plugin.getConfigManager().getSettings().isShowActionBar()) {
                        String format = plugin.getConfigManager().getSettings().getActionBarProgressFormat()
                                .replace("%quest%", quest.getDisplayName())
                                .replace("%progress%", String.valueOf(currentProg))
                                .replace("%target%", String.valueOf(target));
                        plugin.getMessageService().sendActionBar(player, format);
                    }
                }

                // Zapis asynchroniczny po każdej zmianie postępu
                plugin.getDatabaseService().savePlayerProgress(progress);
                break; // Jeden krok na jedno zdarzenie
            }
        }
    }

    private void activateNextQuest(Player player, QuestProgress progress, Quest currentQuest) {
        List<Quest> quests = plugin.getQuestManager().getQuestsByCategory(currentQuest.getCategory());
        for (int i = 0; i < quests.size(); i++) {
            if (quests.get(i).getId().equals(currentQuest.getId()) && i + 1 < quests.size()) {
                Quest nextQuest = quests.get(i + 1);
                progress.getActiveQuestsProgress().put(nextQuest.getId(), 0);

                Bukkit.getPluginManager().callEvent(new QuestStartEvent(player, nextQuest));

                String startedMsg = plugin.getConfigManager().getMessages().getPrefix()
                        + plugin.getConfigManager().getMessages().getQuestStarted()
                            .replace("{quest}", nextQuest.getDisplayName());
                plugin.getMessageService().sendMessage(player, startedMsg);
                break;
            }
        }
    }
}