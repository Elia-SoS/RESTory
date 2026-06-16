package it.unipi.lsmd.restory.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
@EnableMongoRepositories(basePackages = "it.unipi.lsmd.restory.repositories")
public class MongoConfig {

    // Iniettiamo direttamente l'URI completo che contiene sia i 3 nodi che le impostazioni
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    /**
     * Crea esplicitamente il MongoClient configurato con l'URI del cluster (majority e nearest)
     */
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    /**
     * Crea la factory agganciata al MongoClient esplicito
     */
    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, "restory");
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new LocalDateToDateConverter());
        converters.add(new DateToLocalDateConverter());
        converters.add(new LocalDateTimeToDateConverter());
        converters.add(new DateToLocalDateTimeConverter());
        return new MongoCustomConversions(converters);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoMappingContext context, MongoCustomConversions mongoCustomConversions) {
        MappingMongoConverter converter = new MappingMongoConverter(mongoDbFactory, context);

        // Disabilita l'aggiunta del campo _class
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        // Applica le custom conversions
        converter.setCustomConversions(mongoCustomConversions);

        try {
            converter.afterPropertiesSet();
        } catch (Exception e) {
            System.err.println("❌ Error initializing converter: " + e.getMessage());
        }

        return new MongoTemplate(mongoDbFactory, converter);
    }

    // --- I tuoi Converter rimangono identici sotto ---
    @WritingConverter
    public static class LocalDateToDateConverter implements Converter<LocalDate, Date> {
        @Override
        public Date convert(LocalDate source) { return source == null ? null : java.sql.Date.valueOf(source); }
    }

    @ReadingConverter
    public static class DateToLocalDateConverter implements Converter<Date, LocalDate> {
        @Override
        public LocalDate convert(Date source) { return source == null ? null : source.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); }
    }

    @WritingConverter
    public static class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
        @Override
        public Date convert(LocalDateTime source) { return source == null ? null : java.sql.Timestamp.valueOf(source); }
    }

    @ReadingConverter
    public static class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
        @Override
        public LocalDateTime convert(Date source) { return source == null ? null : source.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(); }
    }
}