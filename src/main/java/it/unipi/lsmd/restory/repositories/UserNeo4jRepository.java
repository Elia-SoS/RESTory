package it.unipi.lsmd.restory.repositories;

import it.unipi.lsmd.restory.models.Neo4jUser;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Neo4j per la gestione dei nodi User
 */
@Repository
public interface UserNeo4jRepository extends Neo4jRepository<Neo4jUser, String> {
    
    /**
     * Ricerca un utente per username
     * @param username lo username dell'utente
     * @return Optional contenente l'utente se trovato
     */
    Optional<Neo4jUser> findByUsername(String username);
    
    /**
     * Ricerca un utente per user_id (MongoDB ObjectId)
     * @param user_id l'ID dell'utente da MongoDB
     * @return Optional contenente l'utente se trovato
     */
    @Query("MATCH (u:User {user_id: $user_id}) RETURN u")
    Optional<Neo4jUser> findByUser_id(@Param("user_id") String user_id);
}
