package pl.materiz66.easyquests.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.gui.EasyQuestHolder;
import pl.materiz66.easyquests.gui.QuestPathMenu;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.quest.QuestCategory;
import pl.materiz66.easyquests.util.ValidationUtil;

public class MenuClickListener implements Listener {
    private final EasyQuests plugin;

    public MenuClickListener(EasyQuests plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getHolder() instanceof EasyQuestHolder holder) {
            event.setCancelled(true); // Ochrona przed wyciąganiem przedmiotów

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            // Kliknięcie w menu głównym kategorii
            if (holder.getMenuType().equalsIgnoreCase("main_menu")) {
                int slot = event.getRawSlot();
                int size = event.getInventory().getSize();
                int exitSlot = size - 5; // Slot wyjścia obliczony dynamicznie

                // 1. Sprawdzanie, czy gracz kliknął przycisk wyjścia
                if (slot == exitSlot) {
                    player.closeInventory();

                    if (plugin.getConfig().getBoolean("gui.sounds.enabled", true)) {
                        String soundStr = plugin.getConfig().getString("gui.sounds.open-menu", "BLOCK_CHEST_OPEN");
                        Sound sound = ValidationUtil.getSafeSound(soundStr, Sound.BLOCK_CHEST_OPEN);
                        // Odtwarzamy dźwięk o obniżonej tonacji (0.8f) jako efekt zamknięcia
                        player.playSound(player.getLocation(), sound, 1.0f, 0.8f);
                    }
                    return;
                }

                // 2. Obsługa kliknięć w poszczególne kategorie
                for (QuestCategory category : plugin.getQuestManager().getCategories().values()) {
                    if (category.getSlot() == slot) {
                        new QuestPathMenu(plugin, category, player).open();

                        if (plugin.getConfig().getBoolean("gui.sounds.enabled", true)) {
                            String soundStr = plugin.getConfig().getString("gui.sounds.open-menu", "BLOCK_CHEST_OPEN");
                            Sound sound = ValidationUtil.getSafeSound(soundStr, Sound.BLOCK_CHEST_OPEN);
                            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                        }
                        break;
                    }
                }
            }
            // Kliknięcie wewnątrz ścieżki zadań (QuestPathMenu) - AKTYWACJA ZADANIA
            else if (holder.getMenuType().equalsIgnoreCase("quest_path")) {
                int clickedSlot = event.getRawSlot();
                QuestCategory category = (QuestCategory) holder.getAttachedData();

                int questIndex = -1;
                for (int i = 0; i < QuestPathMenu.SNAKE_PATH.length; i++) {
                    if (QuestPathMenu.SNAKE_PATH[i] == clickedSlot) {
                        questIndex = i;
                        break;
                    }
                }

                if (questIndex >= 0 && questIndex < category.getQuests().size()) {
                    Quest quest = category.getQuests().get(questIndex);
                    plugin.handleQuestActivation(player, quest);
                }
            }
        }
    }
}