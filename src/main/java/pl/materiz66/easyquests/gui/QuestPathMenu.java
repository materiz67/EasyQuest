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
    private final int page; // Numer bieżącej strony

    // Indeksy meandrującej ścieżki wężyka (28 slotów na stronę)
    public static final int[] SNAKE_PATH = {
            10, 11, 12, 13, 14, 15, 16,
            25, 24, 23, 22, 21, 20, 19,
            28, 29, 30, 31, 32, 33, 34,
            43, 42, 41, 40, 39, 38, 37
    };

    // Domyślny konstruktor (otwiera pierwszą stronę)
    public QuestPathMenu(EasyQuests plugin, QuestCategory category, Player player) {
        this(plugin, category, player, 0);
    }

    // Konstruktor wielostronicowy
    public QuestPathMenu(EasyQuests plugin, QuestCategory category, Player player, int page) {
        this.plugin = plugin;
        this.category = category;
        this.player = player;
        this.page = page;
    }

    public void open() {
        // Dodajemy wskaźnik strony do tytułu, jeśli zadań jest więcej niż na jedną stronę
        String title = ColorUtil.formatLegacy(category.getDisplayName() + (category.getQuests().size() > 28 ? " &8| Str. " + (page + 1) : ""));

        QuestPathData pathData = new QuestPathData(category, page);
        EasyQuestHolder holder = new EasyQuestHolder("quest_path", pathData);
        Inventory inv = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inv);

        // Tło
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
                plugin,
                plugin.getConfigManager().getSettingsConfig().getLockedTemplate(),
                plugin.getConfigManager().getSettingsConfig().getUnlockedTemplate(),
                plugin.getConfigManager().getSettingsConfig().getActiveTemplate(),
                plugin.getConfigManager().getSettingsConfig().getCompletedTemplate()
        );

        List<Quest> questsList = category.getQuests();

        // Obliczanie zakresu zadań dla bieżącej strony
        int startIndex = page * 28;
        int endIndex = Math.min(startIndex + 28, questsList.size());

        for (int i = startIndex; i < endIndex; i++) {
            Quest quest = questsList.get(i);

            QuestStatus status = getPlayerQuestStatus(player, quest, i);
            int progress = getPlayerProgress(player, quest);

            // Wyliczanie slotu relatywnie do bieżącej strony
            int slot = SNAKE_PATH[i - startIndex];
            inv.setItem(slot, iconBuilder.buildIcon(player, quest, status, progress));
        }

        // ======================================================
        //   DODANO: PRZYCISK COFANIA DO MENU GŁÓWNEGO (Slot 49)
        // ======================================================
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            try {
                backMeta.displayName(ColorUtil.format("&#ffcc00&lᴘᴏᴡʀᴏᴛ"));
                backMeta.lore(List.of(ColorUtil.format("<gray>▶ Kliknij, aby wrócić do menu głównego.")));
            } catch (NoSuchMethodError e) {
                backMeta.setDisplayName(ColorUtil.formatLegacy("&#ffcc00&lᴘᴏᴡʀᴏᴛ"));
                backMeta.setLore(List.of(ColorUtil.formatLegacy("<gray>▶ Kliknij, aby wrócić do menu głównego.")));
            }
            backItem.setItemMeta(backMeta);
        }
        inv.setItem(49, backItem);

        // ======================================================
        //   DODANO: POPRZEDNIA STRONA (Slot 45) - tylko jeśli page > 0
        // ======================================================
        if (page > 0) {
            ItemStack prevItem = new ItemStack(Material.FEATHER);
            ItemMeta prevMeta = prevItem.getItemMeta();
            if (prevMeta != null) {
                try {
                    prevMeta.displayName(ColorUtil.format("&#ffaa00&lᴘᴏᴘʀᴢᴇᴅɴɪᴀ sᴛʀᴏɴᴀ"));
                    prevMeta.lore(List.of(ColorUtil.format("<gray>▶ Przejdź do strony " + page)));
                } catch (NoSuchMethodError e) {
                    prevMeta.setDisplayName(ColorUtil.formatLegacy("&#ffaa00&lᴘᴏᴘʀᴢᴇᴅɴɪᴀ sᴛʀᴏɴᴀ"));
                    prevMeta.setLore(List.of(ColorUtil.formatLegacy("<gray>▶ Przejdź do strony " + page)));
                }
                prevItem.setItemMeta(prevMeta);
            }
            inv.setItem(45, prevItem);
        }

        // ======================================================
        //   DODANO: NASTĘPNA STRONA (Slot 53) - tylko jeśli zostało więcej zadań
        // ======================================================
        if (questsList.size() > endIndex) {
            ItemStack nextItem = new ItemStack(Material.FEATHER);
            ItemMeta nextMeta = nextItem.getItemMeta();
            if (nextMeta != null) {
                try {
                    nextMeta.displayName(ColorUtil.format("&#ffaa00&lɴᴀsᴛᴇᴘɴᴀ sᴛʀᴏɴᴀ"));
                    nextMeta.lore(List.of(ColorUtil.format("<gray>▶ Przejdź do strony " + (page + 2))));
                } catch (NoSuchMethodError e) {
                    nextMeta.setDisplayName(ColorUtil.formatLegacy("&#ffaa00&lɴᴀsᴛᴇᴘɴᴀ sᴛʀᴏɴᴀ"));
                    nextMeta.setLore(List.of(ColorUtil.formatLegacy("<gray>▶ Przejdź do strony " + (page + 2))));
                }
                nextItem.setItemMeta(nextMeta);
            }
            inv.setItem(53, nextItem);
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