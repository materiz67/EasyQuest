package pl.materiz66.easyquests.quest;

public class QuestObjective {
    private final String type;
    private final String target;
    private final int amount;

    public QuestObjective(String type, String target, int amount) {
        this.type = type;
        this.target = target;
        this.amount = amount;
    }

    public String getType() { return type; }
    public String getTarget() { return target; }
    public int getAmount() { return amount; }
}