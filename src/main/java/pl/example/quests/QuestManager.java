package pl.example.quests;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pl.example.quests.QuestModel.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestManager {
    private final QuestPlugin plugin;
    private final List<QuestCategory> categories = new ArrayList<>();
    private final Map<UUID, Map<String, Integer>> playerProgress = new HashMap<>();
    private final Map<UUID, Set<String>> completedQuests = new HashMap<>();

    public QuestManager(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadQuests() {
        categories.clear();
        File questsFile = new File(plugin.getDataFolder(), "quests.yml");
        if (!questsFile.exists()) {
            plugin.saveResource("quests.yml", false);
        }
        FileConfiguration questsConfig = YamlConfiguration.loadConfiguration(questsFile);
        FileConfiguration mainConfig = plugin.getConfig();

        ConfigurationSection categoriesSection = questsConfig.getConfigurationSection("categories");
        if (categoriesSection == null) return;

        for (String catKey : categoriesSection.getKeys(false)) {
            ConfigurationSection catSec = categoriesSection.getConfigurationSection(catKey);

            String name = color(mainConfig.getString("gui.categories." + catKey + ".name", catKey));
            String icon = mainConfig.getString("gui.categories." + catKey + ".material", "BOOK");

            List<String> lore = new ArrayList<>();
            for (String l : mainConfig.getStringList("gui.categories." + catKey + ".lore")) {
                lore.add(color(l));
            }

            List<Quest> questsList = new ArrayList<>();
            ConfigurationSection questsSection = catSec.getConfigurationSection("quests");
            if (questsSection != null) {
                for (String qKey : questsSection.getKeys(false)) {
                    ConfigurationSection qSec = questsSection.getConfigurationSection(qKey);
                    QuestType type = QuestType.valueOf(qSec.getString("type").toUpperCase());
                    Quest quest = new Quest(
                            qKey,
                            color(qSec.getString("name")),
                            color(qSec.getString("description")),
                            type,
                            qSec.getString("target").toUpperCase(),
                            qSec.getInt("target_amount"),
                            qSec.getDouble("rewards.money", 0.0),
                            qSec.getStringList("rewards.commands"),
                            color(qSec.getString("rewards.description"))
                    );
                    questsList.add(quest);
                }
            }
            categories.add(new QuestCategory(catKey, name, icon, lore, questsList));
        }
    }

    public Quest getQuestById(String questId) {
        for (QuestCategory cat : categories) {
            for (Quest q : cat.getQuests()) {
                if (q.getId().equalsIgnoreCase(questId)) {
                    return q;
                }
            }
        }
        return null;
    }

    public boolean isQuestUnlocked(UUID uuid, Quest quest) {
        QuestCategory category = null;
        for (QuestCategory cat : categories) {
            if (cat.getQuests().contains(quest)) {
                category = cat;
                break;
            }
        }
        if (category == null) return true;

        List<Quest> quests = category.getQuests();
        int index = quests.indexOf(quest);
        if (index == 0) {
            return true;
        }

        Quest previousQuest = quests.get(index - 1);
        return isCompleted(uuid, previousQuest.getId());
    }

    public String getCategoryIdBySlot(int slot) {
        FileConfiguration mainConfig = plugin.getConfig();
        ConfigurationSection categoriesSec = mainConfig.getConfigurationSection("gui.categories");
        if (categoriesSec != null) {
            for (String key : categoriesSec.getKeys(false)) {
                if (categoriesSec.getInt(key + ".slot") == slot) {
                    return key;
                }
            }
        }
        return null;
    }

    public List<QuestCategory> getCategories() { return categories; }

    public QuestCategory getCategory(String id) {
        return categories.stream().filter(c -> c.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        File playerFile = new File(plugin.getDataFolder() + "/data", uuid + ".yml");
        if (!playerFile.exists()) {
            playerProgress.put(uuid, new HashMap<>());
            completedQuests.put(uuid, new HashSet<>());
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        Map<String, Integer> progress = new HashMap<>();
        ConfigurationSection progSec = config.getConfigurationSection("progress");
        if (progSec != null) {
            for (String key : progSec.getKeys(false)) {
                progress.put(key, progSec.getInt(key));
            }
        }
        playerProgress.put(uuid, progress);
        completedQuests.put(uuid, new HashSet<>(config.getStringList("completed")));
    }

    public void savePlayerData(UUID uuid) {
        File playerFile = new File(plugin.getDataFolder() + "/data", uuid + ".yml");
        FileConfiguration config = new YamlConfiguration();

        Map<String, Integer> progress = playerProgress.get(uuid);
        if (progress != null) {
            for (Map.Entry<String, Integer> entry : progress.entrySet()) {
                config.set("progress." + entry.getKey(), entry.getValue());
            }
        }
        Set<String> completed = completedQuests.get(uuid);
        if (completed != null) {
            config.set("completed", new ArrayList<>(completed));
        }

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie udalo sie zapisac danych dla: " + uuid);
        }
    }

    public void saveAndUnloadPlayerData(UUID uuid) {
        savePlayerData(uuid);
        playerProgress.remove(uuid);
        completedQuests.remove(uuid);
    }

    public void setProgress(Player player, Quest quest, int value) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> progress = playerProgress.get(uuid);
        Set<String> completed = completedQuests.get(uuid);
        if (progress == null || completed == null) return;

        if (value >= quest.getRequiredAmount()) {
            forceComplete(player, quest);
        } else {
            progress.put(quest.getId(), value);
            completed.remove(quest.getId());
            savePlayerData(uuid);
        }
    }

    public void resetQuest(Player player, String questId) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> progress = playerProgress.get(uuid);
        Set<String> completed = completedQuests.get(uuid);
        if (progress == null || completed == null) return;

        progress.put(questId, 0);
        completed.remove(questId);
        savePlayerData(uuid);
    }

    public void resetAll(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> progress = playerProgress.get(uuid);
        Set<String> completed = completedQuests.get(uuid);
        if (progress == null || completed == null) return;

        progress.clear();
        completed.clear();
        savePlayerData(uuid);
    }

    public void forceComplete(Player player, Quest quest) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> progress = playerProgress.get(uuid);
        Set<String> completed = completedQuests.get(uuid);
        if (progress == null || completed == null) return;

        progress.put(quest.getId(), quest.getRequiredAmount());
        completed.add(quest.getId());

        String completeMsg = plugin.getMessageManager().getMessage("quest-completed")
                .replace("%quest_name%", quest.getName())
                .replace("%reward_desc%", quest.getRewardDescription());
        player.sendMessage(completeMsg);

        if (quest.getMoneyReward() > 0) {
            plugin.getVaultHook().deposit(player, quest.getMoneyReward());
        }

        for (String cmd : quest.getRewardCommands()) {
            plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(),
                    cmd.replace("%player%", player.getName())
            );
        }
        savePlayerData(uuid);
    }

    public void handleAction(Player player, QuestType type, String target, int amount) {
        UUID uuid = player.getUniqueId();
        Map<String, Integer> progress = playerProgress.get(uuid);
        Set<String> completed = completedQuests.get(uuid);
        if (progress == null || completed == null) return;

        for (QuestCategory cat : categories) {
            for (Quest quest : cat.getQuests()) {
                if (quest.getType() == type && quest.getTarget().equalsIgnoreCase(target)) {
                    if (completed.contains(quest.getId())) continue;
                    if (!isQuestUnlocked(uuid, quest)) continue;

                    int current = progress.getOrDefault(quest.getId(), 0);
                    current += amount;

                    if (current >= quest.getRequiredAmount()) {
                        forceComplete(player, quest);
                    } else {
                        progress.put(quest.getId(), current);
                        plugin.getActionBarNotification().showProgress(player, quest.getName(), current, quest.getRequiredAmount());
                    }
                }
            }
        }
    }

    public int getProgress(UUID uuid, String questId) {
        Map<String, Integer> progress = playerProgress.get(uuid);
        return progress != null ? progress.getOrDefault(questId, 0) : 0;
    }

    public boolean isCompleted(UUID uuid, String questId) {
        Set<String> completed = completedQuests.get(uuid);
        return completed != null && completed.contains(questId);
    }

    public int getQuestTargetAmount(String questId) {
        for (QuestCategory cat : categories) {
            for (Quest q : cat.getQuests()) {
                if (q.getId().equalsIgnoreCase(questId)) {
                    return q.getRequiredAmount();
                }
            }
        }
        return 100;
    }

    // Nowe dynamiczne parsowanie kolorów Hex i tradycyjnych
    private String color(String text) {
        return ColorUtil.color(text);
    }
}