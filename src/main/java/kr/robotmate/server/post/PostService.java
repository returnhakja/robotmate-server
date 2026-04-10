package kr.robotmate.server.post;

import kr.robotmate.server.comment.CommentRepository;
import kr.robotmate.server.common.exception.ForbiddenException;
import kr.robotmate.server.common.exception.NotFoundException;
import kr.robotmate.server.post.dto.*;
import kr.robotmate.server.robot.RobotModel;
import kr.robotmate.server.robot.RobotModelRepository;
import kr.robotmate.server.user.Follow;
import kr.robotmate.server.user.FollowRepository;
import kr.robotmate.server.user.User;
import kr.robotmate.server.user.UserRepository;
import kr.robotmate.server.user.UserRobot;
import kr.robotmate.server.user.UserRobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RobotModelRepository robotModelRepository;
    private final UserRobotRepository userRobotRepository;
    private final LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;

    public Page<PostSummaryResponse> getPosts(PostType type, String model, String tag,
                                               String sort, int page, int size, String currentUserId,
                                               String feed) {
        Specification<Post> spec = Specification
                .where(PostSpecification.hasType(type))
                .and(PostSpecification.hasModelSlug(model))
                .and(PostSpecification.hasTag(tag))
                .and(PostSpecification.visibleTo(currentUserId));

        if ("following".equalsIgnoreCase(feed) && currentUserId != null) {
            java.util.List<String> followingIds = followRepository.findFollowingIdsByFollowerId(currentUserId);
            spec = spec.and(PostSpecification.fromFollowing(followingIds));
        }

        Sort sorting = "popular".equals(sort)
                ? Sort.by(Sort.Direction.DESC, "viewCount")
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page - 1, size, sorting);
        Page<Post> posts = postRepository.findAll(spec, pageable);

        List<String> postIds = posts.map(Post::getId).toList();
        Map<String, Long> likeCounts = getLikeCounts(postIds);
        Map<String, Long> commentCounts = getCommentCounts(postIds);

        return posts.map(post -> PostSummaryResponse.from(
                post,
                likeCounts.getOrDefault(post.getId(), 0L),
                commentCounts.getOrDefault(post.getId(), 0L)
        ));
    }

    @Transactional
    public PostDetailResponse getPost(String postId, String currentUserId) {
        Post post = findPost(postId);

        if (post.getVisibility() == PostVisibility.PRIVATE) {
            if (currentUserId == null || !currentUserId.equals(post.getAuthor().getId())) {
                throw new ForbiddenException("비공개 게시글입니다.");
            }
        }

        postRepository.incrementViewCount(postId);

        long likeCount = likeRepository.countByPostId(postId);
        long commentCount = commentRepository.countByPostId(postId);
        boolean liked = currentUserId != null && likeRepository.existsByUserIdAndPostId(currentUserId, postId);
        boolean bookmarked = currentUserId != null && bookmarkRepository.existsByUserIdAndPostId(currentUserId, postId);

        return PostDetailResponse.from(post, likeCount, commentCount, liked, bookmarked);
    }

    @Transactional
    public PostDetailResponse createPost(CreatePostRequest request, String userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        RobotModel robotModel = null;
        if (request.getRobotModelId() != null) {
            robotModel = robotModelRepository.findBySlug(request.getRobotModelId())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 기종입니다."));
        }

        UserRobot userRobot = null;
        if (request.getUserRobotId() != null) {
            userRobot = userRobotRepository.findByIdAndUserId(request.getUserRobotId(), userId)
                    .orElseThrow(() -> new NotFoundException("반려로봇을 찾을 수 없습니다."));
        }

        Post post = Post.builder()
                .type(request.getType())
                .visibility(request.getVisibility() != null ? request.getVisibility() : PostVisibility.PUBLIC)
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .robotModel(robotModel)
                .userRobot(userRobot)
                .tags(request.getTags())
                .images(request.getImages())
                .saleType(request.getSaleType())
                .salePrice(request.getSalePrice())
                .condition(request.getCondition())
                .usagePeriod(request.getUsagePeriod())
                .tradeMethod(request.getTradeMethod())
                .tradeLocation(request.getTradeLocation())
                .contactInfo(request.getContactInfo())
                .build();

        Post saved = postRepository.save(post);
        return PostDetailResponse.from(saved, 0L, 0L, false, false);
    }

    @Transactional
    public PostDetailResponse updatePost(String postId, UpdatePostRequest request, String userId) {
        Post post = findPost(postId);
        checkOwner(post, userId);

        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getVisibility() != null) post.setVisibility(request.getVisibility());
        if (request.getTags() != null) post.setTags(request.getTags());
        if (request.getImages() != null) post.setImages(request.getImages());
        if (request.getSaleType() != null) post.setSaleType(request.getSaleType());
        if (request.getSalePrice() != null) post.setSalePrice(request.getSalePrice());
        if (request.getCondition() != null) post.setCondition(request.getCondition());
        if (request.getUsagePeriod() != null) post.setUsagePeriod(request.getUsagePeriod());
        if (request.getTradeMethod() != null) post.setTradeMethod(request.getTradeMethod());
        if (request.getTradeLocation() != null) post.setTradeLocation(request.getTradeLocation());
        if (request.getContactInfo() != null) post.setContactInfo(request.getContactInfo());
        if (request.getSold() != null) post.setSold(request.getSold());

        if (request.getRobotModelId() != null) {
            RobotModel robotModel = robotModelRepository.findBySlug(request.getRobotModelId())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 기종입니다."));
            post.setRobotModel(robotModel);
        }

        if (request.getUserRobotId() != null) {
            UserRobot userRobot = userRobotRepository.findByIdAndUserId(request.getUserRobotId(), userId)
                    .orElseThrow(() -> new NotFoundException("반려로봇을 찾을 수 없습니다."));
            post.setUserRobot(userRobot);
        }

        long likeCount = likeRepository.countByPostId(postId);
        long commentCount = commentRepository.countByPostId(postId);
        return PostDetailResponse.from(post, likeCount, commentCount, false, false);
    }

    @Transactional
    public PostDetailResponse markSold(String postId, String userId) {
        Post post = findPost(postId);
        checkOwner(post, userId);
        post.setSold(!post.isSold());
        long likeCount = likeRepository.countByPostId(postId);
        long commentCount = commentRepository.countByPostId(postId);
        return PostDetailResponse.from(post, likeCount, commentCount, false, false);
    }

    @Transactional
    public void deletePost(String postId, String userId) {
        Post post = findPost(postId);
        checkOwner(post, userId);
        postRepository.delete(post);
    }

    @Transactional
    public LikeResponse toggleLike(String postId, String userId) {
        Post post = findPost(postId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        boolean liked;
        Optional<Like> existing = likeRepository.findByUserIdAndPostId(userId, postId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            liked = false;
        } else {
            likeRepository.save(Like.builder().user(user).post(post).build());
            liked = true;
        }

        long likeCount = likeRepository.countByPostId(postId);
        return new LikeResponse(liked, likeCount);
    }

    @Transactional
    public BookmarkResponse toggleBookmark(String postId, String userId) {
        Post post = findPost(postId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        boolean bookmarked;
        Optional<Bookmark> existing = bookmarkRepository.findByUserIdAndPostId(userId, postId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            bookmarked = false;
        } else {
            bookmarkRepository.save(Bookmark.builder().user(user).post(post).build());
            bookmarked = true;
        }

        return new BookmarkResponse(bookmarked);
    }

    // --- 헬퍼 ---

    private Post findPost(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));
    }

    private void checkOwner(Post post, String userId) {
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("본인의 게시글만 수정/삭제할 수 있습니다.");
        }
    }

    private Map<String, Long> getLikeCounts(List<String> postIds) {
        return likeRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
    }

    private Map<String, Long> getCommentCounts(List<String> postIds) {
        return commentRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
    }
}
