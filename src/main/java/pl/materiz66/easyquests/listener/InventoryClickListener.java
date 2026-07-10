package pl.materiz66.easyquests.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.menu.QuestDetailsGUI;
import pl.materiz66.easyquests.menu.QuestRoadGUI;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.QuestProgress;

import java.util.List;

public class InventoryClickListener implements Listener {
    private final EasyQuestPlugin plugin;

    // Struktura slotów zgodna z układem pionowego wężyka
    private static final List<Integer> ROAD_SLOTS = List.of(
            10, 19, 28, 37,
            38, 29, 20, 11,
            12, 21, 30, 39,
            40, 31, 22, 13,
            14, 23, 32, 41,
            42, 33, 24, 15,
            16, 25, 34, 43
    );

    public InventoryClickListener(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder == null) return;

        // 1. Obsługa interakcji w menu głównym (pionowym wężyku)
        if (holder instanceof QuestRoadGUI) {
            event.setCancelled(true); // Blokada wyjmowania przedmiotów z GUI

            if (!(event.getWhoClicked() instanceof Player player)) return;
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= event.getInventory().getSize()) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            // Sprawdzenie, czy gracz kliknął w slot reprezentujący zadanie
            if (ROAD_SLOTS.contains(slot)) {
                int questIndex = ROAD_SLOTS.indexOf(slot);
                var quests = plugin.getQuestManager().getQuestsInOrder();

                if (questIndex >= 0 && questIndex < quests.size()) {
                    Quest quest = quests.get(questIndex);
                    QuestProgress progress = plugin.getCacheManager().getCachedProgress(player.getUniqueId());

                    if (progress == null) return;

                    boolean isCompleted = progress.getCompletedQuests().containsKey(quest.getId());
                    boolean isActive = progress.getActiveQuestsProgress().containsKey(quest.getId());

                    // Pozwalamy otworzyć podgląd tylko, jeśli zadanie jest aktywne lub ukończone (zablokowane są ukryte)
                    if (isActive || isCompleted) {
                        player.openInventory(new QuestDetailsGUI(plugin, player, quest).getInventory());
                        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    } else {
                        // Odtworzenie dźwięku błędu przy próbie kliknięcia zablokowanego questa
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                }
            }
            return;
        }

        // 2. Obsługa interakcji w menu szczegółowym zadania
        if (holder instanceof QuestDetailsGUI) {
            event.setCancelled(true); // Blokada wyjmowania przedmiotów z GUI

            if (!(event.getWhoClicked() instanceof Player player)) return;
            int slot = event.getRawSlot();

            // Słuchacz dla przycisku powrotu (Slot 18 - Lewy dół)
            if (slot == 18) {
                player.openInventory(new QuestRoadGUI(plugin, player).getInventory());
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
        }
    }
}