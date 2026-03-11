package com.system.auth_service.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
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

    // DB mặc định cho auth-service: chỉ chứa refresh_tokens
    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient, Environment env) {
        String dbName = env.getProperty("spring.data.mongodb.database");
        if (dbName == null || dbName.isBlank()) {
            throw new IllegalStateException("Missing MongoDB database name. Set DB_AUTH_NAME in .env or environment variables.");
        }
        return new SimpleMongoClientDatabaseFactory(mongoClient, dbName);
    }

    // mongoTemplate mặc định — RefreshTokenRepository dùng bean này (trỏ vào DB auth)
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }

    // DB thứ hai: users — dùng chung với users-service
    @Bean
    public MongoTemplate usersMongoTemplate(MongoClient mongoClient, Environment env) {
        String dbName = env.getProperty("spring.data.mongodb.database.users");
        if (dbName == null || dbName.isBlank()) {
            throw new IllegalStateException("Missing DB_USER_NAME in .env or environment variables.");
        }
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, dbName));
    }
}

