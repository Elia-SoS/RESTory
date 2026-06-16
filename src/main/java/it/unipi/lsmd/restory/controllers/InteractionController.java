package it.unipi.lsmd.restory.controllers;

import it.unipi.lsmd.restory.models.Interaction;
import it.unipi.lsmd.restory.services.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import it.unipi.lsmd.restory.models.MostAbandonedBookStats;
import it.unipi.lsmd.restory.models.ReadingSeasonalityStats;
import it.unipi.lsmd.restory.models.GenreDropStats;
import it.unipi.lsmd.restory.models.ReadingSpeedStats;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {
    
    @Autowired
    private InteractionService interactionService;
    
    /**
     * Aggiunge una nuova interazione
     * @param interaction l'interazione da aggiungere
     * @return la risposta con l'interazione aggiunta e status 201
     */
    @PostMapping
    public ResponseEntity<?> addInteraction(@RequestBody Interaction interaction) {
        System.out.println("📡 [Controller] POST /api/interactions called");
        try {
            Interaction added = interactionService.addInteraction(interaction);
            System.out.println("✅ [Controller] addInteraction succeeded, mongoId=" + added.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(added);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [Controller] addInteraction failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    /**
     * Recupera tutte le interazioni
     * @return la lista di tutte le interazioni
     */
    @GetMapping
    public ResponseEntity<List<Interaction>> getAllInteractions() {
        System.out.println("📡 [Controller] GET /api/interactions called");
        List<Interaction> interactions = interactionService.getAllInteractions();
        return ResponseEntity.ok(interactions);
    }
    
    /**
     * Recupera un'interazione per ID
     * @param id l'ID dell'interazione
     * @return l'interazione se presente, altrimenti 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Interaction> getInteractionById(@PathVariable String id) {
        System.out.println("📡 [Controller] GET /api/interactions/" + id + " called");
        Optional<Interaction> interaction = interactionService.getInteractionById(id);
        return interaction.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Recupera tutte le interazioni di un utente
     * @param username dell'utente
     * @return la lista di interazioni dell'utente
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<Interaction>> getInteractionsByUsername(@PathVariable String username) {
        System.out.println("📡 [Controller] GET /api/interactions/user/" + username + " called");
        List<Interaction> interactions = interactionService.getInteractionsByUsername(username);
        return ResponseEntity.ok(interactions);
    }
    
    /**
     * Recupera tutte le interazioni per un libro
     * @param title  del libro
     * @return la lista di interazioni per il libro
     */
    @GetMapping("/book/{bookTitle}")
    public ResponseEntity<List<Interaction>> getInteractionsByBookTitle(@PathVariable("bookTitle") String bookTitle) {
        System.out.println("📡 [Controller] GET /api/interactions/book/" + bookTitle + " called");
        List<Interaction> interactions = interactionService.getInteractionsByBookTitle(bookTitle);
        return ResponseEntity.ok(interactions);
    }
    
    /**
     * Recupera le interazioni di un utente per un libro specifico
     * @param username il nome dell'utente
     * @param title il titolo del libro
     * @return la lista di interazioni
     */
    /*
    @GetMapping("/user/{username}/book/{title}")
    public ResponseEntity<List<Interaction>> getInteractionsByUsernameAndBookTitle(
            @PathVariable String username, @PathVariable String title) {
        System.out.println("📡 [Controller] GET /api/interactions/user/" + username + "/book/" + title + " called");
        List<Interaction> interactions = interactionService.getInteractionsByUsernameAndBookTitle(username, title);
        return ResponseEntity.ok(interactions);
    }
    tanto ce n'è una sola
     */
    
    /**
     * Recupera le interazioni lette da un utente
     * @param username il nome dell'utente
     * @return la lista di interazioni lette

    @GetMapping("/user/{username}/read")
    public ResponseEntity<List<Interaction>> getReadInteractionsByUsername(@PathVariable String username) {
        System.out.println("📡 [Controller] GET /api/interactions/user/" + username + "/read called");
        List<Interaction> interactions = interactionService.getReadInteractionsByUsername(username);
        return ResponseEntity.ok(interactions);
    }
     */
    /**
     * Recupera le interazioni non lette da un utente
     * @param username il nome dell'utente
     * @return la lista di interazioni non lette

    @GetMapping("/user/{username}/unread")
    public ResponseEntity<List<Interaction>> getUnreadInteractionsByUsername(@PathVariable String username) {
        System.out.println("📡 [Controller] GET /api/interactions/user/" + username + "/unread called");
        List<Interaction> interactions = interactionService.getUnreadInteractionsByUsername(username);
        return ResponseEntity.ok(interactions);
    }
     */
    /**
     * Recupera le interazioni aggiunte in un intervallo di date
     * @param startDate data inizio (formato: dd/MM/yyyy)
     * @param endDate data fine (formato: dd/MM/yyyy)
     * @return la lista di interazioni nell'intervallo

    @GetMapping("/between-dates")
    public ResponseEntity<List<Interaction>> getInteractionsBetweenDates(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate) {
        System.out.println("📡 [Controller] GET /api/interactions/between-dates called for startDate=" + startDate + ", endDate=" + endDate);
        List<Interaction> interactions = interactionService.getInteractionsBetweenDates(startDate, endDate);
        return ResponseEntity.ok(interactions);
    }
     */
    
    /**
     * Aggiorna un'interazione
     * @param id l'ID dell'interazione
     * @param interaction i dati aggiornati
     * @return la risposta con l'interazione aggiornata
     */
    @PutMapping("/{id}")
    public ResponseEntity<Interaction> updateInteraction(@PathVariable String id, @RequestBody Interaction interaction) {
        System.out.println("📡 [Controller] PUT /api/interactions/" + id + " called");
        try {
            Interaction updated = interactionService.updateInteraction(id, interaction);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Elimina un'interazione
     * @param id l'ID dell'interazione
     * @return la risposta con status 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInteraction(@PathVariable String id) {
        System.out.println("📡 [Controller] DELETE /api/interactions/" + id + " called");
        interactionService.deleteInteraction(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Elimina tutte le interazioni di un utente
     * @param username il nome dell'utente
     * @return la risposta con status 204 (No Content)
     */
    @DeleteMapping("/user/{username}")
    public ResponseEntity<Void> deleteInteractionsByUsername(@PathVariable String username) {
        System.out.println("📡 [Controller] DELETE /api/interactions/user/" + username + " called");
        interactionService.deleteInteractionsByUsername(username);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Recupera il numero totale di interazioni
     * @return il numero di interazioni

    @GetMapping("/count")
    public ResponseEntity<Long> countInteractions() {
        System.out.println("📡 [Controller] GET /api/interactions/count called");
        long count = interactionService.countInteractions();
        return ResponseEntity.ok(count);
    }
     */

    @GetMapping("/stats/most-abandoned")
    public ResponseEntity<?> getMostAbandoned(
            @RequestParam(required = false) Integer minReaders,
            @RequestParam(required = false) Integer limit) {
        int minR = (minReaders == null) ? 20 : minReaders;
        int lim = (limit == null) ? 10 : limit;
        // Validate
        if (minR <= 0) {
            return ResponseEntity.badRequest().body("minReaders must be > 0");
        }
        if (lim <= 0) {
            return ResponseEntity.badRequest().body("limit must be > 0");
        }
        System.out.println("📡 [Controller] GET /api/interactions/stats/most-abandoned called with minReaders=" + minR + ", limit=" + lim);
        List<MostAbandonedBookStats> stats = interactionService.getMostAbandonedBooks(minR, lim);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/reading-seasonality")
    public ResponseEntity<List<ReadingSeasonalityStats>> getReadingSeasonality() {
        System.out.println("📡 [Controller] GET /api/interactions/stats/reading-seasonality called");
        List<ReadingSeasonalityStats> stats = interactionService.getReadingSeasonality();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/genre-drop-averages")
    public ResponseEntity<List<GenreDropStats>> getGenreDropAverages() {
        System.out.println("📡 [Controller] GET /api/interactions/stats/genre-drop-averages called");
        List<GenreDropStats> stats = interactionService.getGenreDropAverages();
        System.out.println("✅ [Controller]  getGenreDropAverages returned count=" + stats.size());
        return ResponseEntity.ok(stats);
    }

    /**
     * Restituisce le statistiche di velocità di lettura per un singolo utente (giorni medi per libro).
     */
    @GetMapping("/stats/reading-speed/{username}")
    public ResponseEntity<List<ReadingSpeedStats>> getReadingSpeedStats(@PathVariable String username) {
        System.out.println("📡 [Controller] GET /api/interactions/stats/reading-speed/" + username + " called");
        List<ReadingSpeedStats> stats = interactionService.getAverageReadingSpeedPerUser(username);
        System.out.println("✅ [Controller]  getReadingSpeedStats returned count=" + stats.size());
        return ResponseEntity.ok(stats);
    }
}
