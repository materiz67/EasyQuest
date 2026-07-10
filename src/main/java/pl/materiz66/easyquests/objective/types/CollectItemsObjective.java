package pl.materiz66.easyquests.objective.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPickupItemEvent;
import pl.materiz66.easyquests.objective.ObjectiveType;
import pl.materiz66.easyquests.objective.QuestObjective;

public class CollectItemsObjective extends QuestObjective {

    public CollectItemsObjective(String target, int amount) {
        super(ObjectiveType.COLLECT_ITEMS, target, amount);
    }

    @Override
    public boolean checkProgress(Player player, Event event) {
        if (event instanceof EntityPickupItemEvent pickupEvent && pickupEvent.getEntity() instanceof Player p) {
            if (p.equals(player)) {
                Material itemType = pickupEvent.getItem().getItemStack().getType();
                return itemType.name().equalsIgnoreCase(getTarget());
            }
        }
        return false;
    }
}