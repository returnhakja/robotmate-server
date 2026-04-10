package kr.robotmate.server.news;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class NewsSpecification {

    public static Specification<News> isPublished() {
        return (root, query, cb) ->
                cb.and(
                        cb.isNotNull(root.get("publishedAt")),
                        cb.lessThanOrEqualTo(root.get("publishedAt"), LocalDateTime.now())
                );
    }

    public static Specification<News> hasType(NewsType type) {
        if (type == null) return null;
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<News> hasRobotModel(String robotModelId) {
        if (robotModelId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("robotModel").get("id"), robotModelId);
    }

    public static Specification<News> isPinned(Boolean pinned) {
        if (pinned == null) return null;
        return (root, query, cb) -> cb.equal(root.get("isPinned"), pinned);
    }

    public static Specification<News> hasKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<News> isPublishedFilter(Boolean published) {
        if (published == null) return null;
        if (published) return isPublished();
        return (root, query, cb) ->
                cb.or(
                        cb.isNull(root.get("publishedAt")),
                        cb.greaterThan(root.get("publishedAt"), LocalDateTime.now())
                );
    }
}
