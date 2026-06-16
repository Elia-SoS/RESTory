package it.unipi.lsmd.restory.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "Interactions")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Interaction {
    
    @Id
    @JsonAlias("_id")
    private String id; // ObjectId convertito a String (_id)
    
    @Field("date_added")
    @JsonProperty("date_added")
    @JsonAlias("dateAdded")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateAdded;
    
    @Field("date_updated")
    @JsonProperty("date_updated")
    @JsonAlias("dateUpdated")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateUpdated;
    
    @Field("is_read")
    @JsonProperty("is_read")
    @JsonAlias("isRead")
    private Boolean isRead;
    
    @Field("read_at")
    @JsonProperty("read_at")
    @JsonAlias("readAt")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate readAt;
    
    @Field("started_at")
    @JsonProperty("started_at")
    @JsonAlias("startedAt")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startedAt;
    
    @Field("username")
    @JsonProperty("username")
    private String username;

    @Transient
    @JsonProperty("userId")
    @JsonAlias("user_id")
    private String userId;
    
    @Field("book")
    @JsonProperty("book")
    private Map<String, Object> book; // Contiene id, title, authors, genre, image_url
    
    // Constructors
    public Interaction() {}
    
    public Interaction(LocalDate dateAdded, LocalDate dateUpdated, Boolean isRead, 
                      LocalDate readAt, LocalDate startedAt,
                      String username, Map<String, Object> book) {
        this.dateAdded = dateAdded;
        this.dateUpdated = dateUpdated;
        this.isRead = isRead;
        this.readAt = readAt;
        this.startedAt = startedAt;
        this.username = username;
        this.book = book;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public LocalDate getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDate readAt) {
        this.readAt = readAt;
    }
    
    public LocalDate getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDate startedAt) {
        this.startedAt = startedAt;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Map<String, Object> getBook() {
        return book;
    }
    
    public void setBook(Map<String, Object> book) {
        this.book = book;
    }
    
    @Override
    public String toString() {
        return "Interaction{" +
                "id='" + id + '\'' +
                ", dateAdded=" + dateAdded +
                ", dateUpdated=" + dateUpdated +
                ", isRead=" + isRead +
                ", readAt=" + readAt +
                ", startedAt=" + startedAt +
                ", user=" + username +
                ", book=" + book +
                '}';
    }
}
