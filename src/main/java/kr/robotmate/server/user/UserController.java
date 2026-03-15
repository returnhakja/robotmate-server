package kr.robotmate.server.user;

import kr.robotmate.server.auth.dto.UserResponse;
import kr.robotmate.server.common.ApiResponse;
import kr.robotmate.server.common.PageResponse;
import kr.robotmate.server.common.SecurityUtil;
import kr.robotmate.server.post.dto.PostSummaryResponse;
import kr.robotmate.server.user.dto.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getMe() {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.getMe(userId)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(
            @RequestBody UpdateUserRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.updateMe(userId, request)));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.from(userService.getMyPosts(userId, page, size))));
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getMyBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.from(userService.getMyBookmarks(userId, page, size))));
    }
}
