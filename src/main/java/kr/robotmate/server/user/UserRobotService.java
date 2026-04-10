package kr.robotmate.server.user;

import kr.robotmate.server.common.exception.ForbiddenException;
import kr.robotmate.server.common.exception.NotFoundException;
import kr.robotmate.server.post.Post;
import kr.robotmate.server.post.PostRepository;
import kr.robotmate.server.post.dto.PostSummaryResponse;
import kr.robotmate.server.post.LikeRepository;
import kr.robotmate.server.comment.CommentRepository;
import kr.robotmate.server.robot.RobotModel;
import kr.robotmate.server.robot.RobotModelRepository;
import kr.robotmate.server.user.dto.UserRobotRequest;
import kr.robotmate.server.user.dto.UserRobotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRobotService {

    private final UserRepository userRepository;
    private final UserRobotRepository userRobotRepository;
    private final RobotModelRepository robotModelRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public List<UserRobotResponse> getMyRobots(String userId) {
        return userRobotRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(UserRobotResponse::from).toList();
    }

    public List<UserRobotResponse> getPublicRobots(String targetUserId) {
        findUser(targetUserId);
        return userRobotRepository.findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(targetUserId)
                .stream().map(UserRobotResponse::from).toList();
    }

    @Transactional
    public UserRobotResponse createRobot(String userId, UserRobotRequest request) {
        User user = findUser(userId);
        RobotModel robotModel = robotModelRepository.findById(request.getRobotModelId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 기종입니다."));

        UserRobot robot = UserRobot.builder()
                .user(user)
                .robotModel(robotModel)
                .nickname(request.getNickname())
                .startDate(request.getStartDate())
                .profileImage(request.getProfileImage())
                .isPublic(request.isPublic())
                .build();

        return UserRobotResponse.from(userRobotRepository.save(robot));
    }

    @Transactional
    public UserRobotResponse updateRobot(String userId, String robotId, UserRobotRequest request) {
        UserRobot robot = findOwnedRobot(userId, robotId);

        robot.setNickname(request.getNickname());

        RobotModel robotModel = robotModelRepository.findById(request.getRobotModelId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 기종입니다."));
        robot.setRobotModel(robotModel);

        if (request.getStartDate() != null) robot.setStartDate(request.getStartDate());
        if (request.getProfileImage() != null) robot.setProfileImage(request.getProfileImage());
        robot.setPublic(request.isPublic());

        return UserRobotResponse.from(robot);
    }

    @Transactional
    public void deleteRobot(String userId, String robotId) {
        UserRobot robot = findOwnedRobot(userId, robotId);
        userRobotRepository.delete(robot);
    }

    public Page<PostSummaryResponse> getRobotPosts(String userId, String robotId, int page, int size) {
        findOwnedRobot(userId, robotId);
        Page<Post> posts = postRepository.findByUserRobotIdOrderByCreatedAtDesc(
                robotId, PageRequest.of(page - 1, size));
        return toSummaryPage(posts);
    }

    private Page<PostSummaryResponse> toSummaryPage(Page<Post> posts) {
        List<String> postIds = posts.map(Post::getId).toList();
        Map<String, Long> likeCounts = likeRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));
        Map<String, Long> commentCounts = commentRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));
        return posts.map(p -> PostSummaryResponse.from(
                p, likeCounts.getOrDefault(p.getId(), 0L), commentCounts.getOrDefault(p.getId(), 0L)));
    }

    private UserRobot findOwnedRobot(String userId, String robotId) {
        return userRobotRepository.findByIdAndUserId(robotId, userId)
                .orElseThrow(() -> new NotFoundException("반려로봇을 찾을 수 없습니다."));
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }
}
