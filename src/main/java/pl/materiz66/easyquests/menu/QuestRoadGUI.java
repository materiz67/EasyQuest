package pl.materiz66.easyquests.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.config.GUIConfig;
import pl.materiz66.easyquests.config.QuestCategory;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.QuestProgress;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu ścieżki zadań dla wybranej kategorii.
 * Zadania układane są w kształcie "węża" wzdłuż slotów z {@link QuestGUIConstants#ROAD_SLOTS}.
 * Konfiguracja: sekcja gui.road-menu w config.yml.
 */
public class QuestRoadGUI implements InventoryHolder {
    private final Inventory inventory;
    private final EasyQuestPlugin plugin;
    private final String categoryId;

    public QuestRoadGUI(EasyQuestPlugin plugin, Player player, String categoryId) {
        this.plugin = plugin;
        this.categoryId = categoryId;

        GUIConfig gui = plugin.getConfigManager().getGui();

        // Pobierz plain-text nazwę kategorii (bez tagów MiniMessage)
        QuestCategory categoryData = plugin.getConfigManager().getSettings().getCategories().get(categoryId);
        String categoryPlainName = categoryId;
        if (categoryData != null) {
            Component parsed = plugin.getMessageService().parse(player, categoryData.getDisplayName());
            categoryPlainName = PlainTextComponentSerializer.plainText().serialize(parsed);
        }

        // Buduj tytuł, zastępując {category} czystą nazwą
        String rawTitle = gui.getRoadMenuTitle().replace("{category}", categoryPlainName);
        int size = gui.getRoadMenuSize();

        this.inventory = Bukkit.createInventory(this, size,
                plugin.getMessageService().parse(player, rawTitle));
        buildMenu(player);
    }

    private void buildMenu(Player player) {
        GUIConfig gui = plugin.getConfigManager().getGui();
        int size = gui.getRoadMenuSize();

        // Tło – wszystkie sloty oprócz slotów ścieżki i przycisku
        ItemStack filler = QuestCategoryGUI.createFiller(gui.getRoadMenuFillMaterial());
        for (int i = 0; i < size; i++) {
            if (!QuestGUIConstants.ROAD_SLOTS.contains(i)) {
                inventory.setItem(i, filler);
            }
        }

        // Przycisk powrotu
        int backSlot = gui.getRoadBackButtonSlot();
        if (backSlot < size) {
            Material backMat = QuestCategoryGUI.parseMaterial(gui.getRoadBackButtonMaterial(), Material.ARROW);
            ItemStack backButton = new ItemStack(backMat);
            ItemMeta backMeta = backButton.getItemMeta();
            if (backMeta != null) {
                backMeta.displayName(plugin.getMessageService().parse(player, gui.getRoadBackButtonName()));
                backButton.setItemMeta(backMeta);
            }
            inventory.setItem(backSlot, backButton);
        }

        // Postęp gracza
        QuestProgress progress = plugin.getCacheManager().getCachedProgress(player.getUniqueId());
        if (progress == null) return;

        List<Quest> registeredQuests = plugin.getQuestManager().getQuestsByCategory(categoryId);

        // Bezpiecznik: jeśli gracz nie ma żadnego postępu w tej kategorii, aktywuj pierwsze zadanie
        if (!registeredQuests.isEmpty() && plugin.getConfigManager().getSettings().isAutoStartFirstQuest()) {
            boolean hasAnyProgress = registeredQuests.stream().anyMatch(q ->
                    progress.getActiveQuestsProgress().containsKey(q.getId())
                    || progress.getCompletedQuests().containsKey(q.getId()));

            if (!hasAnyProgress) {
                progress.getActiveQuestsProgress().put(registeredQuests.get(0).getId(), 0);
                plugin.getDatabaseService().savePlayerProgress(progress);
            }
        }

        // Renderowanie ikon zadań
        for (int step = 0; step < registeredQuests.size() && step < QuestGUIConstants.ROAD_SLOTS.size(); step++) {
            Quest quest = registeredQuests.get(step);
            int slot = QuestGUIConstants.ROAD_SLOTS.get(step);

            ItemStack icon = buildQuestIcon(player, progress, quest);
            inventory.setItem(slot, icon);
        }
    }

    /**
     * Buduje ikonę zadania w zależności od statusu (zablokowane / aktywne / ukończone).
     */
    private ItemStack buildQuestIcon(Player player, QuestProgress progress, Quest quest) {
        if (progress.getCompletedQuests().containsKey(quest.getId())) {
            return quest.getCompletedIcon().clone();
        }

        if (progress.getActiveQuestsProgress().containsKey(quest.getId())) {
            ItemStack icon = quest.getActiveIcon().clone();
            int currentProg = progress.getActiveQuestsProgress().get(quest.getId());
            ItemMeta meta = icon.getItemMeta();
            if (meta != null && meta.lore() != null) {
                List<Component> updatedLore = new ArrayList<>();
                for (Component line : meta.lore()) {
                    // Zastąp %progress% w lore aktualną wartością
                    String serialized = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                            .legacyAmpersand().serialize(line);
                    String replaced = serialized.replace("%progress%", String.valueOf(currentProg));
                    updatedLore.add(plugin.getMessageService().parse(player, replaced));
                }
                meta.lore(updatedLore);
                icon.setItemMeta(meta);
            }
            return icon;
        }

        // Zadanie zablokowane
        return quest.getLockedIcon().clone();
    }

    public String getCategoryId() { return categoryId; }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}