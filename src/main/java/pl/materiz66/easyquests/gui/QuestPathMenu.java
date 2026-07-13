package pl.materiz66.easyquests.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.quest.QuestCategory;
import pl.materiz66.easyquests.quest.QuestStatus;
import pl.materiz66.easyquests.user.PlayerData;
import pl.materiz66.easyquests.util.ColorUtil;

import java.util.List;

public class QuestPathMenu {
    private final EasyQuests plugin;
    private final QuestCategory category;
    private final Player player;

    public static final int[] SNAKE_PATH = {
            10, 11, 12, 13, 14, 15, 16,
            25, 24, 23, 22, 21, 20, 19,
            28, 29, 30, 31, 32, 33, 34,
            43, 42, 41, 40, 39, 38, 37
    };

    public QuestPathMenu(EasyQuests plugin, QuestCategory category, Player player) {
        this.plugin = plugin;
        this.category = category;
        this.player = player;
    }

    public void open() {
        String title = ColorUtil.formatLegacy(category.getDisplayName());

        EasyQuestHolder holder = new EasyQuestHolder("quest_path", category);
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            try {
                fillerMeta.displayName(ColorUtil.format(" "));
            } catch (NoSuchMethodError e) {
                fillerMeta.setDisplayName(" ");
            }
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, filler);
        }

        QuestIconBuilder iconBuilder = new QuestIconBuilder(
                plugin, // Przekazanie instancji głównej
                plugin.getConfigManager().getSettingsConfig().getLockedTemplate(),
                plugin.getConfigManager().getSettingsConfig().getUnlockedTemplate(),
                plugin.getConfigManager().getSettingsConfig().getActiveTemplate(),
                plugin.getConfigManager().getSettingsConfig().getCompletedTemplate()
        );

        List<Quest> questsList = category.getQuests();

        for (int i = 0; i < questsList.size() && i < SNAKE_PATH.length; i++) {
            Quest quest = questsList.get(i);

            QuestStatus status = getPlayerQuestStatus(player, quest, i);
            int progress = getPlayerProgress(player, quest);

            int slot = SNAKE_PATH[i];
            inv.setItem(slot, iconBuilder.buildIcon(player, quest, status, progress)); // Przekazanie obiektu player
        }

        player.openInventory(inv);
    }

    private QuestStatus getPlayerQuestStatus(Player player, Quest quest, int index) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return QuestStatus.LOCKED;

        if (data.isCompleted(quest.getId())) {
            return QuestStatus.COMPLETED;
        }

        if (quest.getId().equals(data.getActiveQuestId())) {
            return QuestStatus.ACTIVE;
        }

        List<Quest> categoryQuests = category.getQuests();
        if (index == 0) {
            return QuestStatus.UNLOCKED;
        }

        Quest prevQuest = categoryQuests.get(index - 1);
        if (data.isCompleted(prevQuest.getId())) {
            return QuestStatus.UNLOCKED;
        }

        return QuestStatus.LOCKED;
    }

    private int getPlayerProgress(Player player, Quest quest) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return 0;
        return data.getQuestProgress(quest.getId());
    }
}