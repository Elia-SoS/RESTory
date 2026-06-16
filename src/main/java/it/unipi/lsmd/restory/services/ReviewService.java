package it.unipi.lsmd.restory.services;

import it.unipi.lsmd.restory.models.Review;
import it.unipi.lsmd.restory.models.User;
import it.unipi.lsmd.restory.models.Book;
import it.unipi.lsmd.restory.repositories.ReviewRepository;
import it.unipi.lsmd.restory.repositories.UserRepository;
import it.unipi.lsmd.restory.repositories.BookRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.AggregateIterable;
import it.unipi.lsmd.restory.models.PolarizingBookStats;
import it.unipi.lsmd.restory.models.HighEngagementReviewerStats;

import java.util.List;
import java.util.Optional;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.time.LocalDate;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired(required = false)
    private Driver neo4jDriver;

    public Review addReview(Review review) {
        System.out.println("🔔 [Service] addReview called");
        // Ensure the review _id is stored as a string: generate a hex string id when missing
        if (review.getId() == null || review.getId().trim().isEmpty()) {
            String generated = new ObjectId().toString();
            review.setId(generated);
            System.out.println("🔔 [Service] addReview generated string id=" + generated);
        }
        if (reviewRepository.existsById(review.getId())) {
            throw new IllegalArgumentException("Review already exists with reviewId=" + review.getId());
        }
        // Ensure dateAdded is set
        if (review.getDateAdded() == null) {
            review.setDateAdded(java.time.LocalDate.now());
        }
        String targetBookId = null;
        if (review.getBook() != null && review.getBook().containsKey("book_id")) {
            // Estraiamo l'ID
            targetBookId = review.getBook().get("book_id").toString();
            // Rimuoviamo l'ID dalla mappa: così NON verrà salvato in MongoDB nella collection reviews!
            review.getBook().remove("book_id");
        }
        review.setnVotes(0);



        // 2. Salvataggio della Review (Ora la mappa book non ha più il book_id)
        Review saved = reviewRepository.save(review);
        // Update user document: push summary into latestReviews, move overflow to otherReviews
        try {
            if (saved.getUsername() != null) {
                java.util.Optional<User> uopt = userRepository.findByUsername(saved.getUsername()); //cambiare
                if (uopt.isPresent()) {
                    User user = uopt.get();

                    java.util.List<java.util.Map<String, Object>> latest = new java.util.ArrayList<>();
                    if (user.getLatestReviews() != null) latest.addAll(user.getLatestReviews());

                    java.util.Map<String, Object> summary = new java.util.HashMap<>();
                    summary.put("id", saved.getId());
                    summary.put("rating", saved.getRating());
                    summary.put("date_added", saved.getDateAdded());
                    summary.put("review_text", saved.getReviewText());
                    summary.put("book", saved.getBook());

                    // Insert most recent at front
                    latest.add(0, summary);

                    // If more than 5, remove the least recent and move its id to otherReviews
                    if (latest.size() > 5) {
                        java.util.Map<String, Object> removed = latest.remove(latest.size() - 1);
                        if (removed != null && removed.get("id") != null) {
                            String removedId = removed.get("id").toString();
                            java.util.List<String> other = new java.util.ArrayList<>();
                            if (user.getOtherReviews() != null) other.addAll(user.getOtherReviews());
                            other.add(removedId);
                            user.setOtherReviews(other);
                        }
                    }

                    user.setLatestReviews(latest);
                    if (user.getReviews() == null) user.setReviews(1);
                    else user.setReviews(user.getReviews() + 1);
                    userRepository.save(user);
                } else {
                    System.out.println("⚠️ [Service] addReview - user not found: " + saved.getUsername());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [Service] addReview - failed to update user reviews: " + e.getMessage());
        }

        // Update book document: push summary into latest_reviews, move overflow to other_reviews
        try {
            if (saved.getBook() != null && saved.getBook().get("title") != null) {
                System.out.println("sono qua!");
                String bookTitle = saved.getBook().get("title").toString();
                java.util.Optional<Book> bopt = bookRepository.findByBookId(targetBookId); //cambiare -->ok
                if (bopt.isPresent()) {
                    Book book = bopt.get();

                    java.util.List<java.util.Map<String, Object>> latestB = new java.util.ArrayList<>();
                    if (book.getLatestReviews() != null) latestB.addAll(book.getLatestReviews());

                    java.util.Map<String, Object> summaryB = new java.util.HashMap<>();
                    summaryB.put("id", saved.getId()); //occhio forse è review_id -->no
                    summaryB.put("rating", saved.getRating());
                    summaryB.put("date_added", saved.getDateAdded());
                    summaryB.put("review_text", saved.getReviewText());
                    summaryB.put("username", saved.getUsername());

                    latestB.add(0, summaryB);

                    if (latestB.size() > 5) {
                        java.util.Map<String, Object> removed = latestB.remove(latestB.size() - 1);
                        if (removed != null && removed.get("id") != null) {
                            String removedId = removed.get("id").toString();
                            java.util.List<String> other = new java.util.ArrayList<>();
                            if (book.getOtherReviews() != null)
                                other.addAll(book.getOtherReviews());
                            other.add(removedId);
                            book.setOtherReviews(other);
                        }
                    }
                    book.setLatestReviews(latestB);
                    if (book.getReviewsCount() == null) book.setReviewsCount(1);
                    else book.setReviewsCount(book.getReviewsCount() + 1);

                    book.setScore(((book.getReviewsCount()-1)*book.getScore() + review.getRating())/(book.getReviewsCount()));


                    bookRepository.save(book);
                } else {
                    System.out.println("⚠️ [Service] addReview - book not found: " + bookTitle);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [Service] addReview - failed to update book reviews: " + e.getMessage());
        }

        // Create REVIEWED relationship in Neo4j
        try {
            if (neo4jDriver != null && saved.getUsername() != null && saved.getBook() != null && saved.getBook().get("title") != null) {
                final String username = saved.getUsername();
                final String bookTitle = saved.getBook().get("title").toString();
                final LocalDate dateAdded = saved.getDateAdded() != null ? saved.getDateAdded() : LocalDate.now();
                final Integer rating = saved.getRating();

                try (Session session = neo4jDriver.session()) {
                    // Cypher query to create REVIEWED relationship
                    String query = """
                            MATCH (u:RegisteredUser {username: $username})
                            MATCH (b:Book {title: $title})
                        MERGE (u)-[r:REVIEWED]->(b)
                        SET r.date_added = $date_added, r.rating = $rating
                        """;

                    var summary = session.executeWrite(tx -> {
                        var result = tx.run(query,
                                Values.parameters(
                                        "username", username,
                                        "title", bookTitle,
                                    "date_added", dateAdded,
                                        "rating", rating
                                )
                        );
                        return result.consume();
                    });

                    int created = summary.counters().relationshipsCreated();
                    if (created > 0) {
                        System.out.println("✅ [Service] addReview - created REVIEWED relationship for user=" + username + ", title=" + bookTitle);
                    } else {
                        System.out.println("⚠️ [Service] addReview - REVIEWED relationship already existed or was updated for user=" + username + ", title=" + bookTitle);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [Service] addReview - failed to create REVIEWED relationship in Neo4j: " + e.getMessage());
        }

        return saved;
    }

    public List<Review> getAllReviews() {
        System.out.println("🔔 [Service] getAllReviews called");
        return reviewRepository.findAll();
    }

    public Optional<Review> getReviewById(String id) {
        System.out.println("🔔 [Service] getReviewById called for id=" + id);
        return reviewRepository.findById(id);
    }
    public List<Review> getReviewsByUsername(String username) {
        System.out.println("🔔 [Service] getReviewsByUsername called for username=" + username);
        return reviewRepository.findByUsername(username);
    }
    public List<Review> getReviewsByUserId(String userId) {
        System.out.println("🔔 [Service] getReviewsByUserId called for userId=" + userId);
        return reviewRepository.findByUserId(userId);
    }
    /*
    public List<Review> getReviewsByBookId(String bookId) {
        System.out.println("🔔 [Service] getReviewsByBookId called for bookId=" + bookId);
        return reviewRepository.findByBookId(bookId);
    }
    */

    public List<Review> getReviewsByUserIdAndBookId(String userId, String bookId) {
        System.out.println("🔔 [Service] getReviewsByUserIdAndBookId called for userId=" + userId + ", bookId=" + bookId);
        return reviewRepository.findByUserIdAndBookId(userId, bookId);
    }

    public void incrementReviewedVotes(String usernameReview, String title) {
        updateReviewedVotes(usernameReview, title, 1);
    }

    public void decrementReviewedVotes(String usernameReview, String title) {
        updateReviewedVotes(usernameReview, title, -1);
    }

    private void updateReviewedVotes(String usernameReview, String title, int delta) {
        if (neo4jDriver == null) {
            System.out.println("⚠️ [Service] updateReviewedVotes skipped: neo4jDriver is null");
            return;
        }

        try (Session session = neo4jDriver.session()) {
            String query = """
                    MATCH (u:RegisteredUser {username: $username_review})-[r:REVIEWED]->(b:Book {title: $title})
                    SET r.n_votes = CASE
                        WHEN $delta > 0 THEN coalesce(r.n_votes, 0) + $delta
                        ELSE CASE
                            WHEN coalesce(r.n_votes, 0) + $delta < 0 THEN 0
                            ELSE coalesce(r.n_votes, 0) + $delta
                        END
                    END
                    RETURN r.n_votes AS n_votes
                    """;

            var result = session.executeWrite(tx -> {
                    var queryResult = tx.run(query,
                        Values.parameters(
                                    "username_review", usernameReview,
                            "title", title,
                            "delta", delta
                        ));
                    return queryResult.hasNext() ? queryResult.single() : null;
            });

            if (result != null && result.containsKey("n_votes")) {
                    System.out.println("✅ [Service] updateReviewedVotes updated n_votes for user=" + usernameReview + ", title=" + title + ", delta=" + delta + ", newValue=" + result.get("n_votes").asInt());
            } else {
                    System.out.println("⚠️ [Service] updateReviewedVotes found no REVIEWED relationship for user=" + usernameReview + ", title=" + title);
            }
        } catch (Exception e) {
                System.err.println("❌ [Service] updateReviewedVotes failed for user=" + usernameReview + ", title=" + title + ": " + e.getMessage());
            throw new RuntimeException("Failed to update REVIEWED n_votes", e);
        }
    }

    public Review updateReview(String id, Review review) {
        System.out.println("🔔 [Service] updateReview called for id=" + id);
        Optional<Review> existing = reviewRepository.findById(id);
        if (existing.isPresent()) {
            Review r = existing.get();
            if (review.getRating() != null) r.setRating(review.getRating());
            if (review.getReviewText() != null) r.setReviewText(review.getReviewText());
            if (review.getDateAdded() != null) r.setDateAdded(review.getDateAdded());
            if (review.getDateUpdated() != null) r.setDateUpdated(review.getDateUpdated());
            if (review.getnVotes() != null) r.setnVotes(review.getnVotes());
            if (review.getUsername() != null) r.setUsername(review.getUsername());
            if (review.getBook() != null) r.setBook(review.getBook());
            return reviewRepository.save(r);
        }
        throw new IllegalArgumentException("Review con ID '" + id + "' non trovata");
    }

    public void deleteReview(String id) {
        System.out.println("🔔 [Service] deleteReview called for id=" + id);
        reviewRepository.deleteById(id);
    }

    public void deleteReviewsByUserId(String userId) {
        System.out.println("🔔 [Service] deleteReviewsByUserId called for userId=" + userId);
        List<Review> list = reviewRepository.findByUserId(userId);
        reviewRepository.deleteAll(list);
    }

    public long countReviews() {
        System.out.println("🔔 [Service] countReviews called");
        return reviewRepository.count();
    }

    /**
     * Recupera i libri "polarizzanti" dove almeno `minPercent`% delle recensioni
     * sono 1-star e almeno `minPercent`% sono 5-star.
     * @param minPercent soglia percentuale (es. 25)
     * @return lista di PolarizingBookStats
     */
    public java.util.List<PolarizingBookStats> getPolarizingBooks(double minPercent) {
        System.out.println("🔔 [Service] getPolarizingBooks called with minPercent=" + minPercent);

        MongoCollection<Document> coll = mongoTemplate.getCollection("Reviews");

        Document group = new Document("$group", new Document("_id", "$book.title")
                .append("total_reviews", new Document("$sum", 1))
                .append("count_1_star", new Document("$sum",
                        new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$rating", 1)), 1, 0))
                ))
                .append("count_5_star", new Document("$sum",
                        new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$rating", 5)), 1, 0))
                ))
        );

        Document project = new Document("$project", new Document("_id", 0)
                .append("title", "$_id")
                .append("total_reviews", 1)
                .append("rate_1_star_perc", new Document("$multiply", Arrays.asList(
                        new Document("$divide", Arrays.asList("$count_1_star", "$total_reviews")), 100
                )))
                .append("rate_5_star_perc", new Document("$multiply", Arrays.asList(
                        new Document("$divide", Arrays.asList("$count_5_star", "$total_reviews")), 100
                )))
        );

        Document match = new Document("$match", new Document("rate_1_star_perc", new Document("$gte", minPercent))
                .append("rate_5_star_perc", new Document("$gte", minPercent)));

        Document sort = new Document("$sort", new Document("total_reviews", -1));

        java.util.List<Document> pipeline = Arrays.asList(group, project, match, sort);

        AggregateIterable<Document> agg = coll.aggregate(pipeline);

        java.util.List<PolarizingBookStats> results = new ArrayList<>();
        for (Document d : agg) {
            String title = d.getString("title");
            Integer total = null;
            Object totObj = d.get("total_reviews");
            if (totObj instanceof Number) total = ((Number) totObj).intValue();
            Double r1 = null;
            Object r1Obj = d.get("rate_1_star_perc");
            if (r1Obj instanceof Number) r1 = ((Number) r1Obj).doubleValue();
            Double r5 = null;
            Object r5Obj = d.get("rate_5_star_perc");
            if (r5Obj instanceof Number) r5 = ((Number) r5Obj).doubleValue();

            results.add(new PolarizingBookStats(title, total, r1, r5));
        }

        return results;
    }

    /**
     * Top reviewers by engagement (sum of n_votes). Optionally filter by minVotes and limit.
     * @param limit max number of results
     * @param minVotes include only reviews with n_votes > minVotes
     * @return list of HighEngagementReviewerStats
     */
    public java.util.List<HighEngagementReviewerStats> getHighEngagementReviewers(int limit, int minVotes) {
        System.out.println("🔔 [Service] getHighEngagementReviewers called with limit=" + limit + ", minVotes=" + minVotes);

        MongoCollection<Document> coll = mongoTemplate.getCollection("Reviews");

        Document match = new Document("$match", new Document("n_votes", new Document("$gt", minVotes)));

        Document group = new Document("$group", new Document("_id", "$username")
                .append("total_engagement", new Document("$sum", "$n_votes"))
                .append("numero_di_recensioni", new Document("$sum", 1)));

        Document sort = new Document("$sort", new Document("total_engagement", -1));
        Document limitDoc = new Document("$limit", limit);

        Document project = new Document("$project", new Document("_id", 0)
                .append("username", "$_id")
                .append("total_engagement", 1)
                .append("numero_di_recensioni", 1)
        );

        java.util.List<Document> pipeline = Arrays.asList(match, group, sort, limitDoc, project);

        AggregateIterable<Document> agg = coll.aggregate(pipeline);

        java.util.List<HighEngagementReviewerStats> results = new ArrayList<>();
        for (Document d : agg) {
            String username = d.getString("username");
            Integer total = null; Object t = d.get("total_engagement"); if (t instanceof Number) total = ((Number) t).intValue();
            Integer count = null; Object c = d.get("numero_di_recensioni"); if (c instanceof Number) count = ((Number) c).intValue();
            results.add(new HighEngagementReviewerStats(username, total, count));
        }

        return results;
    }
}
