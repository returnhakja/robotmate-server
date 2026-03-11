package kr.robotmate.server.post.dto;

import kr.robotmate.server.post.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostDetailResponse {
    private String id;
    private String type;
    private String title;
    private String content;
    private AuthorInfo author;
    private String robotModelSlug;
    private String robotModelName;
    private List<String> tags;
    private List<String> images;
    private int viewCount;
    private long likeCount;
    private long commentCount;
    private boolean liked;
    private boolean bookmarked;
    private LocalDateTime createdAt;

    // 판매/나눔 전용 필드
    private String saleType;
    private Integer salePrice;
    private String condition;
    private String usagePeriod;
    private String tradeMethod;
    private String contactInfo;
    private boolean sold;

    public static PostDetailResponse from(Post post, long likeCount, long commentCount,
                                          boolean liked, boolean bookmarked) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .type(post.getType().name())
                .title(post.getTitle())
                .content(post.getContent())
                .author(AuthorInfo.from(post.getAuthor()))
                .robotModelSlug(post.getRobotModel() != null ? post.getRobotModel().getSlug() : null)
                .robotModelName(post.getRobotModel() != null ? post.getRobotModel().getName() : null)
                .tags(post.getTags())
                .images(post.getImages())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .liked(liked)
                .bookmarked(bookmarked)
                .createdAt(post.getCreatedAt())
                .saleType(post.getSaleType() != null ? post.getSaleType().name() : null)
                .salePrice(post.getSalePrice())
                .condition(post.getCondition())
                .usagePeriod(post.getUsagePeriod())
                .tradeMethod(post.getTradeMethod())
                .contactInfo(post.getContactInfo())
                .sold(post.isSold())
                .build();
    }
}
