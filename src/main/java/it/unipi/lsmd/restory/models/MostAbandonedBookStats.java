package it.unipi.lsmd.restory.models;

public class MostAbandonedBookStats {

    private String title;
    private Integer utentiCheLoIniziano;
    private Integer utentiCheLoDroppano;
    private Double percentualeDrop;

    public MostAbandonedBookStats() {}

    public MostAbandonedBookStats(String title, Integer utentiCheLoIniziano, Integer utentiCheLoDroppano, Double percentualeDrop) {
        this.title = title;
        this.utentiCheLoIniziano = utentiCheLoIniziano;
        this.utentiCheLoDroppano = utentiCheLoDroppano;
        this.percentualeDrop = percentualeDrop;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getUtentiCheLoIniziano() {
        return utentiCheLoIniziano;
    }

    public void setUtentiCheLoIniziano(Integer utentiCheLoIniziano) {
        this.utentiCheLoIniziano = utentiCheLoIniziano;
    }

    public Integer getUtentiCheLoDroppano() {
        return utentiCheLoDroppano;
    }

    public void setUtentiCheLoDroppano(Integer utentiCheLoDroppano) {
        this.utentiCheLoDroppano = utentiCheLoDroppano;
    }

    public Double getPercentualeDrop() {
        return percentualeDrop;
    }

    public void setPercentualeDrop(Double percentualeDrop) {
        this.percentualeDrop = percentualeDrop;
    }
}
