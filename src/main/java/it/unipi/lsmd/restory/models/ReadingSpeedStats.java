package it.unipi.lsmd.restory.models;

public class ReadingSpeedStats {

    private String username;
    private Double velocitaMediaUtente;
    private Integer numeroLibriLetti;

    public ReadingSpeedStats() {}

    public ReadingSpeedStats(String username, Double velocitaMediaUtente, Integer numeroLibriLetti) {
        this.username = username;
        this.velocitaMediaUtente = velocitaMediaUtente;
        this.numeroLibriLetti = numeroLibriLetti;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getVelocitaMediaUtente() {
        return velocitaMediaUtente;
    }

    public void setVelocitaMediaUtente(Double velocitaMediaUtente) {
        this.velocitaMediaUtente = velocitaMediaUtente;
    }

    public Integer getNumeroLibriLetti() {
        return numeroLibriLetti;
    }

    public void setNumeroLibriLetti(Integer numeroLibriLetti) {
        this.numeroLibriLetti = numeroLibriLetti;
    }
}
