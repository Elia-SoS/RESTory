package it.unipi.lsmd.restory.controllers;

import it.unipi.lsmd.restory.models.User;
import it.unipi.lsmd.restory.models.UserDTO;
import it.unipi.lsmd.restory.models.LoginRequest;
import it.unipi.lsmd.restory.models.InfluenceStats;
import it.unipi.lsmd.restory.models.HubAndAuthorityStats;
import it.unipi.lsmd.restory.models.BookSuggestion;
import it.unipi.lsmd.restory.services.UserService;
import it.unipi.lsmd.restory.services.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisService redisService;

    /**
     * Crea un nuovo utente
     * POST /api/users
     * @param user i dati del nuovo utente
     * @return ResponseEntity con l'utente creato e status 201
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        System.out.println("🔔 [Controller] POST /api/users - createUser called with username=" + user.getUsername());
        try {
            User createdUser = userService.addUser(user);
            System.out.println("✅ [Controller] createUser succeeded, mongoId=" + createdUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [Controller] createUser failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Recupera un utente per ID
     * GET /api/users/{id}
     * @param id l'ID dell'utente
     * @return ResponseEntity con l'utente se trovato, status 404 altrimenti
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        System.out.println("🔔 [Controller] GET /api/users/" + id + " - getUserById called");
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            System.out.println("✅ [Controller] getUserById found user with mongoId=" + user.get().getId());
            return ResponseEntity.ok(user.get());
        } else {
            System.out.println("⚠️ [Controller] getUserById - user not found for id=" + id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Recupera un utente per username
     * GET /api/users/username/{username}
     * @param username lo username dell'utente
     * @return ResponseEntity con l'utente se trovato, status 404 altrimenti
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        System.out.println("🔔 [Controller] GET /api/users/username/" + username + " - getUserByUsername called");
        Optional<UserDTO> user = userService.getUserDTOByUsername(username);
        if (user.isPresent()) {
            System.out.println("✅ [Controller] getUserByUsername found user DTO for username=" + username);
            return ResponseEntity.ok(user.get());
        } else {
            System.out.println("⚠️ [Controller] getUserByUsername - not found for username=" + username);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Login utente
     * POST /api/users/login
     * @param request username e password
     * @return true se le credenziali sono valide, false altrimenti
     */
    @PostMapping("/login")
    public ResponseEntity<Boolean> login(@RequestBody LoginRequest request) {
        System.out.println("🔔 [Controller] POST /api/users/login called for username=" + request.getUsername());
        boolean authenticated = userService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(authenticated);
    }

    /**
     * Imposta isBanned a true.
     */
    @PutMapping("/{id}/ban")
    public ResponseEntity<UserDTO> banUser(@PathVariable String id) {
        System.out.println("🔔 [Controller] PUT /api/users/" + id + "/ban called");
        try {
            return ResponseEntity.ok(userService.banUser(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Imposta isBanned a false.
     */
    @PutMapping("/{id}/unban")
    public ResponseEntity<UserDTO> unbanUser(@PathVariable String id) {
        System.out.println("🔔 [Controller] PUT /api/users/" + id + "/unban called");
        try {
            return ResponseEntity.ok(userService.unbanUser(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Recupera un utente per email
     * GET /api/users/email/{email}
     * @param email l'email dell'utente
     * @return ResponseEntity con l'utente se trovato, status 404 altrimenti
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        System.out.println("🔔 [Controller] GET /api/users/email/" + email + " - getUserByEmail called");
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Recupera tutti gli utenti
     * GET /api/users
     * @return ResponseEntity con la lista di tutti gli utenti
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        System.out.println("🔔 [Controller] GET /api/users - getAllUsers called");
        List<User> users = userService.getAllUsers();
        System.out.println("✅ [Controller] getAllUsers returned count=" + users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Ricerca utenti per username
     * GET /api/users/search?username=query
     * @param username il pattern di ricerca
     * @return ResponseEntity con la lista degli utenti corrispondenti
     */
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String username) {
        System.out.println("🔔 [Controller] GET /api/users/search?username=" + username + " - searchUsers called");
        List<User> users = userService.searchUsersByUsername(username);
        System.out.println("✅ [Controller] searchUsers returned count=" + users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Aggiorna un utente
     * PUT /api/users/{id}
     * @param id l'ID dell'utente da aggiornare
     * @param updatedUser i dati aggiornati
     * @return ResponseEntity con l'utente aggiornato
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User updatedUser) {
        System.out.println("🔔 [Controller] PUT /api/users/" + id + " - updateUser called");
        try {
            User user = userService.updateUser(id, updatedUser);
            System.out.println("✅ [Controller] updateUser succeeded for mongoId=" + user.getId());
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [Controller] updateUser failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Elimina un utente
     * DELETE /api/users/{id}
     * @param id l'ID dell'utente da eliminare
     * @return ResponseEntity con status 204 se eliminato con successo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        System.out.println("🔔 [Controller] DELETE /api/users/" + id + " - deleteUser called");
        try {
            userService.deleteUser(id);
            System.out.println("✅ [Controller] deleteUser succeeded for id=" + id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [Controller] deleteUser failed: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Calcola l'Influence Coefficient di un utente
     * GET /api/users/{username}/influence-coefficient
     * 
     * Formula: positiveInteractions / totalBookReviewed
     * - totalBookReviewed: numero di libri recensiti con rating >= 3 dall'utente
     * - positiveInteractions: numero di follower che possiedono i libri 
     *                          positivamente recensiti (rating >= 3) dall'utente
     * 
     * @param username lo username dell'utente
     * @return ResponseEntity con InfluenceStats
     */
    @GetMapping("/{username}/influence-coefficient")
    public ResponseEntity<InfluenceStats> getInfluenceCoefficientAnalytics(@PathVariable String username) {
        System.out.println("🔔 [Controller] GET /api/users/" + username + "/influence-coefficient - getInfluenceCoefficientAnalytics called");
        try {
            InfluenceStats stats = userService.getInfluenceCoefficientAnalytics(username);
            System.out.println("✅ [Controller] getInfluenceCoefficientAnalytics succeeded: " + stats);
            return ResponseEntity.ok(stats);
        } catch (IllegalStateException e) {
            System.out.println("❌ [Controller] getInfluenceCoefficientAnalytics failed (Neo4j not available): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
        } catch (RuntimeException e) {
            System.out.println("❌ [Controller] getInfluenceCoefficientAnalytics failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Calcola gli Expert Influencers utilizzando l'algoritmo HubAndAuthority
     * GET /api/users/analytics/hub-and-authority
     * 
     * Query Parameters (opzionali):
     * - soglia_follower: numero minimo di follower (default: 10)
     * - soglia_min_reviews: numero minimo di recensioni (default: 5)
     * - soglia_helpfulness: utilità media minima (default: 2.0)
     * 
     * Restituisce una lista di expert influencers ordinata per:
     * 1. utilità media delle recensioni (DESC)
     * 2. numero di follower (DESC)
     * 
     * @param soglia_follower numero minimo di follower
     * @param soglia_min_reviews numero minimo di recensioni
     * @param soglia_helpfulness utilità media minima
     * @return ResponseEntity con lista di HubAndAuthorityStats
     */
    @GetMapping("/analytics/hub-and-authority")
    public ResponseEntity<?> getHubAndAuthorityAnalytics(
            @RequestParam(defaultValue = "10") Long soglia_follower,
            @RequestParam(defaultValue = "5") Long soglia_min_reviews,
            @RequestParam(defaultValue = "2.0") Double soglia_helpfulness) {

        System.out.println("🔔 [Controller] GET /api/users/analytics/hub-and-authority - getHubAndAuthorityAnalytics called");
        System.out.println("   Thresholds: followers=" + soglia_follower + ", reviews=" + soglia_min_reviews + 
                          ", helpfulness=" + soglia_helpfulness);

        // Validate inputs
        if (soglia_follower == null || soglia_follower <= 0) {
            return ResponseEntity.badRequest().body("soglia_follower must be > 0");
        }
        if (soglia_min_reviews == null || soglia_min_reviews <= 0) {
            return ResponseEntity.badRequest().body("soglia_min_reviews must be > 0");
        }
        if (soglia_helpfulness == null || soglia_helpfulness < 0.0 || soglia_helpfulness > 5.0) {
            return ResponseEntity.badRequest().body("soglia_helpfulness must be between 0 and 5");
        }

        try {
            List<HubAndAuthorityStats> results = userService.getHubAndAuthorityAnalytics(
                soglia_follower, soglia_min_reviews, soglia_helpfulness);
            System.out.println("✅ [Controller] getHubAndAuthorityAnalytics succeeded: found " + results.size() + " expert influencers");
            return ResponseEntity.ok(results);
        } catch (IllegalStateException e) {
            System.out.println("❌ [Controller] getHubAndAuthorityAnalytics failed (Neo4j not available): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Neo4j driver not available");
        } catch (RuntimeException e) {
            System.out.println("❌ [Controller] getHubAndAuthorityAnalytics failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Suggerisce libri basati sul degree of separation (2 archi di distanza nel grafo social)
     * GET /api/users/{username}/book-suggestions
     * 
     * Algoritmo:
     * 1. Identifica l'utente di partenza
     * 2. Trova gli utenti a 2 archi di distanza (seguiti dai suoi seguiti)
     * 3. Esclude l'utente stesso e gli utenti che segue già direttamente
     * 4. Conta i libri in comune e i following in comune con ogni target user
     * 5. Calcola la rilevanza: 0.8 * libri_comuni + 0.2 * following_comuni
     * 6. Seleziona i 5 utenti più simili (rilevanza più alta)
     * 7. Identifica i libri completati da questi utenti (non già completati/in lettura da u1)
     * 8. Ritorna i 5 libri suggeriti ordinati per popolarità (count DESC)
     * 
     * @param username lo username dell'utente per cui suggerire libri
     * @return ResponseEntity con lista di BookSuggestion (max 5)
     */
    @GetMapping("/{username}/book-suggestions")
    public ResponseEntity<List<BookSuggestion>> suggestBooksByDegreeOfSeparation(@PathVariable String username) {
        System.out.println("🔔 [Controller] GET /api/users/" + username + "/book-suggestions - suggestBooksByDegreeOfSeparation called");
        try {
            List<BookSuggestion> suggestions = userService.suggestBooksByDegreeOfSeparation(username);
            System.out.println("✅ [Controller] suggestBooksByDegreeOfSeparation succeeded: found " + suggestions.size() + " suggested books");
            return ResponseEntity.ok(suggestions);
        } catch (IllegalStateException e) {
            System.out.println("❌ [Controller] suggestBooksByDegreeOfSeparation failed (Neo4j not available): " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
        } catch (RuntimeException e) {
            System.out.println("❌ [Controller] suggestBooksByDegreeOfSeparation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Test della connessione Redis
     * GET /api/users/test-redis
     * @return ResponseEntity con messaggio di successo se Redis è connesso
     */
    @GetMapping("/test-redis")
    public ResponseEntity<String> testRedisConnection() {
        try {
            // Test semplice: salva e recupera un valore
            String testKey = "test:connection";
            String testValue = "Redis connection successful at " + java.time.LocalDateTime.now();

            redisService.set(testKey, testValue);
            Object retrievedValue = redisService.get(testKey);

            if (retrievedValue != null && retrievedValue.equals(testValue)) {
                System.out.println("✅ [Controller] Redis connection test successful");
                return ResponseEntity.ok("Redis connection successful! Retrieved: " + retrievedValue);
            } else {
                System.out.println("❌ [Controller] Redis connection test failed - value mismatch");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Redis connection test failed - value mismatch");
            }
        } catch (Exception e) {
            System.out.println("❌ [Controller] Redis connection test failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Redis connection failed: " + e.getMessage());
        }
    }
}
