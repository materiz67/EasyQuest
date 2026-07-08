package pl.example.quests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.example.quests.QuestModel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestCommand implements CommandExecutor, TabCompleter {
    private final QuestPlugin plugin;

    public QuestCommand(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String sub = args[0].toLowerCase();

            if (sub.equals("reload")) {
                if (!sender.hasPermission("quests.admin")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getMessageManager().loadMessages();
                plugin.getQuestManager().loadQuests();
                sender.sendMessage(plugin.getMessageManager().getMessage("reload-success"));
                return true;
            }

            if (sub.equals("set") || sub.equals("reset") || sub.equals("complete")) {
                if (!sender.hasPermission("quests.admin")) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage("§cPoprawne użycie: /questy " + sub + " <gracz> ...");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cGracz o nazwie '" + args[1] + "' jest offline!");
                    return true;
                }

                if (sub.equals("reset")) {
                    if (args.length == 2) {
                        plugin.getQuestManager().resetAll(target);
                        sender.sendMessage("§aZresetowano wszystkie zadania dla gracza " + target.getName());
                        target.sendMessage("§cTwoje postępy we wszystkich zadaniach zostały zresetowane przez administratora.");
                    } else {
                        String questId = args[2];
                        Quest quest = plugin.getQuestManager().getQuestById(questId);
                        if (quest == null) {
                            sender.sendMessage("§cZadanie o ID '" + questId + "' nie istnieje!");
                            return true;
                        }
                        plugin.getQuestManager().resetQuest(target, quest.getId());
                        sender.sendMessage("§aZresetowano zadanie " + quest.getName() + " §adla gracza " + target.getName());
                        target.sendMessage("§cTwoje postępy w zadaniu " + quest.getName() + " §czostały zresetowane.");
                    }
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage("§cPoprawne użycie: /questy " + sub + " <gracz> <id_zadania> ...");
                    return true;
                }

                String questId = args[2];
                Quest quest = plugin.getQuestManager().getQuestById(questId);
                if (quest == null) {
                    sender.sendMessage("§cZadanie o ID '" + questId + "' nie istnieje!");
                    return true;
                }

                if (sub.equals("complete")) {
                    plugin.getQuestManager().forceComplete(target, quest);
                    sender.sendMessage("§aWymuszono ukończenie zadania " + quest.getName() + " §adla gracza " + target.getName());
                    return true;
                }

                if (sub.equals("set")) {
                    if (args.length < 4) {
                        sender.sendMessage("§cPoprawne użycie: /questy set <gracz> <id_zadania> <wartość>");
                        return true;
                    }
                    int val;
                    try {
                        val = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cWartość musi być liczbą całkowitą!");
                        return true;
                    }
                    plugin.getQuestManager().setProgress(target, quest, val);
                    sender.sendMessage("§aUstawiono postęp zadania " + quest.getName() + " §adla gracza " + target.getName() + " §ana §e" + val);
                    return true;
                }
            }
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("only-players"));
            return true;
        }

        Player player = (Player) sender;
        playSafeSound(player, "open-menu", Sound.BLOCK_CHEST_OPEN);
        openCategoriesMenu(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("quests.admin")) {
            return completions;
        }

        if (args.length == 1) {
            List<String> subCommands = List.of("reload", "set", "reset", "complete");
            for (String sub : subCommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("set") || sub.equals("reset") || sub.equals("complete")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("set") || sub.equals("reset") || sub.equals("complete")) {
                for (QuestCategory cat : plugin.getQuestManager().getCategories()) {
                    for (Quest q : cat.getQuests()) {
                        if (q.getId().toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(q.getId());
                        }
                    }
                }
            }
        }
        return completions;
    }

    public void openCategoriesMenu(Player player) {
        FileConfiguration config = plugin.getConfig();
        String title = ColorUtil.color(config.getString("gui.title", "&0Zadania"));
        int size = config.getInt("gui.size", 27);

        Inventory inv = Bukkit.createInventory(new QuestMenuHolder("CATEGORIES", null), size, title);

        if (config.getBoolean("gui.fill-empty-slots", true)) {
            Material fillerMat = getSafeMaterial(config.getString("gui.filler-material"), Material.GRAY_STAINED_GLASS_PANE);
            ItemStack filler = new ItemStack(fillerMat);
            ItemMeta fillerMeta = filler.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(ColorUtil.color(config.getString("gui.filler-name", " ")));
                filler.setItemMeta(fillerMeta);
            }
            for (int i = 0; i < size; i++) {
                inv.setItem(i, filler);
            }
        }

        ConfigurationSection categoriesSec = config.getConfigurationSection("gui.categories");
        if (categoriesSec != null) {
            for (String key : categoriesSec.getKeys(false)) {
                ConfigurationSection catSec = categoriesSec.getConfigurationSection(key);
                int slot = catSec.getInt("slot");

                Material mat = getSafeMaterial(catSec.getString("material"), Material.BOOK);
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ColorUtil.color(catSec.getString("name", key)));
                    List<String> lore = new ArrayList<>();
                    for (String line : catSec.getStringList("lore")) {
                        lore.add(ColorUtil.color(line));
                    }
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inv.setItem(slot, item);
            }
        }
        player.openInventory(inv);
    }

    public void openQuestsMenu(Player player, String categoryId) {
        QuestCategory category = plugin.getQuestManager().getCategory(categoryId);
        if (category == null) return;

        Inventory inv = Bukkit.createInventory(new QuestMenuHolder("QUESTS_LIST", categoryId), 36, "§0Kategoria: " + category.getName());

        int slot = 10;
        UUID uuid = player.getUniqueId();
        for (Quest quest : category.getQuests()) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(quest.getName());

                List<String> lore = new ArrayList<>();
                lore.add("§7" + quest.getDescription());
                lore.add("");
                lore.add("§eNagroda: §f" + quest.getRewardDescription());
                lore.add("");

                boolean done = plugin.getQuestManager().isCompleted(uuid, quest.getId());
                boolean unlocked = plugin.getQuestManager().isQuestUnlocked(uuid, quest);

                if (done) {
                    lore.add("§a§lUKOŃCZONE");
                    applyCompletedGlow(meta);
                } else if (!unlocked) {
                    lore.add("§c§lZABLOKOWANE");
                    lore.add("§7Ukończ poprzednie zadanie z tej kategorii!");
                } else {
                    int prog = plugin.getQuestManager().getProgress(uuid, quest.getId());
                    lore.add("§7Postęp: §e" + prog + " §7/ §e" + quest.getRequiredAmount());
                    lore.add(getProgressBar(prog, quest.getRequiredAmount()));
                }

                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
        }

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§c§lPowrót");
            back.setItemMeta(backMeta);
        }
        inv.setItem(31, back);

        player.openInventory(inv);
    }

    // Inteligentny i bezpieczny odtwarzacz dźwięków zapobiegający crashom przy literówkach w nazwach dźwięków
    public void playSafeSound(Player player, String soundPath, Sound fallback) {
        if (!plugin.getConfig().getBoolean("gui.sounds.enabled", true)) return;
        try {
            String soundName = plugin.getConfig().getString("gui.sounds." + soundPath);
            if (soundName != null) {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
                return;
            }
        } catch (Exception ignored) {}
        if (fallback != null) {
            player.playSound(player.getLocation(), fallback, 1.0F, 1.0F);
        }
    }

    // Bezpieczne weryfikowanie materiałów
    private Material getSafeMaterial(String name, Material fallback) {
        if (name == null) return fallback;
        try {
            Material mat = Material.matchMaterial(name.toUpperCase());
            return mat != null ? mat : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private void applyCompletedGlow(ItemMeta meta) {
        boolean glintApplied = false;
        try {
            java.lang.reflect.Method glintMethod = meta.getClass().getMethod("setEnchantmentGlintOverride", Boolean.class);
            glintMethod.invoke(meta, true);
            glintApplied = true;
        } catch (Exception ignored) {}

        if (!glintApplied) {
            try {
                org.bukkit.enchantments.Enchantment glowEnchant = org.bukkit.enchantments.Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("unbreaking"));
                if (glowEnchant == null) {
                    glowEnchant = org.bukkit.enchantments.Enchantment.getByName("DURABILITY");
                }
                if (glowEnchant != null) {
                    meta.addEnchant(glowEnchant, 1, true);
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                }
            } catch (Exception ignored) {}
        }
    }

    // Dynamicznie renderowany pasek postępu z config.yml przy użyciu kolorów Hex
    private String getProgressBar(int current, int max) {
        FileConfiguration config = plugin.getConfig();
        String barChar = config.getString("gui.progress-bar.char", "■");
        String colorDone = ColorUtil.color(config.getString("gui.progress-bar.color-completed", "&a"));
        String colorLeft = ColorUtil.color(config.getString("gui.progress-bar.color-uncompleted", "&c"));

        int totalBars = 10;
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);
        int leftBars = totalBars - progressBars;

        StringBuilder sb = new StringBuilder("§8[");
        sb.append(colorDone);
        for (int i = 0; i < progressBars; i++) sb.append(barChar);
        sb.append(colorLeft);
        for (int i = 0; i < leftBars; i++) sb.append(barChar);
        sb.append("§8]");
        return sb.toString();
    }
}