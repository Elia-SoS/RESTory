package it.unipi.lsmd.restory.repositories;

import it.unipi.lsmd.restory.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    
    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    java.util.List<User> findByUsernameContainsIgnoreCase(String username);
}
