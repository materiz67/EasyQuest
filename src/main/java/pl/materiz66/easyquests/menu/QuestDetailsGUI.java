package pl.materiz66.easyquests.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.quest.Quest;

import java.util.ArrayList;
import java.util.List;

public class QuestDetailsGUI implements InventoryHolder {
    private final Inventory inventory;
    private final EasyQuestPlugin plugin;
    private final Quest quest;

    public QuestDetailsGUI(EasyQuestPlugin plugin, Player player, Quest quest) {
        this.plugin = plugin;
        this.quest = quest;
        this.inventory = Bukkit.createInventory(this, 27, plugin.getMessageService().parse(player, "<gray>Zadanie: " + quest.getDisplayName()));
        buildMenu(player);
    }

    private void buildMenu(Player player) {
        // Tło szklane
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.displayName(Component.empty());
            border.setItemMeta(borderMeta);
        }
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        // Środek - Opis zadania (Slot 13)
        ItemStack infoBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta bookMeta = infoBook.getItemMeta();
        if (bookMeta != null) {
            bookMeta.displayName(plugin.getMessageService().parse(player, "<gold>Informacje i Cele"));
            List<Component> lore = new ArrayList<>();
            for (String line : quest.getDescription()) {
                lore.add(plugin.getMessageService().parse(player, line));
            }
            lore.add(Component.empty());
            lore.add(plugin.getMessageService().parse(player, "<gray>Typ celu: <yellow>" + quest.getObjective().getType().name()));
            lore.add(plugin.getMessageService().parse(player, "<gray>Wymagany cel: <yellow>" + quest.getObjective().getTarget()));
            lore.add(plugin.getMessageService().parse(player, "<gray>Ilosc: <yellow>" + quest.getObjective().getAmount()));
            bookMeta.lore(lore);
            infoBook.setItemMeta(bookMeta);
        }
        inventory.setItem(13, infoBook);

        // Przycisk powrotu (Slot 18 - Lewy dół)
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(plugin.getMessageService().parse(player, "<red>Powrot do Drogi Zadan"));
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(18, backButton);
    }

    public Quest getQuest() { return quest; }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}