package kr.robotmate.server.admin.dto;

import kr.robotmate.server.post.Post;
import kr.robotmate.server.post.PostVisibility;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminPostSummaryResponse {
    private String id;
    private String type;
    private String title;
    private String authorId;
    private String authorNickname;
    private String visibility;
    private int viewCount;
    private long likeCount;
    private long commentCount;
    private LocalDateTime createdAt;

    public static AdminPostSummaryResponse from(Post post, long likeCount, long commentCount) {
        return AdminPostSummaryResponse.builder()
                .id(post.getId())
                .type(post.getType().name())
                .title(post.getTitle())
                .authorId(post.getAuthor().getId())
                .authorNickname(post.getAuthor().getNickname())
                .visibility(post.getVisibility() != null ? post.getVisibility().name() : PostVisibility.PUBLIC.name())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
