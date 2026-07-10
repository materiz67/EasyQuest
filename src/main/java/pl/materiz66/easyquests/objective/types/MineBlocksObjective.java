package pl.materiz66.easyquests.objective.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import pl.materiz66.easyquests.objective.ObjectiveType;
import pl.materiz66.easyquests.objective.QuestObjective;

public class MineBlocksObjective extends QuestObjective {

    public MineBlocksObjective(String target, int amount) {
        super(ObjectiveType.MINE_BLOCKS, target, amount);
    }

    @Override
    public boolean checkProgress(Player player, Event event) {
        if (event instanceof BlockBreakEvent breakEvent) {
            Material blockType = breakEvent.getBlock().getType();
            return blockType.name().equalsIgnoreCase(getTarget());
        }
        return false;
    }
}