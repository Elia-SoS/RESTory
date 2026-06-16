package it.unipi.lsmd.restory.services;

import it.unipi.lsmd.restory.models.Book;
import it.unipi.lsmd.restory.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisService redisService;

    @Autowired(required = false)
    private Driver neo4jDriver;
    
    /**
     * Aggiunge un nuovo libro al sistema
     * @param book il libro da aggiungere
     * @return il libro aggiunto con il suo ID generato
     * @throws IllegalArgumentException se il libro esiste già
     */
    public Book addBook(Book book) {
        System.out.println("🔔 [Service] addBook called for title=" + book.getTitle());
        

        
        // Inizializza i campi di default se null
        if (book.getReviewsCount() == null) {
            book.setReviewsCount(0);
        }
        if (book.getIsEbook() == null) {
            book.setIsEbook(false);
        }
        if (book.getNumPages() == null) {
            book.setNumPages(0);
        }
        // publicationDate handled as LocalDate; leave null if not provided
        if (book.getIsbn() == null) {
            book.setIsbn("");
        }
        if (book.getIsbn13() == null) {
            book.setIsbn13("");
        }
        if (book.getAsin() == null) {
            book.setAsin("");
        }
        if (book.getKindleAsin() == null) {
            book.setKindleAsin("");
        }
        if (book.getPublisher() == null) {
            book.setPublisher("");
        }
        if (book.getEditionInformation() == null) {
            book.setEditionInformation("");
        }
        if (book.getLink() == null) {
            book.setLink("");
        }
        if (book.getImageUrl() == null) {
            book.setImageUrl("");
        }
        if (book.getFormat() == null) {
            book.setFormat("");
        }
        
        // Salva e ritorna il libro aggiunto
        Book saved = bookRepository.save(book);
        System.out.println("✅ [Service] addBook saved mongoId=" + saved.getId());

        try {
            java.util.Map<String, Object> bookStat = new java.util.HashMap<>();
            bookStat.put("book_id", saved.getId());
            bookStat.put("title", saved.getTitle());
            bookStat.put("authors", saved.getAuthors());
            bookStat.put("n_added", 0);
            redisService.createBookStat(saved.getId(), bookStat);
            System.out.println("✅ [Service] addBook created initial Redis BookStat for book: " + saved.getId());
        } catch (Exception e) {
            System.out.println("❌ [Service] addBook failed to create initial Redis BookStat: " + e.getMessage());
        }
        // Crea un nodo Book su Neo4j con title e mainAuthor (se Neo4j disponibile)
        try {
            if (neo4jDriver != null) {
                String mainAuthor = "";
                if (saved.getAuthors() != null && !saved.getAuthors().isEmpty()) {
                    Object first = saved.getAuthors().get(0).get("name");
                    if (first == null) {
                        // try other common keys
                        first = saved.getAuthors().get(0).get("author");
                    }
                    if (first != null) mainAuthor = first.toString();
                }
                String neo4jBookId = saved.getId();
                if (mainAuthor != null && !mainAuthor.trim().isEmpty()) {
                    neo4jBookId = saved.getId() + "-" + mainAuthor;
                }

                try (Session session = neo4jDriver.session()) {
                    session.run("CREATE (b:Book {book_id: $book_id, title: $title, mainAuthor: $mainAuthor})",
                            Values.parameters("book_id", neo4jBookId, "title", saved.getTitle(), "mainAuthor", mainAuthor));
                    System.out.println("✅ [Service] Created Neo4j Book node for book_id=" + neo4jBookId);
                }
            } else {
                System.out.println("⚠️ [Service] Neo4j driver not configured; skipping Neo4j node creation");
            }
        } catch (Exception e) {
            System.out.println("❌ [Service] Failed to create Neo4j Book node: " + e.getMessage());
        }
        return saved;
    }
    
    /**
     * Recupera un libro per ID
     * @param id l'ID del libro
     * @return Optional contenente il libro se trovato
     */
    public Optional<Book> getBookById(String id) {
        System.out.println("🔔 [Service] getBookById called with id=" + id);
        Optional<Book> result = bookRepository.findByBookId(id);
        System.out.println(result.isPresent() ? "✅ [Service] getBookById found book" : "⚠️ [Service] getBookById not found");
        return result;
    }
    
    /**
     * Recupera un libro per titolo
     * @param title il titolo del libro
     * @return Optional contenente il libro se trovato
     */
    public Optional<Book> getBookByTitle(String title) {
        System.out.println("🔔 [Service] getBookByTitle called with title=" + title);
        List<Book> books = bookRepository.findAllByTitle(title);
        if (books.isEmpty()) {
            System.out.println("⚠️ [Service] getBookByTitle not found");
            return Optional.empty();
        }
        Book firstBook = books.get(0);
        System.out.println("✅ [Service] getBookByTitle found book, returning first match with mongoId=" + firstBook.getId());
        return Optional.of(firstBook);
    }
    
    /**
     * Recupera un libro per ISBN
     * @param isbn l'ISBN del libro
     * @return Optional contenente il libro se trovato
     */
    public Optional<Book> getBookByIsbn(String isbn) {
        System.out.println("🔔 [Service] getBookByIsbn called with isbn=" + isbn);
        Optional<Book> result = bookRepository.findByIsbn(isbn);
        System.out.println(result.isPresent() ? "✅ [Service] getBookByIsbn found book" : "⚠️ [Service] getBookByIsbn not found");
        return result;
    }
    
    /**
     * Recupera un libro per ISBN13
     * @param isbn13 l'ISBN13 del libro
     * @return Optional contenente il libro se trovato
     */
    public Optional<Book> getBookByIsbn13(String isbn13) {
        System.out.println("🔔 [Service] getBookByIsbn13 called with isbn13=" + isbn13);
        Optional<Book> result = bookRepository.findByIsbn13(isbn13);
        System.out.println(result.isPresent() ? "✅ [Service] getBookByIsbn13 found book" : "⚠️ [Service] getBookByIsbn13 not found");
        return result;
    }
    
    /**
     * Recupera tutti i libri di un genere
     * @param genre il genere
     * @return lista di libri del genere
     */
    public List<Book> getBooksByGenre(String genre) {
        System.out.println("🔔 [Service] getBooksByGenre called with genre=" + genre);
        Query query = new Query();
        query.addCriteria(Criteria.where("genre").regex(genre, "i"));
        List<Book> result = mongoTemplate.find(query, Book.class);
        System.out.println("✅ [Service] getBooksByGenre returned count=" + result.size());
        return result;
    }
    
    /**
     * Recupera tutti i libri di un editore
     * @param publisher l'editore
     * @return lista di libri dell'editore
     */
    public List<Book> getBooksByPublisher(String publisher) {
        System.out.println("🔔 [Service] getBooksByPublisher called with publisher=" + publisher);
        List<Book> result = bookRepository.findByPublisher(publisher);
        System.out.println("✅ [Service] getBooksByPublisher returned count=" + result.size());
        return result;
    }
    
    /**
     * Recupera tutti i libri di un anno di pubblicazione
     * @param year l'anno
     * @return lista di libri dell'anno
     */

    /*
    public List<Book> getBooksByPublicationYear(Integer year) {
        System.out.println("🔔 [Service] getBooksByPublicationYear called with year=" + year);
        if (year == null) {
            System.out.println("⚠️ [Service] getBooksByPublicationYear called with null year");
            return List.of();
        }
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = start.plusYears(1);
        Query query = new Query();
        query.addCriteria(Criteria.where("publicationdate").gte(start).lt(end));
        List<Book> result = mongoTemplate.find(query, Book.class);
        System.out.println("✅ [Service] getBooksByPublicationYear returned count=" + result.size());
        return result;
    }
    */
    /**
     * Ricerca libri per titolo (ricerca parziale, case-insensitive)
     * @param title il titolo da ricercare
     * @return lista di libri trovati
     */
    public List<Book> searchBooksByTitle(String title) {
        System.out.println("🔔 [Service] searchBooksByTitle called with query=" + title);
        

        // Ricerca con regex
        Pattern pattern = Pattern.compile(title, Pattern.CASE_INSENSITIVE);
        Query query = new Query();
        query.addCriteria(Criteria.where("title").regex(pattern.pattern(), "i"));
        List<Book> result = mongoTemplate.find(query, Book.class);
        System.out.println("✅ [Service] searchBooksByTitle returned count=" + result.size());
        
        return result;
    }
    
    /**
     * Ricerca libri per autore
     * @param authorName il nome dell'autore da ricercare
     * @return lista di libri dell'autore
     */
    public List<Book> searchBooksByAuthor(String authorName) {
        System.out.println("🔔 [Service] searchBooksByAuthor called with query=" + authorName);
        Query query = new Query();
        query.addCriteria(Criteria.where("authors.name").regex(authorName, "i"));
        List<Book> result = mongoTemplate.find(query, Book.class);
        System.out.println("✅ [Service] searchBooksByAuthor returned count=" + result.size());
        return result;
    }
    
    /**
     * Recupera libri ebook o no
     * @param isEbook true per ebook, false per libri cartacei
     * @return lista di libri
     */
    public List<Book> getEbooks(Boolean isEbook) {
        System.out.println("🔔 [Service] getEbooks called with isEbook=" + isEbook);
        List<Book> result = bookRepository.findByIsEbook(isEbook);
        System.out.println("✅ [Service] getEbooks returned count=" + result.size());
        return result;
    }
    
    /**
     * Recupera libri con almeno N recensioni
     * @param minReviews numero minimo di recensioni
     * @return lista di libri
     */
    public List<Book> getBooksByMinReviews(Integer minReviews) {
        System.out.println("🔔 [Service] getBooksByMinReviews called with minReviews=" + minReviews);
        List<Book> result = bookRepository.findByReviewsCountGreaterThanOrEqual(minReviews);
        System.out.println("✅ [Service] getBooksByMinReviews returned count=" + result.size());
        return result;
    }
    
    /**
     * Recupera tutti i libri
     * @return lista di tutti i libri
     */
    public List<Book> getAllBooks() {
        System.out.println("🔔 [Service] getAllBooks called");
        List<Book> result = bookRepository.findAll();
        System.out.println("✅ [Service] getAllBooks returned count=" + result.size());
        return result;
    }

    public List<Map<String, Object>> getTrendingNow() {
        System.out.println("🔔 [Service] getTrendingNow called");
        return redisService.getTrendingNow();
    }
    
    /**
     * Aggiorna un libro esistente
     * @param id l'ID del libro da aggiornare
     * @param bookDetails i nuovi dettagli del libro
     * @return il libro aggiornato
     * @throws IllegalArgumentException se il libro non esiste
     */
    public Book updateBook(String id, Book bookDetails) {
        System.out.println("🔔 [Service] updateBook called with id=" + id);
        Optional<Book> existingBook = bookRepository.findByBookId(id);
        
        if (!existingBook.isPresent()) {
            System.out.println("❌ [Service] updateBook failed - book not found: " + id);
            throw new IllegalArgumentException("Libro con ID '" + id + "' non trovato");
        }
        
        Book book = existingBook.get();
        
        // Aggiorna i campi forniti
        if (bookDetails.getTitle() != null) book.setTitle(bookDetails.getTitle());
        if (bookDetails.getGenre() != null) book.setGenre(bookDetails.getGenre());
        if (bookDetails.getIsbn() != null) book.setIsbn(bookDetails.getIsbn());
        if (bookDetails.getIsbn13() != null) book.setIsbn13(bookDetails.getIsbn13());
        if (bookDetails.getReviewsCount() != null) book.setReviewsCount(bookDetails.getReviewsCount());
        if (bookDetails.getAsin() != null) book.setAsin(bookDetails.getAsin());
        if (bookDetails.getIsEbook() != null) book.setIsEbook(bookDetails.getIsEbook());
        if (bookDetails.getKindleAsin() != null) book.setKindleAsin(bookDetails.getKindleAsin());
        if (bookDetails.getNumPages() != null) book.setNumPages(bookDetails.getNumPages());
        if (bookDetails.getAuthors() != null) book.setAuthors(bookDetails.getAuthors());
        if (bookDetails.getPublisher() != null) book.setPublisher(bookDetails.getPublisher());
        if (bookDetails.getPublicationDate() != null) book.setPublicationDate(bookDetails.getPublicationDate());
        if (bookDetails.getScore() != null) book.setScore(bookDetails.getScore());
        if (bookDetails.getNAdded() != null) book.setNAdded(bookDetails.getNAdded());
        if (bookDetails.getLanguageCode() != null) book.setLanguageCode(bookDetails.getLanguageCode());
        if (bookDetails.getDescription() != null) book.setDescription(bookDetails.getDescription());
        if (bookDetails.getLatestReviews() != null) book.setLatestReviews(bookDetails.getLatestReviews());
        if (bookDetails.getOtherReviews() != null) book.setOtherReviews(bookDetails.getOtherReviews());
        if (bookDetails.getEditionInformation() != null) book.setEditionInformation(bookDetails.getEditionInformation());
        if (bookDetails.getLink() != null) book.setLink(bookDetails.getLink());
        if (bookDetails.getImageUrl() != null) book.setImageUrl(bookDetails.getImageUrl());
        if (bookDetails.getFormat() != null) book.setFormat(bookDetails.getFormat());
        
        Book updated = bookRepository.save(book);
        System.out.println("✅ [Service] updateBook succeeded for id=" + id);
        return updated;
    }
    
    /**
     * Elimina un libro
     * @param id l'ID del libro da eliminare
     * @throws IllegalArgumentException se il libro non esiste
     */
    public void deleteBook(String id) {
        System.out.println("🔔 [Service] deleteBook called with id=" + id);
        Optional<Book> book = bookRepository.findByBookId(id);
        
        if (!book.isPresent()) {
            System.out.println("❌ [Service] deleteBook failed - book not found: " + id);
            throw new IllegalArgumentException("Libro con ID '" + id + "' non trovato");
        }
        
        bookRepository.delete(book.get());
        System.out.println("✅ [Service] deleteBook succeeded for id=" + id);
    }
}
