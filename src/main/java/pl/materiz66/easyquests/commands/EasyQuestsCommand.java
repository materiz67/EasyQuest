package pl.materiz66.easyquests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.gui.MainQuestsMenu;
import pl.materiz66.easyquests.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class EasyQuestsCommand implements CommandExecutor, TabCompleter {
    private final EasyQuests plugin;

    public EasyQuestsCommand(EasyQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("easyquests.admin")) {
                sender.sendMessage(ColorUtil.formatLegacy(plugin.getConfigManager().getSettingsConfig().getMsgNoPermission()));
                return true;
            }
            plugin.getConfigManager().loadConfigs();
            sender.sendMessage(ColorUtil.formatLegacy(plugin.getConfigManager().getSettingsConfig().getMsgReloadSuccess()));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Ta komenda moze byc wykonana tylko przez gracza!");
            return true;
        }

        if (!player.hasPermission("easyquests.use")) {
            player.sendMessage(ColorUtil.formatLegacy(plugin.getConfigManager().getSettingsConfig().getMsgNoPermission()));
            return true;
        }

        new MainQuestsMenu(plugin, player).open();
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("easyquests.admin")) {
                completions.add("reload");
            }
        }
        return completions;
    }
}