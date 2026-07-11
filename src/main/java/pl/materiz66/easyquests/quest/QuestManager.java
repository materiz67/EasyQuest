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
import pl.materiz66.easyquests.objective.types.CraftItemsObjective;
import pl.materiz66.easyquests.objective.types.CollectItemsObjective;
import pl.materiz66.easyquests.objective.types.KillMobsObjective;
import pl.materiz66.easyquests.objective.types.MineBlocksObjective;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;

/**
 * Ładuje, przechowuje i zarządza wszystkimi zadaniami pluginu.
 * Zadania są wczytywane z folderu plugins/EasyQuests/quests/*.yml.
 * Domyślne pliki są kopiowane z jar (resources/quests/) przy pierwszym uruchomieniu.
 */
public class QuestManager {
    private final EasyQuestPlugin plugin;
    private final Map<String, Quest> quests = new HashMap<>();
    private final Map<String, List<Quest>> questsByCategory = new LinkedHashMap<>();

    public QuestManager(EasyQuestPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadQuests() {
        quests.clear();
        questsByCategory.clear();

        File questsDir = new File(plugin.getDataFolder(), "quests");
        if (!questsDir.exists()) {
            questsDir.mkdirs();
        }

        // Kopiuj domyślne pliki zadań z resources/ jeśli nie istnieją
        copyDefaultQuestFiles(questsDir);

        // Ładuj wszystkie pliki .yml z folderu quests/
        File[] files = questsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Brak plików zadań w folderze quests/! " +
                    "Użyj domyślnych lub stwórz własne.");
            return;
        }

        // Sortuj pliki alfabetycznie dla spójnego kolejności
        Arrays.sort(files);

        for (File file : files) {
            try {
                loadQuestFile(file);
            } catch (Exception e) {
                plugin.getLogger().severe("Błąd podczas ładowania pliku zadania: " + file.getName());
                e.printStackTrace();
            }
        }

        // Sortuj zadania w każdej kategorii według pola 'order'
        for (List<Quest> categoryQuests : questsByCategory.values()) {
            categoryQuests.sort(Comparator.comparingInt(Quest::getOrder));
        }

