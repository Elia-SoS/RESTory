package it.unipi.lsmd.restory.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Map;

@Document(collection = "Reviews")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Review {

    @Id
    private String id;

    @Field("rating")
    private Integer rating;

    @Field("review_text")
    @JsonProperty("review_text")
    @JsonAlias("reviewText")
    private String reviewText;

    @Field("date_added")
    @JsonProperty("date_added")
    @JsonAlias("dateAdded")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateAdded;

    @Field("date_updated")
    @JsonProperty("date_updated")
    @JsonAlias("dateUpdated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateUpdated;

    @Field("n_votes")
    @JsonProperty("n_votes")
    @JsonAlias("nVotes")
    private Integer nVotes = 0;

    @Field("username")
    private String username;

    @Field("book")
    private Map<String, Object> book; // Contiene id, title, authors, image_url

    public Review() {}

    public Review(Integer rating, String reviewText, LocalDate dateAdded, LocalDate dateUpdated,
                  Integer nVotes, String username, Map<String, Object> book) {
        this.rating = rating;
        this.reviewText = reviewText;
        this.dateAdded = dateAdded;
        this.dateUpdated = dateUpdated;
        this.nVotes = nVotes != null ? nVotes : 0;
        this.username = username;
        this.book = book;
    }

    // Getter e Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }

    public LocalDate getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDate dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Integer getnVotes() {
        return nVotes;
    }

    public void setnVotes(Integer nVotes) {
        this.nVotes = nVotes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Object> getBook() {
        return book;
    }

    public void setBook(Map<String, Object> book) {
        this.book = book;
    }
}
