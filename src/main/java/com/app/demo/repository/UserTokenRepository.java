package com.app.demo.repository;

import com.app.demo.model.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findTopByEmailAndTypeOrderByCreatedAtDesc(String email, String type);

    Optional<UserToken> findByToken(String token);

    @Modifying
    @Query("UPDATE UserToken o SET o.used = true WHERE o.email = :email AND o.type = :type AND o.used = false")
    void expireAllOldToken(
            @Param("email") String email,
            @Param("type") String type
    );
}
