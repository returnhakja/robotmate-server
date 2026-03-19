package kr.robotmate.server.user;

import kr.robotmate.server.auth.dto.UserResponse;
import kr.robotmate.server.comment.Comment;
import kr.robotmate.server.comment.CommentLikeRepository;
import kr.robotmate.server.comment.CommentRepository;
import kr.robotmate.server.comment.dto.MyCommentResponse;
import kr.robotmate.server.common.exception.ConflictException;
import kr.robotmate.server.common.exception.NotFoundException;
import kr.robotmate.server.post.Bookmark;
import kr.robotmate.server.post.BookmarkRepository;
import kr.robotmate.server.post.LikeRepository;
import kr.robotmate.server.post.Post;
import kr.robotmate.server.post.PostRepository;
import kr.robotmate.server.post.dto.PostSummaryResponse;
import kr.robotmate.server.user.dto.UpdateUserRequest;
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
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    public UserResponse getMe(String userId) {
        User user = findUser(userId);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMe(String userId, UpdateUserRequest request) {
        User user = findUser(userId);
        if (request.getNickname() != null) {
            if (userRepository.existsByNicknameAndIdNot(request.getNickname(), userId))
                throw new ConflictException("이미 사용 중인 닉네임입니다.");
            user.setNickname(request.getNickname());
        }
        if (request.getProfileImage() != null) user.setProfileImage(request.getProfileImage());
        return UserResponse.from(user);
    }

    public Page<PostSummaryResponse> getMyPosts(String userId, int page, int size) {
        Page<Post> posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size));
        return toSummaryPage(posts);
    }

    public Page<PostSummaryResponse> getMyBookmarks(String userId, int page, int size) {
        Page<Bookmark> bookmarks = bookmarkRepository.findByUserIdWithPost(
                userId, PageRequest.of(page, size));

        List<Post> posts = bookmarks.map(Bookmark::getPost).toList();
        List<String> postIds = posts.stream().map(Post::getId).toList();

        Map<String, Long> likeCounts = likeRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));
        Map<String, Long> commentCounts = commentRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));

        return bookmarks.map(b -> PostSummaryResponse.from(
                b.getPost(),
                likeCounts.getOrDefault(b.getPost().getId(), 0L),
                commentCounts.getOrDefault(b.getPost().getId(), 0L)
        ));
    }

    public Page<MyCommentResponse> getMyComments(String userId, int page, int size) {
        Page<Comment> comments = commentRepository.findByAuthorIdOrderByCreatedAtDesc(
                userId, PageRequest.of(page, size));
        List<String> commentIds = comments.map(Comment::getId).toList();
        Map<String, Long> likeCounts = commentIds.isEmpty() ? Map.of()
                : commentLikeRepository.countsByCommentIds(commentIds).stream()
                        .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));
        return comments.map(c -> MyCommentResponse.from(c, likeCounts.getOrDefault(c.getId(), 0L)));
    }

    private Page<PostSummaryResponse> toSummaryPage(Page<Post> posts) {
        List<String> postIds = posts.map(Post::getId).toList();
        Map<String, Long> likeCounts = likeRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));
        Map<String, Long> commentCounts = commentRepository.countsByPostIds(postIds).stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));

        return posts.map(post -> PostSummaryResponse.from(
                post,
                likeCounts.getOrDefault(post.getId(), 0L),
                commentCounts.getOrDefault(post.getId(), 0L)
        ));
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }
}
