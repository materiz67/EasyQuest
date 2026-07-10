package pl.materiz66.easyquests.objective.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import pl.materiz66.easyquests.objective.ObjectiveType;
import pl.materiz66.easyquests.objective.QuestObjective;

public class KillMobsObjective extends QuestObjective {

    public KillMobsObjective(String target, int amount) {
        super(ObjectiveType.KILL_MOBS, target, amount);
    }

    @Override
    public boolean checkProgress(Player player, Event event) {
        if (event instanceof EntityDeathEvent deathEvent) {
            EntityType entityType = deathEvent.getEntityType();
            return entityType.name().equalsIgnoreCase(getTarget()) && player.equals(deathEvent.getEntity().getKiller());
        }
        return false;
    }
}