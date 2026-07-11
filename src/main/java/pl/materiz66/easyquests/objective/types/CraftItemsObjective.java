package pl.materiz66.easyquests.objective.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.CraftItemEvent;
import pl.materiz66.easyquests.objective.ObjectiveType;
import pl.materiz66.easyquests.objective.QuestObjective;

/**
 * Cel zadania: wytworz podany przedmiot na stole rzemieślniczym.
 * Słucha zdarzenia {@link CraftItemEvent}.
 */
public class CraftItemsObjective extends QuestObjective {

    public CraftItemsObjective(String target, int amount) {
        super(ObjectiveType.CRAFT_ITEMS, target, amount);
    }

    @Override
    public boolean checkProgress(Player player, Event event) {
        if (event instanceof CraftItemEvent craftEvent) {
            // Upewnienie się, że to ten sam gracz craftuje
            if (!(craftEvent.getWhoClicked() instanceof Player craftingPlayer)) return false;
            if (!craftingPlayer.equals(player)) return false;

            // Weryfikacja materiału craftowanego przedmiotu
            Material resultType = craftEvent.getRecipe().getResult().getType();
            return resultType.name().equalsIgnoreCase(getTarget());
        }
        return false;
    }
}
