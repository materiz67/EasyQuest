package pl.materiz66.easyquests.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.config.MessagesConfig;
import pl.materiz66.easyquests.config.QuestCategory;
import pl.materiz66.easyquests.menu.QuestCategoryGUI;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.QuestProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Komenda /quest – główna komenda gracza.
 * Subkomendy: (brak) → GUI, status, help
 */
public class QuestCommand implements CommandExecutor, TabCompleter {
    private final EasyQuestPlugin plugin;

    public QuestCommand(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        MessagesConfig msg = plugin.getConfigManager().getMessages();
        String prefix = msg.getPrefix();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageService().parse(prefix + msg.getPlayerOnly()));
            return true;
        }

        if (!player.hasPermission("easyquests.use")) {
            plugin.getMessageService().sendMessage(player, prefix + msg.getNoPermission());
            return true;
        }

        // Brak subkomendy → otwórz GUI
        if (args.length == 0) {
            player.openInventory(new QuestCategoryGUI(plugin, player).getInventory());
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "status" -> showStatus(player, prefix, msg);
            case "help" -> showHelp(player, msg);
            default -> {
                plugin.getMessageService().sendMessage(player, prefix + msg.getInvalidUsage());
            }
        }

        return true;
    }

    /**
     * Wyświetla aktualne zadania gracza w chacie.
     */
    private void showStatus(Player player, String prefix, MessagesConfig msg) {
        QuestProgress progress = plugin.getCacheManager().getCachedProgress(player.getUniqueId());

        plugin.getMessageService().sendMessage(player, msg.getStatusHeader());
        plugin.getMessageService().sendMessage(player, msg.getStatusTitle());

        if (progress == null || progress.getActiveQuestsProgress().isEmpty()) {
            plugin.getMessageService().sendMessage(player, msg.getStatusNoActive());
        } else {
            for (Map.Entry<String, Integer> entry : progress.getActiveQuestsProgress().entrySet()) {
                Quest quest = plugin.getQuestManager().getQuestById(entry.getKey());
                if (quest == null) continue;

                // Pobierz wyświetlaną nazwę kategorii
                QuestCategory cat = plugin.getConfigManager().getSettings()
                        .getCategories().get(quest.getCategory());
                String catName = (cat != null) ? cat.getDisplayName() : quest.getCategory();

                String line = msg.getStatusLine()
                        .replace("{quest}", quest.getDisplayName())
                        .replace("{category}", catName)
                        .replace("{progress}", String.valueOf(entry.getValue()))
                        .replace("{target}", String.valueOf(quest.getObjective().getAmount()));
                plugin.getMessageService().sendMessage(player, line);
            }
        }

        plugin.getMessageService().sendMessage(player, msg.getStatusFooter());
    }

    /**
     * Wyświetla listę dostępnych komend.
     */
    private void showHelp(Player player, MessagesConfig msg) {
        plugin.getMessageService().sendMessage(player, msg.getHelpHeader());
        plugin.getMessageService().sendMessage(player, msg.getHelpTitle());
        plugin.getMessageService().sendMessage(player, msg.getHelpQuest());
        plugin.getMessageService().sendMessage(player, msg.getHelpQuestStatus());
        plugin.getMessageService().sendMessage(player, msg.getHelpQuestHelp());
        plugin.getMessageService().sendMessage(player, msg.getHelpFooter());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                               @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("easyquests.use")) return List.of();
        if (args.length == 1) {
            return List.of("status", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}