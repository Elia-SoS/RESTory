package it.unipi.lsmd.restory.models;

public class HighEngagementReviewerStats {
    private String username;
    private Integer totalEngagement;
    private Integer numeroDiRecensioni;

    public HighEngagementReviewerStats() {}

    public HighEngagementReviewerStats(String username, Integer totalEngagement, Integer numeroDiRecensioni) {
        this.username = username;
        this.totalEngagement = totalEngagement;
        this.numeroDiRecensioni = numeroDiRecensioni;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getTotalEngagement() { return totalEngagement; }
    public void setTotalEngagement(Integer totalEngagement) { this.totalEngagement = totalEngagement; }

    public Integer getNumeroDiRecensioni() { return numeroDiRecensioni; }
    public void setNumeroDiRecensioni(Integer numeroDiRecensioni) { this.numeroDiRecensioni = numeroDiRecensioni; }
}
