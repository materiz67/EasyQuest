package pl.materiz66.easyquests.quest;

public enum QuestStatus {
    LOCKED,
    UNLOCKED, // Odblokowane, gotowe do aktywacji w GUI
    ACTIVE,   // Wybrane jako jedyna globalna aktywność gracza
    COMPLETED
}