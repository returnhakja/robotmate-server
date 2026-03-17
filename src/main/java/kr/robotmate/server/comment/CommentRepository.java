package kr.robotmate.server.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {

    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(String postId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.post WHERE c.author.id = :authorId ORDER BY c.createdAt DESC")
    Page<Comment> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") String authorId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countByPostId(@Param("postId") String postId);

    @Query("SELECT c.post.id, COUNT(c) FROM Comment c WHERE c.post.id IN :postIds GROUP BY c.post.id")
    List<Object[]> countsByPostIds(@Param("postIds") List<String> postIds);
}
