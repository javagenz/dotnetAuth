package com.jgz.backend.repository;

import com.jgz.backend.entity.OurUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OurUserRepository extends JpaRepository<OurUser, Long> {
    Optional<OurUser> findByEmail(String email);

    Optional<OurUser> findByPhoneNumber(String email);
}

