package it.unipi.lsmd.restory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

    // Iniettiamo la lista dei nodi dal file properties
    @Value("${spring.data.redis.nodes}")
    private List<String> redisNodes;

    /**
     * Configurazione della Connection Factory per gestire la topologia Master/Replica
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // Estraiamo il primo nodo come Master iniziale (6379)
        String masterNode = redisNodes.get(0);
        String[] masterParts = masterNode.split(":");

        RedisStaticMasterReplicaConfiguration config = new RedisStaticMasterReplicaConfiguration(
                masterParts[0],
                Integer.parseInt(masterParts[1])
        );

        // Aggiungiamo le altre repliche alla configurazione (6380 e 6381)
        for (int i = 1; i < redisNodes.size(); i++) {
            String[] replicaParts = redisNodes.get(i).split(":");
            config.addNode(replicaParts[0], Integer.parseInt(replicaParts[1]));
        }

        // Impostiamo la Read Preference sulle Repliche
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED) // 🎯 Legge dalle repliche, se offline va sul master
                .build();

        return new LettuceConnectionFactory(config, clientConfig);
    }

    /**
     * Il tuo RedisTemplate rimane identico, ma usa la nuova factory multi-nodo
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use Jackson serializer for values
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}