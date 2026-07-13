package pl.materiz66.easyquests.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.PlayerData;

import java.util.List;

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

    // ======================================================
    //   ZABEZPIECZENIE ANTY-EXPLOIT: OZNACZANIE BLOKÓW GRACZA
    // ======================================================
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Zapisujemy lekkie metadane Bukkit, aby oznaczyć blok jako postawiony przez gracza
        event.getBlock().setMetadata("eq_placed", new FixedMetadataValue(plugin, true));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        org.bukkit.block.Block block = event.getBlock();
        Material broken = block.getType();

        // Jeśli blok posiada metadane eq_placed, przerywamy (ochrona przed oszustwem)
        if (block.hasMetadata("eq_placed")) {
            block.removeMetadata("eq_placed", plugin); // Czyścimy metadane z pamięci
            return;
        }

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

    // ======================================================
    //   SŁUCHACZ ALCHEMII (Wspiera 1.16 - 1.20.4 oraz 1.20.5+)
    // ======================================================
    @EventHandler(ignoreCancelled = true)
    public void onBrewRetrieve(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.BREWING) return;
        if (event.getSlotType() != InventoryType.SlotType.CONTAINER) return;

        // Paski na mikstury w statywie mają sloty 0, 1 oraz 2
        if (event.getSlot() > 2) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() != Material.POTION) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Reagujemy tylko na faktyczne wyciągnięcie gotowej mikstury z okna alchemii
        if (event.getClick().isKeyboardClick() || event.getClick().isShiftClick() || event.getClick().isLeftClick() || event.getClick().isRightClick()) {
            Quest activeQuest = getActiveQuest(player);
            if (activeQuest == null) return;

            if (activeQuest.getObjective().getType().equalsIgnoreCase("BREW_POTION")) {
                String targetPotionType = activeQuest.getObjective().getTarget().toUpperCase();

                if (item.getItemMeta() instanceof org.bukkit.inventory.meta.PotionMeta potionMeta) {
                    String typeName = "";
                    try {
                        // Dla wersji 1.16 - 1.20.4 (używa starego PotionData)
                        typeName = potionMeta.getBasePotionData().getType().name();
                    } catch (NoSuchMethodError | NoClassDefFoundError e) {
                        // Refleksyjny fallback dla 1.20.5+ (używa nowego rejestru PotionType)
                        try {
                            Object potionType = potionMeta.getClass().getMethod("getPotionType").invoke(potionMeta);
                            if (potionType != null) {
                                typeName = ((Enum<?>) potionType).name();
                            }
                        } catch (Exception ignored) {}
                    }

                    if (typeName.equalsIgnoreCase(targetPotionType)) {
                        plugin.handleProgress(player, activeQuest, 1);
                    }
                }
            }
        }
    }

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