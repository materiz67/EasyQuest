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
import pl.materiz66.easyquests.user.QuestProgress;

import java.util.ArrayList;
import java.util.List;

public class QuestRoadGUI implements InventoryHolder {
    private final Inventory inventory;
    private final EasyQuestPlugin plugin;

    // Struktura indeksów dla pionowego wężyka
    private static final List<Integer> ROAD_SLOTS = List.of(
            10, 19, 28, 37, // Kolumna 1 (W dół)
            38, 29, 20, 11, // Kolumna 2 (W górę)
            12, 21, 30, 39, // Kolumna 3 (W dół)
            40, 31, 22, 13, // Kolumna 4 (W górę)
            14, 23, 32, 41, // Kolumna 5 (W dół)
            42, 33, 24, 15, // Kolumna 6 (W górę)
            16, 25, 34, 43  // Kolumna 7 (W dół)
    );

    public QuestRoadGUI(EasyQuestPlugin plugin, Player player) {
        this.plugin = plugin;
        String title = plugin.getConfigManager().getSettings().getMenuTitle();
        this.inventory = Bukkit.createInventory(this, 54, plugin.getMessageService().parse(player, title));
        buildMenu(player);
    }

    private void buildMenu(Player player) {
        // Wypełnienie tła
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.displayName(Component.empty());
            border.setItemMeta(borderMeta);
        }

        for (int i = 0; i < 54; i++) {
            if (!ROAD_SLOTS.contains(i)) {
                inventory.setItem(i, border);
            }
        }

        QuestProgress progress = plugin.getCacheManager().getCachedProgress(player.getUniqueId());
        if (progress == null) return;

        var registeredQuests = plugin.getQuestManager().getQuestsInOrder();

        for (int step = 0; step < registeredQuests.size() && step < ROAD_SLOTS.size(); step++) {
            Quest quest = registeredQuests.get(step);
            int slot = ROAD_SLOTS.get(step);

            ItemStack icon;
            if (progress.getCompletedQuests().containsKey(quest.getId())) {
                icon = quest.getCompletedIcon().clone();
            } else if (progress.getActiveQuestsProgress().containsKey(quest.getId())) {
                icon = quest.getActiveIcon().clone();
                // Dynamiczne parsowanie zmiennej %progress% w opisie
                int currentProg = progress.getActiveQuestsProgress().get(quest.getId());
                ItemMeta meta = icon.getItemMeta();
                if (meta != null && meta.lore() != null) {
                    List<Component> updatedLore = new ArrayList<>();
                    for (Component line : meta.lore()) {
                        // Szybka zamiana tagów
                        String serialized = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().serialize(line);
                        String replaced = serialized.replace("%progress%", String.valueOf(currentProg));
                        updatedLore.add(plugin.getMessageService().parse(player, replaced));
                    }
                    meta.lore(updatedLore);
                    icon.setItemMeta(meta);
                }
            } else {
                icon = quest.getLockedIcon().clone();
            }

            inventory.setItem(slot, icon);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}