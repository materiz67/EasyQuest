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
import pl.materiz66.easyquests.user.QuestProgress;

import java.util.List;
import java.util.stream.Collectors;

public class AdminCommand implements CommandExecutor, TabCompleter {
    private final EasyQuestPlugin plugin;

    public AdminCommand(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("easyquests.admin")) {
            String noPermission = plugin.getMessagesYaml().getString("no-permission", "<red>Nie masz uprawnien.");
            if (sender instanceof Player player) {
                plugin.getMessageService().sendMessage(player, noPermission);
            } else {
                sender.sendMessage("Brak wymaganych uprawnien.");
            }
            return true;
        }

        String prefix = plugin.getMessagesYaml().getString("prefix", "<gray>[<gold>EasyQuests</gold>] ");

        if (args.length == 0) {
            sendHelp(sender, prefix);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            plugin.reloadConfig();
            plugin.reloadMessages();
            plugin.getQuestManager().loadQuests();

            sender.sendMessage(plugin.getMessageService().parse(prefix + "<green>Konfiguracja oraz zadania zostaly pomyslnie przeladowane!"));
            return true;
        }

        if (subCommand.equals("reset")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.getMessageService().parse(prefix + "<red>Prawidlowe uzycie: /eq admin reset <gracz>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getMessageService().parse(prefix + "<red>Gracz o nazwie " + args[1] + " jest offline."));
                return true;
            }

            QuestProgress progress = plugin.getCacheManager().getCachedProgress(target.getUniqueId());
            if (progress == null) {
                sender.sendMessage(plugin.getMessageService().parse(prefix + "<red>Blad: Brak danych gracza w pamieci podrecznej."));
                return true;
            }

            // Czyszczenie map postępów i ukończonych zadań
            progress.getActiveQuestsProgress().clear();
            progress.getCompletedQuests().clear();

            // Przydzielenie pierwszego zadania na start dla każdej kategorii
            for (String categoryId : plugin.getConfigManager().getSettings().getCategories().keySet()) {
                var categoryQuests = plugin.getQuestManager().getQuestsByCategory(categoryId);
                if (!categoryQuests.isEmpty()) {
                    progress.getActiveQuestsProgress().put(categoryQuests.get(0).getId(), 0);
                }
            }

            plugin.getDatabaseService().savePlayerProgress(progress);

            sender.sendMessage(plugin.getMessageService().parse(prefix + "<green>Zresetowano postep zadan dla gracza <yellow>" + target.getName() + "</yellow>."));
            target.sendMessage(plugin.getMessageService().parse(prefix + "<yellow>Twoj postep zadan zostal zresetowany przez administratora."));
            return true;
        }

        sendHelp(sender, prefix);
        return true;
    }

    private void sendHelp(CommandSender sender, String prefix) {
        sender.sendMessage(plugin.getMessageService().parse(prefix + "<gray>Lista komend administratora:"));
        sender.sendMessage(plugin.getMessageService().parse("<yellow>/eq admin reload <gray>- Przeladowuje pliki konfiguracji."));
        sender.sendMessage(plugin.getMessageService().parse("<yellow>/eq admin reset <gracz> <gray>- Resetuje postep we wszystkich kategoriach."));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("easyquests.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return List.of("reload", "reset").stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}