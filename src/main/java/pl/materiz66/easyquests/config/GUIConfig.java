package pl.materiz66.easyquests.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Wczytuje i przechowuje konfigurację GUI (rozmiary menu, materiały tła, sloty przycisków).
 * Sekcja {@code gui:} w config.yml.
 */
public class GUIConfig {

    // --- Menu Kategorii ---
    private String categoryMenuTitle;
    private int categoryMenuSize;
    private String categoryMenuFillMaterial;

    // --- Menu Ścieżki Zadań ---
    private String roadMenuTitle;
    private int roadMenuSize;
    private String roadMenuFillMaterial;
    private int roadBackButtonSlot;
    private String roadBackButtonMaterial;
    private String roadBackButtonName;

    // --- Menu Szczegółów Zadania ---
    private String detailsMenuTitle;
    private int detailsMenuSize;
    private String detailsMenuFillMaterial;
    private int detailsInfoItemSlot;
    private String detailsInfoItemMaterial;
    private String detailsInfoItemName;
    private int detailsBackButtonSlot;
    private String detailsBackButtonMaterial;
    private String detailsBackButtonName;

    public void load(FileConfiguration config) {
        // Category menu
        this.categoryMenuTitle = config.getString("gui.category-menu.title",
                "<gradient:gold:yellow>Kategorie Zadań</gradient>");
        this.categoryMenuSize = validateSize(config.getInt("gui.category-menu.size", 27));
        this.categoryMenuFillMaterial = config.getString("gui.category-menu.fill-material",
                "GRAY_STAINED_GLASS_PANE");

        // Road menu
        this.roadMenuTitle = config.getString("gui.road-menu.title",
                "<gray>Ścieżka: <gold>{category}</gold></gray>");
        this.roadMenuSize = validateSize(config.getInt("gui.road-menu.size", 54));
        this.roadMenuFillMaterial = config.getString("gui.road-menu.fill-material",
                "GRAY_STAINED_GLASS_PANE");
        this.roadBackButtonSlot = config.getInt("gui.road-menu.back-button-slot", 45);
        this.roadBackButtonMaterial = config.getString("gui.road-menu.back-button-material", "ARROW");
        this.roadBackButtonName = config.getString("gui.road-menu.back-button-name",
                "<red>⬅ Powrót do Kategorii");

        // Details menu
        this.detailsMenuTitle = config.getString("gui.details-menu.title",
                "<gray>Zadanie: <yellow>{quest}</yellow></gray>");
        this.detailsMenuSize = validateSize(config.getInt("gui.details-menu.size", 27));
        this.detailsMenuFillMaterial = config.getString("gui.details-menu.fill-material",
                "GRAY_STAINED_GLASS_PANE");
        this.detailsInfoItemSlot = config.getInt("gui.details-menu.info-item-slot", 13);
        this.detailsInfoItemMaterial = config.getString("gui.details-menu.info-item-material", "WRITTEN_BOOK");
        this.detailsInfoItemName = config.getString("gui.details-menu.info-item-name",
                "<gold>✦ Informacje i Cele");
        this.detailsBackButtonSlot = config.getInt("gui.details-menu.back-button-slot", 18);
        this.detailsBackButtonMaterial = config.getString("gui.details-menu.back-button-material", "ARROW");
        this.detailsBackButtonName = config.getString("gui.details-menu.back-button-name",
                "<red>⬅ Powrót do Ścieżki");
    }

    /** Walidacja rozmiaru inwentarza – musi być wielokrotnością 9 i mieścić się w [9, 54]. */
    private int validateSize(int size) {
        if (size < 9) return 9;
        if (size > 54) return 54;
        return (size / 9) * 9;
    }

    // --- Gettery ---
    public String getCategoryMenuTitle() { return categoryMenuTitle; }
    public int getCategoryMenuSize() { return categoryMenuSize; }
    public String getCategoryMenuFillMaterial() { return categoryMenuFillMaterial; }

    public String getRoadMenuTitle() { return roadMenuTitle; }
    public int getRoadMenuSize() { return roadMenuSize; }
    public String getRoadMenuFillMaterial() { return roadMenuFillMaterial; }
    public int getRoadBackButtonSlot() { return roadBackButtonSlot; }
    public String getRoadBackButtonMaterial() { return roadBackButtonMaterial; }
    public String getRoadBackButtonName() { return roadBackButtonName; }

    public String getDetailsMenuTitle() { return detailsMenuTitle; }
    public int getDetailsMenuSize() { return detailsMenuSize; }
    public String getDetailsMenuFillMaterial() { return detailsMenuFillMaterial; }
    public int getDetailsInfoItemSlot() { return detailsInfoItemSlot; }
    public String getDetailsInfoItemMaterial() { return detailsInfoItemMaterial; }
    public String getDetailsInfoItemName() { return detailsInfoItemName; }
    public int getDetailsBackButtonSlot() { return detailsBackButtonSlot; }
    public String getDetailsBackButtonMaterial() { return detailsBackButtonMaterial; }
    public String getDetailsBackButtonName() { return detailsBackButtonName; }
}
