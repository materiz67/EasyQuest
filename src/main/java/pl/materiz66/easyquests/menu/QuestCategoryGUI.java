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
import pl.materiz66.easyquests.config.GUIConfig;
import pl.materiz66.easyquests.config.QuestCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu wyboru kategorii zadań.
 * Konfiguracja: sekcja gui.category-menu w config.yml.
 */
public class QuestCategoryGUI implements InventoryHolder {
    private final Inventory inventory;
    private final EasyQuestPlugin plugin;

    public QuestCategoryGUI(EasyQuestPlugin plugin, Player player) {
        this.plugin = plugin;
        GUIConfig gui = plugin.getConfigManager().getGui();

        String title = gui.getCategoryMenuTitle();
        int size = gui.getCategoryMenuSize();

        this.inventory = Bukkit.createInventory(this, size,
                plugin.getMessageService().parse(player, title));
        buildMenu(player);
    }

    private void buildMenu(Player player) {
        GUIConfig gui = plugin.getConfigManager().getGui();

        // Wypełnienie tła
        ItemStack filler = createFiller(gui.getCategoryMenuFillMaterial());
        int size = gui.getCategoryMenuSize();
        for (int i = 0; i < size; i++) {
            inventory.setItem(i, filler);
        }

        // Ikony kategorii
        for (QuestCategory category : plugin.getConfigManager().getSettings().getCategories().values()) {
            Material mat = parseMaterial(category.getMaterial(), Material.BOOK);

            ItemStack icon = new ItemStack(mat);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.displayName(plugin.getMessageService().parse(player, category.getDisplayName()));

                List<Component> lore = new ArrayList<>();
                for (String line : category.getLore()) {
                    lore.add(plugin.getMessageService().parse(player, line));
                }
                meta.lore(lore);
                icon.setItemMeta(meta);
            }

            int slot = category.getSlot();
            if (slot >= 0 && slot < size) {
                inventory.setItem(slot, icon);
            }
        }
    }

    /**
     * Tworzy przedmiot-wypełnienie tła (szare szkło bez nazwy).
     */
    public static ItemStack createFiller(String materialName) {
        Material mat = parseMaterial(materialName, Material.GRAY_STAINED_GLASS_PANE);
        ItemStack filler = new ItemStack(mat);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            filler.setItemMeta(meta);
        }
        return filler;
    }

    /**
     * Bezpieczne parsowanie materiału – fallback do domyślnego.
     */
    public static Material parseMaterial(String name, Material fallback) {
        if (name == null || name.isBlank()) return fallback;
        try {
            Material mat = Material.getMaterial(name.toUpperCase());
            return mat != null ? mat : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}