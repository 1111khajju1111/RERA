package com.rera.auditor.repository;

import com.rera.auditor.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    /**
     * JOIN FETCH pulls the associated User eagerly, in the same query —
     * needed because AuthToken.user is @ManyToOne(LAZY) and
     * spring.jpa.open-in-view=false means there's no lingering Hibernate
     * session after this repository call returns. Without the JOIN FETCH,
     * anything touching token.getUser().getRole() afterwards (as
     * BearerTokenAuthenticationFilter does) throws
     * LazyInitializationException: "no session".
     */
    @Query("SELECT t FROM AuthToken t JOIN FETCH t.user WHERE t.token = :token")
    Optional<AuthToken> findByToken(@Param("token") String token);

    void deleteByToken(String token);
}
