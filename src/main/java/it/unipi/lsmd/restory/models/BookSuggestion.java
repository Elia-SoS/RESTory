package it.unipi.lsmd.restory.models;

/**
 * DTO per rappresentare un libro suggerito tramite degree of separation
 * Contiene il titolo del libro, l'autore, la popolarità e lo score di raccomandazione
 */
public class BookSuggestion {
    
    private String title;
    private String author;
    private long popularity;
    private double recommendationScore;
    
    /**
     * Costruttore vuoto
     */
    public BookSuggestion() {
    }
    
    /**
     * Costruttore completo
     * @param title il titolo del libro
     * @param author l'autore del libro
     * @param popularity il numero di target users che possiedono il libro
     * @param recommendationScore lo score di raccomandazione collaborativo
     */
    public BookSuggestion(String title, String author, long popularity, double recommendationScore) {
        this.title = title;
        this.author = author;
        this.popularity = popularity;
        this.recommendationScore = recommendationScore;
    }
    
    // Getter e Setter
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public long getPopularity() {
        return popularity;
    }
    
    public void setPopularity(long popularity) {
        this.popularity = popularity;
    }
    
    public double getRecommendationScore() {
        return recommendationScore;
    }
    
    public void setRecommendationScore(double recommendationScore) {
        this.recommendationScore = recommendationScore;
    }
    
    @Override
    public String toString() {
        return "BookSuggestion{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", popularity=" + popularity +
                ", recommendationScore=" + recommendationScore +
                '}';
    }
}
