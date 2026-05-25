package com.app.demo.repository;

import com.app.demo.enums.AuthProvider;
import com.app.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    User findByProviderAndProviderId(AuthProvider provider, String providerId);
    Optional<User> findByEmail(String email);

    @Query("""
        select u
        from User u
        left join fetch u.roles
        where u.email = :email
    """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);
}
