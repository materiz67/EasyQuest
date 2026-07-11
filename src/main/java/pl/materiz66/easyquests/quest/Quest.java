package pl.materiz66.easyquests.quest;

import org.bukkit.inventory.ItemStack;
import pl.materiz66.easyquests.objective.QuestObjective;

import java.util.List;

public class Quest {
    private final String id;
    private final String category; // Przypisanie zadania do konkretnej kategorii
    private final String displayName;
    private final List<String> description;
    private final int order;

    private final ItemStack lockedIcon;
    private final ItemStack activeIcon;
    private final ItemStack completedIcon;

    private final QuestObjective objective;
    private final List<String> rewards;

    public Quest(String id, String category, String displayName, List<String> description, int order,
                 ItemStack lockedIcon, ItemStack activeIcon, ItemStack completedIcon,
                 QuestObjective objective, List<String> rewards) {
        this.id = id;
        this.category = category;
        this.displayName = displayName;
        this.description = description;
        this.order = order;
        this.lockedIcon = lockedIcon;
        this.activeIcon = activeIcon;
        this.completedIcon = completedIcon;
        this.objective = objective;
        this.rewards = rewards;
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getDisplayName() { return displayName; }
    public List<String> getDescription() { return description; }
    public int getOrder() { return order; }
    public ItemStack getLockedIcon() { return lockedIcon; }
    public ItemStack getActiveIcon() { return activeIcon; }
    public ItemStack getCompletedIcon() { return completedIcon; }
    public QuestObjective getObjective() { return objective; }
    public List<String> getRewards() { return rewards; }
}