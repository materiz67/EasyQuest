package pl.materiz66.easyquests.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.config.GUIConfig;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.QuestProgress;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu szczegółów wybranego zadania.
 * Konfiguracja: sekcja gui.details-menu w config.yml.
 */
public class QuestDetailsGUI implements InventoryHolder {
    private final Inventory inventory;
    private final EasyQuestPlugin plugin;
    private final Quest quest;

    public QuestDetailsGUI(EasyQuestPlugin plugin, Player player, Quest quest) {
        this.plugin = plugin;
        this.quest = quest;

        GUIConfig gui = plugin.getConfigManager().getGui();

        // Pobierz plain-text nazwę zadania
        Component parsedName = plugin.getMessageService().parse(player, quest.getDisplayName());
        String questPlainName = PlainTextComponentSerializer.plainText().serialize(parsedName);

        String rawTitle = gui.getDetailsMenuTitle().replace("{quest}", questPlainName);
        int size = gui.getDetailsMenuSize();

        this.inventory = Bukkit.createInventory(this, size,
                plugin.getMessageService().parse(player, rawTitle));
        buildMenu(player);
    }

    private void buildMenu(Player player) {
        GUIConfig gui = plugin.getConfigManager().getGui();
        int size = gui.getDetailsMenuSize();

        // Wypełnienie tła
        ItemStack filler = QuestCategoryGUI.createFiller(gui.getDetailsMenuFillMaterial());
        for (int i = 0; i < size; i++) {
            inventory.setItem(i, filler);
        }

        // Aktualny postęp gracza w tym zadaniu
        QuestProgress progress = plugin.getCacheManager().getCachedProgress(player.getUniqueId());
        int currentProgress = 0;
        boolean isCompleted = false;
        if (progress != null) {
            isCompleted = progress.getCompletedQuests().containsKey(quest.getId());
            currentProgress = progress.getActiveQuestsProgress().getOrDefault(quest.getId(), 0);
        }

        // --- Przedmiot informacyjny ---
        Material infoMat = QuestCategoryGUI.parseMaterial(gui.getDetailsInfoItemMaterial(), Material.WRITTEN_BOOK);
        ItemStack infoBook = new ItemStack(infoMat);
        ItemMeta bookMeta = infoBook.getItemMeta();
        if (bookMeta != null) {
            bookMeta.displayName(plugin.getMessageService().parse(player, gui.getDetailsInfoItemName()));

            List<Component> lore = new ArrayList<>();
            // Opis zadania
            for (String line : quest.getDescription()) {
                lore.add(plugin.getMessageService().parse(player, line));
            }
            lore.add(Component.empty());

            // Typ i cel
            lore.add(plugin.getMessageService().parse(player,
                    "<gray>Typ celu: <yellow>" + quest.getObjective().getType().name()));
            lore.add(plugin.getMessageService().parse(player,
                    "<gray>Wymagany cel: <yellow>" + quest.getObjective().getTarget()));
            lore.add(Component.empty());

            // Postęp
            if (isCompleted) {
                lore.add(plugin.getMessageService().parse(player, "<green>✔ Zadanie ukończone!"));
            } else {
                lore.add(plugin.getMessageService().parse(player,
                        "<gray>Postęp: <gold>" + currentProgress + "<gray>/<gold>" + quest.getObjective().getAmount()));
            }
            lore.add(Component.empty());

            // Nagrody
            lore.add(plugin.getMessageService().parse(player, "<yellow>✦ Nagrody:"));
            for (String reward : quest.getRewards()) {
                lore.add(plugin.getMessageService().parse(player, "<gray>  • " + reward));
            }

            bookMeta.lore(lore);
            infoBook.setItemMeta(bookMeta);
        }

        int infoSlot = gui.getDetailsInfoItemSlot();
        if (infoSlot >= 0 && infoSlot < size) {
            inventory.setItem(infoSlot, infoBook);
        }

        // --- Przycisk powrotu ---
        int backSlot = gui.getDetailsBackButtonSlot();
        if (backSlot >= 0 && backSlot < size) {
            Material backMat = QuestCategoryGUI.parseMaterial(gui.getDetailsBackButtonMaterial(), Material.ARROW);
            ItemStack backButton = new ItemStack(backMat);
            ItemMeta backMeta = backButton.getItemMeta();
            if (backMeta != null) {
                backMeta.displayName(plugin.getMessageService().parse(player, gui.getDetailsBackButtonName()));
                backButton.setItemMeta(backMeta);
            }
            inventory.setItem(backSlot, backButton);
        }
    }

    public Quest getQuest() { return quest; }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}