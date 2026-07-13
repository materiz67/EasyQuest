package pl.materiz66.easyquests.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import pl.materiz66.easyquests.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

public class QuestIconTemplate {
    private final Material material;
    private final String name;
    private final List<String> lore;

    public QuestIconTemplate(Material material, String name, List<String> lore) {
        this.material = material;
        this.name = name;
        this.lore = lore != null ? lore : new ArrayList<>();
    }

    public Material getMaterial() { return material; }
    public String getName() { return name; }
    public List<String> getLore() { return lore; }

    public static QuestIconTemplate fromConfig(ConfigurationSection section) {
        if (section == null) {
            return new QuestIconTemplate(null, "", new ArrayList<>());
        }
        String materialStr = section.getString("material");
        Material material = materialStr != null ? ValidationUtil.getSafeMaterial(materialStr, null) : null;
        String name = section.getString("name", "");
        List<String> lore = section.getStringList("lore");
        return new QuestIconTemplate(material, name, lore);
    }
}