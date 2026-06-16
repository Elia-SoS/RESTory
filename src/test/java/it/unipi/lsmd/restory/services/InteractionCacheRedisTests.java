package it.unipi.lsmd.restory.services;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InteractionCacheRedisTests {

    @Test
    void createInteractionCacheStoresEachFieldAsSimpleKey() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        RedisService redisService = new RedisService();
        ReflectionTestUtils.setField(redisService, "redisTemplate", redisTemplate);

        LocalDate date = LocalDate.of(2025, 2, 2);
        redisService.createInteractionCache("interaction-123", date, date, date);

        verify(valueOperations).set("interaction:interaction-123:date_added", "2025-02-02");
        verify(valueOperations).set("interaction:interaction-123:started_at", "2025-02-02");
        verify(valueOperations).set("interaction:interaction-123:read_at", "2025-02-02");
        verify(valueOperations, never()).set(eq("interaction:interaction-123"), any());
    }

    @Test
    void syncInteractionsToMongoDbTreatsFieldKeysAsOneInteraction() {
        RedisService redisService = mock(RedisService.class);
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);

        InteractionSyncService syncService = new InteractionSyncService();
        ReflectionTestUtils.setField(syncService, "redisService", redisService);
        ReflectionTestUtils.setField(syncService, "mongoTemplate", mongoTemplate);

        Set<String> keys = Set.of(
                "interaction:interaction-123:date_added",
                "interaction:interaction-123:started_at",
                "interaction:interaction-123:read_at"
        );

        Map<String, Object> interactionData = new HashMap<>();
        interactionData.put("started_at", "2025-02-01");
        interactionData.put("read_at", "2025-02-02");

        when(redisService.getKeysPattern("interaction:*")).thenReturn(keys);
        when(redisService.getInteractionCache("interaction-123")).thenReturn(interactionData);

        syncService.syncInteractionsToMongoDB();

        verify(redisService, times(1)).getInteractionCache("interaction-123");
        verify(mongoTemplate, times(1)).updateFirst(any(), any(), eq("interactions"));
    }
}
