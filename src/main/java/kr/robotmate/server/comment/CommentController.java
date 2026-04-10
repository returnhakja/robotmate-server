package kr.robotmate.server.comment;

import jakarta.validation.Valid;
import kr.robotmate.server.comment.dto.CommentLikeResponse;
import kr.robotmate.server.comment.dto.CommentResponse;
import kr.robotmate.server.comment.dto.CreateCommentRequest;
import kr.robotmate.server.common.ApiResponse;
import kr.robotmate.server.common.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "댓글", description = "게시글 댓글 조회, 작성, 대댓글, 삭제")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 반환합니다. 대댓글은 replies 필드에 포함됩니다. 로그인 시 liked 필드가 정확하게 반환됩니다.")
    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable String postId) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(commentService.getComments(postId, userId)));
    }

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다. 로그인 필요.")
    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable String postId,
            @Valid @RequestBody CreateCommentRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        CommentResponse response = commentService.createComment(postId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "대댓글 작성", description = "댓글에 대댓글을 작성합니다. 로그인 필요.")
    @PostMapping("/api/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<CommentResponse>> createReply(
            @PathVariable String commentId,
            @Valid @RequestBody CreateCommentRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        CommentResponse response = commentService.createReply(commentId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다. 로그인 필요.")
    @PutMapping("/api/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable String commentId,
            @Valid @RequestBody CreateCommentRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(commentService.updateComment(commentId, request, userId)));
    }

    @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다. 로그인 필요.")
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable String commentId) {
        String userId = SecurityUtil.getCurrentUserId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "댓글 좋아요 토글", description = "댓글 좋아요를 누르거나 취소합니다. 응답의 liked 필드로 현재 상태를 확인하세요. 로그인 필요.")
    @PostMapping("/api/comments/{commentId}/like")
    public ResponseEntity<ApiResponse<CommentLikeResponse>> toggleCommentLike(@PathVariable String commentId) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(commentService.toggleCommentLike(commentId, userId)));
    }
}
