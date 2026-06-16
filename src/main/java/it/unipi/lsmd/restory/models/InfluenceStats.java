package it.unipi.lsmd.restory.models;

/**
 * DTO per rappresentare le statistiche di influenza di un utente
 * Contiene il numero totale di libri recensiti con rating >= 3,
 * il numero di interazioni positive (follower che possiedono quei libri)
 * e il coefficiente di influenza calcolato come ratio tra interazioni positive e libri recensiti
 */
public class InfluenceStats {
    
    private Long totalBookReviewed;
    private Long positiveInteractions;
    private Double influenceCoefficient;

    /**
     * Costruttore completo per InfluenceStats
     */
    public InfluenceStats(Long totalBookReviewed, Long positiveInteractions, Double influenceCoefficient) {
        this.totalBookReviewed = totalBookReviewed;
        this.positiveInteractions = positiveInteractions;
        this.influenceCoefficient = influenceCoefficient;
    }

    /**
     * Costruttore vuoto
     */
    public InfluenceStats() {
    }

    // Getter e Setter
    public Long getTotalBookReviewed() {
        return totalBookReviewed;
    }

    public void setTotalBookReviewed(Long totalBookReviewed) {
        this.totalBookReviewed = totalBookReviewed;
    }

    public Long getPositiveInteractions() {
        return positiveInteractions;
    }

    public void setPositiveInteractions(Long positiveInteractions) {
        this.positiveInteractions = positiveInteractions;
    }

    public Double getInfluenceCoefficient() {
        return influenceCoefficient;
    }

    public void setInfluenceCoefficient(Double influenceCoefficient) {
        this.influenceCoefficient = influenceCoefficient;
    }

    @Override
    public String toString() {
        return "InfluenceStats{" +
                "totalBookReviewed=" + totalBookReviewed +
                ", positiveInteractions=" + positiveInteractions +
                ", influenceCoefficient=" + influenceCoefficient +
                '}';
    }
}
