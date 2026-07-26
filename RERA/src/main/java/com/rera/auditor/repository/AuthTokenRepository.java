package com.rera.auditor.repository;

import com.rera.auditor.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    // JOIN FETCH pulls the User row back in the same query, so
    // BearerTokenAuthenticationFilter gets a real initialized User
    // instead of a lazy Hibernate proxy that can't load outside a
    // session. Without this, user.getRole() throws
    // LazyInitializationException and the request is left
    // unauthenticated (which then surfaces as a 403 downstream).
    @Query("SELECT at FROM AuthToken at JOIN FETCH at.user WHERE at.token = :token")
    Optional<AuthToken> findByToken(@Param("token") String token);

    void deleteByToken(String token);
}
