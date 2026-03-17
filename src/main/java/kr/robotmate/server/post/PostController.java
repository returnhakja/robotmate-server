package kr.robotmate.server.post;

import jakarta.validation.Valid;
import kr.robotmate.server.common.ApiResponse;
import kr.robotmate.server.common.PageResponse;
import kr.robotmate.server.common.SecurityUtil;
import kr.robotmate.server.post.dto.BookmarkResponse;
import kr.robotmate.server.post.dto.CreatePostRequest;
import kr.robotmate.server.post.dto.LikeResponse;
import kr.robotmate.server.post.dto.PostDetailResponse;
import kr.robotmate.server.post.dto.PostSummaryResponse;
import kr.robotmate.server.post.dto.UpdatePostRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시글", description = "게시글 목록 조회, 작성, 수정, 삭제 및 좋아요/북마크")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "type(SHOW/REVIEW/QUESTION/CONCEPT/SALE), model(slug), tag, sort(latest/popular), page, size 필터 지원")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getPosts(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        PostType postType = (type == null || type.equalsIgnoreCase("all")) ? null : PostType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.from(postService.getPosts(postType, model, tag, sort, page, size))));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 상세 내용을 조회합니다. 로그인 시 좋아요/북마크 여부도 반환됩니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable String id) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(postService.getPost(id, userId)));
    }

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다. 로그인 필요.")
    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        PostDetailResponse response = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "게시글 수정", description = "본인이 작성한 게시글을 수정합니다. 로그인 필요.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable String id,
            @Valid @RequestBody UpdatePostRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(postService.updatePost(id, request, userId)));
    }

    @Operation(summary = "게시글 삭제", description = "본인이 작성한 게시글을 삭제합니다. 로그인 필요.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable String id) {
        String userId = SecurityUtil.getCurrentUserId();
        postService.deletePost(id, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @Operation(summary = "좋아요 토글", description = "좋아요를 누르거나 취소합니다. 응답의 liked 필드로 현재 상태를 확인하세요.")
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<LikeResponse>> toggleLike(@PathVariable String id) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(postService.toggleLike(id, userId)));
    }

    @Operation(summary = "북마크 토글", description = "북마크를 추가하거나 해제합니다. 응답의 bookmarked 필드로 현재 상태를 확인하세요.")
    @PostMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<BookmarkResponse>> toggleBookmark(@PathVariable String id) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(postService.toggleBookmark(id, userId)));
    }
}
