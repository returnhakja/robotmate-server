package kr.robotmate.server.admin;

import kr.robotmate.server.admin.dto.*;
import kr.robotmate.server.auth.RefreshTokenRepository;
import kr.robotmate.server.common.PageResponse;
import kr.robotmate.server.common.exception.ConflictException;
import kr.robotmate.server.common.exception.NotFoundException;
import kr.robotmate.server.news.NewsRepository;
import kr.robotmate.server.news.NewsService;
import kr.robotmate.server.news.NewsType;
import kr.robotmate.server.news.dto.NewsDetailResponse;
import kr.robotmate.server.news.dto.NewsRequest;
import kr.robotmate.server.news.dto.NewsSummaryResponse;
import kr.robotmate.server.post.Post;
import kr.robotmate.server.post.PostRepository;
import kr.robotmate.server.post.PostSpecification;
import kr.robotmate.server.post.PostType;
import kr.robotmate.server.post.PostVisibility;
import kr.robotmate.server.post.LikeRepository;
import kr.robotmate.server.post.dto.UpdatePostRequest;
import kr.robotmate.server.robot.RobotModel;
import kr.robotmate.server.robot.RobotModelRepository;
import kr.robotmate.server.robot.dto.RobotModelResponse;
import kr.robotmate.server.user.Role;
import kr.robotmate.server.user.User;
import kr.robotmate.server.user.UserRepository;
import kr.robotmate.server.user.UserRobotRepository;
import kr.robotmate.server.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final RobotModelRepository robotModelRepository;
    private final UserRobotRepository userRobotRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NewsRepository newsRepository;
    private final NewsService newsService;

    // ── 대시보드 통계 ──────────────────────────────────────

    public AdminStatsResponse getStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalPosts(postRepository.count())
                .totalMarketPosts(postRepository.countByType(PostType.SALE))
                .totalNews(newsRepository.count())
                .pendingReports(0)
                .newUsersToday(userRepository.countByCreatedAtAfter(todayStart))
                .newPostsToday(postRepository.countByCreatedAtAfter(todayStart))
                .build();
    }

    // ── 회원 관리 ──────────────────────────────────────────

    public Page<AdminUserResponse> getUsers(String keyword, Role role, UserStatus status, int page, int size) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasKeyword(keyword))
                .and(UserSpecification.hasRole(role))
                .and(UserSpecification.hasStatus(status));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(spec, pageable).map(AdminUserResponse::from);
    }

    @Transactional
    public AdminUserResponse changeUserRole(String userId, Role role) {
        User user = findUser(userId);
        user.setRole(role);
        return AdminUserResponse.from(user);
    }

    @Transactional
    public AdminUserResponse changeUserStatus(String userId, UserStatus status, String suspendReason) {
        User user = findUser(userId);
        user.setStatus(status);
        if (status == UserStatus.SUSPENDED) {
            user.setSuspendReason(suspendReason);
        } else {
            user.setSuspendReason(null);
        }
        return AdminUserResponse.from(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = findUser(userId);
        user.setStatus(UserStatus.PENDING_DELETION);
        user.setSuspendReason(null);
        refreshTokenRepository.deleteByUserId(userId);
    }

    // ── 게시글 관리 ────────────────────────────────────────

    public Page<AdminPostSummaryResponse> getPosts(String keyword, PostType type,
                                                   String authorId, PostVisibility visibility,
                                                   int page, int size) {
        Specification<Post> spec = Specification
                .where(PostSpecification.hasKeyword(keyword))
                .and(PostSpecification.hasType(type))
                .and(PostSpecification.hasAuthorId(authorId))
                .and(PostSpecification.hasVisibility(visibility));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findAll(spec, pageable);

        List<String> postIds = posts.map(Post::getId).toList();
        Map<String, Long> likeCounts = likeRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
        Map<String, Long> commentCounts = Map.of(); // 어드민은 likeCount만 표시, 필요시 추가

        return posts.map(post -> AdminPostSummaryResponse.from(
                post,
                likeCounts.getOrDefault(post.getId(), 0L),
                commentCounts.getOrDefault(post.getId(), 0L)
        ));
    }

    @Transactional
    public void updatePost(String postId, UpdatePostRequest request) {
        Post post = findPost(postId);

        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getTags() != null) post.setTags(request.getTags());
        if (request.getImages() != null) post.setImages(request.getImages());
        if (request.getSaleType() != null) post.setSaleType(request.getSaleType());
        if (request.getSalePrice() != null) post.setSalePrice(request.getSalePrice());
        if (request.getCondition() != null) post.setCondition(request.getCondition());
        if (request.getUsagePeriod() != null) post.setUsagePeriod(request.getUsagePeriod());
        if (request.getTradeMethod() != null) post.setTradeMethod(request.getTradeMethod());
        if (request.getContactInfo() != null) post.setContactInfo(request.getContactInfo());
        if (request.getSold() != null) post.setSold(request.getSold());

        if (request.getRobotModelId() != null) {
            RobotModel robotModel = robotModelRepository.findBySlug(request.getRobotModelId())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 기종입니다."));
            post.setRobotModel(robotModel);
        }
    }

    @Transactional
    public void deletePost(String postId) {
        Post post = findPost(postId);
        postRepository.delete(post);
    }

    // ── 로봇 모델 관리 ─────────────────────────────────────

    @Transactional
    public RobotModelResponse createModel(AdminModelRequest request) {
        if (robotModelRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("이미 존재하는 slug입니다: " + request.getSlug());
        }

        RobotModel model = RobotModel.builder()
                .slug(request.getSlug())
                .name(request.getName())
                .maker(request.getMaker())
                .price(request.getPrice())
                .emoji(request.getEmoji())
                .description(request.getDescription())
                .keywords(request.getKeywords() != null ? request.getKeywords() : List.of())
                .officialSite(request.getOfficialSite())
                .imageUrl(request.getImageUrl())
                .build();

        return RobotModelResponse.from(robotModelRepository.save(model));
    }

    @Transactional
    public RobotModelResponse updateModel(String slug, AdminModelRequest request) {
        RobotModel model = robotModelRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 기종입니다: " + slug));

        if (!model.getSlug().equals(request.getSlug()) && robotModelRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException("이미 존재하는 slug입니다: " + request.getSlug());
        }

        model.setSlug(request.getSlug());
        model.setName(request.getName());
        model.setMaker(request.getMaker());
        model.setPrice(request.getPrice());
        model.setEmoji(request.getEmoji());
        model.setDescription(request.getDescription());
        if (request.getKeywords() != null) model.setKeywords(request.getKeywords());
        model.setOfficialSite(request.getOfficialSite());
        model.setImageUrl(request.getImageUrl());

        return RobotModelResponse.from(model);
    }

    @Transactional
    public void deleteModel(String slug) {
        RobotModel model = robotModelRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 기종입니다: " + slug));

        long postCount = postRepository.countByRobotModelId(model.getId());
        long robotCount = userRobotRepository.countByRobotModelId(model.getId());

        if (postCount > 0 || robotCount > 0) {
            throw new ConflictException(
                    "연결된 게시글 " + postCount + "개, 등록된 반려로봇 " + robotCount + "개가 있습니다");
        }

        robotModelRepository.delete(model);
    }

    // ── 뉴스/소식 관리 ─────────────────────────────────────

    public Page<NewsSummaryResponse> getNews(String keyword, NewsType type, String robotModelSlug, Boolean published, int page, int size) {
        return newsService.getAllNews(keyword, type, robotModelSlug, published, page, size);
    }

    public NewsDetailResponse getNewsById(String id) {
        return newsService.getById(id);
    }

    @Transactional
    public NewsDetailResponse createNews(NewsRequest request) {
        return newsService.create(request);
    }

    @Transactional
    public NewsDetailResponse updateNews(String id, NewsRequest request) {
        return newsService.updateById(id, request);
    }

    @Transactional
    public void deleteNews(String id) {
        newsService.deleteById(id);
    }

    @Transactional
    public NewsDetailResponse pinNews(String id, boolean isPinned) {
        return newsService.setPinById(id, isPinned);
    }

    // ── 헬퍼 ──────────────────────────────────────────────

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));
    }

    private Post findPost(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));
    }
}
