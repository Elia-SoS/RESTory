package it.unipi.lsmd.restory.services;

import it.unipi.lsmd.restory.models.Book;
import it.unipi.lsmd.restory.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BookStatSyncService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RedisService redisService;

    /**
     * Sincronizza n_added dei libri dalla cache Redis a MongoDB ogni notte alle 3:00
     */
    @Scheduled(cron = "0 0 3 * * ?") // Ogni notte alle 3:00
    public void syncBookStatsToMongoDB() {
        System.out.println("🔄 [BookStatSync] Starting scheduled sync of book stats from Redis to MongoDB");

        // Sync only books touched by addInteraction
        Set<String> dirtyBooks = redisService.getDirtyBooks();

        if (dirtyBooks == null || dirtyBooks.isEmpty()) {
            System.out.println("ℹ️ [BookStatSync] No dirty books found in Redis");
            return;
        }

        int syncedCount = 0;
        int errorCount = 0;

        for (String bookId : dirtyBooks) {
            try {
                // Recupera n_added dalla cache Redis
                Integer redisNAdded = redisService.getBookNAdded(bookId);
                if (redisNAdded == null || redisNAdded == 0) {
                    continue;
                }

                // Aggiorna MongoDB
                Book book = bookRepository.findByBookId(bookId).orElse(null);
                if (book != null) {
                    Integer currentNAdded = (book.getNAdded() == null) ? 0 : book.getNAdded();
                    if (!currentNAdded.equals(redisNAdded)) {
                        book.setNAdded(redisNAdded);
                        bookRepository.save(book);
                        syncedCount++;
                        System.out.println("✅ [BookStatSync] Updated book " + bookId + " n_added: " + currentNAdded + " -> " + redisNAdded);
                    }
                } else {
                    System.err.println("⚠️ [BookStatSync] Book not found in MongoDB: " + bookId);
                    errorCount++;
                }

            } catch (Exception e) {
                System.err.println("❌ [BookStatSync] Error syncing book stat: " + e.getMessage());
                errorCount++;
            }
        }

        // Clear dirty set after sync completion
        redisService.clearDirtyBooks();

        System.out.println("✅ [BookStatSync] Sync completed. Synced: " + syncedCount + ", Errors: " + errorCount);
    }

    /**
     * Controlla i BookStat scaduti in cache e popola il bucket trending_now
     */
    @Scheduled(cron = "0 */5 * * * ?") // ogni 5 minuti
    public void syncTrendingNowFromExpiredBookStats() {
        System.out.println("🔄 [BookStatSync] Starting scheduled sync of TrendingNow from expired BookStat cache entries");

        String pattern = RedisService.BOOK_STAT_CACHE_PREFIX + "*" + RedisService.BOOK_STAT_N_ADDED_SUFFIX;
        Set<String> keys = redisService.getKeysPattern(pattern);

        if (keys == null || keys.isEmpty()) {
            System.out.println("ℹ️ [BookStatSync] No book n_added keys found in Redis for trending check");
            return;
        }

        final int SUCCESS_THRESHOLD = 1;
        int addedCount = 0;

        for (String fullKey : keys) {
            try {
                String bookId = fullKey.substring(
                        RedisService.BOOK_STAT_CACHE_PREFIX.length(),
                        fullKey.length() - RedisService.BOOK_STAT_N_ADDED_SUFFIX.length()
                );

                String bookStatCacheKey = RedisService.BOOK_STAT_CACHE_PREFIX + bookId;

                // Salta libri già decisi in precedenza
                if (redisService.isDirtyBookStat(bookId)) {
                    continue;
                }

                boolean windowActive = redisService.exists(bookStatCacheKey);

                if (windowActive) {
                    // Finestra ancora aperta: controlla se ha superato la soglia
                    Integer nAdded = redisService.getBookNAdded(bookId);
                    if (nAdded != null && nAdded > SUCCESS_THRESHOLD) {
                        Book book = bookRepository.findByBookId(bookId).orElse(null);
                        if (book != null) {
                            redisService.addBookToTrendingNow(book);
                            redisService.addToDirtyBookStats(bookId);
                            addedCount++;
                        }
                    }
                } else {
                    // Finestra scaduta senza aver superato la soglia: marca come processato
                    redisService.addToDirtyBookStats(bookId);
                }
            } catch (Exception e) {
                System.err.println("❌ [BookStatSync] Error updating trending: " + e.getMessage());
            }
        }

        System.out.println("✅ [BookStatSync] TrendingNow sync done. Entries added: " + addedCount);
    }

    /**
     * Sincronizzazione manuale per forzare la sincronizzazione
     */
    public void manualSync() {
        System.out.println("🔄 [BookStatSync] Manual sync triggered");
        syncBookStatsToMongoDB();
    }

    /**
     * Sincronizza una singola statistica libro
     */
    public void syncSingleBookStat(String bookId) {
        try {
            Integer redisNAdded = redisService.getBookNAdded(bookId);
            if (redisNAdded == null || redisNAdded == 0) {
                System.out.println("ℹ️ [BookStatSync] No n_added cache found for book: " + bookId);
                return;
            }

            Book book = bookRepository.findByBookId(bookId).orElse(null);

            if (book != null) {
                Integer currentNAdded = (book.getNAdded() == null) ? 0 : book.getNAdded();
                if (!currentNAdded.equals(redisNAdded)) {
                    book.setNAdded(redisNAdded);
                    bookRepository.save(book);
                    System.out.println("✅ [BookStatSync] Synced book " + bookId + " n_added: " + currentNAdded + " -> " + redisNAdded);
                } else {
                    System.out.println("ℹ️ [BookStatSync] Book " + bookId + " n_added already in sync");
                }
            } else {
                System.err.println("❌ [BookStatSync] Book not found in MongoDB: " + bookId);
            }

        } catch (Exception e) {
            System.err.println("❌ [BookStatSync] Error syncing book stat " + bookId + ": " + e.getMessage());
        }
    }
}