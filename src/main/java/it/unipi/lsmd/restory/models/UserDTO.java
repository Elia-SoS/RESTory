package it.unipi.lsmd.restory.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO pubblico per l'utente.
 * Esclude password, isBanned e isAdmin.
 */
public class UserDTO {
    private String id;
    private String username;
    private String name;
    private String surname;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthdate;
    private String bio;
    private List<String> favouriteGenres;
    private String city;
    private List<Map<String, Object>> recentlyAdded;
    private List<Map<String, Object>> latestReviews;
    private Integer followers;
    private Integer reviews;
    private Integer books;
    private Integer averageSpeed;
    private List<String> otherAdded;
    private List<String> otherReviews;
    public UserDTO() {
    }

    public UserDTO(String id, String username, String name, String surname, String email,
                   LocalDate birthdate, String bio, List<String> favouriteGenres, String city,
                   List<Map<String, Object>> recentlyAdded, List<Map<String, Object>> latestReviews,
                   Integer followers, Integer reviews, Integer books, Integer averageSpeed,
                   List<String> otherAdded, List<String> otherReviews) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.birthdate = birthdate;
        this.bio = bio;
        this.favouriteGenres = favouriteGenres;
        this.city = city;
        this.recentlyAdded = recentlyAdded;
        this.latestReviews = latestReviews;
        this.followers = followers;
        this.reviews = reviews;
        this.books = books;
        this.averageSpeed = averageSpeed;
        this.otherAdded = otherAdded;
        this.otherReviews = otherReviews;
    }

    public static UserDTO fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getBirthdate(),
                user.getBio(),
                user.getFavouriteGenres(),
                user.getCity(),
                user.getRecentlyAdded(),
                user.getLatestReviews(),
                user.getFollowers(),
                user.getReviews(),
                user.getBooks(),
                user.getAverageSpeed(),
                user.getOtherAdded(),
                user.getOtherReviews()
        );
    }

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
        this.recentlyAdded = recentlyAdded;
    }

    public List<Map<String, Object>> getLatestReviews() {
        return latestReviews;
    }

    public void setLatestReviews(List<Map<String, Object>> latestReviews) {
        this.latestReviews = latestReviews;
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

    public List<String> getOtherAdded() {
        return otherAdded;
    }

    public void setOtherAdded(List<String> otherAdded) {
        this.otherAdded = otherAdded;
    }

    public List<String> getOtherReviews() {
        return otherReviews;
    }

    public void setOtherReviews(List<String> otherReviews) {
        this.otherReviews = otherReviews;
    }
}