package kr.robotmate.server.news;

import kr.robotmate.server.common.exception.NotFoundException;
import kr.robotmate.server.news.dto.NewsDetailResponse;
import kr.robotmate.server.news.dto.NewsRequest;
import kr.robotmate.server.news.dto.NewsSummaryResponse;
import kr.robotmate.server.robot.RobotModel;
import kr.robotmate.server.robot.RobotModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;
    private final RobotModelRepository robotModelRepository;

    // ── Public ─────────────────────────────────────────────

    public Page<NewsSummaryResponse> getPublishedNews(NewsType type, String robotModelSlug,
                                                      Boolean pinned, int page, int size) {
        Specification<News> spec = Specification
                .where(NewsSpecification.isPublished())
                .and(NewsSpecification.hasType(type))
                .and(NewsSpecification.hasRobotModel(robotModelSlug))
                .and(NewsSpecification.isPinned(pinned));

        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "isPinned")
                        .and(Sort.by(Sort.Direction.DESC, "publishedAt")));

        return newsRepository.findAll(spec, pageable).map(NewsSummaryResponse::from);
    }

    public NewsDetailResponse getPublishedById(String id) {
        News news = findById(id);
        LocalDateTime now = LocalDateTime.now();
        if (news.getPublishedAt() == null || news.getPublishedAt().isAfter(now)) {
            throw new NotFoundException("존재하지 않는 뉴스입니다.");
        }
        return NewsDetailResponse.from(news);
    }

    // ── Admin ──────────────────────────────────────────────

    public Page<NewsSummaryResponse> getAllNews(String keyword, NewsType type,
                                               String robotModelSlug, Boolean published,
                                               int page, int size) {
        Specification<News> spec = Specification
                .where(NewsSpecification.hasKeyword(keyword))
                .and(NewsSpecification.hasType(type))
                .and(NewsSpecification.hasRobotModel(robotModelSlug))
                .and(NewsSpecification.isPublishedFilter(published));

        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return newsRepository.findAll(spec, pageable).map(NewsSummaryResponse::from);
    }

    public NewsDetailResponse getById(String id) {
        return NewsDetailResponse.from(findById(id));
    }

    @Transactional
    public NewsDetailResponse create(NewsRequest request) {
        News news = News.builder()
                .type(request.getType())
                .title(request.getTitle())
                .summary(resolveSummary(request.getSummary(), request.getContent()))
                .content(request.getContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .isPinned(request.isPinned())
                .robotModel(resolveRobotModel(request.getRobotModelSlug()))
                .sourceUrl(request.getSourceUrl())
                .sourceName(request.getSourceName())
                .publishedAt(request.getPublishedAt() != null ? request.getPublishedAt() : LocalDateTime.now())
                .build();

        return NewsDetailResponse.from(newsRepository.save(news));
    }

    @Transactional
    public NewsDetailResponse updateById(String id, NewsRequest request) {
        News news = findById(id);

        news.setType(request.getType());
        news.setTitle(request.getTitle());
        news.setSummary(resolveSummary(request.getSummary(), request.getContent()));
        news.setContent(request.getContent());
        news.setThumbnailUrl(request.getThumbnailUrl());
        news.setPinned(request.isPinned());
        news.setRobotModel(resolveRobotModel(request.getRobotModelSlug()));
        news.setSourceUrl(request.getSourceUrl());
        news.setSourceName(request.getSourceName());
        news.setPublishedAt(request.getPublishedAt() != null ? request.getPublishedAt() : LocalDateTime.now());

        return NewsDetailResponse.from(news);
    }

    @Transactional
    public void deleteById(String id) {
        newsRepository.delete(findById(id));
    }

    @Transactional
    public NewsDetailResponse setPinById(String id, boolean isPinned) {
        News news = findById(id);
        news.setPinned(isPinned);
        return NewsDetailResponse.from(news);
    }

    // ── 헬퍼 ──────────────────────────────────────────────

    private News findById(String id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 뉴스입니다: " + id));
    }

    private RobotModel resolveRobotModel(String robotModelSlug) {
        if (robotModelSlug == null || robotModelSlug.isBlank()) return null;
        return robotModelRepository.findBySlug(robotModelSlug)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 기종입니다: " + robotModelSlug));
    }

    /** summary 미입력 시 content HTML 태그 제거 후 앞 150자 자동 생성 */
    private String resolveSummary(String summary, String content) {
        if (summary != null && !summary.isBlank()) return summary;
        if (content == null || content.isBlank()) return null;
        String plain = content.replaceAll("<[^>]+>", "").strip();
        return plain.length() > 150 ? plain.substring(0, 150) + "..." : plain;
    }
}
