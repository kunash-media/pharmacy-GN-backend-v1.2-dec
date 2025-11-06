package com.gn.pharmacy.repository;
import com.gn.pharmacy.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);

    boolean existsByEmail(String email);


    // Add this method to find user by phone/mobile
    Optional<UserEntity> findByPhone(String phone);

}
