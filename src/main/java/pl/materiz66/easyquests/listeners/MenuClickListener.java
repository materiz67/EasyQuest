package pl.materiz66.easyquests.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.gui.EasyQuestHolder;
import pl.materiz66.easyquests.gui.MainQuestsMenu;
import pl.materiz66.easyquests.gui.QuestPathData;
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
                int exitSlot = size - 5;

                if (slot == exitSlot) {
                    player.closeInventory();

                    if (plugin.getConfig().getBoolean("gui.sounds.enabled", true)) {
                        String soundStr = plugin.getConfig().getString("gui.sounds.open-menu", "BLOCK_CHEST_OPEN");
                        Sound sound = ValidationUtil.getSafeSound(soundStr, Sound.BLOCK_CHEST_OPEN);
                        player.playSound(player.getLocation(), sound, 1.0f, 0.8f);
                    }
                    return;
                }

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
            // Kliknięcie wewnątrz ścieżki zadań (QuestPathMenu)
            else if (holder.getMenuType().equalsIgnoreCase("quest_path")) {
                int clickedSlot = event.getRawSlot();
                QuestPathData pathData = (QuestPathData) holder.getAttachedData();
                QuestCategory category = pathData.getCategory();
                int page = pathData.getPage();

                int backSlot = 49;     // Przycisk powrotu do menu głównego
                int prevPageSlot = 45; // Przycisk poprzedniej strony
                int nextPageSlot = 53; // Przycisk następnej strony

                // 1. Obsługa przycisku POWRÓT (Cofa do Menu Głównego kategorii)
                if (clickedSlot == backSlot) {
                    new MainQuestsMenu(plugin, player).open();

                    if (plugin.getConfig().getBoolean("gui.sounds.enabled", true)) {
                        String soundStr = plugin.getConfig().getString("gui.sounds.open-menu", "BLOCK_CHEST_OPEN");
                        Sound sound = ValidationUtil.getSafeSound(soundStr, Sound.BLOCK_CHEST_OPEN);
                        // Zwracamy wyższą tonację (1.1f) sygnalizującą powrót do góry
                        player.playSound(player.getLocation(), sound, 1.0f, 1.1f);
                    }
                    return;
                }

                // 2. Obsługa przycisku poprzedniej strony
                if (clickedSlot == prevPageSlot && page > 0) {
                    new QuestPathMenu(plugin, category, player, page - 1).open();

                    if (plugin.getConfig().getBoolean("gui.sounds.enabled", true)) {
                        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                    }
                    return;
                }

                // 3. Obsługa przycisku następnej strony
                int startIndex = (page + 1) * 28;
                if (clickedSlot == nextPageSlot && category.getQuests().size() > startIndex) {
                    new QuestPathMenu(plugin, category, player, page + 1).open();

                    if (plugin.getConfig().getBoolean("gui.sounds.enabled", true)) {
                        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
                    }
                    return;
                }

                // 4. Obsługa kliknięć w misje na ścieżce (uwzględnia offset wyznaczonej strony)
                int questIndexInSnake = -1;
                for (int i = 0; i < QuestPathMenu.SNAKE_PATH.length; i++) {
                    if (QuestPathMenu.SNAKE_PATH[i] == clickedSlot) {
                        questIndexInSnake = i;
                        break;
                    }
                }

                if (questIndexInSnake >= 0) {
                    int globalQuestIndex = (page * 28) + questIndexInSnake;
                    if (globalQuestIndex < category.getQuests().size()) {
                        Quest quest = category.getQuests().get(globalQuestIndex);
                        plugin.handleQuestActivation(player, quest);
                    }
                }
            }
        }
    }
}