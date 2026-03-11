package com.system.auth_service.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class DBConfig {

    @Bean
    public MongoClient mongoClient(Environment env) {
        String uri = env.getProperty("spring.data.mongodb.uri");
        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException("Missing MongoDB URI. Set DB_AUTH_URI in .env or environment variables.");
        }
        return MongoClients.create(uri);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient, Environment env) {
        String dbName = env.getProperty("spring.data.mongodb.database");
        if (dbName == null || dbName.isBlank()) {
            throw new IllegalStateException("Missing MongoDB database name. Set DB_AUTH_NAME in .env or environment variables.");
        }
        return new SimpleMongoClientDatabaseFactory(mongoClient, dbName);
    }
}

