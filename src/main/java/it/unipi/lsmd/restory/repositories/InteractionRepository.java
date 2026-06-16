package it.unipi.lsmd.restory.repositories;

import it.unipi.lsmd.restory.models.Interaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends MongoRepository<Interaction, String> {
    
    /**
     * Trova tutte le interazioni per un determinato utente
     * @param username il nome dell'utente
     * @return lista di interazioni dell'utente
     */
    @Query("{ 'username' : ?0 }")
    List<Interaction> findByUsername(String username);
    
    /**
     * Trova tutte le interazioni per un determinato libro
     * @param bookId l'ID del libro
     * @return lista di interazioni per il libro
     */
    @Query("{ 'book.title' : ?0 }")
    List<Interaction> findByBookTitle(String title);
    
    /**
     * Trova tutte le interazioni per un determinato utente e libro
     * @param username il nome dell'utente
     * @param title il titolo del libro
     * @return lista di interazioni
     */
    @Query("{ 'username' : ?0, 'book.title' : ?1 }")
    List<Interaction> findByUsernameAndBookTitle(String username, String title);
    
    /**
     * Trova le interazioni lette da un utente
     * @param userId l'ID dell'utente
     * @return lista di interazioni lette
     */
    @Query("{ 'username' : ?0, 'is_read' : true }")
    List<Interaction> findReadInteractionsByUsername(String username);
    
    /**
     * Trova le interazioni non lette da un utente
     * @param userId l'ID dell'utente
     * @return lista di interazioni non lette
     */
    @Query("{ 'username' : ?0, 'is_read' : false }")
    List<Interaction> findUnreadInteractionsByUsername(String username);
    
    /**
     * Trova le interazioni aggiunte in un intervallo di date
     * @param startDate data inizio
     * @param endDate data fine
     * @return lista di interazioni nell'intervallo
     */
    @Query("{ 'date_added' : { $gte : ?0, $lte : ?1 } }")
    List<Interaction> findInteractionsBetweenDates(LocalDate startDate, LocalDate endDate);
}
