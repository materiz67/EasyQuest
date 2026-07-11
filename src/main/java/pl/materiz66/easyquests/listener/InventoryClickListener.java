package pl.materiz66.easyquests.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.config.QuestCategory;
import pl.materiz66.easyquests.menu.QuestCategoryGUI;
import pl.materiz66.easyquests.menu.QuestDetailsGUI;
import pl.materiz66.easyquests.menu.QuestGUIConstants;
import pl.materiz66.easyquests.menu.QuestRoadGUI;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.QuestProgress;

import java.util.List;

/**
 * Obsługuje kliknięcia w inventarzach GUI EasyQuests.
 */
public class InventoryClickListener implements Listener {
    private final EasyQuestPlugin plugin;

    public InventoryClickListener(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        // ── 1. Menu Kategorii ──────────────────────────────────────────
        if (holder instanceof QuestCategoryGUI) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            for (QuestCategory cat : plugin.getConfigManager().getSettings().getCategories().values()) {
                if (cat.getSlot() == slot) {
                    player.openInventory(new QuestRoadGUI(plugin, player, cat.getId()).getInventory());
                    playClickSound(player);
                    break;
                }
            }
            return;
        }

        // ── 2. Ścieżka Zadań ──────────────────────────────────────────
        if (holder instanceof QuestRoadGUI roadGUI) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            int backSlot = plugin.getConfigManager().getGui().getRoadBackButtonSlot();

            // Przycisk powrotu do kategorii
            if (slot == backSlot) {
                player.openInventory(new QuestCategoryGUI(plugin, player).getInventory());
                playClickSound(player);
                return;
            }

            // Kliknięcie w zadanie na ścieżce
            List<Integer> roadSlots = QuestGUIConstants.ROAD_SLOTS;
            if (roadSlots.contains(slot)) {
                int questIndex = roadSlots.indexOf(slot);
                List<Quest> categoryQuests = plugin.getQuestManager().getQuestsByCategory(roadGUI.getCategoryId());

                if (questIndex >= 0 && questIndex < categoryQuests.size()) {
                    Quest quest = categoryQuests.get(questIndex);
                    QuestProgress progress = plugin.getCacheManager().getCachedProgress(player.getUniqueId());
                    if (progress == null) return;

                    boolean isCompleted = progress.getCompletedQuests().containsKey(quest.getId());
                    boolean isActive = progress.getActiveQuestsProgress().containsKey(quest.getId());

                    if (isActive || isCompleted) {
                        // Otwórz szczegóły zadania
                        player.openInventory(new QuestDetailsGUI(plugin, player, quest).getInventory());
                        playClickSound(player);
                    } else {
                        // Zadanie zablokowane – powiadom dźwiękiem i wiadomością
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        String msg = plugin.getConfigManager().getMessages().getPrefix()
                                + plugin.getConfigManager().getMessages().getQuestLocked();
                        plugin.getMessageService().sendMessage(player, msg);
                    }
                }
            }
            return;
        }

        // ── 3. Szczegóły Zadania ──────────────────────────────────────
        if (holder instanceof QuestDetailsGUI detailsGUI) {
            event.setCancelled(true);

            int backSlot = plugin.getConfigManager().getGui().getDetailsBackButtonSlot();
            if (slot == backSlot) {
                player.openInventory(new QuestRoadGUI(plugin, player,
                        detailsGUI.getQuest().getCategory()).getInventory());
                playClickSound(player);
            }
        }
    }

    private void playClickSound(Player player) {
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }
}