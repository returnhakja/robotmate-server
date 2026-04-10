package kr.robotmate.server.comment;

import kr.robotmate.server.comment.dto.CommentLikeResponse;
import kr.robotmate.server.comment.dto.CommentResponse;
import kr.robotmate.server.comment.dto.CreateCommentRequest;
import kr.robotmate.server.common.exception.ForbiddenException;
import kr.robotmate.server.common.exception.NotFoundException;
import kr.robotmate.server.post.Post;
import kr.robotmate.server.post.PostRepository;
import kr.robotmate.server.user.User;
import kr.robotmate.server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<CommentResponse> getComments(String postId, String currentUserId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("존재하지 않는 게시글입니다.");
        }
        List<Comment> topLevel = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId);

        // 모든 댓글 id (최상위 + 대댓글) 수집
        List<String> allIds = topLevel.stream()
                .flatMap(c -> {
                    java.util.stream.Stream<String> self = java.util.stream.Stream.of(c.getId());
                    java.util.stream.Stream<String> replies = c.getReplies().stream().map(Comment::getId);
                    return java.util.stream.Stream.concat(self, replies);
                }).toList();

        Map<String, Long> likeCounts = allIds.isEmpty() ? Map.of()
                : commentLikeRepository.countsByCommentIds(allIds).stream()
                        .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));

        Set<String> likedIds = (currentUserId != null && !allIds.isEmpty())
                ? Set.copyOf(commentLikeRepository.findLikedCommentIds(currentUserId, allIds))
                : Set.of();

        return topLevel.stream()
                .map(c -> CommentResponse.from(
                        c,
                        likeCounts.getOrDefault(c.getId(), 0L),
                        likedIds.contains(c.getId()),
                        likeCounts,
                        likedIds))
                .toList();
    }

    @Transactional
    public CommentResponse createComment(String postId, CreateCommentRequest request, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글입니다."));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .post(post)
                .build();

        return toResponse(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse createReply(String commentId, CreateCommentRequest request, String userId) {
        Comment parent = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));
        if (parent.getParent() != null) {
            throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다.");
        }
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        Comment reply = Comment.builder()
                .content(request.getContent())
                .author(author)
                .post(parent.getPost())
                .parent(parent)
                .build();

        return toResponse(commentRepository.save(reply));
    }

    @Transactional
    public CommentLikeResponse toggleCommentLike(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        commentLikeRepository.findByUserIdAndCommentId(userId, commentId).ifPresentOrElse(
                commentLikeRepository::delete,
                () -> commentLikeRepository.save(CommentLike.builder().user(user).comment(comment).build())
        );

        long likeCount = commentLikeRepository.countByCommentId(commentId);
        boolean liked = commentLikeRepository.existsByUserIdAndCommentId(userId, commentId);
        return new CommentLikeResponse(liked, likeCount);
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.from(comment, 0L, false, Map.of(), Set.of());
    }

    @Transactional
    public CommentResponse updateComment(String commentId, CreateCommentRequest request, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("본인의 댓글만 수정할 수 있습니다.");
        }
        comment.setContent(request.getContent());
        return toResponse(comment);
    }

    @Transactional
    public void deleteComment(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("본인의 댓글만 삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);
    }
}
