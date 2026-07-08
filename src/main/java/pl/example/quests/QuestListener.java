package pl.example.quests;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import pl.example.quests.QuestModel.QuestCategory;
import pl.example.quests.QuestModel.QuestType;

import java.util.List;

public class QuestListener implements Listener {
    private final QuestPlugin plugin;

    public QuestListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getQuestManager().loadPlayerData(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getQuestManager().saveAndUnloadPlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        plugin.getQuestManager().handleAction(
                event.getPlayer(),
                QuestType.BREAK,
                event.getBlock().getType().name(),
                1
        );
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        plugin.getQuestManager().handleAction(
                event.getPlayer(),
                QuestType.PLACE,
                event.getBlock().getType().name(),
                1
        );
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity() instanceof Monster) {
            Monster monster = (Monster) event.getEntity();
            Player killer = monster.getKiller();
            if (killer != null) {
                plugin.getQuestManager().handleAction(
                        killer,
                        QuestType.KILL,
                        monster.getType().name(),
                        1
                );
            }
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() instanceof Item) {
            plugin.getQuestManager().handleAction(
                    event.getPlayer(),
                    QuestType.FISH,
                    "RAW_FISH",
                    1
            );
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof QuestMenuHolder)) return;
        event.setCancelled(true); // Całkowita ochrona przedmiotów w GUI

        Player player = (Player) event.getWhoClicked();
        QuestMenuHolder holder = (QuestMenuHolder) event.getInventory().getHolder();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        QuestCommand commandExecutor = (QuestCommand) plugin.getCommand("questy").getExecutor();
        if (commandExecutor == null) return;

        if (holder.getViewType().equals("CATEGORIES")) {
            int clickedSlot = event.getSlot();
            String categoryId = plugin.getQuestManager().getCategoryIdBySlot(clickedSlot);

            if (categoryId != null) {
                commandExecutor.playSafeSound(player, "click-category", Sound.UI_BUTTON_CLICK);
                commandExecutor.openQuestsMenu(player, categoryId);
            }
        } else if (holder.getViewType().equals("QUESTS_LIST")) {
            int clickedSlot = event.getSlot();

            if (clickedItem.getType() == Material.BARRIER) {
                commandExecutor.playSafeSound(player, "click-back", Sound.BLOCK_FIRE_EXTINGUISH);
                commandExecutor.openCategoriesMenu(player);
            } else if (clickedSlot >= 10 && clickedSlot < 36) {
                // Inteligenty nasłuch statusu zadania na podstawie Lore
                if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasLore()) {
                    List<String> lore = clickedItem.getItemMeta().getLore();
                    boolean isLocked = false;
                    if (lore != null) {
                        for (String line : lore) {
                            if (line.contains("ZABLOKOWANE")) {
                                isLocked = true;
                                break;
                            }
                        }
                    }
                    if (isLocked) {
                        commandExecutor.playSafeSound(player, "quest-locked", Sound.ENTITY_VILLAGER_NO);
                    } else {
                        commandExecutor.playSafeSound(player, "quest-unlocked", Sound.BLOCK_NOTE_BLOCK_PLING);
                    }
                }
            }
        }
    }
}