package kr.robotmate.server.comment.dto;

import kr.robotmate.server.comment.Comment;
import kr.robotmate.server.post.dto.AuthorInfo;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponse {
    private String id;
    private String content;
    private AuthorInfo author;
    private LocalDateTime createdAt;
    private long likeCount;
    private boolean liked;
    private List<CommentResponse> replies;

    public static CommentResponse from(Comment comment, long likeCount, boolean liked,
                                       java.util.Map<String, Long> replyLikeCounts,
                                       java.util.Set<String> likedCommentIds) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(AuthorInfo.from(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .likeCount(likeCount)
                .liked(liked)
                .replies(comment.getReplies().stream()
                        .map(r -> CommentResponse.from(
                                r,
                                replyLikeCounts.getOrDefault(r.getId(), 0L),
                                likedCommentIds.contains(r.getId()),
                                java.util.Map.of(),
                                java.util.Set.of()))
                        .toList())
                .build();
    }
}
