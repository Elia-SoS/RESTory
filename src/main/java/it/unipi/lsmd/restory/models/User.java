package it.unipi.lsmd.restory.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Document(collection = "Users")
public class User {
    @Id
    private String id;
    @Field("username")
    private String username;
    @Field("name")
    private String name;
    @Field("surname")
    private String surname;
    @Field("email")
    private String email;
    @Field("password")
    private String password;
    @Field("birthdate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthdate;
    @Field("bio")
    private String bio;
    @Field("favourite_genres")
    private List<String> favouriteGenres;
    @Field("city")
    private String city;
    @Field("recently_added")
    private List<Map<String, Object>> recentlyAdded;
    @Field("latest_reviews")
    private List<Map<String, Object>> latestReviews;
    @Field("followers")
    private Integer followers = 0;
    @Field("reviews")
    private Integer reviews = 0;
    @Field("books")
    private Integer books = 0;
    @Field("average_speed")
    private Integer averageSpeed = 0;
    @Field("is_banned")
    private Boolean isBanned = false;
    @Field("is_admin")
    private Boolean isAdmin = false;
    @Field("other_added")
    private List<String> otherAdded;
    @Field("other_reviews")
    private List<String> otherReviews;
    
    // Costruttori
    public User() {
    }
    
    public User(String username, String name, String surname, 
                String email, String password, LocalDate birthdate, String bio,
                List<String> favouriteGenres, String city) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.birthdate = birthdate;
        this.bio = bio;
        this.favouriteGenres = favouriteGenres;
        this.city = city;
        this.followers = 0;
        this.reviews = 0;
        this.books = 0;
            this.recentlyAdded = new java.util.ArrayList<>();
            this.latestReviews = new java.util.ArrayList<>();
        this.averageSpeed = 0;
        this.isBanned = false;
        this.isAdmin = false;
            this.otherAdded = new java.util.ArrayList<>();
            this.otherReviews = new java.util.ArrayList<>();
    }

    public User(String username, String name, String surname,
                String email, String password, LocalDate birthdate, String bio,
                List<String> favouriteGenres, String city,
                Integer followers, Integer reviews, Integer books, Integer averageSpeed) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.birthdate = birthdate;
        this.bio = bio;
        this.favouriteGenres = favouriteGenres;
        this.city = city;
        this.followers = followers != null ? followers : 0;
        this.reviews = reviews != null ? reviews : 0;
        this.books = books != null ? books : 0;
             this.recentlyAdded = new java.util.ArrayList<>();
             this.latestReviews = new java.util.ArrayList<>();
        this.averageSpeed = 0;
        this.isBanned = false;
        this.isAdmin = false;
             this.otherAdded = new java.util.ArrayList<>();
             this.otherReviews = new java.util.ArrayList<>();
    }
    
    // Getter e Setter
    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty("_id")
    public void setId(String id) {
        this.id = id;
    }
    

    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSurname() {
        return surname;
    }
    
    public void setSurname(String surname) {
        this.surname = surname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public List<String> getFavouriteGenres() {
        return favouriteGenres;
    }
    
    public void setFavouriteGenres(List<String> favouriteGenres) {
        this.favouriteGenres = favouriteGenres;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public List<Map<String, Object>> getRecentlyAdded() {
        return recentlyAdded;
    }
    
    public void setRecentlyAdded(List<Map<String, Object>> recentlyAdded) {
        if (recentlyAdded == null) return;
        this.recentlyAdded = new java.util.ArrayList<>(recentlyAdded);
    }
    
    public List<Map<String, Object>> getLatestReviews() {
        return latestReviews;
    }

    public void setLatestReviews(List<Map<String, Object>> latestReviews) {
        if (latestReviews == null) return;
        this.latestReviews = new java.util.ArrayList<>(latestReviews);
    }
    
    public Boolean getIsBanned() {
        return isBanned;
    }

    public void setIsBanned(Boolean isBanned) {
        this.isBanned = isBanned;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public List<String> getOtherAdded() {
        return otherAdded;
    }

    public void setOtherAdded(List<String> otherAdded) {
        if (otherAdded == null) return;
        this.otherAdded = new java.util.ArrayList<>(otherAdded);
    }

    public List<String> getOtherReviews() {
        return otherReviews;
    }

    public void setOtherReviews(List<String> otherReviews) {
        if (otherReviews == null) return;
        this.otherReviews = new java.util.ArrayList<>(otherReviews);
    }
    public Integer getFollowers() {
        return followers;
    }
    
    public void setFollowers(Integer followers) {
        this.followers = followers;
    }
    
    public Integer getReviews() {
        return reviews;
    }
    
    public void setReviews(Integer reviews) {
        this.reviews = reviews;
    }
    
    public Integer getBooks() {
        return books;
    }
    
    public void setBooks(Integer books) {
        this.books = books;
    }

    public Integer getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(Integer averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
}
