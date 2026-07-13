package pl.materiz66.easyquests.quest;

import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;

public class QuestCategory {
    private final String id;
    private final String displayName;
    private final Material material;
    private final int slot;
    private final List<String> lore;
    private final List<Quest> quests;

    public QuestCategory(String id, String displayName, Material material, int slot, List<String> lore) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.slot = slot;
        this.lore = lore;
        this.quests = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
    public List<String> getLore() { return lore; }
    public List<Quest> getQuests() { return quests; }

    public void addQuest(Quest quest) {
        this.quests.add(quest);
    }
}