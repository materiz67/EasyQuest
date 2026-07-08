package pl.example.quests;

import org.bukkit.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String color(String message) {
        if (message == null) return null;

        // Tłumaczenie formatu Hex: &#FFFFFF -> §x§F§F§F§F§F§F (wewnętrzny format Minecrafta)
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString());
        }
        matcher.appendTail(buffer);

        // Tłumaczenie klasycznych kolorów: &a, &c, &l itp.
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}