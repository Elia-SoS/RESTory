package it.unipi.lsmd.restory.repositories;

import it.unipi.lsmd.restory.models.Neo4jUser;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface Neo4jUserRepository extends Neo4jRepository<Neo4jUser, String> {
    
    /**
     * Trova un utente per username
     * @param username lo username da cercare
     * @return Optional contenente l'utente se trovato
     */
    Optional<Neo4jUser> findByUsername(String username);
    
    /**
     * Aggiunge una relazione FOLLOWS tra due utenti
     * Crea i nodi se non esistono
     * @param followerUsername l'username di chi segue
     * @param followingUsername l'username di chi viene seguito
     */
    @Query("MERGE (follower:RegisteredUser {username: $followerUsername}) " +
           "MERGE (following:RegisteredUser {username: $followingUsername}) " +
           "MERGE (follower)-[:FOLLOWS]->(following)")
    void addFollow(@Param("followerUsername") String followerUsername, 
                   @Param("followingUsername") String followingUsername);
    
    /**
     * Rimuove la relazione FOLLOWS tra due utenti
     * @param followerUsername l'username di chi segue
     * @param followingUsername l'username di chi viene seguito
     */
    @Query("MATCH (follower:RegisteredUser {username: $followerUsername})-[r:FOLLOWS]->(following:RegisteredUser {username: $followingUsername}) " +
           "DELETE r")
    void removeFollow(@Param("followerUsername") String followerUsername,
                      @Param("followingUsername") String followingUsername);
    
    /**
     * Verifica se un utente segue un altro
     * @param followerUsername l'username di chi segue
     * @param followingUsername l'username di chi viene seguito
     * @return true se esiste la relazione FOLLOWS
     */
    @Query("OPTIONAL MATCH (follower:RegisteredUser {username: $followerUsername})-[r:FOLLOWS]->(following:RegisteredUser {username: $followingUsername}) " +
           "RETURN COUNT(r) > 0 AS isFollowing")
    Boolean isFollowing(@Param("followerUsername") String followerUsername,
                        @Param("followingUsername") String followingUsername);
    
    /**
     * Ottiene il numero di seguaci di un utente
     * @param username lo username dell'utente
     * @return il numero di seguaci
     */
    @Query("MATCH (u:RegisteredUser {username: $username})<-[:FOLLOWS]-(follower) " +
           "RETURN count(follower)")
    int getFollowersCount(@Param("username") String username);
    
    /**
     * Ottiene il numero di utenti seguiti
     * @param username lo username dell'utente
     * @return il numero di utenti seguiti
     */
    @Query("MATCH (u:RegisteredUser {username: $username})-[:FOLLOWS]->(following) " +
           "RETURN count(following)")
    int getFollowingCount(@Param("username") String username);
    
    /**
     * Ottiene la lista di seguaci di un utente
     * @param username lo username dell'utente
     * @return Set di username dei seguaci
     */
    @Query("MATCH (u:RegisteredUser {username: $username})<-[:FOLLOWS]-(follower) " +
           "RETURN follower.username")
    Set<String> getFollowers(@Param("username") String username);
    
    /**
     * Ottiene la lista di utenti seguiti
     * @param username lo username dell'utente
     * @return Set di username degli utenti seguiti
     */
    @Query("MATCH (u:RegisteredUser {username: $username})-[:FOLLOWS]->(following) " +
           "RETURN following.username")
    Set<String> getFollowing(@Param("username") String username);
}
