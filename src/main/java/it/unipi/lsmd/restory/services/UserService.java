package it.unipi.lsmd.restory.services;

import it.unipi.lsmd.restory.models.User;
import it.unipi.lsmd.restory.models.UserDTO;
import it.unipi.lsmd.restory.models.Neo4jUser;
import it.unipi.lsmd.restory.models.InfluenceStats;
import it.unipi.lsmd.restory.models.HubAndAuthorityStats;
import it.unipi.lsmd.restory.models.BookSuggestion;
import it.unipi.lsmd.restory.repositories.UserRepository;
import it.unipi.lsmd.restory.repositories.UserNeo4jRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired(required = false)
    private UserNeo4jRepository userNeo4jRepository;
    @Autowired(required = false)
    private Driver neo4jDriver;
    /**
     * Aggiunge un nuovo utente al sistema
     * @param user l'utente da aggiungere
     * @return l'utente aggiunto con il suo ID generato
     * @throws IllegalArgumentException se username o email sono già in uso
     */
    public User addUser(User user) {
        System.out.println("🔔 [Service] addUser called for username=" + user.getUsername());
        // Validazione: controlla se username è già in uso
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            System.out.println("❌ [Service] addUser failed - username already in use: " + user.getUsername());
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' è già in uso");
        }
        // Validazione: controlla se email è già in uso
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' è già in uso");
        }
        if (user.getFollowers() == null) {
            user.setFollowers(0);
        }
        if (user.getReviews() == null) {
            user.setReviews(0);
        }
        if (user.getBooks() == null) {
            user.setBooks(0);
        }

        user.setIsBanned(false);
        user.setIsAdmin(false);

        if (user.getOtherAdded() == null) {
            user.setOtherAdded(List.of());
        }
        if (user.getOtherReviews() == null) {
            user.setOtherReviews(List.of());
        }

        // Non generare manualmente l'id: lascia che Mongo assegni `_id` automaticamente
        
        // Salva e ritorna l'utente aggiunto
        User saved = userRepository.save(user);
        System.out.println("✅ [Service] addUser saved mongoId=" + saved.getId());
        
        // Crea il nodo User nel database Neo4j con username e user_id
        if (userNeo4jRepository != null) {
            try {
                Neo4jUser neo4jUser = new Neo4jUser(saved.getUsername(), saved.getId());
                userNeo4jRepository.save(neo4jUser);
                System.out.println("✅ [Service] addUser created Neo4j node for username=" + saved.getUsername());
            } catch (Exception e) {
                System.out.println("⚠️ [Service] addUser warning - failed to create Neo4j node: " + e.getMessage());
                // Non sollevare l'eccezione: l'utente è comunque stato salvato in MongoDB
            }
        }
        
        return saved;
    }
    
    /**
     * Recupera un utente per ID
     * @param id l'ID dell'utente
     * @return Optional contenente l'utente se trovato
     */
    public Optional<User> getUserById(String id) {
        System.out.println("🔔 [Service] getUserById called with id=" + id);
        Optional<User> result = userRepository.findById(id);
        System.out.println(result.isPresent() ? "✅ [Service] getUserById found user" : "⚠️ [Service] getUserById not found");
        return result;
    }
    
    /**
     * Recupera un utente per username
     * @param username lo username dell'utente
     * @return Optional contenente l'utente se trovato
     */
    public Optional<User> getUserByUsername(String username) {
        System.out.println("🔔 [Service] getUserByUsername called with username=" + username);
        Optional<User> result = userRepository.findByUsername(username);
        System.out.println(result.isPresent() ? "✅ [Service] getUserByUsername found user" : "⚠️ [Service] getUserByUsername not found");
        return result;
    }

    public Optional<UserDTO> getUserDTOByUsername(String username) {
        System.out.println("🔔 [Service] getUserDTOByUsername called with username=" + username);
        return getUserByUsername(username).map(UserDTO::fromUser);
    }

    public boolean login(String username, String password) {
        System.out.println("🔔 [Service] login called for username=" + username);
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (Boolean.TRUE.equals(user.getIsBanned())) {
            return false;
        }

        return password.equals(user.getPassword());
    }

    public UserDTO banUser(String id) {
        System.out.println("🔔 [Service] banUser called for id=" + id);
        return setBannedFlag(id, true);
    }

    public UserDTO unbanUser(String id) {
        System.out.println("🔔 [Service] unbanUser called for id=" + id);
        return setBannedFlag(id, false);
    }

    private UserDTO setBannedFlag(String id, boolean banned) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Utente con ID '" + id + "' non trovato");
        }

        User user = userOpt.get();
        user.setIsBanned(banned);
        User saved = userRepository.save(user);
        return UserDTO.fromUser(saved);
    }
    
    /**
     * Recupera un utente per email
     * @param email l'email dell'utente
     * @return Optional contenente l'utente se trovato
     */
    public Optional<User> getUserByEmail(String email) {
        System.out.println("🔔 [Service] getUserByEmail called with email=" + email);
        Optional<User> result = userRepository.findByEmail(email);
        System.out.println(result.isPresent() ? "✅ [Service] getUserByEmail found user" : "⚠️ [Service] getUserByEmail not found");
        return result;
    }
    
    // getUserByUserId removed - use getUserById (by Mongo id) or getUserByUsername/getUserByEmail
    
    /**
     * Recupera tutti gli utenti
     * @return lista di tutti gli utenti
     */
    public List<User> getAllUsers() {
        System.out.println("🔔 [Service] getAllUsers called");
        List<User> list = userRepository.findAll();
        System.out.println("✅ [Service] getAllUsers returned count=" + list.size());
        return list;
    }
    
    /**
     * Ricerca utenti per username
     * @param username il pattern di ricerca (case-insensitive)
     * @return lista di utenti che corrispondono al pattern
     */
    public List<User> searchUsersByUsername(String username) {
        System.out.println("🔔 [Service] searchUsersByUsername called with username=" + username);
        List<User> list = userRepository.findByUsernameContainsIgnoreCase(username);
        System.out.println("✅ [Service] searchUsersByUsername returned count=" + list.size());
        return list;
    }
    
    /**
     * Aggiorna un utente esistente
     * @param id l'ID dell'utente da aggiornare
     * @param updatedUser i dati aggiornati dell'utente
     * @return l'utente aggiornato
     * @throws IllegalArgumentException se l'utente non esiste
     */
    public User updateUser(String id, User updatedUser) {
        System.out.println("🔔 [Service] updateUser called for id=" + id);
        Optional<User> existingUser = userRepository.findById(id);

        if (existingUser.isEmpty()) {
            System.out.println("❌ [Service] updateUser - user not found id=" + id);
            throw new IllegalArgumentException("Utente con ID '" + id + "' non trovato");
        }

        User user = existingUser.get();
        
        // Aggiorna i campi (mantieni l'ID originale)
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(updatedUser.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username '" + updatedUser.getUsername() + "' è già in uso");
            }
            user.setUsername(updatedUser.getUsername());
        }
        
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(updatedUser.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email '" + updatedUser.getEmail() + "' è già in uso");
            }
            user.setEmail(updatedUser.getEmail());
        }
        
        // Aggiorna altri campi
        if (updatedUser.getName() != null) user.setName(updatedUser.getName());
        if (updatedUser.getSurname() != null) user.setSurname(updatedUser.getSurname());
        if (updatedUser.getPassword() != null) user.setPassword(updatedUser.getPassword());
        if (updatedUser.getBirthdate() != null) user.setBirthdate(updatedUser.getBirthdate());
        if (updatedUser.getBio() != null) user.setBio(updatedUser.getBio());
        if (updatedUser.getFavouriteGenres() != null) user.setFavouriteGenres(updatedUser.getFavouriteGenres());
        if (updatedUser.getCity() != null) user.setCity(updatedUser.getCity());
        // Aggiorna conteggi e collezioni se forniti
        if (updatedUser.getFollowers() != null) user.setFollowers(updatedUser.getFollowers());
        if (updatedUser.getReviews() != null) user.setReviews(updatedUser.getReviews());
        if (updatedUser.getBooks() != null) user.setBooks(updatedUser.getBooks());
        if (updatedUser.getRecentlyAdded() != null) user.setRecentlyAdded(updatedUser.getRecentlyAdded());
        if (updatedUser.getLatestReviews() != null) user.setLatestReviews(updatedUser.getLatestReviews());
        if (updatedUser.getIsBanned() != null) user.setIsBanned(updatedUser.getIsBanned());
        if (updatedUser.getIsAdmin() != null) user.setIsAdmin(updatedUser.getIsAdmin());
        if (updatedUser.getOtherAdded() != null) user.setOtherAdded(updatedUser.getOtherAdded());
        if (updatedUser.getOtherReviews() != null) user.setOtherReviews(updatedUser.getOtherReviews());
        if(updatedUser.getAverageSpeed() != null) user.setAverageSpeed(updatedUser.getAverageSpeed());
        User saved = userRepository.save(user);
        System.out.println("✅ [Service] updateUser succeeded for mongoId=" + saved.getId());
        return saved;
    }
    
    /**
     * Elimina un utente
     * @param id l'ID dell'utente da eliminare
     * @throws IllegalArgumentException se l'utente non esiste
     */
    public void deleteUser(String id) {
        System.out.println("🔔 [Service] deleteUser called for id=" + id);
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            System.out.println("❌ [Service] deleteUser - user not found id=" + id);
            throw new IllegalArgumentException("Utente con ID '" + id + "' non trovato");
        }

        User user = userOpt.get();
        userRepository.deleteById(id);

        if (neo4jDriver != null && user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            try (Session session = neo4jDriver.session()) {
                String query = """
                    MATCH (u:RegisteredUser {username: $username})
                    DETACH DELETE u
                    """;

                session.executeWrite(tx -> {
                    tx.run(query, Values.parameters("username", user.getUsername())).consume();
                    return null;
                });
            } catch (Exception e) {
                System.err.println("❌ [Service] deleteUser - failed to delete Neo4j node: " + e.getMessage());
            }
        }

        System.out.println("✅ [Service] deleteUser succeeded for id=" + id);
    }

    /**
     * Calcola l'Influence Coefficient per un utente
     * 
     * Logica:
     * 1. Trova l'utente per username
     * 2. Trova i libri recensiti positivamente (rating >= 3) dall'utente
     * 3. Trova i follower dell'utente
     * 4. Conta quanti follower possiedono i libri identificati
     * 5. Calcola il coefficiente: positiveInteractions / totalBookReviewed
     * 
     * @param username lo username dell'utente
     * @return InfluenceStats con totalBookReviewed, positiveInteractions e influenceCoefficient
     * @throws IllegalStateException se il driver Neo4j non è disponibile
     * @throws RuntimeException se l'utente non esiste in Neo4j
     */
    public InfluenceStats getInfluenceCoefficientAnalytics(String username) {
        System.out.println("🔔 [Service] getInfluenceCoefficientAnalytics called for username=" + username);
        
        if (neo4jDriver == null) {
            System.out.println("❌ [Service] getInfluenceCoefficientAnalytics - Neo4j driver not available");
            throw new IllegalStateException("Driver Neo4j non disponibile");
        }

        try (Session session = neo4jDriver.session()) {
            String query = """
                // 1.
                MATCH (follower:RegisteredUser)-[:FOLLOWS]->(u:RegisteredUser {username: $user_input})-[r:REVIEWED]->(b:Book)
                WHERE r.rating >= $threshold_rating

                // 2.
                OPTIONAL MATCH (follower)-[p:POSSESSES]->(b)
                WHERE p.date_added >= r.date_added

                // 3.
                WITH count(DISTINCT b) AS totalBookReviewed,
                     count(p) AS positiveInteractions
                RETURN totalBookReviewed,
                       positiveInteractions,
                       CASE
                           WHEN totalBookReviewed > 0 THEN toFloat(positiveInteractions) / totalBookReviewed
                           ELSE 0
                       END AS influenceCoefficient
                """;

            int threshold_rating = 3;
            Result result = session.run(query, Values.parameters("user_input", username, "threshold_rating", threshold_rating));

            if (result.hasNext()) {
                Record record = result.next();
                Long totalBookReviewed = record.get("totalBookReviewed").asLong(); //sarebbe positevly reviewed
                Long positiveInteractions = record.get("positiveInteractions").asLong();
                Double influenceCoefficient = record.get("influenceCoefficient").asDouble();

                InfluenceStats stats = new InfluenceStats(totalBookReviewed, positiveInteractions, influenceCoefficient);
                System.out.println("✅ [Service] getInfluenceCoefficientAnalytics succeeded: " + stats);
                return stats;
            } else {
                System.out.println("❌ [Service] getInfluenceCoefficientAnalytics - utente non trovato in Neo4j: " + username);
                throw new RuntimeException("Utente '" + username + "' non trovato in Neo4j");
            }
        } catch (Exception e) {
            System.out.println("❌ [Service] getInfluenceCoefficientAnalytics error: " + e.getMessage());
            throw new RuntimeException("Errore durante il calcolo dell'influence coefficient: " + e.getMessage(), e);
        }
    }

    /**
     * Calcola gli Expert Influencers utilizzando l'algoritmo HubAndAuthority
     * 
     * Logica:
     * 1. Identifica ogni utente e conta i suoi follower entranti
     * 2. Filtra gli utenti che raggiungono la soglia minima di follower
     * 3. Per gli influencer rimasti, analizza le loro recensioni
     * 4. Filtra per quantità minima di recensioni e utilità media
     * 5. Restituisce gli Expert Influencers ordinati per qualità
     * 
     * @param soglia_follower numero minimo di follower (default: 10)
     * @param soglia_min_reviews numero minimo di recensioni (default: 5)
     * @param soglia_helpfulness utilità media minima delle recensioni (default: 2.0)
     * @return lista di HubAndAuthorityStats ordinata per media_helpfulness DESC, follower_count DESC
     * @throws IllegalStateException se il driver Neo4j non è disponibile
     */
    public List<HubAndAuthorityStats> getHubAndAuthorityAnalytics(
            Long soglia_follower, 
            Long soglia_min_reviews, 
            Double soglia_helpfulness) {
        
        System.out.println("🔔 [Service] getHubAndAuthorityAnalytics called with thresholds: followers=" + soglia_follower + 
                          ", reviews=" + soglia_min_reviews + ", helpfulness=" + soglia_helpfulness);
        
        if (neo4jDriver == null) {
            System.out.println("❌ [Service] getHubAndAuthorityAnalytics - Neo4j driver not available");
            throw new IllegalStateException("Driver Neo4j non disponibile");
        }

        try (Session session = neo4jDriver.session()) {
            String query = """
                MATCH (u:RegisteredUser)
                WITH u, COUNT { (:RegisteredUser)-[:FOLLOWS]->(u:RegisteredUser) } AS follower_count
                WHERE follower_count >= $soglia_follower
                MATCH (u)-[r:REVIEWED]->(:Book)
                WITH u,
                     follower_count,
                     count(r) AS review_count,
                     avg(r.n_votes) AS media_helpfulness
                WHERE review_count >= $soglia_min_reviews
                AND media_helpfulness >= $soglia_helpfulness
                RETURN u.username AS influencer_username,
                       follower_count,
                       review_count,
                       media_helpfulness
                ORDER BY media_helpfulness DESC, follower_count DESC
                """;

            Result result = session.run(query, Values.parameters(
                "soglia_follower", soglia_follower,
                "soglia_min_reviews", soglia_min_reviews,
                "soglia_helpfulness", soglia_helpfulness
            ));

            List<HubAndAuthorityStats> expertInfluencers = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                HubAndAuthorityStats stats = new HubAndAuthorityStats(
                    record.get("influencer_username").asString(),
                    record.get("follower_count").asLong(),
                    record.get("review_count").asLong(),
                    record.get("media_helpfulness").asDouble()
                );
                expertInfluencers.add(stats);
            }

            System.out.println("✅ [Service] getHubAndAuthorityAnalytics succeeded: found " + expertInfluencers.size() + " expert influencers");
            return expertInfluencers;
        } catch (Exception e) {
            System.out.println("❌ [Service] getHubAndAuthorityAnalytics error: " + e.getMessage());
            throw new RuntimeException("Errore durante il calcolo dell'HubAndAuthority analytics: " + e.getMessage(), e);
        }
    }

    /**
     * Suggerisce libri basati sul degree of separation (2 archi di distanza)
     * 
     * Algoritmo:
     * 1. Identifica l'utente di partenza tramite username
     * 2. Trova gli utenti a 2 archi di distanza (seguiti dai suoi seguiti)
     * 3. Esclude l'utente stesso e gli utenti che segue già direttamente
     * 4. Conta i libri in comune e i following in comune con ogni target user
     * 5. Calcola la rilevanza: 0.8 * libri_comuni + 0.2 * following_comuni
     * 6. Seleziona i 5 utenti più simili (rilevanza più alta)
     * 7. Trova i libri completati da questi utenti
     * 8. Esclude i libri già completati o in lettura dall'utente principale
     * 9. Ritorna i 5 libri suggeriti ordinati per popolarità (count DESC)
     * 
     * @param username lo username dell'utente per cui suggerire libri
     * @return lista di BookSuggestion (max 5) ordinati per popolarità decrescente
     * @throws IllegalStateException se il driver Neo4j non è disponibile
     * @throws RuntimeException se l'utente non esiste in Neo4j
     */
    public List<BookSuggestion> suggestBooksByDegreeOfSeparation(String username) {
        System.out.println("🔔 [Service] suggestBooksByDegreeOfSeparation called for username=" + username);
        
        if (neo4jDriver == null) {
            System.out.println("❌ [Service] suggestBooksByDegreeOfSeparation - Neo4j driver not available");
            throw new IllegalStateException("Driver Neo4j non disponibile");
        }

        try (Session session = neo4jDriver.session()) {
            String query = """
                // 1. Identifica l'utente di partenza
                
                MATCH (u1:RegisteredUser {username: $username})
                
                // 2. Trova il network esteso a 2 salti
                
                MATCH (u1)-[:FOLLOWS]->(:RegisteredUser)-[:FOLLOWS]-(target:RegisteredUser)
                WHERE target <> u1 AND NOT (u1)-[:FOLLOWS]-(target)
                WITH DISTINCT u1, target
                
                // 3. Calcola i tratti in comune (Pattern Comprehension veloci)
                
                WITH u1, target,
                count { (u1)-[:POSSESSES]->(:Book)<-[:POSSESSES]-(target) } AS n_common_books,
                count { (target)-[:POSSESSES]->(:Book) } AS n_tot_books_target,
                count { (target)-[:FOLLOWS]->(:RegisteredUser) } AS n_tot_follow_target,
                count { (u1)-[:FOLLOWS]->(:RegisteredUser)<-[:FOLLOWS]-(target) } AS n_common_follow
                
                // 4. Calcola la similarità e crea la "Coorte"
                
                    WITH u1, target,
                                 (CASE WHEN n_tot_books_target > 0 THEN (0.8 * toFloat(n_common_books) / n_tot_books_target) ELSE 0.0 END +
                                  CASE WHEN n_tot_follow_target > 0 THEN (0.2 * toFloat(n_common_follow) / n_tot_follow_target) ELSE 0.0 END) AS similarity_score
                            WHERE similarity_score > 0
                
                // 5. Trova i libri dei target
                
                MATCH (target)-[:POSSESSES]->(recBook:Book)
                // 5.5. Escludi i libri che l'utente di partenza possiede già!
                WHERE NOT (u1)-[:POSSESSES]->(recBook)
                
                // 6. Ritorna i libri calcolando lo score collaborativo
                
                RETURN
                    recBook.title AS TitoloLibro,
                    recBook.main_author AS AutoreLibro,
                    count(DISTINCT target) AS PopolaritaTraSimili,
                    sum(similarity_score) AS RecommendationScore
                ORDER BY RecommendationScore DESC, PopolaritaTraSimili DESC
                LIMIT 5
                
                """;

            Result result = session.run(query, Values.parameters("username", username));

            List<BookSuggestion> suggestions = new ArrayList<>();
            int count = 0;
            
            while (result.hasNext() && count < 5) {
                Record record = result.next();
                String title = record.get("TitoloLibro").asString();
                String author = record.get("AutoreLibro").asString();
                Long popularity = record.get("PopolaritaTraSimili").asLong();
                Double recommendationScore = record.get("RecommendationScore").asDouble();
                
                suggestions.add(new BookSuggestion(title, author, popularity, recommendationScore));
                count++;
            }

            System.out.println("✅ [Service] suggestBooksByDegreeOfSeparation succeeded: found " + suggestions.size() + " suggested books");
            return suggestions;
        } catch (Exception e) {
            System.out.println("❌ [Service] suggestBooksByDegreeOfSeparation error: " + e.getMessage());
            throw new RuntimeException("Errore durante il suggerimento libri: " + e.getMessage(), e);
        }
    }
}
