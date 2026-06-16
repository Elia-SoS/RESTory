package it.unipi.lsmd.restory.repositories;

import it.unipi.lsmd.restory.models.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {
    
    @Query("{ '_id' : ?0 }")
    Optional<Book> findByBookId(String id);
    
    Optional<Book> findByTitle(String title);
    
    List<Book> findAllByTitle(String title);
    
    Optional<Book> findByIsbn(String isbn);
    
    Optional<Book> findByIsbn13(String isbn13);
    
    List<Book> findByGenre(String genre);
    
    List<Book> findByPublisher(String publisher);
    
   // List<Book> findByPublicationYear(Integer year);
    
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<Book> findByTitleContainsIgnoreCase(String title);
    
    @Query("{ 'genre': { $regex: ?0, $options: 'i' } }")
    List<Book> findByGenreContainsIgnoreCase(String genre);
    
    @Query("{ 'authors.name': { $regex: ?0, $options: 'i' } }")
    List<Book> findByAuthorNameContainsIgnoreCase(String authorName);
    
    @Query("{ 'is_ebook': ?0 }")
    List<Book> findByIsEbook(Boolean isEbook);
    
    @Query("{ 'reviews_count': { $gte: ?0 } }")
    List<Book> findByReviewsCountGreaterThanOrEqual(Integer count);
}
