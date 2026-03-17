package kr.robotmate.server.user;

import kr.robotmate.server.auth.dto.UserResponse;
import kr.robotmate.server.comment.dto.MyCommentResponse;
import kr.robotmate.server.common.ApiResponse;
import kr.robotmate.server.common.PageResponse;
import kr.robotmate.server.common.SecurityUtil;
import kr.robotmate.server.post.dto.PostSummaryResponse;
import kr.robotmate.server.user.dto.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "마이페이지", description = "내 정보 조회/수정, 내 게시글/북마크 목록")
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 유저의 프로필 정보를 반환합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getMe() {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.getMe(userId)));
    }

    @Operation(summary = "내 정보 수정", description = "닉네임 또는 프로필 이미지를 변경합니다. 보내지 않은 필드는 변경되지 않습니다. 닉네임은 2~20자.")
    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @Valid @RequestBody UpdateUserRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.updateMe(userId, request)));
    }

    @Operation(summary = "내 게시글 목록", description = "내가 작성한 게시글 목록을 최신순으로 반환합니다.")
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.from(userService.getMyPosts(userId, page, size))));
    }

    @Operation(summary = "내 북마크 목록", description = "내가 북마크한 게시글 목록을 반환합니다.")
    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getMyBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.from(userService.getMyBookmarks(userId, page, size))));
    }

    @Operation(summary = "내 댓글 목록", description = "내가 작성한 댓글/대댓글 목록을 최신순으로 반환합니다. isReply=true이면 대댓글입니다.")
    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<PageResponse<MyCommentResponse>>> getMyComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.from(userService.getMyComments(userId, page, size))));
    }
}
