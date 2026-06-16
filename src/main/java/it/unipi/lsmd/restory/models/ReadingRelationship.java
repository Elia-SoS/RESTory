package it.unipi.lsmd.restory.models;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import java.time.LocalDate;

/**
 * Rappresenta la relazione :READING tra User e Book in Neo4j
 * Un utente sta leggendo un libro
 * Proprietà della relazione:
 * - date: data in cui l'utente ha iniziato a leggere il libro
 */
@RelationshipProperties
public class ReadingRelationship {
    
    @Id
    @GeneratedValue
    private Long id;
    
    private LocalDate date;
    
    @TargetNode
    private BookNeo4j book;

    /**
     * Costruttore vuoto
     */
    public ReadingRelationship() {
    }

    /**
     * Costruttore completo
     */
    public ReadingRelationship(LocalDate date, BookNeo4j book) {
        this.date = date;
        this.book = book;
    }

    // Getter e Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BookNeo4j getBook() {
        return book;
    }

    public void setBook(BookNeo4j book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "ReadingRelationship{" +
                "date=" + date +
                ", book=" + (book != null ? book.getTitle() : "null") +
                '}';
    }
}
