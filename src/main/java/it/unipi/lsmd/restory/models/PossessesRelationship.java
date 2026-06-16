package it.unipi.lsmd.restory.models;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import java.time.LocalDate;

/**
 * Rappresenta la relazione :POSSESSES tra User e Book in Neo4j
 * Un utente possiede un libro
 * Proprietà della relazione:
 * - date_added: data in cui l'utente ha aggiunto il libro alla sua collezione
 */
@RelationshipProperties
public class PossessesRelationship {
    
    @Id
    @GeneratedValue
    private Long id;
    
    @Property("date_added")
    private LocalDate dateAdded;
    
    @TargetNode
    private BookNeo4j book;

    /**
     * Costruttore vuoto
     */
    public PossessesRelationship() {
    }

    /**
     * Costruttore completo
     */
    public PossessesRelationship(LocalDate dateAdded, BookNeo4j book) {
        this.dateAdded = dateAdded != null ? dateAdded : LocalDate.now();
        this.book = book;
    }

    public PossessesRelationship(BookNeo4j book) {
        this(null, book);
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

    public BookNeo4j getBook() {
        return book;
    }

    public void setBook(BookNeo4j book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "PossessesRelationship{" +
                "dateAdded=" + dateAdded +
                ", book=" + (book != null ? book.getTitle() : "null") +
                '}';
    }
}
