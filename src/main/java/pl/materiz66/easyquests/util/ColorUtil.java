package pl.materiz66.easyquests.util;

import net.md_5.bungee.api.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Konwertuje tradycyjne kody &#ffffff na format MiniMessage <#ffffff>
     */
    public static String convertToMiniMessage(String input) {
        if (input == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<#" + matcher.group(1) + ">");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String formatLegacy(String input) {
        if (input == null) return "";

        // Przekształcamy kody Bungee Hex na MiniMessage przed parsowaniem
        String miniMessageFormatted = convertToMiniMessage(input);

        // Jeśli ciąg zawiera tagi MiniMessage, pozwalamy mu go zserializować
        if (miniMessageFormatted.contains("<") && miniMessageFormatted.contains(">")) {
            Component component = MINI_MESSAGE.deserialize(miniMessageFormatted);
            return LegacyComponentSerializer.builder().character('&').build().serialize(component)
                    .replace("&", "§"); // Zamiana na natywny symbol sekcji Bukkit
        }

        // Klasyczny fallback Bungee/Bukkit
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            String color = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + color).toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static Component format(String input) {
        if (input == null) return Component.empty();
        String legacy = formatLegacy(input);
        return LEGACY_SERIALIZER.deserialize(legacy);
    }

    /**
     * Generuje tekstowy pasek postępu [■■■■■□□□]
     */
    public static String getProgressBar(int current, int max, int totalBars, char barChar, String completedColor, String uncompletedColor) {
        if (max <= 0) return "";
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);
        if (progressBars > totalBars) progressBars = totalBars;
        if (progressBars < 0) progressBars = 0;

        StringBuilder sb = new StringBuilder();
        sb.append(completedColor);
        for (int i = 0; i < progressBars; i++) {
            sb.append(barChar);
        }
        sb.append(uncompletedColor);
        for (int i = progressBars; i < totalBars; i++) {
            sb.append(barChar);
        }
        return sb.toString();
    }
}