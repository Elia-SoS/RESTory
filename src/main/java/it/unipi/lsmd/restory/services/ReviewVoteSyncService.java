package it.unipi.lsmd.restory.services;

import it.unipi.lsmd.restory.models.Review;
import it.unipi.lsmd.restory.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewVoteSyncService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RedisService redisService;

    /**
     * Routine giornaliera a mezzanotte per sincronizzare n_votes su MongoDB con i like salvati in Redis.
     * Usa il pattern dirty list per aggiornare solo le review modificate durante il giorno.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void syncReviewVotesFromRedis() {
        List<Object> dirtyReviewIds = redisService.getDirtyList();
        long processed = 0;

        for (Object reviewIdObj : dirtyReviewIds) {
            String reviewId = reviewIdObj.toString();
            Review review = reviewRepository.findById(reviewId).orElse(null);
            if (review != null) {
                long redisCount = redisService.getLikesCount(reviewId);
                Integer currentVotes = (review.getnVotes() == null) ? 0 : review.getnVotes();
                if (currentVotes.longValue() != redisCount) {
                    review.setnVotes((int) redisCount);
                    reviewRepository.save(review);
                }
                processed++;
            }
        }

        // Pulisce la dirty list dopo la sincronizzazione
        redisService.clearDirtyList();
        System.out.println("✅ [ReviewVoteSyncService] syncReviewVotesFromRedis completed. Dirty reviews processed: " + processed);
    }

    /**
     * Metodo chiamabile manualmente per forzare la sincronizzazione.
     * Usa il pattern dirty list per aggiornare solo le review modificate.
     */
    public long syncReviewVotesNow() {
        List<Object> dirtyReviewIds = redisService.getDirtyList();
        long modified = 0;

        for (Object reviewIdObj : dirtyReviewIds) {
            String reviewId = reviewIdObj.toString();
            Review review = reviewRepository.findById(reviewId).orElse(null);
            if (review != null) {
                long redisCount = redisService.getLikesCount(reviewId);
                Integer currentVotes = (review.getnVotes() == null) ? 0 : review.getnVotes();
                if (currentVotes.longValue() != redisCount) {
                    review.setnVotes((int) redisCount);
                    reviewRepository.save(review);
                    modified++;
                }
            }
        }

        // Opzionale: pulisce la dirty list anche nella chiamata manuale
        redisService.clearDirtyList();
        System.out.println("✅ [ReviewVoteSyncService] syncReviewVotesNow completed. Modified reviews: " + modified);
        return modified;
    }
}
