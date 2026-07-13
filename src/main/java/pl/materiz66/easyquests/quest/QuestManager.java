package pl.materiz66.easyquests.quest;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.materiz66.easyquests.EasyQuests;
import pl.materiz66.easyquests.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class QuestManager {
    private final EasyQuests plugin;
    private final Map<String, QuestCategory> categories = new LinkedHashMap<>();
    private final Map<String, Quest> quests = new HashMap<>();

    public QuestManager(EasyQuests plugin) {
        this.plugin = plugin;
    }

    public void load() {
        categories.clear();
        quests.clear();

        File categoriesDir = new File(plugin.getDataFolder(), "categories");
        if (!categoriesDir.exists()) {
            categoriesDir.mkdirs();
            // Wypakowanie wszystkich 7 zdefiniowanych kategorii przy pierwszym uruchomieniu
            saveDefaultCategory("gornik.yml");
            saveDefaultCategory("kowal.yml");
            saveDefaultCategory("mysliwy.yml");
            saveDefaultCategory("drwal.yml");
            saveDefaultCategory("rolnik.yml");
            saveDefaultCategory("rybak.yml");
            saveDefaultCategory("alchemik.yml");
        } else {
            File[] files = categoriesDir.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files == null || files.length == 0) {
                saveDefaultCategory("gornik.yml");
                saveDefaultCategory("kowal.yml");
                saveDefaultCategory("mysliwy.yml");
                saveDefaultCategory("drwal.yml");
                saveDefaultCategory("rolnik.yml");
                saveDefaultCategory("rybak.yml");
                saveDefaultCategory("alchemik.yml");
            }
        }

        File[] files = categoriesDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String categoryId = file.getName().replace(".yml", "");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            String displayName = config.getString("display-name", categoryId);
            String materialStr = config.getString("material", "BOOK");
            Material material = ValidationUtil.getSafeMaterial(materialStr, Material.BOOK);

            int slot = config.getInt("slot", 10);
            List<String> lore = config.getStringList("lore");

            QuestCategory category = new QuestCategory(categoryId, displayName, material, slot, lore);

            List<?> rawQuestsList = config.getList("quests");
            if (rawQuestsList != null) {
                for (Object obj : rawQuestsList) {
                    if (obj instanceof Map<?, ?> questMap) {
                        String id = (String) questMap.get("id");
                        String qDisplayName = (String) questMap.get("display-name");

                        List<String> description = new ArrayList<>();
                        Object descObj = questMap.get("description");
                        if (descObj instanceof List<?>) {
                            for (Object line : (List<?>) descObj) {
                                description.add(String.valueOf(line));
                            }
                        } else if (descObj instanceof String) {
                            description.add((String) descObj);
                        }

                        String qMaterialStr = (String) questMap.get("material");
                        Material qMaterial = ValidationUtil.getSafeMaterial(qMaterialStr, Material.PAPER);

                        QuestObjective objective = null;
                        Object objSection = questMap.get("objective");
                        if (objSection instanceof Map<?, ?> objMap) {
                            String type = (String) objMap.get("type");
                            String target = String.valueOf(objMap.get("target"));
                            int amount = objMap.get("amount") instanceof Number ? ((Number) objMap.get("amount")).intValue() : 1;
                            objective = new QuestObjective(type, target, amount);
                        }

                        // Wczytywanie surowych komend nagród wykonywanych przez konsolę
                        List<String> rewards = new ArrayList<>();
                        Object rewardsObj = questMap.get("rewards");
                        if (rewardsObj instanceof List<?>) {
                            for (Object r : (List<?>) rewardsObj) {
                                rewards.add(String.valueOf(r));
                            }
                        }

                        // Wczytywanie opisu nagród w GUI (rewards-display)
                        List<String> rewardsDisplay = new ArrayList<>();
                        Object rewardsDisplayObj = questMap.get("rewards-display");
                        if (rewardsDisplayObj instanceof List<?>) {
                            for (Object r : (List<?>) rewardsDisplayObj) {
                                rewardsDisplay.add(String.valueOf(r));
                            }
                        } else {
                            // INTELIGENTNY FALLBACK: Jeśli admin nie zdefiniował sekcji rewards-display,
                            // wtyczka automatycznie tłumaczy komendy /give oraz /xp na polskie napisy w GUI.
                            for (String command : rewards) {
                                String friendly = parseCommandToFriendly(command);
                                if (friendly != null) {
                                    rewardsDisplay.add(friendly);
                                }
                            }
                        }

                        if (id != null && objective != null) {
                            Quest quest = new Quest(id, qDisplayName, description, qMaterial, objective, rewards, rewardsDisplay, category);
                            category.addQuest(quest);
                            quests.put(id, quest);
                        }
                    }
                }
            }

            categories.put(categoryId, category);
            plugin.getLogger().info("Pomyślnie załadowano kategorię: " + categoryId + " z " + category.getQuests().size() + " zadaniami.");
        }
    }

    private void saveDefaultCategory(String fileName) {
        File file = new File(plugin.getDataFolder(), "categories/" + fileName);
        if (!file.exists()) {
            try (InputStream in = plugin.getResource("categories/" + fileName)) {
                if (in == null) {
                    plugin.getLogger().warning("Brak domyślnego zasobu w JAR: categories/" + fileName);
                    return;
                }
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Nie udało się zapisać domyślnej kategorii " + fileName, e);
            }
        }
    }

    /**
     * Tłumaczy standardowe komendy konsolowe /give oraz /xp na czytelny opis nagród w GUI
     */
    private String parseCommandToFriendly(String command) {
        String cmd = command.toLowerCase().trim();
        if (cmd.startsWith("give %player% ")) {
            String rest = command.substring("give %player% ".length()).trim();
            String[] parts = rest.split(" ");
            if (parts.length >= 1) {
                String item = parts[0];
                int amount = 1;
                if (parts.length >= 2) {
                    try {
                        amount = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException ignored) {}
                }
                return "<gray>» <white>" + amount + "x " + ValidationUtil.getFriendlyName(item);
            }
        } else if (cmd.startsWith("xp give %player% ")) {
            String rest = command.substring("xp give %player% ".length()).trim();
            String[] parts = rest.split(" ");
            if (parts.length >= 1) {
                try {
                    int amount = Integer.parseInt(parts[0]);
                    return "<gray>» <yellow>" + amount + " XP";
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    public Map<String, QuestCategory> getCategories() { return categories; }
    public Map<String, Quest> getQuests() { return quests; }
    public QuestCategory getCategory(String id) { return categories.get(id); }
    public Quest getQuest(String id) { return quests.get(id); }
}