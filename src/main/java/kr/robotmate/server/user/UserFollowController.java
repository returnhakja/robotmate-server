package kr.robotmate.server.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.robotmate.server.common.ApiResponse;
import kr.robotmate.server.common.SecurityUtil;
import kr.robotmate.server.user.dto.FollowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "팔로우", description = "유저 팔로우/언팔로우")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserService userService;

    @Operation(summary = "팔로우/언팔로우 토글", description = "대상 유저를 팔로우하거나 언팔로우합니다. 로그인 필요. 응답의 following 필드로 현재 상태를 확인하세요.")
    @PostMapping("/{id}/follow")
    public ResponseEntity<ApiResponse<FollowResponse>> toggleFollow(@PathVariable String id) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.toggleFollow(currentUserId, id)));
    }
}
