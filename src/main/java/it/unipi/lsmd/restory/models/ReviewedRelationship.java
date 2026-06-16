package it.unipi.lsmd.restory.models;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import java.time.LocalDate;

/**
 * Rappresenta la relazione :REVIEWED tra User e Book in Neo4j
 * Un utente ha scritto una recensione per un libro
 * Proprietà della relazione:
 * - date_added: data in cui l'utente ha scritto la recensione
 * - rating: voto dato dall'utente (generalmente 1-5)
 * - n_votes: numero di voti ricevuti dalla recensione (utilità della recensione)
 */
@RelationshipProperties
public class ReviewedRelationship {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Property("date_added")
    private LocalDate dateAdded;
    
    private Integer rating;
    
    private Integer n_votes;
    
    @TargetNode
    private BookNeo4j book;

    /**
     * Costruttore vuoto
     */
    public ReviewedRelationship() {
    }

    /**
     * Costruttore completo
     */
    public ReviewedRelationship(LocalDate dateAdded, Integer rating, Integer n_votes, BookNeo4j book) {
        this.dateAdded = dateAdded != null ? dateAdded : LocalDate.now();
        this.rating = rating;
        this.n_votes = n_votes;
        this.book = book;
    }

    public ReviewedRelationship(Integer rating, Integer n_votes, BookNeo4j book) {
        this(null, rating, n_votes, book);
    }

    // Getter e Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded != null ? dateAdded : LocalDate.now();
    }

    public LocalDate getDate() {
        return getDateAdded();
    }

    public void setDate(LocalDate date) {
        setDateAdded(date);
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getN_votes() {
        return n_votes;
    }

    public void setN_votes(Integer n_votes) {
        this.n_votes = n_votes;
    }

    public BookNeo4j getBook() {
        return book;
    }

    public void setBook(BookNeo4j book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "ReviewedRelationship{" +
                "dateAdded=" + dateAdded +
                ", rating=" + rating +
                ", n_votes=" + n_votes +
                ", book=" + (book != null ? book.getTitle() : "null") +
                '}';
    }
}
