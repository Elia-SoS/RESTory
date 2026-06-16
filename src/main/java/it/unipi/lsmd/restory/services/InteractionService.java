package it.unipi.lsmd.restory.services;

import it.unipi.lsmd.restory.models.Interaction;
import it.unipi.lsmd.restory.models.User;
import it.unipi.lsmd.restory.models.Book;
import it.unipi.lsmd.restory.models.Neo4jUser;
import it.unipi.lsmd.restory.models.BookNeo4j;
import it.unipi.lsmd.restory.repositories.InteractionRepository;
import it.unipi.lsmd.restory.repositories.UserRepository;
import it.unipi.lsmd.restory.repositories.BookRepository;
import org.bson.BsonType;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.time.LocalDate;
import java.util.*;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.AggregateIterable;
import it.unipi.lsmd.restory.models.MostAbandonedBookStats;
import it.unipi.lsmd.restory.models.ReadingSeasonalityStats;
import it.unipi.lsmd.restory.models.GenreDropStats;
import java.util.ArrayList;
import java.util.Arrays;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.AggregateIterable;
import it.unipi.lsmd.restory.models.ReadingSpeedStats;

@Service
public class InteractionService {

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RedisService redisService;

    @Autowired(required = false)
    private Driver neo4jDriver;

    /**
     * Aggiunge una nuova interazione al sistema
     *
     * @param interaction l'interazione da aggiungere
     * @return l'interazione aggiunta con il suo ID generato
     */
    public Interaction addInteraction(Interaction interaction) {
        System.out.println("🔔 [Service] addInteraction called");

        if(interaction.getUserId() == null || interaction.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("UserId is required for an interaction");
        }
        if(interaction.getBook() == null || interaction.getBook().get("book_id") == null || interaction.getBook().get("book_id").toString().trim().isEmpty()) {
            throw new IllegalArgumentException("Book with valid book_id is required for an interaction");
        }
        String interactionId = RedisService.generateInteractionId(interaction.getUserId(), interaction.getBook().get("book_id").toString());
        if (interactionRepository.existsById(interactionId)) {
            throw new IllegalArgumentException("Interaction already exists with interaction_id=" + interactionId);
        }
        interaction.setId(interactionId); // Settiamo l'ID generato prima di salvare
        
        // Ensure dateAdded set
        if (interaction.getDateAdded() == null) {
            interaction.setDateAdded(java.time.LocalDate.now());
        }
        //ma non devevo rimuovere il book_id?
        Interaction saved = interactionRepository.save(interaction);

        // Update user: push summary into recently_added, move overflow to other_added
        try {
            if (saved.getUsername() != null) {
                java.util.Optional<User> uopt = userRepository.findByUsername(saved.getUsername()); 
                if (uopt.isPresent()) {
                    User user = uopt.get();

                    java.util.List<java.util.Map<String, Object>> recent = new java.util.ArrayList<>();
                    if (user.getRecentlyAdded() != null) recent.addAll(user.getRecentlyAdded());

                    java.util.Map<String, Object> summary = new java.util.HashMap<>();
                    summary.put("interaction_id", saved.getId());
                    summary.put("date_added", saved.getDateAdded());
                    summary.put("book", saved.getBook());

                    // insert most recent at front
                    recent.add(0, summary);

                    // if more than 10, remove last and move its id to other_added
                    if (recent.size() > 10) {
                        java.util.Map<String, Object> removed = recent.remove(recent.size() - 1);
                        if (removed != null && removed.get("interaction_id") != null) {
                            String removedId = removed.get("interaction_id").toString();
                            java.util.List<String> other = new java.util.ArrayList<>();
                            if (user.getOtherAdded() != null) other.addAll(user.getOtherAdded());
                            other.add(removedId);
                            user.setOtherAdded(other);
                        }
                    }

                    user.setRecentlyAdded(recent);
                    Integer currentBooks = user.getBooks() != null ? user.getBooks() : 0;
                    user.setBooks(currentBooks + 1);
                    userRepository.save(user);
                } else {
                    System.out.println("⚠️ [Service] addInteraction - user not found: " + saved.getUsername());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [Service] addInteraction - failed to update user recentlyAdded: " + e.getMessage());
        }
        System.out.println("qui!!!!");
        // Update book: increment n_added in Redis cache
        System.out.println(saved.toString());
        try {
            System.out.println("quaaa!!!!");
            if (saved.getBook() != null && saved.getBook().get("book_id") != null) { // elimino? se lo faccio su redis non dovei farlo qui
                String bookId = saved.getBook().get("book_id").toString();
                System.out.println("📊 [Service] addInteraction - incrementing n_added for book: " + bookId);

                // Increment n_added in Redis BookStat cache
                redisService.incrementBookNAdded(bookId);

                // Create BookStat cache if it doesn't exist
                Map<String, Object> existingStat = redisService.getBookStat(bookId);
                if (existingStat == null) {
                    Map<String, Object> bookStat = new java.util.HashMap<>();
                    bookStat.put("book_id", bookId);
                    bookStat.put("title", saved.getBook().get("title"));
                    bookStat.put("authors", saved.getBook().get("authors"));
                    bookStat.put("n_added", 1); // Will be incremented by the method above
                    redisService.createBookStat(bookId, bookStat);
                }

                // Track modified books to sync only changed entries to MongoDB
                redisService.addToDirtyBooks(bookId);

                System.out.println("✅ [Service] addInteraction - updated Redis BookStat cache for book: " + bookId);
            }
        } catch (Exception e) {
            System.err.println("❌ [Service] addInteraction - failed to update Redis BookStat cache: " + e.getMessage());
        }

        // Create POSSESSES relationship in Neo4j
        try {
            if (neo4jDriver != null && saved.getUsername() != null && saved.getBook() != null && saved.getBook().get("book_id") != null) {
                String username = saved.getUsername();
                String bookId = saved.getBook().get("book_id").toString();
                String title = saved.getBook().get("title").toString();
                LocalDate dateAdded = saved.getDateAdded() != null ? saved.getDateAdded() : LocalDate.now();

                try (Session session = neo4jDriver.session()) {
                    // Cypher query to create POSSESSES relationship
                    String query = """
                        MATCH (u:RegisteredUser {username: $username})
                        MATCH (b:Book {title: $title})
                        MERGE (u)-[r:POSSESSES {date_added: $date_added}]->(b)
                        """;

                    session.executeWrite(tx -> {
                        var result = tx.run(query,
                            Values.parameters(
                                "username", username,
                                "title", title,
                                "date_added", dateAdded
                            )
                        );
                        result.consume();
                        return null;
                    });

                    System.out.println("✅ [Service] addInteraction - created POSSESSES relationship for user=" + username + ", book_id=" + bookId);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [Service] addInteraction - failed to create POSSESSES relationship in Neo4j: " + e.getMessage());
        }

        // Create Redis cache for the interaction
        try {
            if (saved.getUsername() != null && saved.getBook() != null && saved.getBook().get("book_id") != null) {

                String bookId = saved.getBook().get("book_id").toString();


                redisService.createInteractionCache(
                    interactionId,
                    saved.getDateAdded(),
                    saved.getStartedAt(),
                    saved.getReadAt()
                );

                System.out.println("✅ [Service] addInteraction - created Redis cache for interaction: " + interactionId);
            }
        } catch (Exception e) {
            System.err.println("❌ [Service] addInteraction - failed to create Redis cache: " + e.getMessage());
        }

        return saved;
    }

    /**
     * Recupera tutte le interazioni
     *
     * @return lista di tutte le interazioni
     */
    public List<Interaction> getAllInteractions() {
        System.out.println("🔔 [Service] getAllInteractions called");
        return interactionRepository.findAll();
    }

    /**
     * Recupera un'interazione per ID
     *
     * @param id l'ID dell'interazione
     * @return l'interazione se presente, altrimenti Optional vuoto
     */
    public Optional<Interaction> getInteractionById(String id) {
        System.out.println("🔔 [Service] getInteractionById called for id=" + id);
        return interactionRepository.findById(id);
    }

    /**
     * Recupera tutte le interazioni di un utente
     *
     * @param userId l'ID dell'utente
     * @return lista di interazioni dell'utente
     */
    public List<Interaction> getInteractionsByUsername(String username) {
        System.out.println("🔔 [Service] getInteractionsByUsername called for username=" + username);
        return interactionRepository.findByUsername(username);
    }

    /**
     * Recupera tutte le interazioni per un libro
     *
     * @param title del libro
     * @return lista di interazioni per il libro
     */
    public List<Interaction> getInteractionsByBookTitle(String title) {
        System.out.println("🔔 [Service] getInteractionsByBookTitle called for title=" + title);
        return interactionRepository.findByBookTitle(title);
    }

    /**
     * Recupera le interazioni di un utente per un libro specifico
     *
     * @param username il nome dell'utente
     * @param title    il titolo del libro
     * @return lista di interazioni
     */
    public List<Interaction> getInteractionsByUsernameAndBookTitle(String username, String title) {
        System.out.println("🔔 [Service] getInteractionsByUsernameAndBookTitle called for username=" + username + ", title=" + title);
        return interactionRepository.findByUsernameAndBookTitle(username, title);
    }

    /**
     * Recupera le interazioni lette da un utente
     *
     * @param username il nome dell'utente
     * @return lista di interazioni lette
     */
    public List<Interaction> getReadInteractionsByUsername(String username) {
        System.out.println("🔔 [Service] getReadInteractionsByUsername called for username=" + username);
        return interactionRepository.findReadInteractionsByUsername(username);
    }

    /**
     * Recupera le interazioni non lette da un utente
     *
     * @param username il nome dell'utente
     * @return lista di interazioni non lette
     */
    public List<Interaction> getUnreadInteractionsByUsername(String username) {
        System.out.println("🔔 [Service] getUnreadInteractionsByUsername called for username=" + username);
        return interactionRepository.findUnreadInteractionsByUsername(username);
    }

    /**
     * Recupera le interazioni aggiunte in un intervallo di date
     *
     * @param startDate data inizio
     * @param endDate   data fine
     * @return lista di interazioni nell'intervallo
     */
    public List<Interaction> getInteractionsBetweenDates(LocalDate startDate, LocalDate endDate) {
        System.out.println("🔔 [Service] getInteractionsBetweenDates called for startDate=" + startDate + ", endDate=" + endDate);
        return interactionRepository.findInteractionsBetweenDates(startDate, endDate);
    }

    /**
     * Aggiorna un'interazione esistente
     *
     * @param id          l'ID dell'interazione
     * @param interaction i dati aggiornati
     * @return l'interazione aggiornata
     */
    public Interaction updateInteraction(String id, Interaction interaction) {
    Map<String, Object> interactionData = redisService.getInteractionCache(id);

    if (interactionData == null) {
        throw new IllegalArgumentException("Interaction non trovata in Redis");
    }

    if (interaction.getDateAdded() != null) {
        interactionData.put("date_added", interaction.getDateAdded());
    }
    if (interaction.getDateUpdated() != null) {
        interactionData.put("date_updated", interaction.getDateUpdated());
    } else {
        interactionData.put("date_updated", LocalDate.now());
    }
    if (interaction.getStartedAt() != null) {
        interactionData.put("started_at", interaction.getStartedAt());
    }
    if (interaction.getReadAt() != null) {
        interactionData.put("read_at", interaction.getReadAt());

    }


    redisService.set("interaction:" + id, interactionData);
    redisService.addToDirtyInteractions(id);
    return interaction;
}

    /**
     * Elimina un'interazione per ID
     *
     * @param id l'ID dell'interazione
     */
    public void deleteInteraction(String id) {
        System.out.println("🔔 [Service] deleteInteraction called for id=" + id);
        Optional<Interaction> existing = interactionRepository.findById(id);
        interactionRepository.deleteById(id);

        redisService.deleteInteractionCache(id);
        redisService.removeFromDirtyInteractions(id);

        if (neo4jDriver != null && existing.isPresent()) {
            Interaction interaction = existing.get();
            String username = interaction.getUsername();
            String bookTitle = interaction.getBook() != null && interaction.getBook().get("title") != null
                ? interaction.getBook().get("title").toString()
                : null;

            if (username != null && bookTitle != null) {
                try (Session session = neo4jDriver.session()) {
                    String query = """
                        MATCH (u:RegisteredUser {username: $username})-[r:POSSESSES]->(b:Book {title: $title})
                        DELETE r
                        """;

                    session.executeWrite(tx -> {
                        tx.run(query, Values.parameters("username", username, "title", bookTitle)).consume();
                        return null;
                    });
                } catch (Exception e) {
                    System.err.println("❌ [Service] deleteInteraction - failed to remove Neo4j relationship: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Elimina tutte le interazioni di un utente
     *
     * @param username il nome dell'utente
     */
    public void deleteInteractionsByUsername(String username) {
        System.out.println("🔔 [Service] deleteInteractionsByUsername called for username=" + username);
        List<Interaction> interactions = interactionRepository.findByUsername(username);
        interactionRepository.deleteAll(interactions);
    }

    /**
     * Conta il numero di interazioni
     *
     * @return numero totale di interazioni
     */
    public long countInteractions() {
        System.out.println("🔔 [Service] countInteractions called");
        return interactionRepository.count();
    }

        /**
         * Restituisce la Top N dei libri più abbandonati.
         * @param minReaders soglia minima di utenti che hanno iniziato il libro (es. 20)
         * @param limit numero massimo di risultati (es. 10)
         * @return lista di statistiche MostAbandonedBookStats
         */
        public List<MostAbandonedBookStats> getMostAbandonedBooks(int minReaders, int limit) {
        System.out.println("🔔 [Service] getMostAbandonedBooks called with minReaders=" + minReaders + ", limit=" + limit);

        MongoCollection<Document> coll = mongoTemplate.getCollection("Interactions");

        Document matchStarted = new Document("$match", new Document("started_at", new Document("$exists", true).append("$type", "date")));

        Document dateDiffNow = new Document("$dateDiff", new Document("startDate", "$started_at").append("endDate", "$$NOW").append("unit", "month"));

        Document dateDiffIfRead = new Document("$dateDiff", new Document("startDate", "$started_at").append("endDate", new Document("$ifNull", Arrays.asList("$read_at", "$$NOW")).append("unit", "month")));

        // Build $addFields -> is_dropped: cond(if: or(condA, condB), then:1, else:0)
        Document condA = new Document("$and", Arrays.asList(
            new Document("$eq", Arrays.asList("$is_read", false)),
            new Document("$gt", Arrays.asList(dateDiffNow, 6))
        ));

        Document condB = new Document("$and", Arrays.asList(
            new Document("$eq", Arrays.asList("$is_read", true)),
            new Document("$gt", Arrays.asList(new Document("$dateDiff", new Document("startDate", "$started_at").append("endDate", new Document("$ifNull", Arrays.asList("$read_at", "$$NOW"))).append("unit", "month")), 6))
        ));

        Document addFields = new Document("$addFields", new Document("is_dropped",
            new Document("$cond", new Document("if", new Document("$or", Arrays.asList(condA, condB))).append("then", 1).append("else", 0))
        ));

        Document group = new Document("$group", new Document("_id", "$book.title")
            .append("utenti_che_lo_iniziano", new Document("$sum", 1))
            .append("utenti_che_lo_droppano", new Document("$sum", "$is_dropped"))
        );

        Document matchMin = new Document("$match", new Document("utenti_che_lo_iniziano", new Document("$gte", minReaders)));

        Document project = new Document("$project", new Document("_id", 0)
            .append("title", "$_id")
            .append("utenti_che_lo_iniziano", 1)
            .append("utenti_che_lo_droppano", 1)
            .append("percentuale_drop", new Document("$multiply", Arrays.asList(
                new Document("$divide", Arrays.asList("$utenti_che_lo_droppano", "$utenti_che_lo_iniziano")), 100
            )))
        );

        Document sort = new Document("$sort", new Document("percentuale_drop", -1));

        Document limitDoc = new Document("$limit", limit);

        List<Document> pipeline = Arrays.asList(matchStarted, addFields, group, matchMin, project, sort, limitDoc);

        AggregateIterable<Document> agg = coll.aggregate(pipeline);

        List<MostAbandonedBookStats> results = new ArrayList<>();
        for (Document d : agg) {
            String title = d.getString("title");
            Integer started = null;
            Object s = d.get("utenti_che_lo_iniziano");
            if (s instanceof Number) started = ((Number) s).intValue();
            Integer dropped = null;
            Object dr = d.get("utenti_che_lo_droppano");
            if (dr instanceof Number) dropped = ((Number) dr).intValue();
            Double perc = null;
            Object p = d.get("percentuale_drop");
            if (p instanceof Number) perc = ((Number) p).doubleValue();

            results.add(new MostAbandonedBookStats(title, started, dropped, perc));
        }

        return results;
        }

    /**
     * Restituisce la seasonality delle letture: conteggio di inizi per mese (1-12) e per genere.
     * @return lista di ReadingSeasonalityStats ordinata per mese asc e started_count desc
     */
    public List<ReadingSeasonalityStats> getReadingSeasonality() {
        System.out.println("🔔 [Service] getReadingSeasonality called");

        MongoCollection<Document> coll = mongoTemplate.getCollection("Interactions");

        Document matchStarted = new Document("$match", new Document("started_at", new Document("$exists", true).append("$type", "date")));

        Document group = new Document("$group", new Document("_id", new Document("month", new Document("$month", "$started_at")).append("genre", "$book.genre"))
                .append("started_count", new Document("$sum", 1)));

        Document project = new Document("$project", new Document("_id", 0)
                .append("month", "$_id.month")
                .append("genre", "$_id.genre")
                .append("started_count", "$started_count")
        );

        Document sort = new Document("$sort", new Document("month", 1).append("started_count", -1));

        List<Document> pipeline = Arrays.asList(matchStarted, group, project, sort);

        AggregateIterable<Document> agg = coll.aggregate(pipeline);

        List<ReadingSeasonalityStats> results = new ArrayList<>();
        for (Document d : agg) {
            Integer month = null;
            Object m = d.get("month");
            if (m instanceof Number) month = ((Number) m).intValue();
            String genre = d.getString("genre");
            Integer count = null;
            Object c = d.get("started_count");
            if (c instanceof Number) count = ((Number) c).intValue();

            results.add(new ReadingSeasonalityStats(month, genre, count));
        }

        return results;
    }

        /**
         * Calcola la media delle percentuali di drop a livello di genere.
         * Per ogni libro (title+genre) calcola il drop rate = droppati/iniziali * 100,
         * poi raggruppa per genere e prende la media delle percentuali per i titoli del genere.
         * @return lista di GenreDropStats
         */
        public List<GenreDropStats> getGenreDropAverages() {
        System.out.println("🔔 [Service] getGenreDropAverages called");
        MongoCollection<Document> coll = mongoTemplate.getCollection("Interactions");

        Document matchStarted = new Document("$match", new Document("started_at", new Document("$exists", true).append("$type", "date")));

        // is_dropped logic (same as other): 1 if dropped, else 0
        Document condA = new Document("$and", Arrays.asList(
            new Document("$eq", Arrays.asList("$is_read", false)),
            new Document("$gt", Arrays.asList(new Document("$dateDiff", new Document("startDate", "$started_at").append("endDate", "$$NOW").append("unit", "month")), 6))
        ));

        Document condB = new Document("$and", Arrays.asList(
            new Document("$eq", Arrays.asList("$is_read", true)),
            new Document("$gt", Arrays.asList(new Document("$dateDiff", new Document("startDate", "$started_at").append("endDate", new Document("$ifNull", Arrays.asList("$read_at", "$$NOW"))).append("unit", "month")), 6))
        ));

        Document addFields = new Document("$addFields", new Document("is_dropped",
            new Document("$cond", new Document("if", new Document("$or", Arrays.asList(condA, condB))).append("then", 1).append("else", 0))
        ));

        // Group per book title + genre
        Document groupByBook = new Document("$group", new Document("_id", new Document("title", "$book.title").append("genre", "$book.genre"))
            .append("utenti_che_lo_iniziano", new Document("$sum", 1))
            .append("utenti_che_lo_droppano", new Document("$sum", "$is_dropped"))
        );

        // Compute percentuale per libro
        Document addFieldsBookPerc = new Document("$addFields", new Document("percentuale_drop_libro",
            new Document("$multiply", Arrays.asList(
                new Document("$divide", Arrays.asList("$utenti_che_lo_droppano", "$utenti_che_lo_iniziano")), 100
            ))
        ));
        // Group by genre and average the per-book percentages
        Document groupByGenre = new Document("$group", new Document("_id", "$_id.genre")
            .append("percentuale_genere_droppato", new Document("$avg", "$percentuale_drop_libro"))
            .append("numero_libri_valutati", new Document("$sum", 1))
        );

        Document sort = new Document("$sort", new Document("percentuale_genere_droppato", -1));

        List<Document> pipeline = Arrays.asList(matchStarted, addFields, groupByBook, addFieldsBookPerc, groupByGenre, sort);

        AggregateIterable<Document> agg = coll.aggregate(pipeline);

        List<GenreDropStats> results = new ArrayList<>();
        for (Document d : agg) {
            String genre = d.getString("_id");
            Double avg = null;
            Object a = d.get("percentuale_genere_droppato");
            if (a instanceof Number) avg = ((Number) a).doubleValue();
            Integer count = null;
            Object c = d.get("numero_libri_valutati");
            if (c instanceof Number) count = ((Number) c).intValue();
            results.add(new GenreDropStats(genre, avg, count));
        }
        return results;
        }

    /**
     * Calcola la velocità media di lettura (in giorni) per un singolo utente usando un'aggregation MongoDB.
     * Filtra le interazioni con is_read=true, username specifico e date valide, calcola la differenza in giorni
     * tra `started_at` e `read_at`, e restituisce media e conteggio.
     *
     * @param username il nome dell'utente per cui calcolare la velocità di lettura
     * @return lista di statistiche per l'utente specificato
     */
    public List<ReadingSpeedStats> getAverageReadingSpeedPerUser(String username) {
        // Otteniamo la collection nativa tramite il MongoTemplate
        MongoCollection<Document> collection = mongoTemplate.getCollection("Interactions");

        // STAGE 1: $match - filtriamo per username e interazioni completate
        Document matchStage = new Document("$match", new Document("is_read", true)
                .append("username", username)
                .append("started_at", new Document("$exists", true).append("$type", 9))
                .append("read_at", new Document("$exists", true).append("$type", 9))
        );

        // STAGE 2: $project con $dateDiff
        Document dateDiffDoc = new Document("$dateDiff", new Document("startDate", "$started_at")
                .append("endDate", "$read_at")
                .append("unit", "day")
        );

        Document projectStage = new Document("$project", new Document("username", 1)
                .append("book.title", 1)
                .append("velocita_book", dateDiffDoc)
        );

        // STAGE 3: $group - raggruppiamo per l'utente specifico
        Document groupStage = new Document("$group", new Document("_id", "$username")
                .append("velocita_media_utente", new Document("$avg", "$velocita_book"))
                .append("numero_libri_letti", new Document("$sum", 1))
        );
        List<Document> pipeline = Arrays.asList(matchStage, projectStage, groupStage);
        AggregateIterable<Document> result = collection.aggregate(pipeline);

        // MAPPATURA MANUALE DA BSON DOCUMENT A JAVA OBJECT
        List<ReadingSpeedStats> statsList = new ArrayList<>();

        for (Document doc : result) {
            String user = doc.getString("_id");

            // Estrazione sicura dei numeri (MongoDB può restituire Integer o Double a seconda dei calcoli)
            Number avgNum = doc.get("velocita_media_utente", Number.class);
            Double velocitaMedia = (avgNum != null) ? avgNum.doubleValue() : 0.0;

            Number countNum = doc.get("numero_libri_letti", Number.class);
            Integer numeroLibri = (countNum != null) ? countNum.intValue() : 0;

            // Supponendo che il costruttore di ReadingSpeedStats sia (String id, Double avg, Integer count)
            statsList.add(new ReadingSpeedStats(user, velocitaMedia, numeroLibri));

            // Aggiorna il campo average_speed dell'utente
            try {
                java.util.Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    User userEntity = userOpt.get();
                    // Arrotonda la velocità media a intero
                    Integer averageSpeedInt = (int) Math.round(velocitaMedia);
                    userEntity.setAverageSpeed(averageSpeedInt);
                    userRepository.save(userEntity);
                    System.out.println("✅ [Service] getAverageReadingSpeedPerUser - updated average_speed for user=" + username + " to " + averageSpeedInt);
                } else {
                    System.out.println("⚠️ [Service] getAverageReadingSpeedPerUser - user not found: " + username);
                }
            } catch (Exception e) {
                System.err.println("❌ [Service] getAverageReadingSpeedPerUser - failed to update user average_speed: " + e.getMessage());
            }
        }

        return statsList;
    }
}
