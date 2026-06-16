package it.unipi.lsmd.restory.controllers;

import it.unipi.lsmd.restory.services.Neo4jUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/follows")
public class FollowController {
    
    @Autowired
    private Neo4jUserService neo4jUserService;
    
    /**
     * Aggiunge un follow tra due utenti
     * POST /api/follows/add
     * @param request mappa contenente "followerUsername" e "followingUsername"
     * @return ResponseEntity con messaggio di successo
     */
    @PostMapping("/add")
    public ResponseEntity<?> addFollow(@RequestBody Map<String, String> request) {
        System.out.println("🔔 [Controller] POST /api/follows/add - addFollow called");
        
        try {
            String followerUsername = request.get("followerUsername");
            String followingUsername = request.get("followingUsername");
            
            if (followerUsername == null || followerUsername.trim().isEmpty() ||
                followingUsername == null || followingUsername.trim().isEmpty()) {
                System.out.println("❌ [Controller] Invalid input - missing or empty usernames");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "followerUsername e followingUsername sono obbligatori"));
            }
            
            neo4jUserService.addFollowRelationship(followerUsername, followingUsername);
            System.out.println("✅ [Controller] Follow relationship created");
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", followerUsername + " now follows " + followingUsername
            ));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [Controller] Bad request: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            System.out.println("❌ [Controller] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Rimuove un follow tra due utenti
     * DELETE /api/follows/remove
     * @param request mappa contenente "followerUsername" e "followingUsername"
     * @return ResponseEntity con messaggio di successo
     */
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFollow(@RequestBody Map<String, String> request) {
        System.out.println("🔔 [Controller] DELETE /api/follows/remove - removeFollow called");
        
        try {
            String followerUsername = request.get("followerUsername");
            String followingUsername = request.get("followingUsername");
            
            if (followerUsername == null || followerUsername.trim().isEmpty() ||
                followingUsername == null || followingUsername.trim().isEmpty()) {
                System.out.println("❌ [Controller] Invalid input - missing or empty usernames");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "followerUsername e followingUsername sono obbligatori"));
            }
            
            neo4jUserService.removeFollowRelationship(followerUsername, followingUsername);
            System.out.println("✅ [Controller] Follow relationship removed");
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", followerUsername + " no longer follows " + followingUsername
            ));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [Controller] Bad request: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            System.out.println("❌ [Controller] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Verifica se un utente segue un altro
     * GET /api/follows/is-following/{followerUsername}/{followingUsername}
     * @param followerUsername l'username di chi segue
     * @param followingUsername l'username di chi viene seguito
     * @return ResponseEntity con boolean
     */
    @GetMapping("/is-following/{followerUsername}/{followingUsername}")
    public ResponseEntity<?> isFollowing(@PathVariable String followerUsername, 
                                         @PathVariable String followingUsername) {
        System.out.println("🔔 [Controller] GET /api/follows/is-following/" + followerUsername + "/" + followingUsername);
        
        try {
            boolean isFollowing = neo4jUserService.isFollowing(followerUsername, followingUsername);
            System.out.println("✅ [Controller] Check completed: " + isFollowing);
            
            return ResponseEntity.ok(Map.of(
                    "followerUsername", followerUsername,
                    "followingUsername", followingUsername,
                    "isFollowing", isFollowing
            ));
        } catch (RuntimeException e) {
            System.out.println("❌ [Controller] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Ottiene il numero di seguaci di un utente
     * GET /api/follows/followers-count/{username}
     * @param username lo username dell'utente
     * @return ResponseEntity con il numero di seguaci
     */
    @GetMapping("/followers-count/{username}")
    public ResponseEntity<?> getFollowersCount(@PathVariable String username) {
        System.out.println("🔔 [Controller] GET /api/follows/followers-count/" + username);
        
        try {
            int count = neo4jUserService.getFollowersCount(username);
            System.out.println("✅ [Controller] Followers count: " + count);
            
            return ResponseEntity.ok(Map.of(
                    "username", username,
                    "followersCount", count
            ));
        } catch (RuntimeException e) {
            System.out.println("❌ [Controller] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Ottiene il numero di utenti seguiti
     * GET /api/follows/following-count/{username}
     * @param username lo username dell'utente
     * @return ResponseEntity con il numero di utenti seguiti
     */
    @GetMapping("/following-count/{username}")
    public ResponseEntity<?> getFollowingCount(@PathVariable String username) {
        System.out.println("🔔 [Controller] GET /api/follows/following-count/" + username);
        
        try {
            int count = neo4jUserService.getFollowingCount(username);
            System.out.println("✅ [Controller] Following count: " + count);
            
            return ResponseEntity.ok(Map.of(
                    "username", username,
                    "followingCount", count
            ));
        } catch (RuntimeException e) {
            System.out.println("❌ [Controller] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Ottiene la lista dei seguaci di un utente
     * GET /api/follows/followers/{username}
     * @param username lo username dell'utente
     * @return ResponseEntity con Set di username dei seguaci
     */
    @GetMapping("/followers/{username}")
    public ResponseEntity<?> getFollowers(@PathVariable String username) {
        System.out.println("🔔 [Controller] GET /api/follows/followers/" + username);
        
        try {
            Set<String> followers = neo4jUserService.getFollowers(username);
            System.out.println("✅ [Controller] Retrieved followers: " + followers.size());
            
            return ResponseEntity.ok(Map.of(
                    "username", username,
                    "followers", followers,
                    "count", followers.size()
            ));
        } catch (RuntimeException e) {
            System.out.println("❌ [Controller] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Ottiene la lista degli utenti seguiti
     * GET /api/follows/following/{username}
     * @param username lo username dell'utente
     * @return ResponseEntity con Set di username degli utenti seguiti
     */
    @GetMapping("/following/{username}")
    public ResponseEntity<?> getFollowing(@PathVariable String username) {
        System.out.println("🔔 [Controller] GET /api/follows/following/" + username);
        
        try {
            Set<String> following = neo4jUserService.getFollowing(username);
            System.out.println("✅ [Controller] Retrieved following: " + following.size());
            
            return ResponseEntity.ok(Map.of(
                    "username", username,
                    "following", following,
                    "count", following.size()
            ));
        } catch (RuntimeException e) {
            System.out.println("❌ [Controller] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
