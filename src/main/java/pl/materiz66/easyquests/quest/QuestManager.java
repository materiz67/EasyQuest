package pl.materiz66.easyquests.quest;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.objective.ObjectiveType;
import pl.materiz66.easyquests.objective.QuestObjective;
import pl.materiz66.easyquests.objective.types.CollectItemsObjective;
import pl.materiz66.easyquests.objective.types.KillMobsObjective;
import pl.materiz66.easyquests.objective.types.MineBlocksObjective;

import java.io.File;
import java.util.*;

public class QuestManager {
    private final EasyQuestPlugin plugin;
    private final Map<String, Quest> quests = new HashMap<>();
    private final List<Quest> questsInOrder = new ArrayList<>();

    public QuestManager(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Wczytuje i parsuje pliki zadań z katalogu plugins/EasyQuest/quests/
     */
    public void loadQuests() {
        quests.clear();
        questsInOrder.clear();

        File questsDir = new File(plugin.getDataFolder(), "quests");
        if (!questsDir.exists()) {
            questsDir.mkdirs();
            plugin.saveResource("quests/01-poczatki.yml", false);
            plugin.saveResource("quests/02-gornik.yml", false);
        }

        File[] files = questsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                String id = yaml.getString("id");
                if (id == null) continue;

                String displayName = yaml.getString("display-name", id);
                List<String> description = yaml.getStringList("description");
                int order = yaml.getInt("order", 0);

                // Generowanie Ikon na podstawie konfiguracji YAML
                ItemStack locked = loadIcon(yaml, "icons.locked");
                ItemStack active = loadIcon(yaml, "icons.active");
                ItemStack completed = loadIcon(yaml, "icons.completed");

                // Parsowanie Celu Zadania
                String typeStr = yaml.getString("objective.type", "MINE_BLOCKS");
                String target = yaml.getString("objective.target", "STONE");
                int amount = yaml.getInt("objective.amount", 1);

                ObjectiveType type = ObjectiveType.valueOf(typeStr.toUpperCase());
                QuestObjective objective = switch (type) {
                    case MINE_BLOCKS -> new MineBlocksObjective(target, amount);
                    case KILL_MOBS -> new KillMobsObjective(target, amount);
                    case COLLECT_ITEMS -> new CollectItemsObjective(target, amount);
                };

                List<String> rewards = yaml.getStringList("rewards.commands");

                Quest quest = new Quest(id, displayName, description, order, locked, active, completed, objective, rewards);
                quests.put(id, quest);
                questsInOrder.add(quest);
            } catch (Exception e) {
                plugin.getLogger().warning("Blad podczas ladowania pliku zadania: " + file.getName());
                e.printStackTrace();
            }
        }

        // Sortowanie drogi zgodnie z parametrem 'order' z konfiguracji
        questsInOrder.sort(Comparator.comparingInt(Quest::getOrder));
    }

    private ItemStack loadIcon(FileConfiguration yaml, String path) {
        Material material = Material.valueOf(yaml.getString(path + ".material", "STONE").toUpperCase());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getMessageService().parse(yaml.getString(path + ".name", "")));
            List<Component> lore = new ArrayList<>();
            for (String line : yaml.getStringList(path + ".lore")) {
                lore.add(plugin.getMessageService().parse(line));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public List<Quest> getQuestsInOrder() { return questsInOrder; }
    public Quest getQuestById(String id) { return quests.get(id); }
}