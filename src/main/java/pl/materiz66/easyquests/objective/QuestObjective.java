package pl.materiz66.easyquests.objective;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class QuestObjective {
    private final ObjectiveType type;
    private final String target;
    private final int amount;

    public QuestObjective(ObjectiveType type, String target, int amount) {
        this.type = type;
        this.target = target;
        this.amount = amount;
    }

    public ObjectiveType getType() { return type; }
    public String getTarget() { return target; }
    public int getAmount() { return amount; }

    /**
     * Weryfikuje, czy zaistniałe zdarzenie w grze przyczynia się do postępu danego celu.
     */
    public abstract boolean checkProgress(Player player, Event event);
}