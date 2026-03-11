package com.system.device_service.repository;

import com.system.device_service.model.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {

    List<Device> findByUserId(String userId);

    Optional<Device> findByUserIdAndFcmToken(String userId, String fcmToken);

    void deleteByUserIdAndFcmToken(String userId, String fcmToken);
}
