package it.unipi.lsmd.restory.models;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Rappresenta un Libro nel database Neo4j
 * Nodo Graph: :Book
 * Attributi:
 * - title: titolo del libro
 * - MainAuthor: autore principale del libro
 * - book_id: identificativo univoco del libro
 * 
 * Relazioni supportate:
 * - :POSSESSES -> User (con property date)
 * - :READING -> User (con property date)
 * - :COMPLETED -> User (con property date)
 * - :REVIEWED -> User (con property date, rating, n_votes)
 */
@Node("Book")
public class BookNeo4j {
    
    @Id
    private String book_id;
    
    private String title;
    
    private String MainAuthor;

    /**
     * Costruttore vuoto
     */
    public BookNeo4j() {
    }

    /**
     * Costruttore completo
     */
    public BookNeo4j(String book_id, String title, String MainAuthor) {
        this.book_id = book_id;
        this.title = title;
        this.MainAuthor = MainAuthor;
    }

    // Getter e Setter
    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMainAuthor() {
        return MainAuthor;
    }

    public void setMainAuthor(String MainAuthor) {
        this.MainAuthor = MainAuthor;
    }

    @Override
    public String toString() {
        return "BookNeo4j{" +
                "book_id='" + book_id + '\'' +
                ", title='" + title + '\'' +
                ", MainAuthor='" + MainAuthor + '\'' +
                '}';
    }
}
