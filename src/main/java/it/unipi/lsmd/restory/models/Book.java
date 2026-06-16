package it.unipi.lsmd.restory.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Document(collection = "Books")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Book {
    
    @Id
    @JsonAlias("_id")
    private String id; // ObjectId convertito a String
    
    // `bookId` removed: use Mongo `_id` via `id` field instead
    
    @Field("title")
    @JsonProperty("title")
    private String title;
    
    @Field("genre")
    @JsonProperty("genre")
    private String genre;
    
    @Field("isbn")
    @JsonProperty("isbn")
    private String isbn;
    
    @Field("isbn13")
    @JsonProperty("isbn13")
    private String isbn13;
    
    @Field("reviews_count")
    @JsonProperty("reviews_count")
    @JsonAlias("reviewsCount")
    private Integer reviewsCount;
    
    @Field("asin")
    @JsonProperty("asin")
    private String asin;
    
    @Field("is_ebook")
    @JsonProperty("is_ebook")
    @JsonAlias("isEbook")
    private Boolean isEbook;
    
    @Field("kindle_asin")
    @JsonProperty("kindle_asin")
    @JsonAlias("kindleAsin")
    private String kindleAsin;
    
    @Field("num_pages")
    @JsonProperty("num_pages")
    @JsonAlias("numPages")
    private Integer numPages;
    
    @Field("authors")
    @JsonProperty("authors")
    private List<Map<String, Object>> authors;
    
    @Field("publisher")
    @JsonProperty("publisher")
    private String publisher;
    


    @Field("publicationdate")
    @JsonProperty("publicationdate")
    @JsonAlias({"publicationDate","publication_date"})
    private LocalDate publicationDate;
    
    @Field("edition_information")
    @JsonProperty("edition_information")
    @JsonAlias("editionInformation")
    private String editionInformation;
    
    @Field("link")
    @JsonProperty("link")
    private String link;
    
    @Field("image_url")
    @JsonProperty("image_url")
    @JsonAlias("imageUrl")
    private String imageUrl;
    
    @Field("format")
    @JsonProperty("format")
    private String format;

    @Field("score")
    @JsonProperty("score")
    private Float score;

    @Field("n_added")
    @JsonProperty("n_added")
    private Integer nAdded;

    @Field("language_code")
    @JsonProperty("language_code")
    private String languageCode;

    @Field("description")
    @JsonProperty("description")
    private String description;

    @Field("latest_reviews")
    @JsonProperty("latest_reviews")
    private List<Map<String, Object>> latestReviews;

    @Field("other_reviews")
    @JsonProperty("other_reviews")
    private List<String> otherReviews;
    
    // Costruttori
    public Book() {
        this.latestReviews = new java.util.ArrayList<>();
        this.otherReviews = new java.util.ArrayList<>();
    }
    
    public Book(String title, String genre, String isbn, String isbn13,
                Integer reviewsCount, String asin, Boolean isEbook, String kindleAsin,
                Integer numPages, List<Map<String, Object>> authors, String publisher,
                LocalDate publicationDate, String editionInformation, String link, String imageUrl, String format,
                Float score, Integer nAdded, String languageCode, String description,
                List<Map<String, Object>> latestReviews, List<String> otherReviews) {
        // id is the Mongo `_id` and is set by MongoDB; do not set here
        this.title = title;
        this.genre = genre;
        this.isbn = isbn;
        this.isbn13 = isbn13;
        this.reviewsCount = reviewsCount != null ? reviewsCount : 0;
        this.asin = asin;
        this.isEbook = isEbook != null ? isEbook : false;
        this.kindleAsin = kindleAsin;
        this.numPages = numPages;
        this.authors = authors;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
    
            this.score = score;
            this.nAdded = nAdded;
            this.languageCode = languageCode;
            this.description = description;
            this.latestReviews = latestReviews != null ? latestReviews : new java.util.ArrayList<>();
            this.otherReviews = otherReviews != null ? otherReviews : new java.util.ArrayList<>();
        this.editionInformation = editionInformation;
        this.link = link;
        this.imageUrl = imageUrl;
        this.format = format;
    }
    
    // Getter e Setter
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    // bookId removed: use getId()/setId() for the identifier
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getIsbn13() {
        return isbn13;
    }
    
    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }
    
    public Integer getReviewsCount() {
        return reviewsCount;
    }
    
    public void setReviewsCount(Integer reviewsCount) {
        this.reviewsCount = reviewsCount;
    }
    
    public String getAsin() {
        return asin;
    }
    
    public void setAsin(String asin) {
        this.asin = asin;
    }
    
    public Boolean getIsEbook() {
        return isEbook;
    }
    
    public void setIsEbook(Boolean isEbook) {
        this.isEbook = isEbook;
    }
    
    public String getKindleAsin() {
        return kindleAsin;
    }
    
    public void setKindleAsin(String kindleAsin) {
        this.kindleAsin = kindleAsin;
    }
    
    public Integer getNumPages() {
        return numPages;
    }
    
    public void setNumPages(Integer numPages) {
        this.numPages = numPages;
    }
    
    public List<Map<String, Object>> getAuthors() {
        return authors;
    }
    
    public void setAuthors(List<Map<String, Object>> authors) {
        this.authors = authors;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    

    
    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Integer getNAdded() {
        return nAdded;
    }

    public void setNAdded(Integer nAdded) {
        this.nAdded = nAdded;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Map<String, Object>> getLatestReviews() {
        return latestReviews;
    }

    public void setLatestReviews(List<Map<String, Object>> latestReviews) {
        if (latestReviews == null) return;
        this.latestReviews = new java.util.ArrayList<>(latestReviews);
    }

    public List<String> getOtherReviews() {
        return otherReviews;
    }

    public void setOtherReviews(List<String> otherReviews) {
        if (otherReviews == null) return;
        this.otherReviews = new java.util.ArrayList<>(otherReviews);
    }
    
    public String getEditionInformation() {
        return editionInformation;
    }
    
    public void setEditionInformation(String editionInformation) {
        this.editionInformation = editionInformation;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
}
