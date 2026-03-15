package kr.robotmate.server.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, String> {

    Optional<Bookmark> findByUserIdAndPostId(String userId, String postId);

    boolean existsByUserIdAndPostId(String userId, String postId);

    @Query(value = "SELECT b FROM Bookmark b JOIN FETCH b.post WHERE b.user.id = :userId ORDER BY b.createdAt DESC",
           countQuery = "SELECT COUNT(b) FROM Bookmark b WHERE b.user.id = :userId")
    Page<Bookmark> findByUserIdWithPost(@Param("userId") String userId, Pageable pageable);
}
