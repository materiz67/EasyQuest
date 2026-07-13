package pl.materiz66.easyquests.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.config.QuestIconTemplate;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.quest.QuestStatus;
import pl.materiz66.easyquests.user.PlayerData;
import pl.materiz66.easyquests.util.ColorUtil;
import pl.materiz66.easyquests.util.GlowUtil;
import pl.materiz66.easyquests.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

public class QuestIconBuilder {
    private final EasyQuests plugin;
    private final QuestIconTemplate lockedTemplate;
    private final QuestIconTemplate unlockedTemplate;
    private final QuestIconTemplate activeTemplate;
    private final QuestIconTemplate completedTemplate;

    public QuestIconBuilder(EasyQuests plugin, QuestIconTemplate lockedTemplate, QuestIconTemplate unlockedTemplate, QuestIconTemplate activeTemplate, QuestIconTemplate completedTemplate) {
        this.plugin = plugin;
        this.lockedTemplate = lockedTemplate;
        this.unlockedTemplate = unlockedTemplate;
        this.activeTemplate = activeTemplate;
        this.completedTemplate = completedTemplate;
    }

    public ItemStack buildIcon(Player player, Quest quest, QuestStatus status, int progress) {
        QuestIconTemplate template = switch (status) {
            case LOCKED -> lockedTemplate;
            case UNLOCKED -> unlockedTemplate;
            case ACTIVE -> activeTemplate;
            case COMPLETED -> completedTemplate;
        };

        Material material = template.getMaterial();
        if (material == null) material = quest.getMaterial();
        if (material == null) material = Material.PAPER;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String questName = quest.getDisplayName() != null ? quest.getDisplayName() : quest.getId();
        String targetName = quest.getObjective() != null ? ValidationUtil.getFriendlyName(quest.getObjective().getTarget()) : "N/A";
        String targetAmountStr = quest.getObjective() != null ? String.valueOf(quest.getObjective().getAmount()) : "0";
        String progressStr = String.valueOf(progress);

        char barChar = '■';
        String compColor = "&#55ff55";
        String uncompColor = "&#ff5555";
        String progressBar = ColorUtil.getProgressBar(progress, quest.getObjective().getAmount(), 10, barChar, compColor, uncompColor);

        // Dynamiczne określenie akcji aktywacji / zmiany na podstawie statusu gracza
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        String actionText = "Kliknij, aby aktywować to zadanie!";
        if (data != null && data.getActiveQuestId() != null) {
            actionText = "Kliknij, aby zmienić zadanie!";
        }

        String formattedName = template.getName()
                .replace("{quest}", questName)
                .replace("{target_name}", targetName)
                .replace("{progress}", progressStr)
                .replace("{target_amount}", targetAmountStr)
                .replace("{progress_bar}", progressBar)
                .replace("{action}", actionText);

        try {
            meta.displayName(ColorUtil.format(formattedName));
        } catch (NoSuchMethodError e) {
            meta.setDisplayName(ColorUtil.formatLegacy(formattedName));
        }

        List<String> rawLore = template.getLore();

        try {
            List<net.kyori.adventure.text.Component> formattedLore = new ArrayList<>();
            for (String line : rawLore) {
                String formattedLine = replacePlaceholders(line, questName, targetName, progressStr, targetAmountStr, progressBar, actionText);

                if (formattedLine.contains("{description}")) {
                    for (String descLine : quest.getDescription()) {
                        formattedLore.add(ColorUtil.format(descLine));
                    }
                } else if (formattedLine.contains("{rewards}")) {
                    for (String rewardLine : quest.getRewardsDisplay()) {
                        formattedLore.add(ColorUtil.format(rewardLine));
                    }
                } else {
                    formattedLore.add(ColorUtil.format(formattedLine));
                }
            }
            meta.lore(formattedLore);
        } catch (NoSuchMethodError e) {
            List<String> formattedLoreLegacy = new ArrayList<>();
            for (String line : rawLore) {
                String formattedLine = replacePlaceholders(line, questName, targetName, progressStr, targetAmountStr, progressBar, actionText);

                if (formattedLine.contains("{description}")) {
                    for (String descLine : quest.getDescription()) {
                        formattedLoreLegacy.add(ColorUtil.formatLegacy(descLine));
                    }
                } else if (formattedLine.contains("{rewards}")) {
                    for (String rewardLine : quest.getRewardsDisplay()) {
                        formattedLoreLegacy.add(ColorUtil.formatLegacy(rewardLine));
                    }
                } else {
                    formattedLoreLegacy.add(ColorUtil.formatLegacy(formattedLine));
                }
            }
            meta.setLore(formattedLoreLegacy);
        }

        item.setItemMeta(meta);

        if (status == QuestStatus.COMPLETED) {
            GlowUtil.applyGlow(item);
        }

        return item;
    }

    private String replacePlaceholders(String text, String quest, String target, String progress, String amount, String progressBar, String action) {
        return text.replace("{quest}", quest)
                .replace("{target_name}", target)
                .replace("{progress}", progress)
                .replace("{target_amount}", amount)
                .replace("{progress_bar}", progressBar)
                .replace("{action}", action);
    }
}