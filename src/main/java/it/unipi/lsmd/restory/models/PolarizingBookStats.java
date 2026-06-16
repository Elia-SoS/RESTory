package it.unipi.lsmd.restory.models;

public class PolarizingBookStats {

    private String title;
    private Integer totalReviews;
    private Double rate1StarPerc;
    private Double rate5StarPerc;

    public PolarizingBookStats() {}

    public PolarizingBookStats(String title, Integer totalReviews, Double rate1StarPerc, Double rate5StarPerc) {
        this.title = title;
        this.totalReviews = totalReviews;
        this.rate1StarPerc = rate1StarPerc;
        this.rate5StarPerc = rate5StarPerc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Double getRate1StarPerc() {
        return rate1StarPerc;
    }

    public void setRate1StarPerc(Double rate1StarPerc) {
        this.rate1StarPerc = rate1StarPerc;
    }

    public Double getRate5StarPerc() {
        return rate5StarPerc;
    }

    public void setRate5StarPerc(Double rate5StarPerc) {
        this.rate5StarPerc = rate5StarPerc;
    }
}
