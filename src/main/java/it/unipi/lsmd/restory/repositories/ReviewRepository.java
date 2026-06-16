package it.unipi.lsmd.restory.repositories;

import it.unipi.lsmd.restory.models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    @Query("{ 'user' : ?0 }")
    List<Review> findByUserId(String userId);

    @Query("{ 'username' : ?0 }")
    List<Review> findByUsername(String username);

    @Query("{ 'book.id' : ?0 }")
    List<Review> findByBookId(String bookId);

    @Query("{ 'user' : ?0, 'book.id' : ?1 }")
    List<Review> findByUserIdAndBookId(String userId, String bookId);

    @Query("{ 'book.id' : ?0, 'rating' : { $gte : ?1 } }")
    List<Review> findByBookIdAndMinRating(String bookId, Integer minRating);
}
