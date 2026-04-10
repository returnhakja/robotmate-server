package kr.robotmate.server.news;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.robotmate.server.common.ApiResponse;
import kr.robotmate.server.common.PageResponse;
import kr.robotmate.server.news.dto.NewsDetailResponse;
import kr.robotmate.server.news.dto.NewsSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "News", description = "뉴스/소식 공개 API")
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @Operation(
            summary = "뉴스 목록 조회",
            description = "발행된 뉴스/소식 목록을 반환합니다. isPinned=true인 항목이 상단에 정렬됩니다."
    )
    @GetMapping
    public ApiResponse<PageResponse<NewsSummaryResponse>> getNews(
            @Parameter(description = "유형 필터 (NOTICE | NEWS | ARTICLE)") @RequestParam(required = false) NewsType type,
            @Parameter(description = "연결 기종 ID 필터") @RequestParam(required = false) String robotModelId,
            @Parameter(description = "고정 글만 조회") @RequestParam(required = false) Boolean pinned,
            @Parameter(description = "페이지 번호 (1부터 시작)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수") @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(PageResponse.from(newsService.getPublishedNews(type, robotModelId, pinned, page, size)));
    }

    @Operation(
            summary = "뉴스 상세 조회",
            description = "ID로 발행된 뉴스 상세 정보를 반환합니다."
    )
    @GetMapping("/{id}")
    public ApiResponse<NewsDetailResponse> getNews(
            @Parameter(description = "뉴스 ID") @PathVariable String id) {
        return ApiResponse.ok(newsService.getPublishedById(id));
    }
}
