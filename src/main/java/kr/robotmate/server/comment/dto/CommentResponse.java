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
    private List<CommentResponse> replies;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(AuthorInfo.from(comment.getAuthor()))
                .createdAt(comment.getCreatedAt())
                .replies(comment.getReplies().stream().map(CommentResponse::from).toList())
                .build();
    }
}
