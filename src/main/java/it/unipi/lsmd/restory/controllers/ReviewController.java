package it.unipi.lsmd.restory.controllers;

import it.unipi.lsmd.restory.models.Review;
import it.unipi.lsmd.restory.services.ReviewService;
import it.unipi.lsmd.restory.services.RedisService;
import it.unipi.lsmd.restory.services.ReviewVoteSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import it.unipi.lsmd.restory.models.HighEngagementReviewerStats;
import it.unipi.lsmd.restory.models.PolarizingBookStats;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ReviewVoteSyncService reviewVoteSyncService;

    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review) {
        System.out.println("📡 [Controller] POST /api/reviews - addReview called");
        try {
            Review added = reviewService.addReview(review);
            System.out.println("✅ [Controller] addReview succeeded, mongoId=" + added.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(added);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [Controller] addReview failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        System.out.println("📡 [Controller] GET /api/reviews - getAllReviews called");
        List<Review> list = reviewService.getAllReviews();
        System.out.println("✅ [Controller] getAllReviews returned count=" + list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable String id) {
        System.out.println("📡 [Controller] GET /api/reviews/" + id + " - getReviewById called");
        Optional<Review> review = reviewService.getReviewById(id);
        if (review.isPresent()) {
            System.out.println("✅ [Controller] getReviewById found review with mongoId=" + review.get().getId());
            return ResponseEntity.ok(review.get());
        } else {
            System.out.println("⚠️ [Controller] getReviewById - review not found for id=" + id);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<List<Review>> getReviewsByUsername(@PathVariable String username) {
        System.out.println("📡 [Controller] GET /api/reviews/username/" + username + " - getReviewsByUsername called");
        List<Review> list = reviewService.getReviewsByUsername(username);
        System.out.println("✅ [Controller] getReviewsByUsername returned count=" + list.size());
        return ResponseEntity.ok(list);
    }
    /*
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUserId(@PathVariable String userId) {
        System.out.println("📡 [Controller] GET /api/reviews/user/" + userId + " - getReviewsByUserId called");
        List<Review> list = reviewService.getReviewsByUserId(userId);
        System.out.println("✅ [Controller] getReviewsByUserId returned count=" + list.size());
        return ResponseEntity.ok(list);
    }*/
    /*
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Review>> getReviewsByBookId(@PathVariable String bookId) {
        System.out.println("📡 [Controller] GET /api/reviews/book/" + bookId + " - getReviewsByBookId called");
        List<Review> list = reviewService.getReviewsByBookId(bookId);
        System.out.println("✅ [Controller] getReviewsByBookId returned count=" + list.size());
        return ResponseEntity.ok(list);
    }*/
    /*
    @GetMapping("/user/{userId}/book/{bookId}")
    public ResponseEntity<List<Review>> getReviewsByUserIdAndBookId(@PathVariable String userId, @PathVariable String bookId) {
        System.out.println("📡 [Controller] GET /api/reviews/user/" + userId + "/book/" + bookId + " - getReviewsByUserIdAndBookId called");
        List<Review> list = reviewService.getReviewsByUserIdAndBookId(userId, bookId);
        System.out.println("✅ [Controller] getReviewsByUserIdAndBookId returned count=" + list.size());
        return ResponseEntity.ok(list);
    }
    */

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable String id, @RequestBody Review review) {
        System.out.println("📡 [Controller] PUT /api/reviews/" + id + " - updateReview called");
        try {
            Review updated = reviewService.updateReview(id, review);
            System.out.println("✅ [Controller] updateReview succeeded for mongoId=" + updated.getId());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            System.out.println("❌ [Controller] updateReview failed: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        System.out.println("📡 [Controller] DELETE /api/reviews/" + id + " - deleteReview called");
        try {
            reviewService.deleteReview(id);
            System.out.println("✅ [Controller] deleteReview succeeded for id=" + id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("❌ [Controller] deleteReview failed: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    /*
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteReviewsByUserId(@PathVariable String userId) {
        System.out.println("📡 [Controller] DELETE /api/reviews/user/" + userId + " - deleteReviewsByUserId called");
        reviewService.deleteReviewsByUserId(userId);
        System.out.println("✅ [Controller] deleteReviewsByUserId completed for userId=" + userId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/count")
    public ResponseEntity<Long> countReviews() {
        System.out.println("📡 [Controller] GET /api/reviews/count - countReviews called");
        long count = reviewService.countReviews();
        System.out.println("✅ [Controller] countReviews result=" + count);
        return ResponseEntity.ok(count);
    }
    */
    @GetMapping("/stats/high-engagement-reviewers")
    public ResponseEntity<?> getHighEngagementReviewers(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer minVotes) {
        int lim = (limit == null) ? 10 : limit;
        int mv = (minVotes == null) ? 0 : minVotes;
        // Validate
        if (lim <= 0) {
            return ResponseEntity.badRequest().body("limit must be > 0");
        }
        if (mv <= 0) {
            return ResponseEntity.badRequest().body("minVotes must be > 0");
        }
        System.out.println("📡 [Controller] GET /api/reviews/stats/high-engagement-reviewers called with limit=" + lim + ", minVotes=" + mv);
        List<HighEngagementReviewerStats> list = reviewService.getHighEngagementReviewers(lim, mv);
        System.out.println("✅ [Controller] getHighEngagementReviewers returned count=" + list.size());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/stats/polarizing-books")
    public ResponseEntity<?> getPolarizingBooks(@RequestParam(required = false) Double minPerc) {
        double threshold = (minPerc == null) ? 25.0 : minPerc;
        // Validate: minPerc between 0 and 50
        if (threshold < 0.0 || threshold > 50.0) {
            return ResponseEntity.badRequest().body("minPerc must be between 0 and 50");
        }
        System.out.println("📡 [Controller] GET /api/reviews/stats/polarizing-books called with threshold=" + threshold);
        List<PolarizingBookStats> list = reviewService.getPolarizingBooks(threshold);
        System.out.println("✅ [Controller] getPolarizingBooks returned count=" + list.size());
        return ResponseEntity.ok(list);
    }

    /**
     * Aggiunge un like a una review
     * POST /api/reviews/{reviewId}/like?username={username}&username_review={username_review}&title={title}
     * @param reviewId l'ID della review
     * @param username l'username dell'utente che mette like
     * @param username_review l'username di chi ha scritto la review
     * @param title il titolo del libro recensito
     * @return ResponseEntity con messaggio di successo
     */
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<String> likeReview(@PathVariable String reviewId, @RequestParam String username, @RequestParam("username_review") String usernameReview, @RequestParam String title) {
        try {
            redisService.likeReview(username, reviewId);
            reviewService.incrementReviewedVotes(usernameReview, title);
            System.out.println("✅ [Controller] Like added: " + username + " -> review:" + reviewId);
            return ResponseEntity.ok("Like added successfully");
        } catch (Exception e) {
            System.out.println("❌ [Controller] Failed to add like: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to add like: " + e.getMessage());
        }
    }

    /**
     * Rimuove un like da una review
     * DELETE /api/reviews/{reviewId}/like?username={username}&username_review={username_review}&title={title}
     * @param reviewId l'ID della review
     * @param username l'username dell'utente che rimuove il like
     * @param username_review l'username di chi ha scritto la review
     * @param title il titolo del libro recensito
     * @return ResponseEntity con messaggio di successo
     */
    @DeleteMapping("/{reviewId}/like")
    public ResponseEntity<String> unlikeReview(@PathVariable String reviewId, @RequestParam String username, @RequestParam("username_review") String usernameReview, @RequestParam String title) {
        try {
            redisService.unlikeReview(username, reviewId);
            reviewService.decrementReviewedVotes(usernameReview, title);
            System.out.println("✅ [Controller] Like removed: " + username + " -> review:" + reviewId);
            return ResponseEntity.ok("Like removed successfully");
        } catch (Exception e) {
            System.out.println("❌ [Controller] Failed to remove like: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to remove like: " + e.getMessage());
        }
    }

    /**
     * Verifica se un utente ha messo like a una review
     * GET /api/reviews/{reviewId}/like?username={username}
     * @param reviewId l'ID della review
     * @param username l'username dell'utente
     * @return ResponseEntity con true/false
     */
    @GetMapping("/{reviewId}/like")
    public ResponseEntity<Boolean> hasLikedReview(@PathVariable String reviewId, @RequestParam String username) {
        try {
            boolean hasLiked = redisService.hasLikedReview(username, reviewId);
            System.out.println("✅ [Controller] Check like: " + username + " -> review:" + reviewId + " = " + hasLiked);
            return ResponseEntity.ok(hasLiked);
        } catch (Exception e) {
            System.out.println("❌ [Controller] Failed to check like: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    /**
     * Ottiene il numero di like per una review
     * GET /api/reviews/{reviewId}/likes/count
     * @param reviewId l'ID della review
     * @return ResponseEntity con il conteggio dei like
     */
    @GetMapping("/{reviewId}/likes/count")
    public ResponseEntity<Long> getLikesCount(@PathVariable String reviewId) {
        try {
            long count = redisService.getLikesCount(reviewId);
            System.out.println("✅ [Controller] Likes count for review:" + reviewId + " = " + count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            System.out.println("❌ [Controller] Failed to get likes count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0L);
        }
    }

    /**
     * Sincronizza i like da Redis a MongoDB n_votes.
     * GET /api/reviews/sync-votes
     */
    @GetMapping("/sync-votes")
    public ResponseEntity<String> syncVotes() {
        try {
            long modified = reviewVoteSyncService.syncReviewVotesNow();
            return ResponseEntity.ok("Synced votes for " + modified + " reviews");
        } catch (Exception e) {
            System.out.println("❌ [Controller] Failed to sync votes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to sync votes: " + e.getMessage());
        }
    }
}
