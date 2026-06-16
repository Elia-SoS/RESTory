package it.unipi.lsmd.restory.controllers;

import it.unipi.lsmd.restory.services.InteractionSyncService;
import it.unipi.lsmd.restory.services.ReviewVoteSyncService;
import it.unipi.lsmd.restory.services.BookStatSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    @Autowired
    private ReviewVoteSyncService reviewVoteSyncService;

    @Autowired
    private InteractionSyncService interactionSyncService;

    @Autowired
    private BookStatSyncService bookStatSyncService;

    /**
     * Sincronizzazione manuale dei voti delle review
     * POST /api/sync/reviews
     */
    @PostMapping("/reviews")
    public ResponseEntity<String> syncReviews() {
        try {
            reviewVoteSyncService.syncReviewVotesNow();
            return ResponseEntity.ok("Review votes sync completed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error during review sync: " + e.getMessage());
        }
    }

    /**
     * Sincronizzazione manuale delle interazioni
     * POST /api/sync/interactions
     */
    @PostMapping("/interactions")
    public ResponseEntity<String> syncInteractions() {
        try {
            interactionSyncService.manualSync();
            return ResponseEntity.ok("Interactions sync completed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error during interactions sync: " + e.getMessage());
        }
    }

    /**
     * Sincronizzazione di una singola interazione
     * POST /api/sync/interactions/{interactionId}
     */
    @PostMapping("/interactions/{interactionId}")
    public ResponseEntity<String> syncSingleInteraction(@PathVariable String interactionId) {
        try {
            interactionSyncService.syncSingleInteraction(interactionId);
            return ResponseEntity.ok("Interaction " + interactionId + " synced successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error during single interaction sync: " + e.getMessage());
        }
    }

    /**
     * Sincronizzazione manuale delle statistiche libro
     * POST /api/sync/bookstats
     */
    @PostMapping("/bookstats")
    public ResponseEntity<String> syncBookStats() {
        try {
            bookStatSyncService.manualSync();
            return ResponseEntity.ok("Book stats sync completed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error during book stats sync: " + e.getMessage());
        }
    }

    /**
     * Sincronizzazione di una singola statistica libro
     * POST /api/sync/bookstats/{bookId}
     */
    @PostMapping("/bookstats/{bookId}")
    public ResponseEntity<String> syncSingleBookStat(@PathVariable String bookId) {
        try {
            bookStatSyncService.syncSingleBookStat(bookId);
            return ResponseEntity.ok("Book stat " + bookId + " synced successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error during single book stat sync: " + e.getMessage());
        }
    }
}