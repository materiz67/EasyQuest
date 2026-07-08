package pl.example.quests;

import java.util.List;

public class QuestModel {

    public enum QuestType {
        BREAK, PLACE, KILL, FISH
    }

    public static class QuestCategory {
        private final String id;
        private final String name;
        private final String iconMaterial;
        private final List<String> lore;
        private final List<Quest> quests;

        public QuestCategory(String id, String name, String iconMaterial, List<String> lore, List<Quest> quests) {
            this.id = id;
            this.name = name;
            this.iconMaterial = iconMaterial;
            this.lore = lore;
            this.quests = quests;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getIconMaterial() { return iconMaterial; }
        public List<String> getLore() { return lore; }
        public List<Quest> getQuests() { return quests; }
    }

    public static class Quest {
        private final String id;
        private final String name;
        private final String description;
        private final QuestType type;
        private final String target;
        private final int requiredAmount;
        private final double moneyReward;
        private final List<String> rewardCommands;
        private final String rewardDescription;

        public Quest(String id, String name, String description, QuestType type, String target, int requiredAmount, double moneyReward, List<String> rewardCommands, String rewardDescription) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.type = type;
            this.target = target;
            this.requiredAmount = requiredAmount;
            this.moneyReward = moneyReward;
            this.rewardCommands = rewardCommands;
            this.rewardDescription = rewardDescription;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public QuestType getType() { return type; }
        public String getTarget() { return target; }
        public int getRequiredAmount() { return requiredAmount; }
        public double getMoneyReward() { return moneyReward; }
        public List<String> getRewardCommands() { return rewardCommands; }
        public String getRewardDescription() { return rewardDescription; }
    }
}