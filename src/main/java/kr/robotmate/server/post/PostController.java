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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(@PathVariable String id) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(postService.getPost(id, userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        PostDetailResponse response = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(
            @PathVariable String id,
            @Valid @RequestBody UpdatePostRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(postService.updatePost(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable String id) {
        String userId = SecurityUtil.getCurrentUserId();
        postService.deletePost(id, userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<LikeResponse>> toggleLike(@PathVariable String id) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(postService.toggleLike(id, userId)));
    }

    @PostMapping("/{id}/bookmark")
    public ResponseEntity<ApiResponse<BookmarkResponse>> toggleBookmark(@PathVariable String id) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(postService.toggleBookmark(id, userId)));
    }
}
