package kr.robotmate.server.news.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.robotmate.server.news.NewsType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsRequest {

    @NotNull
    private NewsType type;

    @NotBlank
    private String title;

    private String summary;
    private String content;
    private String thumbnailUrl;
    private boolean isPinned = false;

    /** 연결할 기종의 slug (nullable) */
    private String robotModelSlug;

    private String sourceUrl;
    private String sourceName;

    /**
     * null → 즉시 발행
     * 과거/현재 시간 → 즉시 발행
     * 미래 시간 → 예약 발행
     */
    private LocalDateTime publishedAt;
}
