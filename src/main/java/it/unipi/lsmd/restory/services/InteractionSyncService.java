package it.unipi.lsmd.restory.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class InteractionSyncService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Sincronizza le interazioni dalla cache Redis a MongoDB ogni notte alle 2:00
     */
    @Scheduled(cron = "0 0 2 * * ?") // Ogni notte alle 2:00
    public void syncInteractionsToMongoDB() {
        System.out.println("🔄 [InteractionSync] Starting scheduled sync of interactions from Redis to MongoDB");

        // Sync only interactions modified by PUT update
        var dirtyInteractions = redisService.getDirtyInteractions();

        if (dirtyInteractions == null || dirtyInteractions.isEmpty()) {
            System.out.println("ℹ️ [InteractionSync] No dirty interactions found in Redis");
            return;
        }

        int syncedCount = 0;
        int errorCount = 0;

        for (String interactionId : dirtyInteractions) {
            try {
                // Recupera i dati dalla cache Redis
                Map<String, Object> interactionData = redisService.getInteractionCache(interactionId);
                if (interactionData == null) {
                    continue;
                }

                // Aggiorna MongoDB
                Query query = new Query(Criteria.where("_id").is(interactionId));
                Update update = new Update();

                if (interactionData.containsKey("started_at")) {
                    LocalDate startedAt = parseLocalDateValue(interactionData.get("started_at"));
                    if (startedAt != null) {
                        update.set("started_at", startedAt);
                    }
                }

                if (interactionData.containsKey("read_at")) {
                    LocalDate readAt = parseLocalDateValue(interactionData.get("read_at"));

                    if (readAt != null) {
                        update.set("read_at", readAt);
                        update.set("is_read", true);
                    }
                }

                update.set("date_updated", LocalDate.now());

                mongoTemplate.updateFirst(query, update, "Interactions");
                syncedCount++;

            } catch (Exception e) {
                System.err.println("❌ [InteractionSync] Error syncing interaction " + interactionId + ": " + e.getMessage());
                errorCount++;
            }
        }

        // Clear dirty set at end of sync
        redisService.clearDirtyInteractions();

        System.out.println("✅ [InteractionSync] Sync completed. Synced: " + syncedCount + ", Errors: " + errorCount);
    }

    /**
     * Endpoint manuale per forzare la sincronizzazione
     */
    public void manualSync() {
        System.out.println("🔄 [InteractionSync] Manual sync triggered");
        syncInteractionsToMongoDB();
    }

    private LocalDate parseLocalDateValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof List<?> parts && parts.size() >= 3
                && parts.get(0) instanceof Number year
                && parts.get(1) instanceof Number month
                && parts.get(2) instanceof Number day) {
            return LocalDate.of(year.intValue(), month.intValue(), day.intValue());
        }

        String raw = value.toString().trim();
        if (raw.isEmpty() || "null".equalsIgnoreCase(raw)) {
            return null;
        }

        try {
            return LocalDate.parse(raw);
        } catch (Exception ignored) {
            if (raw.startsWith("[") && raw.endsWith("]")) {
                String[] tokens = raw.substring(1, raw.length() - 1).split(",");
                if (tokens.length >= 3) {
                    return LocalDate.of(
                        Integer.parseInt(tokens[0].trim()),
                        Integer.parseInt(tokens[1].trim()),
                        Integer.parseInt(tokens[2].trim())
                    );
                }
            }
            throw new IllegalArgumentException("Unsupported LocalDate value: " + raw, ignored);
        }
    }

    /**
     * Sincronizza una singola interazione
     */
    public void syncSingleInteraction(String interactionId) {
        try {
            Map<String, Object> interactionData = redisService.getInteractionCache(interactionId);
            if (interactionData == null) {
                System.out.println("ℹ️ [InteractionSync] No cache found for interaction: " + interactionId);
                return;
            }

            Query query = new Query(Criteria.where("_id").is(interactionId));
            Update update = new Update();

            if (interactionData.containsKey("started_at")) {
                LocalDate startedAt = parseLocalDateValue(interactionData.get("started_at"));
                if (startedAt != null) {
                    update.set("started_at", startedAt);
                }
            }

            if (interactionData.containsKey("read_at")) {
                LocalDate readAt = parseLocalDateValue(interactionData.get("read_at"));
                if (readAt != null) {
                    update.set("read_at", readAt);
                    update.set("is_read", true);
                }
            }

            update.set("date_updated", LocalDate.now());

            mongoTemplate.updateFirst(query, update, "Interactions");
            System.out.println("✅ [InteractionSync] Synced interaction: " + interactionId);

        } catch (Exception e) {
            System.err.println("❌ [InteractionSync] Error syncing interaction " + interactionId + ": " + e.getMessage());
        }
    }
}