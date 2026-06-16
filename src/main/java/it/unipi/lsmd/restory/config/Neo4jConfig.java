package it.unipi.lsmd.restory.config;

import jakarta.annotation.PreDestroy;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {

    private final Driver driver;

    public Neo4jConfig() {
        String uri = "neo4j://127.0.0.1:7687";
        String user = "neo4j";
        String password = "123456789";

        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Bean
    public Driver neo4jDriver() {
        return this.driver;
    }

    @PreDestroy
    public void closeDriver() {
        if (this.driver != null) {
            this.driver.close();
        }
    }

}
