package it.unipi.lsmd.restory.models;

public class ReadingSeasonalityStats {
    private Integer month;
    private String genre;
    private Integer startedCount;

    public ReadingSeasonalityStats() {}

    public ReadingSeasonalityStats(Integer month, String genre, Integer startedCount) {
        this.month = month;
        this.genre = genre;
        this.startedCount = startedCount;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getStartedCount() {
        return startedCount;
    }

    public void setStartedCount(Integer startedCount) {
        this.startedCount = startedCount;
    }
}
