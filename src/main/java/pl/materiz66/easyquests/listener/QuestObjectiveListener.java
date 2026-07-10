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
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.api.event.QuestCompleteEvent;
import pl.materiz66.easyquests.api.event.QuestStartEvent;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.QuestProgress;

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

    /**
     * Główna metoda przetwarzająca postępy aktywnego celu u gracza.
     */
    private void handleProgress(Player player, Event event) {
        QuestProgress progress = plugin.getCacheManager().getCachedProgress(player.getUniqueId());
        if (progress == null) return;

        // Iteracja po aktywnych zadaniach gracza
        for (String questId : progress.getActiveQuestsProgress().keySet()) {
            Quest quest = plugin.getQuestManager().getQuestById(questId);
            if (quest == null) continue;

            // Sprawdzenie, czy zaistniałe zdarzenie w świecie gry pasuje do typu i celu zadania
            if (quest.getObjective().checkProgress(player, event)) {
                int currentProg = progress.getActiveQuestsProgress().get(questId) + 1;
                int target = quest.getObjective().getAmount();

                if (currentProg >= target) {
                    // 1. Oznaczenie zadania jako ukończone w pamięci podręcznej
                    progress.getActiveQuestsProgress().remove(questId);
                    progress.getCompletedQuests().put(questId, true);

                    // 2. Wywołanie zdarzenia ukończenia zadania w publicznym API dla zewnętrznych pluginów
                    Bukkit.getPluginManager().callEvent(new QuestCompleteEvent(player, quest));

                    // 3. Wysłanie komunikatu gratulacyjnego na czat gracza
                    String message = plugin.getConfigManager().getMessages().getPrefix() +
                            plugin.getConfigManager().getMessages().getQuestCompleted().replace("%quest%", quest.getDisplayName());
                    plugin.getMessageService().sendMessage(player, message);

                    // 4. Wykonanie nagród z poziomu konsoli serwera
                    for (String cmd : quest.getRewards()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                    }

                    // 5. Aktywacja kolejnego zadania w hierarchii (ścieżka liniowa)
                    activateNextQuest(player, progress, quest);
                } else {
                    // Zwykła aktualizacja postępu zadania
                    progress.getActiveQuestsProgress().put(questId, currentProg);

                    // Wyświetlenie dynamicznego paska ActionBar nad ekwipunkiem gracza
                    String format = plugin.getConfigManager().getSettings().getActionBarProgressFormat()
                            .replace("%quest%", quest.getDisplayName())
                            .replace("%progress%", String.valueOf(currentProg))
                            .replace("%target%", String.valueOf(target));
                    plugin.getMessageService().sendActionBar(player, format);
                }

                // Asynchroniczny zapis zmodyfikowanego stanu danych gracza do bazy danych
                plugin.getDatabaseService().savePlayerProgress(progress);
                break;
            }
        }
    }

    /**
     * Aktywuje kolejne zadanie po ukończeniu bieżącego.
     */
    private void activateNextQuest(Player player, QuestProgress progress, Quest currentQuest) {
        var quests = plugin.getQuestManager().getQuestsInOrder();
        for (int i = 0; i < quests.size(); i++) {
            if (quests.get(i).getId().equals(currentQuest.getId())) {
                if (i + 1 < quests.size()) {
                    Quest nextQuest = quests.get(i + 1);
                    progress.getActiveQuestsProgress().put(nextQuest.getId(), 0);

                    // Wywołanie zdarzenia rozpoczęcia zadania w publicznym API
                    Bukkit.getPluginManager().callEvent(new QuestStartEvent(player, nextQuest));

                    // Poinformowanie gracza o rozpoczęciu nowego questa
                    String message = plugin.getConfigManager().getMessages().getPrefix() +
                            plugin.getConfigManager().getMessages().getQuestStarted().replace("%quest%", nextQuest.getDisplayName());
                    plugin.getMessageService().sendMessage(player, message);
                }
                break;
            }
        }
    }
}