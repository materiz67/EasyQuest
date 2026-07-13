package pl.materiz66.easyquests.user;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private final Set<String> completedQuests = new HashSet<>();
    private final Map<String, Integer> progress = new HashMap<>();
    private String activeQuestId; // ID jedynego aktywnego zadania (maksymalnie jedno w danej chwili)

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() { return uuid; }
    public Set<String> getCompletedQuests() { return completedQuests; }
    public Map<String, Integer> getProgress() { return progress; }

    public boolean isCompleted(String questId) {
        return completedQuests.contains(questId);
    }

    public void completeQuest(String questId) {
        completedQuests.add(questId);
        progress.remove(questId);
        if (questId.equals(activeQuestId)) {
            activeQuestId = null; // Czyszczenie aktywności po ukończeniu
        }
    }

    public int getQuestProgress(String questId) {
        return progress.getOrDefault(questId, 0);
    }

    public void setQuestProgress(String questId, int amount) {
        progress.put(questId, amount);
    }

    public String getActiveQuestId() { return activeQuestId; }
    public void setActiveQuestId(String activeQuestId) { this.activeQuestId = activeQuestId; }
}