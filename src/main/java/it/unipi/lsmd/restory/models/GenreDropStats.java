package it.unipi.lsmd.restory.models;

public class GenreDropStats {
    private String genre;
    private Double percentualeGenereDroppato;
    private Integer numeroLibriValutati;

    public GenreDropStats() {}

    public GenreDropStats(String genre, Double percentualeGenereDroppato, Integer numeroLibriValutati) {
        this.genre = genre;
        this.percentualeGenereDroppato = percentualeGenereDroppato;
        this.numeroLibriValutati = numeroLibriValutati;
    }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Double getPercentualeGenereDroppato() { return percentualeGenereDroppato; }
    public void setPercentualeGenereDroppato(Double percentualeGenereDroppato) { this.percentualeGenereDroppato = percentualeGenereDroppato; }

    public Integer getNumeroLibriValutati() { return numeroLibriValutati; }
    public void setNumeroLibriValutati(Integer numeroLibriValutati) { this.numeroLibriValutati = numeroLibriValutati; }
}
