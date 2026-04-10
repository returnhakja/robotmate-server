package kr.robotmate.server.news.dto;

import kr.robotmate.server.news.News;
import kr.robotmate.server.news.NewsType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NewsSummaryResponse {

    private String id;
    private NewsType type;
    private String title;
    private String summary;
    private String thumbnailUrl;
    private boolean isPinned;
    private RobotModelRef robotModel;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    public static NewsSummaryResponse from(News news) {
        return NewsSummaryResponse.builder()
                .id(news.getId())
                .type(news.getType())
                .title(news.getTitle())
                .summary(news.getSummary())
                .thumbnailUrl(news.getThumbnailUrl())
                .isPinned(news.isPinned())
                .robotModel(RobotModelRef.from(news.getRobotModel()))
                .publishedAt(news.getPublishedAt())
                .createdAt(news.getCreatedAt())
                .build();
    }
}
