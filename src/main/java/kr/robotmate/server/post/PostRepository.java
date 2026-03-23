package kr.robotmate.server.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PostRepository extends JpaRepository<Post, String>, JpaSpecificationExecutor<Post> {

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") String id);

    Page<Post> findByAuthorIdOrderByCreatedAtDesc(String authorId, Pageable pageable);

    long countByType(PostType type);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :start")
    long countByCreatedAtAfter(@Param("start") LocalDateTime start);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.robotModel.id = :robotModelId")
    long countByRobotModelId(@Param("robotModelId") String robotModelId);
}
