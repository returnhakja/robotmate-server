package kr.robotmate.server.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.robotmate.server.admin.dto.*;
import kr.robotmate.server.post.PostType;
import kr.robotmate.server.post.PostVisibility;
import kr.robotmate.server.post.dto.UpdatePostRequest;
import kr.robotmate.server.robot.dto.RobotModelResponse;
import kr.robotmate.server.user.Role;
import kr.robotmate.server.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "어드민 전용 API — role=ADMIN 필수. 미인증 시 403 반환.")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ── 대시보드 통계 ──────────────────────────────────────

    @Operation(
            summary = "대시보드 통계 조회",
            description = "전체 회원 수, 게시글 수, 마켓 게시글 수, 오늘 신규 유저/게시글 등 대시보드에 표시할 집계 데이터를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음", content = @Content)
    })
    @GetMapping("/stats")
    public kr.robotmate.server.common.ApiResponse<AdminStatsResponse> getStats() {
        return kr.robotmate.server.common.ApiResponse.ok(adminService.getStats());
    }

    // ── 회원 관리 ──────────────────────────────────────────

    @Operation(
            summary = "회원 목록 조회",
            description = "전체 회원 목록을 페이지네이션으로 반환합니다. keyword(이메일/닉네임), role, status 필터를 조합할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음", content = @Content)
    })
    @GetMapping("/users")
    public kr.robotmate.server.common.ApiResponse<Page<AdminUserResponse>> getUsers(
            @Parameter(description = "이메일 또는 닉네임 검색어") @RequestParam(required = false) String keyword,
            @Parameter(description = "역할 필터 (USER | ADMIN)") @RequestParam(required = false) Role role,
            @Parameter(description = "상태 필터 (ACTIVE | SUSPENDED | DELETED)") @RequestParam(required = false) UserStatus status,
            @Parameter(description = "페이지 번호 (1부터 시작)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수") @RequestParam(defaultValue = "20") int size) {
        return kr.robotmate.server.common.ApiResponse.ok(adminService.getUsers(keyword, role, status, page, size));
    }

    @Operation(
            summary = "회원 역할 변경",
            description = "특정 회원의 role을 USER 또는 ADMIN으로 변경합니다. 변경 즉시 DB에 반영되며, 기존 발급된 JWT는 만료 전까지 이전 role로 동작합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "역할 변경 성공"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원", content = @Content)
    })
    @PatchMapping("/users/{id}/role")
    public kr.robotmate.server.common.ApiResponse<AdminUserResponse> changeUserRole(
            @Parameter(description = "변경할 회원 ID") @PathVariable String id,
            @Valid @RequestBody ChangeRoleRequest request) {
        return kr.robotmate.server.common.ApiResponse.ok(adminService.changeUserRole(id, request.getRole()));
    }

    @Operation(
            summary = "회원 상태 변경",
            description = "회원 상태를 ACTIVE / SUSPENDED / DELETED로 변경합니다. " +
                    "SUSPENDED로 변경 시 suspendReason을 함께 전달하면 정지 사유가 저장됩니다. " +
                    "정지된 유저는 로그인 및 토큰 갱신 시 403(USER_SUSPENDED)이 반환됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원", content = @Content)
    })
    @PatchMapping("/users/{id}/status")
    public kr.robotmate.server.common.ApiResponse<AdminUserResponse> changeUserStatus(
            @Parameter(description = "변경할 회원 ID") @PathVariable String id,
            @Valid @RequestBody ChangeStatusRequest request) {
        return kr.robotmate.server.common.ApiResponse.ok(adminService.changeUserStatus(id, request.getStatus(), request.getSuspendReason()));
    }

    @Operation(
            summary = "회원 강제 탈퇴 (익명화)",
            description = "회원을 강제 탈퇴 처리합니다. 계정 삭제가 아닌 익명화로, " +
                    "이메일/닉네임/프로필이미지/googleId를 무작위 값으로 대체하고 status를 DELETED로 변경합니다. " +
                    "작성한 게시글과 댓글은 유지됩니다. 발급된 리프레시 토큰은 즉시 무효화됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 처리 성공"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원", content = @Content)
    })
    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @Parameter(description = "탈퇴 처리할 회원 ID") @PathVariable String id) {
        adminService.deleteUser(id);
    }

    // ── 게시글 관리 ────────────────────────────────────────

    @Operation(
            summary = "게시글 목록 조회 (어드민)",
            description = "전체 게시글을 페이지네이션으로 반환합니다. " +
                    "keyword(제목 검색), type(게시글 분류), authorId(특정 유저 게시글), visibility(공개/비공개) 필터를 조합할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음", content = @Content)
    })
    @GetMapping("/posts")
    public kr.robotmate.server.common.ApiResponse<Page<AdminPostSummaryResponse>> getPosts(
            @Parameter(description = "제목 검색어") @RequestParam(required = false) String keyword,
            @Parameter(description = "게시글 타입 필터 (SHOW | REVIEW | QUESTION | CONCEPT | SALE)") @RequestParam(required = false) PostType type,
            @Parameter(description = "특정 작성자 ID로 필터") @RequestParam(required = false) String authorId,
            @Parameter(description = "공개 범위 필터 (PUBLIC | PRIVATE)") @RequestParam(required = false) PostVisibility visibility,
            @Parameter(description = "페이지 번호 (1부터 시작)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수") @RequestParam(defaultValue = "20") int size) {
        return kr.robotmate.server.common.ApiResponse.ok(adminService.getPosts(keyword, type, authorId, visibility, page, size));
    }

    @Operation(
            summary = "게시글 수정 (어드민)",
            description = "어드민 권한으로 임의의 게시글을 수정합니다. 작성자 본인 여부를 확인하지 않습니다. " +
                    "null인 필드는 무시되며, 입력한 필드만 업데이트됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글", content = @Content)
    })
    @PutMapping("/posts/{id}")
    public kr.robotmate.server.common.ApiResponse<Void> updatePost(
            @Parameter(description = "수정할 게시글 ID") @PathVariable String id,
            @RequestBody UpdatePostRequest request) {
        adminService.updatePost(id, request);
        return kr.robotmate.server.common.ApiResponse.ok();
    }

    @Operation(
            summary = "게시글 삭제 (어드민)",
            description = "어드민 권한으로 임의의 게시글을 삭제합니다. 작성자 본인 여부를 확인하지 않습니다. " +
                    "연관된 댓글, 좋아요, 북마크도 함께 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글", content = @Content)
    })
    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @Parameter(description = "삭제할 게시글 ID") @PathVariable String id) {
        adminService.deletePost(id);
    }

    // ── 로봇 모델 관리 ─────────────────────────────────────

    @Operation(
            summary = "로봇 모델 추가",
            description = "새로운 로봇 기종을 DB에 등록합니다. slug는 고유값이며 URL에 사용됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 slug", content = @Content)
    })
    @PostMapping("/models")
    @ResponseStatus(HttpStatus.CREATED)
    public kr.robotmate.server.common.ApiResponse<RobotModelResponse> createModel(
            @Valid @RequestBody AdminModelRequest request) {
        return kr.robotmate.server.common.ApiResponse.ok(adminService.createModel(request));
    }

    @Operation(
            summary = "로봇 모델 수정",
            description = "기존 로봇 기종 정보를 수정합니다. slug 변경도 가능하며, " +
                    "변경하려는 slug가 이미 다른 기종에 사용 중이면 409를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 기종", content = @Content),
            @ApiResponse(responseCode = "409", description = "slug 중복", content = @Content)
    })
    @PutMapping("/models/{slug}")
    public kr.robotmate.server.common.ApiResponse<RobotModelResponse> updateModel(
            @Parameter(description = "수정할 기종의 현재 slug") @PathVariable String slug,
            @Valid @RequestBody AdminModelRequest request) {
        return kr.robotmate.server.common.ApiResponse.ok(adminService.updateModel(slug, request));
    }

    @Operation(
            summary = "로봇 모델 삭제",
            description = "로봇 기종을 DB에서 삭제합니다. " +
                    "해당 기종으로 작성된 게시글이나 사용자가 등록한 반려로봇이 1개 이상이면 409를 반환합니다. " +
                    "응답 메시지 예: '연결된 게시글 3개, 등록된 반려로봇 2개가 있습니다'"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "ADMIN 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 기종", content = @Content),
            @ApiResponse(responseCode = "409", description = "연결된 데이터가 있어 삭제 불가", content = @Content)
    })
    @DeleteMapping("/models/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModel(
            @Parameter(description = "삭제할 기종의 slug") @PathVariable String slug) {
        adminService.deleteModel(slug);
    }
}
