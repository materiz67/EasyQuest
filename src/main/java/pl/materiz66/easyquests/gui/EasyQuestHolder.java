package pl.materiz66.easyquests.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class EasyQuestHolder implements InventoryHolder {
    private Inventory inventory;
    private final String menuType;
    private final Object attachedData;

    public EasyQuestHolder(String menuType, Object attachedData) {
        this.menuType = menuType;
        this.attachedData = attachedData;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public String getMenuType() { return menuType; }
    public Object getAttachedData() { return attachedData; }
}