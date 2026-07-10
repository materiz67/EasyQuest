package pl.materiz66.easyquests.user;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserCacheManager {
    // Thread-safe mapa zapobiegająca problemom przy asynchronicznym zapisie/odczycie danych
    private final Map<UUID, QuestProgress> cache = new ConcurrentHashMap<>();

    /**
     * Zapisuje tymczasowo postęp gracza w pamięci RAM.
     */
    public void cacheProgress(UUID uuid, QuestProgress progress) {
        cache.put(uuid, progress);
    }

    /**
     * Pobiera postęp gracza bezpośrednio z pamięci RAM.
     */
    public QuestProgress getCachedProgress(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Usuwa dane gracza z pamięci podręcznej (wywoływane np. podczas wyjścia z serwera).
     */
    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }
}