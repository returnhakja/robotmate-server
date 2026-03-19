package kr.robotmate.server.comment.dto;

import kr.robotmate.server.comment.Comment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MyCommentResponse {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private String postId;
    private String postTitle;
    private boolean isReply;
    private long likeCount;

    public static MyCommentResponse from(Comment comment, long likeCount) {
        return MyCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .postId(comment.getPost().getId())
                .postTitle(comment.getPost().getTitle())
                .isReply(comment.getParent() != null)
                .likeCount(likeCount)
                .build();
    }
}
