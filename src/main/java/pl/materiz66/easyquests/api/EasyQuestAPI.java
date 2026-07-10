package pl.materiz66.easyquests.api;

import org.bukkit.entity.Player;
import pl.materiz66.easyquests.EasyQuestPlugin;
import pl.materiz66.easyquests.quest.Quest;
import pl.materiz66.easyquests.user.QuestProgress;

import java.util.Optional;
import java.util.UUID;

public final class EasyQuestAPI {
    private static EasyQuestPlugin plugin;

    // Prywatny konstruktor zapobiega instancjonowaniu klasy API
    private EasyQuestAPI() {}

    /**
     * Inicjalizuje instancję API. Metoda przeznaczona wyłącznie do użytku wewnętrznego wtyczki.
     *
     * @param instance Główna instancja pluginu EasyQuest
     */
    public static void setPlugin(EasyQuestPlugin instance) {
        if (plugin != null) {
            throw new IllegalStateException("API zostalo juz uprzednio zainicjalizowane!");
        }
        plugin = instance;
    }

    /**
     * Pobiera instancję głównego pluginu.
     *
     * @return Instancja JavaPlugin
     */
    public static EasyQuestPlugin getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("API nie zostalo jeszcze zainicjalizowane przez rdzen wtyczki!");
        }
        return plugin;
    }

    /**
     * Pobiera dane o postępach zadań gracza na podstawie jego unikalnego identyfikatora UUID.
     * Dane pobierane są asynchronicznie z pamięci podręcznej (Cache).
     *
     * @param playerUuid UUID gracza, którego postępy chcemy odczytać
     * @return Kontener Optional zawierający QuestProgress, lub pusty jeśli gracz jest offline
     */
    public static Optional<QuestProgress> getPlayerProgress(UUID playerUuid) {
        return Optional.ofNullable(getPlugin().getCacheManager().getCachedProgress(playerUuid));
    }

    /**
     * Pobiera dane o postępach zadań gracza online.
     *
     * @param player Obiekt gracza Bukkit
     * @return Kontener Optional zawierający QuestProgress
     */
    public static Optional<QuestProgress> getPlayerProgress(Player player) {
        return getPlayerProgress(player.getUniqueId());
    }

    /**
     * Pobiera zarejestrowane zadanie (Quest) na podstawie jego identyfikatora tekstowego.
     *
     * @param questId Unikalny identyfikator questa (np. "01-poczatki")
     * @return Kontener Optional zawierający dane konfiguracji zadania
     */
    public static Optional<Quest> getQuest(String questId) {
        return Optional.ofNullable(getPlugin().getQuestManager().getQuestById(questId));
    }
}