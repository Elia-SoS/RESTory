package it.unipi.lsmd.restory.services;

import it.unipi.lsmd.restory.models.Neo4jUser;
import it.unipi.lsmd.restory.models.User;
import it.unipi.lsmd.restory.repositories.Neo4jUserRepository;
import it.unipi.lsmd.restory.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class Neo4jUserService {
    
    @Autowired
    private Neo4jUserRepository neo4jUserRepository;

    @Autowired
    private UserRepository userRepository;
    
    /**
     * Aggiunge un follow tra due utenti
     * @param followerUsername l'username di chi segue (follower)
     * @param followingUsername l'username di chi viene seguito (following)
     * @throws IllegalArgumentException se gli username sono uguali o null/vuoti
     */
    public void addFollowRelationship(String followerUsername, String followingUsername) {
        System.out.println("🔔 [Neo4jUserService] addFollowRelationship called: " + followerUsername + " -> " + followingUsername);
        
        // Validazioni
        if (followerUsername == null || followerUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Username del follower non può essere null o vuoto");
        }
        
        if (followingUsername == null || followingUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Username di chi viene seguito non può essere null o vuoto");
        }
        
        if (followerUsername.equals(followingUsername)) {
            throw new IllegalArgumentException("Un utente non può seguire se stesso");
        }
        
        try {
            neo4jUserRepository.addFollow(followerUsername, followingUsername);
            getFollowersCount(followingUsername);
            System.out.println("✅ [Neo4jUserService] Follow relationship created successfully");
        } catch (Exception e) {
            System.out.println("❌ [Neo4jUserService] Error creating follow relationship: " + e.getMessage());
            throw new RuntimeException("Errore nell'aggiunta della relazione di follow", e);
        }
    }
    
    /**
     * Rimuove un follow tra due utenti
     * @param followerUsername l'username di chi segue
     * @param followingUsername l'username di chi viene seguito
     */
    public void removeFollowRelationship(String followerUsername, String followingUsername) {
        System.out.println("🔔 [Neo4jUserService] removeFollowRelationship called: " + followerUsername + " -> " + followingUsername);
        
        if (followerUsername == null || followerUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Username del follower non può essere null o vuoto");
        }
        
        if (followingUsername == null || followingUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Username di chi viene seguito non può essere null o vuoto");
        }
        
        try {
            neo4jUserRepository.removeFollow(followerUsername, followingUsername);
            System.out.println("✅ [Neo4jUserService] Follow relationship removed successfully");
        } catch (Exception e) {
            System.out.println("❌ [Neo4jUserService] Error removing follow relationship: " + e.getMessage());
            throw new RuntimeException("Errore nella rimozione della relazione di follow", e);
        }
    }
    
    /**
     * Verifica se un utente segue un altro
     * @param followerUsername l'username di chi segue
     * @param followingUsername l'username di chi viene seguito
     * @return true se esiste la relazione FOLLOWS
     */
    public boolean isFollowing(String followerUsername, String followingUsername) {
        System.out.println("🔔 [Neo4jUserService] isFollowing called: checking " + followerUsername + " -> " + followingUsername);
        try {
            Boolean result = neo4jUserRepository.isFollowing(followerUsername, followingUsername);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            System.out.println("❌ [Neo4jUserService] Error checking follow relationship: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ottiene il numero di seguaci di un utente
     * @param username lo username dell'utente
     * @return il numero di seguaci
     */
    public int getFollowersCount(String username) {
        System.out.println("🔔 [Neo4jUserService] getFollowersCount called for: " + username);
        try {
            int count = neo4jUserRepository.getFollowersCount(username);
            try {
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setFollowers(count);
                    userRepository.save(user);
                    System.out.println("✅ [Neo4jUserService] Synced Mongo followers for " + username + ": " + count);
                } else {
                    System.out.println("⚠️ [Neo4jUserService] Mongo user not found for username=" + username);
                }
            } catch (Exception mongoEx) {
                System.out.println("❌ [Neo4jUserService] Error syncing Mongo followers: " + mongoEx.getMessage());
            }
            System.out.println("✅ [Neo4jUserService] Followers count: " + count);
            return count;
        } catch (Exception e) {
            System.out.println("❌ [Neo4jUserService] Error getting followers count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Ottiene il numero di utenti seguiti
     * @param username lo username dell'utente
     * @return il numero di utenti seguiti
     */
    public int getFollowingCount(String username) {
        System.out.println("🔔 [Neo4jUserService] getFollowingCount called for: " + username);
        try {
            int count = neo4jUserRepository.getFollowingCount(username);
            System.out.println("✅ [Neo4jUserService] Following count: " + count);
            return count;
        } catch (Exception e) {
            System.out.println("❌ [Neo4jUserService] Error getting following count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Ottiene la lista di seguaci di un utente
     * @param username lo username dell'utente
     * @return Set di username dei seguaci
     */
    public Set<String> getFollowers(String username) {
        System.out.println("🔔 [Neo4jUserService] getFollowers called for: " + username);
        try {
            Set<String> followers = neo4jUserRepository.getFollowers(username);
            System.out.println("✅ [Neo4jUserService] Retrieved " + followers.size() + " followers");
            return followers;
        } catch (Exception e) {
            System.out.println("❌ [Neo4jUserService] Error getting followers: " + e.getMessage());
            return Set.of();
        }
    }
    
    /**
     * Ottiene la lista di utenti seguiti
     * @param username lo username dell'utente
     * @return Set di username degli utenti seguiti
     */
    public Set<String> getFollowing(String username) {
        System.out.println("🔔 [Neo4jUserService] getFollowing called for: " + username);
        try {
            Set<String> following = neo4jUserRepository.getFollowing(username);
            System.out.println("✅ [Neo4jUserService] Retrieved " + following.size() + " following");
            return following;
        } catch (Exception e) {
            System.out.println("❌ [Neo4jUserService] Error getting following: " + e.getMessage());
            return Set.of();
        }
    }
    
    /**
     * Ottiene o crea un utente Neo4j
     * @param username lo username dell'utente
     * @return l'utente Neo4j
     */
    public Neo4jUser getOrCreateUser(String username) {
        System.out.println("🔔 [Neo4jUserService] getOrCreateUser called for: " + username);
        try {
            Optional<Neo4jUser> existing = neo4jUserRepository.findByUsername(username);
            if (existing.isPresent()) {
                System.out.println("✅ [Neo4jUserService] User found: " + username);
                return existing.get();
            }
            
            Neo4jUser newUser = new Neo4jUser(username);
            Neo4jUser saved = neo4jUserRepository.save(newUser);
            System.out.println("✅ [Neo4jUserService] User created: " + username);
            return saved;
        } catch (Exception e) {
            System.out.println("❌ [Neo4jUserService] Error getting or creating user: " + e.getMessage());
            throw new RuntimeException("Errore nel recupero o creazione dell'utente Neo4j", e);
        }
    }
}
