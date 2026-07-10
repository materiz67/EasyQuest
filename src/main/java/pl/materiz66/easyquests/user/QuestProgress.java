package pl.materiz66.easyquests.user;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestProgress {
    private final UUID playerUuid;
    private final Map<String, Integer> activeQuestsProgress; // QuestID -> Krok/Postęp (np. "zabite_moby" -> 5)
    private final Map<String, Boolean> completedQuests;     // QuestID -> Status ukończenia (true/false)

    public QuestProgress(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.activeQuestsProgress = new HashMap<>();
        this.completedQuests = new HashMap<>();
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Map<String, Integer> getActiveQuestsProgress() {
        return activeQuestsProgress;
    }

    public Map<String, Boolean> getCompletedQuests() {
        return completedQuests;
    }
}