package pl.materiz66.easyquests.config;

import java.util.List;

public class QuestCategory {
    private final String id;
    private final String displayName;
    private final String material;
    private final int slot;
    private final List<String> lore;

    public QuestCategory(String id, String displayName, String material, int slot, List<String> lore) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.slot = slot;
        this.lore = lore;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getMaterial() { return material; }
    public int getSlot() { return slot; }
    public List<String> getLore() { return lore; }
}