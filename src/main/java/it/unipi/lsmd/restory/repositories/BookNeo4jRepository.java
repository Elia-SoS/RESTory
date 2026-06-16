package it.unipi.lsmd.restory.repositories;

import it.unipi.lsmd.restory.models.BookNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Neo4j per la gestione dei nodi Book
 */
@Repository
public interface    BookNeo4jRepository extends Neo4jRepository<BookNeo4j, String> {
    
    /**
     * Ricerca un libro per titolo
     * @param title il titolo del libro
     * @return Optional contenente il libro se trovato
     */
    Optional<BookNeo4j> findByTitle(String title);
    
    /**
     * Ricerca un libro per autore principale
     * @param MainAuthor l'autore principale
     * @return Optional contenente il libro se trovato
     */
    Optional<BookNeo4j> findByMainAuthor(String MainAuthor);
}
