package pl.materiz66.easyquests.util;

import org.bukkit.Material;
import org.bukkit.Sound;

public class ValidationUtil {

    public static Material getSafeMaterial(String materialName, Material fallback) {
        if (materialName == null) return fallback;
        Material material = Material.matchMaterial(materialName.toUpperCase());
        return material != null ? material : fallback;
    }

    public static Sound getSafeSound(String soundName, Sound fallback) {
        if (soundName == null) return fallback;
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    /**
     * Tłumaczy surowe nazwy Minecraft (materiały, potwory, itp.) na ładne polskie nazwy w GUI
     */
    public static String getFriendlyName(String target) {
        if (target == null) return "";

        switch (target.toUpperCase()) {
            case "COAL_ORE": return "Ruda Węgla";
            case "IRON_ORE": return "Ruda Żelaza";
            case "LAPIS_ORE": return "Ruda Lapis Lazuli";
            case "GOLD_ORE": return "Ruda Złota";
            case "REDSTONE_ORE": return "Ruda Redstone";
            case "DIAMOND_ORE": return "Ruda Diamentu";
            case "ZOMBIE": return "Zombie";
            case "SKELETON": return "Szkielet";
            case "SPIDER": return "Pająk";
            case "CREEPER": return "Creeper";
            case "ENDERMAN": return "Enderman";
            case "OAK_LOG": return "Kłoda Dębu";
            case "BIRCH_LOG": return "Kłoda Brzozy";
            case "SPRUCE_LOG": return "Kłoda Świerku";
            case "DARK_OAK_LOG": return "Kłoda Ciemnego Dębu";
            case "MANGROVE_LOG": return "Kłoda Mangrowca";
            case "CHERRY_LOG": return "Kłoda Wiśni";
            case "WHEAT": return "Pszenica";
            case "CARROT": return "Marchewka";
            case "POTATO": return "Ziemniak";
            case "PUMPKIN": return "Dynia";
            case "MELON": return "Arbuz";
            case "NETHER_WART": return "Netherowa Brodawka";
            case "COD": return "Dorsz";
            case "SALMON": return "Łosoś";
            case "PUFFERFISH": return "Rozdymka";
            case "TROPICAL_FISH": return "Ryba Tropikalna";
            case "SADDLE": return "Siodło";
            case "GLASS_BOTTLE": return "Szklana Butelka";
            case "AWKWARD": return "Niezręczna Mikstura";
            case "INSTANT_HEALTH": return "Mikstura Zdrowia";
            case "STRENGTH": return "Mikstura Siły";
            case "INVISIBILITY": return "Mikstura Niewidzialności";
            default:
                // Fallback na wypadek gdyby brakowało powyższego tłumaczenia
                String formatted = target.replace("_", " ").toLowerCase();
                String[] words = formatted.split(" ");
                StringBuilder sb = new StringBuilder();
                for (String word : words) {
                    if (word.length() > 0) {
                        sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
                    }
                }
                return sb.toString().trim();
        }
    }
}