package org.opencds.cqf.tooling.casereporting.tes;

public class CurationCoverageLevelEntry {

    private String level;

    public String getLevel() {
        return level;
    }

    private String levelReason;

    public String getLevelReason() {
        return levelReason;
    }

    private String author;

    public String getAuthor() {
        return author;
    }

    private String dateTime;

    public String getDateTime() {
        return dateTime;
    }

    private String note;

    public String getNote() {
        return note;
    }

    public CurationCoverageLevelEntry(
            String level,
            String levelReason,
            String author,
            String dateTime,
            String note) {
        this.level = level;
        this.levelReason = levelReason;
        this.author = author;
        this.dateTime = dateTime;
        this.note = note;
    }
}