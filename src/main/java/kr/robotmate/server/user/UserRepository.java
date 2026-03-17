package kr.robotmate.server.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    boolean existsByNickname(String nickname);
    boolean existsByNicknameAndIdNot(String nickname, String id);
}
