package com.app.demo.repository;

import com.app.demo.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findTopByEmailOrderByCreatedAtDesc(String email);

    @Modifying
    @Query("UPDATE OtpCode o SET o.used = true WHERE o.email = :email AND o.used = false")
    void expireAllOldOtp(@Param("email") String email);
}
