package pl.materiz66.easyquests.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.quest.QuestCategory;
import pl.materiz66.easyquests.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class MainQuestsMenu {
    private final EasyQuests plugin;
    private final Player player;

    public MainQuestsMenu(EasyQuests plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        String title = ColorUtil.formatLegacy(plugin.getConfig().getString("gui.title", "&6&lZadania"));
        int size = plugin.getConfig().getInt("gui.size", 27);

        // Bezpieczny EasyQuestHolder chroniący przed duplikacją
        EasyQuestHolder holder = new EasyQuestHolder("main_menu", null);
        Inventory inv = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inv);

        // Opcjonalne wypełnienie pustych slotów szarym szkłem
        if (plugin.getConfig().getBoolean("gui.fill-empty-slots", true)) {
            String fillerMaterialStr = plugin.getConfig().getString("gui.filler-material", "GRAY_STAINED_GLASS_PANE");
            Material fillerMat = Material.matchMaterial(fillerMaterialStr);
            if (fillerMat == null) fillerMat = Material.GRAY_STAINED_GLASS_PANE;

            ItemStack filler = new ItemStack(fillerMat);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                try {
                    fillerMeta.displayName(ColorUtil.format(" "));
                } catch (NoSuchMethodError e) {
                    fillerMeta.setDisplayName(" ");
                }
                filler.setItemMeta(fillerMeta);
            }

            for (int i = 0; i < size; i++) {
                inv.setItem(i, filler);
            }
        }

        // Układanie załadowanych dynamicznie kategorii
        for (QuestCategory category : plugin.getQuestManager().getCategories().values()) {
            ItemStack item = new ItemStack(category.getMaterial());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                try {
                    meta.displayName(ColorUtil.format(category.getDisplayName()));
                    List<net.kyori.adventure.text.Component> formattedLore = new ArrayList<>();
                    for (String line : category.getLore()) {
                        formattedLore.add(ColorUtil.format(line));
                    }
                    meta.lore(formattedLore);
                } catch (NoSuchMethodError e) {
                    meta.setDisplayName(ColorUtil.formatLegacy(category.getDisplayName()));
                    List<String> formattedLoreLegacy = new ArrayList<>();
                    for (String line : category.getLore()) {
                        formattedLoreLegacy.add(ColorUtil.formatLegacy(line));
                    }
                    meta.setLore(formattedLoreLegacy);
                }
                item.setItemMeta(meta);
            }

            if (category.getSlot() >= 0 && category.getSlot() < size) {
                inv.setItem(category.getSlot(), item);
            }
        }

        // ======================================================
        //     DODANO: PRZYCISK WYJŚCIA (ZAMKNIĘCIA MENU)
        // ======================================================
        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exitItem.getItemMeta();
        if (exitMeta != null) {
            try {
                exitMeta.displayName(ColorUtil.format("&#ff3333&lᴡʏᴊsᴄɪᴇ"));
                exitMeta.lore(List.of(ColorUtil.format("<gray>▶ Kliknij, aby zamknąć to menu.")));
            } catch (NoSuchMethodError e) {
                exitMeta.setDisplayName(ColorUtil.formatLegacy("&#ff3333&lᴡʏᴊsᴄɪᴇ"));
                exitMeta.setLore(List.of(ColorUtil.formatLegacy("<gray>▶ Kliknij, aby zamknąć to menu.")));
            }
            exitItem.setItemMeta(exitMeta);
        }

        // Dynamiczne wyliczenie środkowego slotu w ostatnim rzędzie (np. 22 dla rozmiaru 27)
        int exitSlot = size - 5;
        if (exitSlot >= 0 && exitSlot < size) {
            inv.setItem(exitSlot, exitItem);
        }

        player.openInventory(inv);
    }
}