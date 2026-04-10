package kr.robotmate.server.news.dto;

import kr.robotmate.server.news.News;
import kr.robotmate.server.news.NewsType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NewsDetailResponse {

    private String id;
    private NewsType type;
    private String title;
    private String summary;
    private String content;
    private String thumbnailUrl;
    private boolean isPinned;
    private RobotModelRef robotModel;
    private String sourceUrl;
    private String sourceName;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NewsDetailResponse from(News news) {
        return NewsDetailResponse.builder()
                .id(news.getId())
                .type(news.getType())
                .title(news.getTitle())
                .summary(news.getSummary())
                .content(news.getContent())
                .thumbnailUrl(news.getThumbnailUrl())
                .isPinned(news.isPinned())
                .robotModel(RobotModelRef.from(news.getRobotModel()))
                .sourceUrl(news.getSourceUrl())
                .sourceName(news.getSourceName())
                .publishedAt(news.getPublishedAt())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .build();
    }
}
