package it.unipi.lsmd.restory.services;

import it.unipi.lsmd.restory.models.Book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    public static final String NAMESPACE = "";
    public static final String BOOK_STAT_CACHE_PREFIX = "book_stat:";
    public static final String BOOK_STAT_N_ADDED_SUFFIX = ":n_added";
    private static final String DIRTY_INTERACTIONS_KEY = "dirty_interaction";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(NAMESPACE + key, value);
    }

    public void setWithExpiry(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(NAMESPACE + key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(NAMESPACE + key);
    }

    public boolean exists(String key) {
        return redisTemplate.hasKey(NAMESPACE + key);
    }

    public void delete(String key) {
        redisTemplate.delete(NAMESPACE + key);
    }

    public void pushToList(String key, Object value) {
        redisTemplate.opsForList().rightPush(NAMESPACE + key, value);
    }

    public List<Object> getList(String key) {
        return redisTemplate.opsForList().range(NAMESPACE + key, 0, -1);
    }

    public void pushToListLeft(String key, Object value) {
        redisTemplate.opsForList().leftPush(NAMESPACE + key, value);
    }

    public Object popFromList(String key) {
        return redisTemplate.opsForList().leftPop(NAMESPACE + key);
    }

    public Long getListSize(String key) {
        return redisTemplate.opsForList().size(NAMESPACE + key);
    }

    public void setHashField(String key, String field, Object value) {
        redisTemplate.opsForHash().put(NAMESPACE + key, field, value);
    }

    public Object getHashField(String key, String field) {
        return redisTemplate.opsForHash().get(NAMESPACE + key, field);
    }

    public void deleteHashField(String key, String field) {
        redisTemplate.opsForHash().delete(NAMESPACE + key, field);
    }

    public void increment(String key, long delta) {
        redisTemplate.opsForValue().increment(NAMESPACE + key, delta);
    }

    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(NAMESPACE + key, unit);
    }

    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(NAMESPACE + key, timeout, unit);
    }

    // Like management methods
    public void likeReview(String username, String reviewId) {
        String key = "like:" + username + ":" + reviewId;
        set(key, true);
        addToDirtyList(reviewId);
    }

    public void unlikeReview(String username, String reviewId) {
        String key = "like:" + username + ":" + reviewId;
        delete(key);
        addToDirtyList(reviewId);
    }

    public boolean hasLikedReview(String username, String reviewId) {
        String key = "like:" + username + ":" + reviewId;
        return exists(key);
    }

    public long getLikesCount(String reviewId) {
        String pattern = NAMESPACE + "like:*:" + reviewId;
        // Nota: in produzione sarebbe meglio usare SCAN invece di KEYS
        // per performance, ma per semplicità usiamo KEYS
        var keys = redisTemplate.keys(pattern);
        return (keys == null) ? 0 : keys.size();
    }

    // Dirty list management methods
    public void addToDirtyList(String reviewId) {
        String key = "dirty_reviews";
        redisTemplate.opsForSet().add(NAMESPACE + key, reviewId);
    }

    public void removeFromDirtyList(String reviewId) {
        String key = "dirty_reviews";
        redisTemplate.opsForSet().remove(NAMESPACE + key, reviewId);
    }

    public List<Object> getDirtyList() {
        String key = "dirty_reviews";
        var members = redisTemplate.opsForSet().members(NAMESPACE + key);
        return (members == null) ? new java.util.ArrayList<>() : new java.util.ArrayList<>(members);
    }

    public void clearDirtyList() {
        String key = "dirty_reviews";
        redisTemplate.delete(NAMESPACE + key);
    }

    public long getDirtyListSize() {
        String key = "dirty_reviews";
        return redisTemplate.opsForSet().size(NAMESPACE + key);
    }

    // Interaction cache methods
    public void createInteractionCache(String interactionId, LocalDate dateAdded, LocalDate startedAt, LocalDate readAt) {
        String key = "interaction:" + interactionId;
        Map<String, Object> interactionData = new HashMap<>();
        interactionData.put("date_added", dateAdded);
        if (startedAt != null) {
            interactionData.put("started_at", startedAt);
        }
        if (readAt != null) {
            interactionData.put("read_at", readAt);
        }
        set(key, interactionData);
    }

    public void updateInteractionStartedAt(String interactionId, LocalDate startedAt) {
        String key = "interaction:" + interactionId;
        Map<String, Object> interactionData = (Map<String, Object>) get(key);
        if (interactionData != null) {
            interactionData.put("started_at", startedAt);
            set(key, interactionData);
        }
    }

    public void updateInteractionReadAt(String interactionId, LocalDate readAt) {
        String key = "interaction:" + interactionId;
        Map<String, Object> interactionData = (Map<String, Object>) get(key);
        if (interactionData != null) {
            interactionData.put("read_at", readAt);
            set(key, interactionData);
        }
    }

    public Map<String, Object> getInteractionCache(String interactionId) {
        String key = "interaction:" + interactionId;
        return (Map<String, Object>) get(key);
    }

    public void deleteInteractionCache(String interactionId) {
        String key = "interaction:" + interactionId;
        delete(key);
    }

    public void addToDirtyInteractions(String interactionId) {
        redisTemplate.opsForSet().add(NAMESPACE + DIRTY_INTERACTIONS_KEY, interactionId);
    }

    public void removeFromDirtyInteractions(String interactionId) {
        redisTemplate.opsForSet().remove(NAMESPACE + DIRTY_INTERACTIONS_KEY, interactionId);
    }

    public java.util.Set<String> getDirtyInteractions() {
        var members = redisTemplate.opsForSet().members(NAMESPACE + DIRTY_INTERACTIONS_KEY);
        if (members == null || members.isEmpty()) {
            return new java.util.HashSet<>();
        }

        java.util.Set<String> dirtyInteractions = new java.util.HashSet<>();
        for (Object member : members) {
            if (member != null) {
                dirtyInteractions.add(member.toString());
            }
        }
        return dirtyInteractions;
    }

    public void clearDirtyInteractions() {
        redisTemplate.delete(NAMESPACE + DIRTY_INTERACTIONS_KEY);
    }

    // Utility method to generate interaction_id
    public static String generateInteractionId(String userId, String bookId) {
        return userId + "_" + bookId;
    }

    // Utility method to get keys matching a pattern
    public java.util.Set<java.lang.String> getKeysPattern(String pattern) {
        var keys = redisTemplate.keys(pattern);
        return (keys == null) ? new java.util.HashSet<>() : keys;
    }

    // BookStat cache methods
    public void createBookStat(String bookId, Map<String, Object> bookData) {
        // Create cache with TTL for temporary fields (exclude n_added)
        String cacheKey = BOOK_STAT_CACHE_PREFIX + bookId;
        Map<String, Object> cacheData = new HashMap<>(bookData);
        cacheData.remove("n_added"); // n_added is stored separately
        if (!cacheData.isEmpty()) {
            set(cacheKey, cacheData);
            // Set TTL for temporary fields (24-25 hours)
            expire(cacheKey, 24, TimeUnit.HOURS);
        }

        // n_added is stored permanently in a separate key
        if (bookData.containsKey("n_added")) {
            String nAddedKey = getBookStatNAddedKey(bookId);
            set(nAddedKey, bookData.get("n_added"));
            // No TTL for n_added - it persists permanently
        }
    }

    public void incrementBookNAdded(String bookId) {
        String nAddedKey = getBookStatNAddedKey(bookId);
        // Use Redis INCR for atomic increment
        Long newValue = redisTemplate.opsForValue().increment(NAMESPACE + nAddedKey, 1);
        System.out.println("📊 [RedisService] Incremented n_added for book " + bookId + " to " + newValue);
    }

    public Integer getBookNAdded(String bookId) {
        String nAddedKey = getBookStatNAddedKey(bookId);
        Object value = get(nAddedKey);
        return (value instanceof Number) ? ((Number) value).intValue() : 0;
    }

    public Map<String, Object> getBookStat(String bookId) {
        String cacheKey = BOOK_STAT_CACHE_PREFIX + bookId;
        String nAddedKey = getBookStatNAddedKey(bookId);

        Map<String, Object> result = new HashMap<>();

        // Get temporary fields (may have expired)
        Map<String, Object> cacheData = (Map<String, Object>) get(cacheKey);
        if (cacheData != null) {
            result.putAll(cacheData);
        }

        // Always get n_added (permanent)
        Integer nAdded = getBookNAdded(bookId);
        if (nAdded > 0) {
            result.put("n_added", nAdded);
        }

        return result.isEmpty() ? null : result;
    }

    public void updateBookStatField(String bookId, String field, Object value) {
        if ("n_added".equals(field)) {
            // n_added is handled separately
            String nAddedKey = getBookStatNAddedKey(bookId);
            set(nAddedKey, value);
        } else {
            // Other fields go to the cache with TTL
            String cacheKey = BOOK_STAT_CACHE_PREFIX + bookId;
            Map<String, Object> cacheData = (Map<String, Object>) get(cacheKey);
            if (cacheData == null) {
                cacheData = new HashMap<>();
            }
            cacheData.put(field, value);
            set(cacheKey, cacheData);
            expire(cacheKey, 24, TimeUnit.HOURS);
        }
    }

    private String getBookStatNAddedKey(String bookId) {
        return BOOK_STAT_CACHE_PREFIX + bookId + BOOK_STAT_N_ADDED_SUFFIX;
    }

    public static final String TRENDING_NOW_KEY = "trending_now";
    private static final String DIRTY_BOOKS_KEY = "dirty_book";
    private static final String DIRTY_BOOKSTATS_KEY = "dirty_bookstats";

    public void addToDirtyBooks(String bookId) {
        redisTemplate.opsForSet().add(NAMESPACE + DIRTY_BOOKS_KEY, bookId);
    }

    public java.util.Set<String> getDirtyBooks() {
        var members = redisTemplate.opsForSet().members(NAMESPACE + DIRTY_BOOKS_KEY);
        if (members == null || members.isEmpty()) {
            return new java.util.HashSet<>();
        }

        java.util.Set<String> dirtyBooks = new java.util.HashSet<>();
        for (Object member : members) {
            if (member != null) {
                dirtyBooks.add(member.toString());
            }
        }
        return dirtyBooks;
    }

    public void clearDirtyBooks() {
        redisTemplate.delete(NAMESPACE + DIRTY_BOOKS_KEY);
    }

    public void addToDirtyBookStats(String bookId) {
        redisTemplate.opsForSet().add(NAMESPACE + DIRTY_BOOKSTATS_KEY, bookId);
    }

    public boolean isDirtyBookStat(String bookId) {
        Boolean member = redisTemplate.opsForSet().isMember(NAMESPACE + DIRTY_BOOKSTATS_KEY, bookId);
        return Boolean.TRUE.equals(member);
    }

    public void removeFromDirtyBookStats(String bookId) {
        redisTemplate.opsForSet().remove(NAMESPACE + DIRTY_BOOKSTATS_KEY, bookId);
    }

    public void clearDirtyBookStats() {
        redisTemplate.delete(NAMESPACE + DIRTY_BOOKSTATS_KEY);
    }

    public void deleteBookStat(String bookId) {
        String cacheKey = BOOK_STAT_CACHE_PREFIX + bookId;
        String nAddedKey = getBookStatNAddedKey(bookId);
        delete(cacheKey);
        delete(nAddedKey);
    }

    public void trimList(String key, long start, long end) {
        redisTemplate.opsForList().trim(NAMESPACE + key, start, end);
    }

    public void addBookToTrendingNow(Book book) {
        if (book == null || book.getTitle() == null || book.getTitle().isBlank()) {
            return;
        }
        String title = book.getTitle();
        String author = "";
        if (book.getAuthors() != null && !book.getAuthors().isEmpty()) {
            Object firstAuthor = book.getAuthors().get(0).get("name");
            if (firstAuthor == null) {
                firstAuthor = book.getAuthors().get(0).get("author");
            }
            author = (firstAuthor != null) ? firstAuthor.toString() : "";
        }
        String imageUrl = (book.getImageUrl() != null) ? book.getImageUrl() : "";
        Map<String, Object> trendingEntry = new HashMap<>();
        trendingEntry.put("title", title);
        trendingEntry.put("author", author);
        trendingEntry.put("image_url", imageUrl);

        // Push entry as most recent
        pushToListLeft(TRENDING_NOW_KEY, trendingEntry);

        // Mantieni solo i 5 elementi più recenti
        trimList(TRENDING_NOW_KEY, 0, 4);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTrendingNow() {
        List<Object> raw = getList(TRENDING_NOW_KEY);
        if (raw == null || raw.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Object obj : raw) {
            if (obj instanceof Map) {
                result.add((Map<String, Object>) obj);
            }
        }
        return result;
    }
}