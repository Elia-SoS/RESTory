package it.unipi.lsmd.restory.models;

/**
 * Rappresenta i dati di un "Expert Influencer" calcolati dall'analytics HubAndAuthority
 * 
 * Un Expert Influencer è un utente che:
 * - Ha un numero significativo di follower (soglia_follower)
 * - Ha scritto molte recensioni (soglia_min_reviews)  
 * - Le sue recensioni sono state considerate utili in media (soglia_helpfulness)
 */
public class HubAndAuthorityStats {
    
    private String influencer_username;
    private Long follower_count;
    private Long review_count;
    private Double media_helpfulness;

    /**
     * Costruttore vuoto
     */
    public HubAndAuthorityStats() {
    }

    /**
     * Costruttore completo
     */
    public HubAndAuthorityStats(String influencer_username, Long follower_count, Long review_count, Double media_helpfulness) {
        this.influencer_username = influencer_username;
        this.follower_count = follower_count;
        this.review_count = review_count;
        this.media_helpfulness = media_helpfulness;
    }

    // Getter e Setter
    public String getInfluencer_username() {
        return influencer_username;
    }

    public void setInfluencer_username(String influencer_username) {
        this.influencer_username = influencer_username;
    }

    public Long getFollower_count() {
        return follower_count;
    }

    public void setFollower_count(Long follower_count) {
        this.follower_count = follower_count;
    }

    public Long getReview_count() {
        return review_count;
    }

    public void setReview_count(Long review_count) {
        this.review_count = review_count;
    }

    public Double getMedia_helpfulness() {
        return media_helpfulness;
    }

    public void setMedia_helpfulness(Double media_helpfulness) {
        this.media_helpfulness = media_helpfulness;
    }

    @Override
    public String toString() {
        return "HubAndAuthorityStats{" +
                "influencer_username='" + influencer_username + '\'' +
                ", follower_count=" + follower_count +
                ", review_count=" + review_count +
                ", media_helpfulness=" + media_helpfulness +
                '}';
    }
}
