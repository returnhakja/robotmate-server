package kr.robotmate.server.robot;

import kr.robotmate.server.common.ApiResponse;
import kr.robotmate.server.robot.dto.RobotModelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "로봇 모델", description = "지원하는 반려 로봇 모델 목록 조회")
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class RobotModelController {

    private final RobotModelService robotModelService;

    @Operation(summary = "로봇 모델 전체 목록", description = "등록된 반려 로봇 모델 목록을 반환합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RobotModelResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(robotModelService.getAll()));
    }

    @Operation(summary = "로봇 모델 단건 조회", description = "slug로 특정 로봇 모델 정보를 조회합니다.")
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<RobotModelResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(robotModelService.getBySlug(slug)));
    }
}
