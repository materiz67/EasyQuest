package pl.materiz66.easyquests.menu;

import java.util.List;

/**
 * Centralna klasa stałych używanych w GUI zadań.
 * Eliminuje duplikację listy ROAD_SLOTS pomiędzy QuestRoadGUI a InventoryClickListener.
 */
public final class QuestGUIConstants {

    private QuestGUIConstants() {}

    /**
     * Sloty ścieżki zadań w kolejności "węża" (snake path) dla menu 54-slotowego.
     * Zadania są umieszczane w kolumnach 2-5 (sloty 10-43), omijając ramki.
     *
     * Wizualizacja (54 sloty, 6 rzędów):
     *  [■][■][■][■][■][■][■][■][■]  rząd 1 – tło
     *  [■][1][2][3][4][5][6][■][■]  rząd 2 – zadania
     *  [■][7][6][5][4][3][8][■][■]  rząd 3 – ścieżka w prawo
     *  ...itd.
     */
    public static final List<Integer> ROAD_SLOTS = List.of(
            10, 11, 12, 13, 14, 15,
            24, 23, 22, 21, 20, 19,
            28, 29, 30, 31, 32, 33,
            42, 41, 40, 39, 38, 37
    );

    /** Maksymalna liczba zadań wyświetlanych na ścieżce. */
    public static final int MAX_ROAD_QUESTS = ROAD_SLOTS.size();
}
