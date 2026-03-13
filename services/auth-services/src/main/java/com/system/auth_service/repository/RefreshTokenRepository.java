package com.system.auth_service.repository;

import com.system.auth_service.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    Optional<RefreshToken> findByUserIdAndDeviceId(String userId, String deviceId);

    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
