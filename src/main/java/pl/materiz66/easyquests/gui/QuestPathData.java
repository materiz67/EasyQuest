package pl.materiz66.easyquests.gui;

import pl.materiz66.easyquests.quest.QuestCategory;

public class QuestPathData {
    private final QuestCategory category;
    private final int page;

    public QuestPathData(QuestCategory category, int page) {
        this.category = category;
        this.page = page;
    }

    public QuestCategory getCategory() { return category; }
    public int getPage() { return page; }
}