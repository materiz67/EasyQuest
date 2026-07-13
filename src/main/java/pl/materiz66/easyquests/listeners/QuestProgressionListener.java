package pl.materiz66.easyquests.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.PlayerData;

public class QuestProgressionListener implements Listener {
    private final EasyQuests plugin;

    public QuestProgressionListener(EasyQuests plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().loadPlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().unloadPlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material broken = event.getBlock().getType();

        Quest activeQuest = getActiveQuest(player);
        if (activeQuest == null) return;

        if (activeQuest.getObjective().getType().equalsIgnoreCase("MINE_BLOCKS")) {
            String targetStr = activeQuest.getObjective().getTarget();
            if (isMatchingOre(targetStr, broken)) {
                plugin.handleProgress(player, activeQuest, 1);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack result = event.getRecipe().getResult();

        Quest activeQuest = getActiveQuest(player);
        if (activeQuest == null) return;

        if (activeQuest.getObjective().getType().equalsIgnoreCase("CRAFT_ITEM")) {
            Material target = Material.matchMaterial(activeQuest.getObjective().getTarget());
            if (target == result.getType()) {
                plugin.handleProgress(player, activeQuest, result.getAmount());
            }
        }
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        EntityType killedType = event.getEntityType();

        Quest activeQuest = getActiveQuest(killer);
        if (activeQuest == null) return;

        if (activeQuest.getObjective().getType().equalsIgnoreCase("KILL_MOBS")) {
            if (activeQuest.getObjective().getTarget().equalsIgnoreCase(killedType.name())) {
                plugin.handleProgress(killer, activeQuest, 1);
            }
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (event.getCaught() == null) return;

        Quest activeQuest = getActiveQuest(player);
        if (activeQuest == null) return;

        if (activeQuest.getObjective().getType().equalsIgnoreCase("FISH_ITEM")) {
            if (event.getCaught() instanceof org.bukkit.entity.Item itemEntity) {
                Material caughtMaterial = itemEntity.getItemStack().getType();
                if (activeQuest.getObjective().getTarget().equalsIgnoreCase(caughtMaterial.name())) {
                    plugin.handleProgress(player, activeQuest, 1);
                }
            }
        }
    }

    /**
     * Szybkie pobieranie aktywnego zadania gracza w czasie O(1) z RAM cache
     */
    private Quest getActiveQuest(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return null;

        String activeId = data.getActiveQuestId();
        if (activeId == null) return null;

        return plugin.getQuestManager().getQuest(activeId);
    }

    private boolean isMatchingOre(String target, Material broken) {
        Material targetMat = Material.matchMaterial(target);
        if (targetMat == null) return false;
        if (targetMat == broken) return true;

        String brokenName = broken.name();
        String targetName = targetMat.name();

        if (targetName.startsWith("DEEPSLATE_")) {
            return targetName.substring("DEEPSLATE_".length()).equals(brokenName);
        }
        if (brokenName.startsWith("DEEPSLATE_")) {
            return brokenName.substring("DEEPSLATE_".length()).equals(targetName);
        }

        return false;
    }
}