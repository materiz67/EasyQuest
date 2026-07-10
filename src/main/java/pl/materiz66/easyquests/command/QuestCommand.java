package pl.materiz66.easyquests.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.menu.QuestRoadGUI;

public class QuestCommand implements CommandExecutor {
    private final EasyQuestPlugin plugin;

    public QuestCommand(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Blokada uruchomienia komendy przez konsolę
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Ta komenda moze byc wykonana tylko przez gracza w grze.");
            return true;
        }

        // Sprawdzenie uprawnienia do przeglądania zadań
        if (!player.hasPermission("easyquests.use")) {
            String noPermission = plugin.getMessagesYaml().getString("no-permission", "<red>Nie masz uprawnien.");
            plugin.getMessageService().sendMessage(player, noPermission);
            return true;
        }

        // Zainicjowanie i otwarcie menu GUI z pionową ścieżką zadań
        QuestRoadGUI gui = new QuestRoadGUI(plugin, player);
        player.openInventory(gui.getInventory());
        return true;
    }
}