package pl.materiz66.easyquests.command;

import org.bukkit.Bukkit;
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
import pl.materiz66.easyquests.user.QuestProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Komenda administratora /easyquest (skrót /eq).
 * Zapewnia administrację systemem zadań (reload, reset, give).
 */
public class AdminCommand implements CommandExecutor, TabCompleter {
    private final EasyQuestPlugin plugin;

    public AdminCommand(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        MessagesConfig msg = plugin.getConfigManager().getMessages();
        String prefix = msg.getPrefix();

        if (!sender.hasPermission("easyquests.admin")) {
            if (sender instanceof Player player) {
                plugin.getMessageService().sendMessage(player, prefix + msg.getNoPermission());
            } else {
                sender.sendMessage(plugin.getMessageService().parse(prefix + msg.getNoPermission()));
            }
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, msg);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            plugin.reloadConfig();
            plugin.getQuestManager().loadQuests();

            String reloadMsg = prefix + msg.getReloadSuccess();
            if (sender instanceof Player player) {
                plugin.getMessageService().sendMessage(player, reloadMsg);
            } else {
                sender.sendMessage(plugin.getMessageService().parse(reloadMsg));
            }
            return true;
        }

        if (subCommand.equals("reset")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.getMessageService().parse(prefix + "<red>Prawidłowe użycie: /eq reset <gracz>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                String offlineMsg = prefix + msg.getPlayerOffline().replace("{player}", args[1]);
                sender.sendMessage(plugin.getMessageService().parse(offlineMsg));
                return true;
            }

            QuestProgress progress = plugin.getCacheManager().getCachedProgress(target.getUniqueId());
            if (progress == null) {
                sender.sendMessage(plugin.getMessageService().parse(prefix + msg.getNoProgressData()));
                return true;
            }

            progress.getActiveQuestsProgress().clear();
            progress.getCompletedQuests().clear();

            if (plugin.getConfigManager().getSettings().isAutoStartFirstQuest()) {
                for (String categoryId : plugin.getConfigManager().getSettings().getCategories().keySet()) {
                    var categoryQuests = plugin.getQuestManager().getQuestsByCategory(categoryId);
                    if (!categoryQuests.isEmpty()) {
                        progress.getActiveQuestsProgress().put(categoryQuests.get(0).getId(), 0);
                    }
                }
            }

            plugin.getDatabaseService().savePlayerProgress(progress);

            String resetSuccessMsg = prefix + msg.getResetSuccess().replace("{player}", target.getName());
            sender.sendMessage(plugin.getMessageService().parse(resetSuccessMsg));
            plugin.getMessageService().sendMessage(target, prefix + msg.getResetNotify());
            return true;
        }

        if (subCommand.equals("give")) {
            if (args.length < 3) {
                sender.sendMessage(plugin.getMessageService().parse(prefix + "<red>Prawidłowe użycie: /eq give <gracz> <kategoria>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                String offlineMsg = prefix + msg.getPlayerOffline().replace("{player}", args[1]);
                sender.sendMessage(plugin.getMessageService().parse(offlineMsg));
                return true;
            }

            String catId = args[2].toLowerCase();
            QuestCategory category = plugin.getConfigManager().getSettings().getCategories().get(catId);
            if (category == null) {
                String catNotFoundMsg = prefix + msg.getCategoryNotFound().replace("{category}", catId);
                sender.sendMessage(plugin.getMessageService().parse(catNotFoundMsg));
                return true;
            }

            QuestProgress progress = plugin.getCacheManager().getCachedProgress(target.getUniqueId());
            if (progress == null) {
                sender.sendMessage(plugin.getMessageService().parse(prefix + msg.getNoProgressData()));
                return true;
            }

            var categoryQuests = plugin.getQuestManager().getQuestsByCategory(catId);
            if (categoryQuests.isEmpty()) {
                sender.sendMessage(plugin.getMessageService().parse(prefix + "<red>Błąd: Ta kategoria nie posiada żadnych zadań."));
                return true;
            }

            // Czyścimy aktywny i ukończony postęp tylko z tej kategorii, by nie resetować innych
            for (var quest : categoryQuests) {
                progress.getActiveQuestsProgress().remove(quest.getId());
                progress.getCompletedQuests().remove(quest.getId());
            }

            // Rozpoczynamy od pierwszego zadania z tej kategorii
            progress.getActiveQuestsProgress().put(categoryQuests.get(0).getId(), 0);
            plugin.getDatabaseService().savePlayerProgress(progress);

            String giveSuccessMsg = prefix + msg.getGiveSuccess()
                    .replace("{player}", target.getName())
                    .replace("{category}", category.getDisplayName());
            sender.sendMessage(plugin.getMessageService().parse(giveSuccessMsg));

            String giveNotifyMsg = prefix + msg.getGiveNotify()
                    .replace("{category}", category.getDisplayName());
            plugin.getMessageService().sendMessage(target, giveNotifyMsg);
            return true;
        }

        sendHelp(sender, msg);
        return true;
    }

    private void sendHelp(CommandSender sender, MessagesConfig msg) {
        String prefix = msg.getPrefix();
        sender.sendMessage(plugin.getMessageService().parse(prefix + "<gray>Lista komend administratora:"));
        sender.sendMessage(plugin.getMessageService().parse("<yellow>/eq reload <gray>- Przeładowuje pliki konfiguracji i zadania."));
        sender.sendMessage(plugin.getMessageService().parse("<yellow>/eq reset <gracz> <gray>- Resetuje postęp gracza we wszystkich kategoriach."));
        sender.sendMessage(plugin.getMessageService().parse("<yellow>/eq give <gracz> <kategoria> <gray>- Nadaje lub resetuje start w określonej kategorii."));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("easyquests.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return List.of("reload", "reset", "give").stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("give"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return new ArrayList<>(plugin.getConfigManager().getSettings().getCategories().keySet()).stream()
                    .filter(cat -> cat.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}