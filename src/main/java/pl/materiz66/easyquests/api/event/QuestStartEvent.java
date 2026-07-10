package pl.materiz66.easyquests.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.materiz66.easyquests.quest.Quest;

public class QuestStartEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Quest quest;

    public QuestStartEvent(@NotNull Player player, @NotNull Quest quest) {
        this.player = player;
        this.quest = quest;
    }

    /**
     * Zwraca gracza, który rozpoczął zadanie.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Zwraca dane zadania, które zostało rozpoczęte.
     */
    @NotNull
    public Quest getQuest() {
        return quest;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}