package pl.materiz66.easyquests.quest;

import org.bukkit.Material;
import java.util.List;

public class Quest {
    private final String id;
    private final String displayName;
    private final List<String> description;
    private final Material material;
    private final QuestObjective objective;
    private final List<String> rewards;
    private final List<String> rewardsDisplay; // Nowe pole do wyświetlania nagród w GUI
    private final QuestCategory category;

    public Quest(String id, String displayName, List<String> description, Material material,
                 QuestObjective objective, List<String> rewards, List<String> rewardsDisplay, QuestCategory category) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.material = material;
        this.objective = objective;
        this.rewards = rewards;
        this.rewardsDisplay = rewardsDisplay;
        this.category = category;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public List<String> getDescription() { return description; }
    public Material getMaterial() { return material; }
    public QuestObjective getObjective() { return objective; }
    public List<String> getRewards() { return rewards; }
    public List<String> getRewardsDisplay() { return rewardsDisplay; }
    public QuestCategory getCategory() { return category; }
}