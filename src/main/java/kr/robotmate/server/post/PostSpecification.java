package kr.robotmate.server.post;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class PostSpecification {

    public static Specification<Post> hasType(PostType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Post> hasModelSlug(String slug) {
        return (root, query, cb) -> {
            if (slug == null) return null;
            Join<Object, Object> model = root.join("robotModel", JoinType.LEFT);
            return cb.equal(model.get("slug"), slug);
        };
    }

    public static Specification<Post> hasTag(String tag) {
        return (root, query, cb) -> {
            if (tag == null) return null;
            Join<Object, Object> tags = root.join("tags", JoinType.LEFT);
            return cb.equal(tags, tag);
        };
    }

    public static Specification<Post> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null) return null;
            return cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<Post> hasAuthorId(String authorId) {
        return (root, query, cb) ->
                authorId == null ? null : cb.equal(root.get("author").get("id"), authorId);
    }

    public static Specification<Post> hasVisibility(PostVisibility visibility) {
        return (root, query, cb) ->
                visibility == null ? null : cb.equal(root.get("visibility"), visibility);
    }

    /**
     * 팔로잉 중인 유저의 게시글만 필터링
     */
    public static Specification<Post> fromFollowing(java.util.List<String> followingIds) {
        return (root, query, cb) -> {
            if (followingIds == null || followingIds.isEmpty()) {
                return cb.disjunction(); // 팔로잉이 없으면 결과 없음
            }
            return root.get("author").get("id").in(followingIds);
        };
    }

    /**
     * 비로그인/타인: PUBLIC만 노출
     * 로그인한 본인: 본인의 PRIVATE + 모든 PUBLIC
     */
    public static Specification<Post> visibleTo(String currentUserId) {
        return (root, query, cb) -> {
            if (currentUserId == null) {
                return cb.equal(root.get("visibility"), PostVisibility.PUBLIC);
            }
            return cb.or(
                    cb.equal(root.get("visibility"), PostVisibility.PUBLIC),
                    cb.and(
                            cb.equal(root.get("visibility"), PostVisibility.PRIVATE),
                            cb.equal(root.get("author").get("id"), currentUserId)
                    )
            );
        };
    }
}
