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
}
