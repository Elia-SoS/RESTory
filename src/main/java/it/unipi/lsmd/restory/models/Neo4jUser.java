package it.unipi.lsmd.restory.models;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.HashSet;
import java.util.Set;

@Node("RegisteredUser")
public class Neo4jUser {
    
    @Id
    private String username;
    
    private String user_id;
    
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    private Set<Neo4jUser> following = new HashSet<>();
    
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.INCOMING)
    private Set<Neo4jUser> followers = new HashSet<>();
    
    // Constructors
    public Neo4jUser() {
    }
    
    public Neo4jUser(String username) {
        this.username = username;
    }

    public Neo4jUser(String username, String user_id) {
        this.username = username;
        this.user_id = user_id;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
    
    public Set<Neo4jUser> getFollowing() {
        return following;
    }
    
    public void setFollowing(Set<Neo4jUser> following) {
        this.following = following;
    }
    
    public Set<Neo4jUser> getFollowers() {
        return followers;
    }
    
    public void setFollowers(Set<Neo4jUser> followers) {
        this.followers = followers;
    }
    
    @Override
    public String toString() {
        return "Neo4jUser{" +
                "username='" + username + '\'' +
                ", user_id='" + user_id + '\'' +
                ", followingCount=" + following.size() +
                ", followersCount=" + followers.size() +
                '}';
    }
}
