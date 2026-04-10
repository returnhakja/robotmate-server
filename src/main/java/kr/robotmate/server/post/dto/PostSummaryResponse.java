package kr.robotmate.server.post.dto;

import kr.robotmate.server.post.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostSummaryResponse {
    private String id;
    private String type;
    private String visibility;
    private String title;
    private String content;  // 앞 150자만
    private AuthorInfo author;
    private String robotModelSlug;
    private String robotModelName;
    private UserRobotInfo userRobot;
    private List<String> tags;
    private String thumbnail;  // images[0]
    private int viewCount;
    private long likeCount;
    private long commentCount;
    private boolean sold;
    private LocalDateTime createdAt;

    public static PostSummaryResponse from(Post post, long likeCount, long commentCount) {
        return PostSummaryResponse.builder()
                .id(post.getId())
                .type(post.getType().name())
                .visibility(post.getVisibility().name())
                .title(post.getTitle())
                .content(truncate(post.getContent(), 150))
                .author(AuthorInfo.from(post.getAuthor()))
                .robotModelSlug(post.getRobotModel() != null ? post.getRobotModel().getSlug() : null)
                .robotModelName(post.getRobotModel() != null ? post.getRobotModel().getName() : null)
                .userRobot(post.getUserRobot() != null ? UserRobotInfo.from(post.getUserRobot()) : null)
                .tags(post.getTags())
                .thumbnail(post.getImages().isEmpty() ? null : post.getImages().get(0))
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .sold(post.isSold())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