        plugin.getLogger().info("Załadowano " + quests.size() + " zadań w " +
                questsByCategory.size() + " kategoriach.");
    }

    private void loadQuestFile(File file) {
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        String id = yaml.getString("id");
        if (id == null || id.isBlank()) {
            plugin.getLogger().warning("Pominięto plik " + file.getName() + " – brak pola 'id'.");
            return;
        }

        String category = yaml.getString("category", "gornik").toLowerCase();
        String displayName = yaml.getString("display-name", id);
        List<String> description = yaml.getStringList("description");
        int order = yaml.getInt("order", 0);

        ItemStack locked = loadIcon(yaml, "icons.locked");
        ItemStack active = loadIcon(yaml, "icons.active");
        ItemStack completed = loadIcon(yaml, "icons.completed");

        String typeStr = yaml.getString("objective.type", "MINE_BLOCKS");
        String target = yaml.getString("objective.target", "STONE");
        int amount = yaml.getInt("objective.amount", 1);

        ObjectiveType type;
        try {
            type = ObjectiveType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Nieznany typ celu '" + typeStr + "' w pliku " + file.getName()
                    + ". Używam MINE_BLOCKS jako domyślnego.");
            type = ObjectiveType.MINE_BLOCKS;
        }

        QuestObjective objective = switch (type) {
            case MINE_BLOCKS -> new MineBlocksObjective(target, amount);
            case KILL_MOBS -> new KillMobsObjective(target, amount);
            case COLLECT_ITEMS -> new CollectItemsObjective(target, amount);
            case CRAFT_ITEMS -> new CraftItemsObjective(target, amount);
        };

        List<String> rewards = yaml.getStringList("rewards.commands");

        Quest quest = new Quest(id, category, displayName, description, order,
                locked, active, completed, objective, rewards);
        quests.put(id, quest);
        questsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(quest);
    }

    /**
     * Kopiuje domyślne pliki zadań z resources/quests/ do folderu pluginu.
     * Używa ClassLoader zamiast ręcznego FileWriter.
     */
    private void copyDefaultQuestFiles(File questsDir) {
        String[] defaultFiles = {
                "01-poczatki.yml",
                "02-gornik.yml",
                "03-lowca.yml",
                // Kategorii generowanych programowo
        };

        for (String fileName : defaultFiles) {
            File dest = new File(questsDir, fileName);
            if (!dest.exists()) {
                try (InputStream is = plugin.getResource("quests/" + fileName)) {
                    if (is != null) {
                        try (OutputStream os = Files.newOutputStream(dest.toPath())) {
                            is.transferTo(os);
                        }
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("Nie udało się skopiować domyślnego pliku: " + fileName);
                }
            }
        }

        // Generuj pozostałe zadania programowo (jeśli nie istnieją)
        generateDefaultQuests(questsDir);
    }

    /**
     * Programowe generowanie domyślnych plików zadań dla kategorii nieposiadających pliku w resources.
     */
    private void generateDefaultQuests(File questsDir) {
        // ── GÓRNICTWO ──
        writeQuestFile(new File(questsDir, "04-gornik-diament.yml"),
                "04-gornik-diament", "gornik", "<aqua>Diamentowy Blask",
                List.of("<gray>Zejdź na sam dół świata i wydobądź najcenniejszy kruszec."),
                3, "DIAMOND_ORE", "MINE_BLOCKS", "DIAMOND_ORE", 3,
                List.of("give %player% diamond 1", "xp give %player% 300"));

        // ── MYŚLISTWO ──
        writeQuestFile(new File(questsDir, "05-mysliwy-szkielet.yml"),
                "05-mysliwy-szkielet", "mysliwy", "<white>Lustracja Kości",
                List.of("<gray>Pokonaj łuczników nawiedzających nocy."),
                2, "SKELETON_SKULL", "KILL_MOBS", "SKELETON", 5,
                List.of("give %player% bow 1", "give %player% arrow 16"));

        writeQuestFile(new File(questsDir, "06-mysliwy-creeper.yml"),
                "06-mysliwy-creeper", "mysliwy", "<green>Syczący Koszmar",
                List.of("<gray>Wyeliminuj wybuchowe creepery zanim wyrządzą szkody."),
                3, "CREEPER_HEAD", "KILL_MOBS", "CREEPER", 3,
                List.of("give %player% tnt 2", "xp give %player% 200"));

        // ── ROLNICTWO ──
        writeQuestFile(new File(questsDir, "07-rolnik-pszenica.yml"),
                "07-rolnik-pszenica", "rolnik", "<green>Zielona Dolina",
                List.of("<gray>Zbierz do ekwipunku plony pszenicy."),
                1, "WHEAT", "COLLECT_ITEMS", "WHEAT", 20,
                List.of("give %player% emerald 3"));

        writeQuestFile(new File(questsDir, "08-rolnik-ziemniaki.yml"),
                "08-rolnik-ziemniaki", "rolnik", "<gold>Ziemniaczany Raj",
                List.of("<gray>Zgromadź zapasy ziemniaków z upraw."),
                2, "POTATO", "COLLECT_ITEMS", "POTATO", 20,
                List.of("give %player% baked_potato 16"));

        writeQuestFile(new File(questsDir, "09-rolnik-marchewki.yml"),
                "09-rolnik-marchewki", "rolnik", "<gold>Złote Runo",
                List.of("<gray>Zbierz dorodne marchewki ze swojego pola."),
                3, "CARROT", "COLLECT_ITEMS", "CARROT", 20,
                List.of("give %player% golden_carrot 3"));

        // ── RYBACTWO ──
        writeQuestFile(new File(questsDir, "10-rybak-dorsz.yml"),
                "10-rybak-dorsz", "rybak", "<blue>Morski Łowca",
                List.of("<gray>Złów i zbierz surowego dorsza z toni wodnej."),
                1, "COD", "COLLECT_ITEMS", "COD", 8,
                List.of("give %player% fishing_rod 1"));

        writeQuestFile(new File(questsDir, "11-rybak-losos.yml"),
                "11-rybak-losos", "rybak", "<aqua>Dziki Potok",
                List.of("<gray>Wyłów czerwonego łososia z rzek i oceanów."),
                2, "SALMON", "COLLECT_ITEMS", "SALMON", 5,
                List.of("give %player% water_bucket 1"));

        writeQuestFile(new File(questsDir, "12-rybak-rozdymka.yml"),
                "12-rybak-rozdymka", "rybak", "<red>Egzotyczna Trucizna",
                List.of("<gray>Złów rzadkie i niebezpieczne rozdymki."),
                3, "PUFFERFISH", "COLLECT_ITEMS", "PUFFERFISH", 2,
                List.of("effect give %player% water_breathing 300"));

        // ── DRWALNICTWO ──
        writeQuestFile(new File(questsDir, "13-drwal-dab.yml"),
                "13-drwal-dab", "drwal", "<dark_green>Dąb Bartny",
                List.of("<gray>Ścień pnie dębu, aby pozyskać drewno budowlane."),
                1, "OAK_LOG", "MINE_BLOCKS", "OAK_LOG", 16,
                List.of("give %player% golden_axe 1"));

        writeQuestFile(new File(questsDir, "14-drwal-brzoza.yml"),
                "14-drwal-brzoza", "drwal", "<white>Srebrzysta Kora",
                List.of("<gray>Ścień pnie brzozy do produkcji papieru."),
                2, "BIRCH_LOG", "MINE_BLOCKS", "BIRCH_LOG", 16,
                List.of("give %player% iron_axe 1"));

        writeQuestFile(new File(questsDir, "15-drwal-ciemny-dab.yml"),
                "15-drwal-ciemny-dab", "drwal", "<dark_green>Mroczny Bór",
                List.of("<gray>Wytnij pnie ciemnego dębu z puszcz."),
                3, "DARK_OAK_LOG", "MINE_BLOCKS", "DARK_OAK_LOG", 32,
                List.of("give %player% diamond_axe 1", "xp give %player% 150"));

        // ── KOWALSTWO ──
        writeQuestFile(new File(questsDir, "16-kowal-miecz.yml"),
                "16-kowal-miecz", "kowal", "<yellow>Pierwszy Oręż",
                List.of("<gray>Wykuj żelazny miecz na stole rzemieślniczym."),
                1, "IRON_SWORD", "CRAFT_ITEMS", "IRON_SWORD", 1,
                List.of("give %player% iron_ingot 3", "xp give %player% 75"));

        writeQuestFile(new File(questsDir, "17-kowal-zbroja.yml"),
                "17-kowal-zbroja", "kowal", "<yellow>Żelazna Zbroja",
                List.of("<gray>Wykuj żelazny napierśnik – ochronę swojego ciała."),
                2, "IRON_CHESTPLATE", "CRAFT_ITEMS", "IRON_CHESTPLATE", 1,
                List.of("give %player% iron_ingot 5", "xp give %player% 150"));

        writeQuestFile(new File(questsDir, "18-kowal-helm.yml"),
                "18-kowal-helm", "kowal", "<aqua>Diamentowy Hełm",
                List.of("<gray>Wykuj hełm z najtwardszego minerału."),
                3, "DIAMOND_HELMET", "CRAFT_ITEMS", "DIAMOND_HELMET", 1,
                List.of("give %player% diamond 2", "xp give %player% 400"));
    }

    private void writeQuestFile(File file, String id, String category, String name,
                                List<String> desc, int order, String mat, String type,
                                String target, int amount, List<String> rewards) {
        if (file.exists()) return;
        String cleanName = name.replaceAll("<[^>]*>", "");
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id: \"" + id + "\"");
            lines.add("category: \"" + category + "\"");
            lines.add("display-name: \"" + name + "\"");
            lines.add("description:");
            for (String d : desc) lines.add("  - \"" + d + "\"");
            lines.add("order: " + order);
            lines.add("");
            lines.add("icons:");
            lines.add("  locked:");
            lines.add("    material: \"BARRIER\"");
            lines.add("    name: \"<red>✖ Zablokowane Zadanie\"");
            lines.add("    lore:");
            lines.add("      - \"<gray>Musisz ukończyć poprzednie kroki.</gray>\"");
            lines.add("  active:");
            lines.add("    material: \"" + mat + "\"");
            lines.add("    name: \"<yellow>⚒ W trakcie: " + cleanName + "\"");
            lines.add("    lore:");
            lines.add("      - \"<gray>Postęp: <gold>%progress%/" + amount + "</gold>\"");
            lines.add("      - \"\"");
            lines.add("      - \"<yellow>▶ Kliknij po szczegóły\"");
            lines.add("  completed:");
            lines.add("    material: \"EMERALD\"");
            lines.add("    name: \"<green>✔ Ukończono: " + cleanName + "\"");
            lines.add("    lore:");
            lines.add("      - \"<gray>Zadanie zostało pomyślnie zrealizowane.</gray>\"");
            lines.add("");
            lines.add("objective:");
            lines.add("  type: \"" + type + "\"");
            lines.add("  target: \"" + target + "\"");
            lines.add("  amount: " + amount);
            lines.add("");
            lines.add("rewards:");
            lines.add("  commands:");
            for (String rew : rewards) lines.add("    - \"" + rew + "\"");

            Files.write(file.toPath(), lines, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().severe("Nie udało się zapisać pliku zadania: " + file.getName());
            e.printStackTrace();
        }
    }

    private ItemStack loadIcon(FileConfiguration yaml, String path) {
        String matName = yaml.getString(path + ".material", "STONE");
        Material material;
        try {
            material = Material.valueOf(matName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Nieznany materiał '" + matName + "' w ścieżce '" + path
                    + "'. Używam STONE.");
            material = Material.STONE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String rawName = yaml.getString(path + ".name", "");
            meta.displayName(plugin.getMessageService().parse(rawName));

            List<Component> lore = new ArrayList<>();
            for (String line : yaml.getStringList(path + ".lore")) {
                lore.add(plugin.getMessageService().parse(line));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public List<Quest> getQuestsByCategory(String categoryId) {
        return questsByCategory.getOrDefault(categoryId.toLowerCase(), Collections.emptyList());
    }

    public Quest getQuestById(String id) {
        return quests.get(id);
    }

    public Map<String, Quest> getAllQuests() {
        return Collections.unmodifiableMap(quests);
    }
}