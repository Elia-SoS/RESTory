package it.unipi.lsmd.restory.controllers;

import it.unipi.lsmd.restory.models.Book;
import it.unipi.lsmd.restory.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * Crea un nuovo libro
     * POST /api/books
     * @param book i dati del nuovo libro
     * @return ResponseEntity con il libro creato e status 201
     */
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        System.out.println("🔔 [Controller] POST /api/books - createBook called with title=" + book.getTitle());
        try {
            Book createdBook = bookService.addBook(book);
            System.out.println("✅ [Controller] createBook succeeded, mongoId=" + createdBook.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [Controller] createBook failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Recupera un libro per ID
     * GET /api/books/{id}
     * @param id l'ID del libro
     * @return ResponseEntity con il libro se trovato, status 404 altrimenti
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable String id) {
        System.out.println("🔔 [Controller] GET /api/books/" + id + " - getBookById called");
        Optional<Book> book = bookService.getBookById(id);
        if (book.isPresent()) {
            System.out.println("✅ [Controller] getBookById found book with mongoId=" + book.get().getId());
            return ResponseEntity.ok(book.get());
        } else {
            System.out.println("⚠️ [Controller] getBookById - book not found for id=" + id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Recupera un libro per titolo
     * GET /api/books/title/{title}
     * @param title il titolo del libro
     * @return ResponseEntity con il libro se trovato, status 404 altrimenti
     */
    @GetMapping("/title/{title}")
    public ResponseEntity<Book> getBookByTitle(@PathVariable String title) {
        System.out.println("🔔 [Controller] GET /api/books/title/" + title + " - getBookByTitle called");
        Optional<Book> book = bookService.getBookByTitle(title);
        if (book.isPresent()) {
            System.out.println("✅ [Controller] getBookByTitle found mongoId=" + book.get().getId());
            return ResponseEntity.ok(book.get());
        } else {
            System.out.println("⚠️ [Controller] getBookByTitle - not found for title=" + title);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Recupera un libro per ISBN
     * GET /api/books/isbn/{isbn}
     * @param isbn l'ISBN del libro
     * @return ResponseEntity con il libro se trovato, status 404 altrimenti
     */
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
        System.out.println("🔔 [Controller] GET /api/books/isbn/" + isbn + " - getBookByIsbn called");
        Optional<Book> book = bookService.getBookByIsbn(isbn);
        return book.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Recupera un libro per ISBN13
     * GET /api/books/isbn13/{isbn13}
     * @param isbn13 l'ISBN13 del libro
     * @return ResponseEntity con il libro se trovato, status 404 altrimenti
     */
    @GetMapping("/isbn13/{isbn13}")
    public ResponseEntity<Book> getBookByIsbn13(@PathVariable String isbn13) {
        System.out.println("🔔 [Controller] GET /api/books/isbn13/" + isbn13 + " - getBookByIsbn13 called");
        Optional<Book> book = bookService.getBookByIsbn13(isbn13);
        return book.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Recupera tutti i libri di un genere
     * GET /api/books/genre/{genre}
     * @param genre il genere
     * @return ResponseEntity con la lista di libri del genere
     */
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Book>> getBooksByGenre(@PathVariable String genre) {
        System.out.println("🔔 [Controller] GET /api/books/genre/" + genre + " - getBooksByGenre called");
        List<Book> books = bookService.getBooksByGenre(genre);
        System.out.println("✅ [Controller] getBooksByGenre returned count=" + books.size());
        return ResponseEntity.ok(books);
    }

    /**
     * Recupera tutti i libri di un editore
     * GET /api/books/publisher/{publisher}
     * @param publisher l'editore
     * @return ResponseEntity con la lista di libri dell'editore
     */
    @GetMapping("/publisher/{publisher}")
    public ResponseEntity<List<Book>> getBooksByPublisher(@PathVariable String publisher) {
        System.out.println("🔔 [Controller] GET /api/books/publisher/" + publisher + " - getBooksByPublisher called");
        List<Book> books = bookService.getBooksByPublisher(publisher);
        System.out.println("✅ [Controller] getBooksByPublisher returned count=" + books.size());
        return ResponseEntity.ok(books);
    }

    @GetMapping("/trending-now")
    public ResponseEntity<List<Map<String, Object>>> getTrendingNow() {
        System.out.println("🔔 [Controller] GET /api/books/trending-now - getTrendingNow called");
        List<Map<String, Object>> trending = bookService.getTrendingNow();
        return ResponseEntity.ok(trending);
    }

    /**
     * Recupera tutti i libri di un anno di pubblicazione
     * GET /api/books/year/{year}
     * @param year l'anno
     * @return ResponseEntity con la lista di libri dell'anno
     */
    /*
    @GetMapping("/year/{year}")
    public ResponseEntity<List<Book>> getBooksByYear(@PathVariable Integer year) {
        System.out.println("🔔 [Controller] GET /api/books/year/" + year + " - getBooksByYear called");
        List<Book> books = bookService.getBooksByPublicationYear(year);
        System.out.println("✅ [Controller] getBooksByYear returned count=" + books.size());
        return ResponseEntity.ok(books);
    }
    */

    /**
     * Ricerca libri per titolo (ricerca parziale)
     * GET /api/books/search/title?query={query}
     * @param query il titolo da ricercare
     * @return ResponseEntity con la lista di libri trovati
     */
    @GetMapping("/search/title")
    public ResponseEntity<List<Book>> searchByTitle(@RequestParam String query) {
        System.out.println("🔔 [Controller] GET /api/books/search/title?query=" + query + " - searchByTitle called");
        List<Book> books = bookService.searchBooksByTitle(query);
        System.out.println("✅ [Controller] searchByTitle returned count=" + books.size());
        return ResponseEntity.ok(books);
    }

    /**
     * Ricerca libri per autore
     * GET /api/books/search/author?query={query}
     * @param query il nome dell'autore da ricercare
     * @return ResponseEntity con la lista di libri trovati
     */
    @GetMapping("/search/author")
    public ResponseEntity<List<Book>> searchByAuthor(@RequestParam String query) {
        System.out.println("🔔 [Controller] GET /api/books/search/author?query=" + query + " - searchByAuthor called");
        List<Book> books = bookService.searchBooksByAuthor(query);
        System.out.println("✅ [Controller] searchByAuthor returned count=" + books.size());
        return ResponseEntity.ok(books);
    }

    /**
     * Recupera libri ebook
     * GET /api/books/ebook?isEbook={true|false}
     * @param isEbook true per ebook, false per libri cartacei
     * @return ResponseEntity con la lista di libri

    @GetMapping("/ebook")
    public ResponseEntity<List<Book>> getEbooks(@RequestParam Boolean isEbook) {
        System.out.println("🔔 [Controller] GET /api/books/ebook?isEbook=" + isEbook + " - getEbooks called");
        List<Book> books = bookService.getEbooks(isEbook);
        System.out.println("✅ [Controller] getEbooks returned count=" + books.size());
        return ResponseEntity.ok(books);
    }
     */
    /**
     * Recupera libri con almeno N recensioni
     * GET /api/books/popular?minReviews={count}
     * @param minReviews numero minimo di recensioni
     * @return ResponseEntity con la lista di libri
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Book>> getPopularBooks(@RequestParam Integer minReviews) {
        System.out.println("🔔 [Controller] GET /api/books/popular?minReviews=" + minReviews + " - getPopularBooks called");
        List<Book> books = bookService.getBooksByMinReviews(minReviews);
        System.out.println("✅ [Controller] getPopularBooks returned count=" + books.size());
        return ResponseEntity.ok(books);
    }

    /**
     * Recupera tutti i libri
     * GET /api/books
     * @return ResponseEntity con la lista di tutti i libri
     */
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        System.out.println("🔔 [Controller] GET /api/books - getAllBooks called");
        List<Book> books = bookService.getAllBooks();
        System.out.println("✅ [Controller] getAllBooks returned count=" + books.size());
        return ResponseEntity.ok(books);
    }

    /**
     * Aggiorna un libro esistente
     * PUT /api/books/{id}
     * @param id l'ID del libro da aggiornare
     * @param bookDetails i nuovi dettagli del libro
     * @return ResponseEntity con il libro aggiornato
     */
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @RequestBody Book bookDetails) {
        System.out.println("🔔 [Controller] PUT /api/books/" + id + " - updateBook called");
        try {
            Book updatedBook = bookService.updateBook(id, bookDetails);
            System.out.println("✅ [Controller] updateBook succeeded for id=" + id);
            return ResponseEntity.ok(updatedBook);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [Controller] updateBook failed: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un libro
     * DELETE /api/books/{id}
     * @param id l'ID del libro da eliminare
     * @return ResponseEntity con status 204 se eliminato
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        System.out.println("🔔 [Controller] DELETE /api/books/" + id + " - deleteBook called");
        try {
            bookService.deleteBook(id);
            System.out.println("✅ [Controller] deleteBook succeeded for id=" + id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [Controller] deleteBook failed: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
