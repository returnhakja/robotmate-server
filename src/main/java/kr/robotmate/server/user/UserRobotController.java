package kr.robotmate.server.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.robotmate.server.common.ApiResponse;
import kr.robotmate.server.common.PageResponse;
import kr.robotmate.server.common.SecurityUtil;
import kr.robotmate.server.post.dto.PostSummaryResponse;
import kr.robotmate.server.user.dto.UserRobotRequest;
import kr.robotmate.server.user.dto.UserRobotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "반려로봇", description = "내 반려로봇 등록/수정/삭제, 타인 반려로봇 조회")
@RestController
@RequiredArgsConstructor
public class UserRobotController {

    private final UserRobotService userRobotService;

    @Operation(summary = "내 반려로봇 목록", description = "비공개 포함 내 반려로봇 전체 목록을 반환합니다.")
    @GetMapping("/api/users/me/robots")
    public ResponseEntity<ApiResponse<List<UserRobotResponse>>> getMyRobots() {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userRobotService.getMyRobots(userId)));
    }

    @Operation(summary = "반려로봇 등록", description = "새 반려로봇을 등록합니다.")
    @PostMapping("/api/users/me/robots")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<UserRobotResponse>> createRobot(
            @Valid @RequestBody UserRobotRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(userRobotService.createRobot(userId, request)));
    }

    @Operation(summary = "반려로봇 수정", description = "반려로봇 정보를 수정합니다.")
    @PutMapping("/api/users/me/robots/{id}")
    public ResponseEntity<ApiResponse<UserRobotResponse>> updateRobot(
            @PathVariable String id,
            @Valid @RequestBody UserRobotRequest request) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userRobotService.updateRobot(userId, id, request)));
    }

    @Operation(summary = "반려로봇 삭제", description = "반려로봇을 삭제합니다.")
    @DeleteMapping("/api/users/me/robots/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRobot(@PathVariable String id) {
        String userId = SecurityUtil.getCurrentUserId();
        userRobotService.deleteRobot(userId, id);
    }

    @Operation(summary = "반려로봇 게시글 목록", description = "특정 반려로봇이 태그된 게시글 목록을 반환합니다.")
    @GetMapping("/api/users/me/robots/{id}/posts")
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> getRobotPosts(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        String userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.from(userRobotService.getRobotPosts(userId, id, page, size))));
    }

    @Operation(summary = "타인 반려로봇 목록", description = "대상 유저의 공개(PUBLIC) 반려로봇 목록을 반환합니다.")
    @GetMapping("/api/users/{userId}/robots")
    public ResponseEntity<ApiResponse<List<UserRobotResponse>>> getPublicRobots(
            @PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok(userRobotService.getPublicRobots(userId)));
    }
}
