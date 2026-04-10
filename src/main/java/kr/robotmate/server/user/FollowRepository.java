package kr.robotmate.server.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, String> {

    Optional<Follow> findByFollowerIdAndFollowingId(String followerId, String followingId);

    boolean existsByFollowerIdAndFollowingId(String followerId, String followingId);

    long countByFollowingId(String followingId);

    long countByFollowerId(String followerId);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId")
    List<String> findFollowingIdsByFollowerId(@Param("followerId") String followerId);
}
