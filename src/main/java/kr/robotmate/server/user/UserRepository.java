package kr.robotmate.server.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    boolean existsByNickname(String nickname);
    boolean existsByNicknameAndIdNot(String nickname, String id);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :start")
    long countByCreatedAtAfter(@Param("start") LocalDateTime start);
}
