package com.example.studentinternship.Repository;

import com.example.studentinternship.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
//    Optional<UserEntity> findByEmailAndOtp(String email, String otp);

}
