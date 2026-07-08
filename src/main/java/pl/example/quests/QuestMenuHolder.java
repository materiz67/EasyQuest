package pl.example.quests;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class QuestMenuHolder implements InventoryHolder {
    private final String viewType;
    private final String selectedCategoryId;

    public QuestMenuHolder(String viewType, String selectedCategoryId) {
        this.viewType = viewType;
        this.selectedCategoryId = selectedCategoryId;
    }

    public String getViewType() { return viewType; }
    public String getSelectedCategoryId() { return selectedCategoryId; }

    @Override
    public Inventory getInventory() { return null; }
}