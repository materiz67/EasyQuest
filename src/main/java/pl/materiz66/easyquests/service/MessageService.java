package pl.materiz66.easyquests.service;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MessageService {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final boolean placeholderApiInstalled;

    public MessageService() {
        // Detekcja obecności PlaceholderAPI na serwerze
        this.placeholderApiInstalled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /**
     * Konwertuje ciąg znaków (String) z tagami MiniMessage na obiekt Component.
     */
    public Component parse(String input) {
        if (input == null) return Component.empty();
        return miniMessage.deserialize(input);
    }

    /**
     * Konwertuje ciąg znaków (String), uwzględniając opcjonalne zmienne z PlaceholderAPI oraz MiniMessage.
     */
    public Component parse(Player player, String input) {
        if (input == null) return Component.empty();

        // Najpierw parsujemy placeholderami (jeśli dostępne)
        String formatted = input;
        if (placeholderApiInstalled && player != null) {
            formatted = PlaceholderAPI.setPlaceholders(player, formatted);
        }

        return miniMessage.deserialize(formatted);
    }

    /**
     * Wysyła sformatowaną wiadomość na czat gracza.
     */
    public void sendMessage(Player player, String rawMessage) {
        player.sendMessage(parse(player, rawMessage));
    }

    /**
     * Wyświetla sformatowany pasek postępu nad ekwipunkiem gracza (ActionBar).
     */
    public void sendActionBar(Player player, String rawMessage) {
        player.sendActionBar(parse(player, rawMessage));
    }
}