package kr.robotmate.server.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, String> {

    Optional<CommentLike> findByUserIdAndCommentId(String userId, String commentId);

    boolean existsByUserIdAndCommentId(String userId, String commentId);

    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment.id = :commentId")
    long countByCommentId(@Param("commentId") String commentId);

    @Query("SELECT cl.comment.id, COUNT(cl) FROM CommentLike cl WHERE cl.comment.id IN :commentIds GROUP BY cl.comment.id")
    List<Object[]> countsByCommentIds(@Param("commentIds") List<String> commentIds);

    @Query("SELECT cl.comment.id FROM CommentLike cl WHERE cl.user.id = :userId AND cl.comment.id IN :commentIds")
    List<String> findLikedCommentIds(@Param("userId") String userId, @Param("commentIds") List<String> commentIds);
}
