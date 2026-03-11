package kr.robotmate.server.robot;

import kr.robotmate.server.common.ApiResponse;
import kr.robotmate.server.robot.dto.RobotModelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class RobotModelController {

    private final RobotModelService robotModelService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RobotModelResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(robotModelService.getAll()));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<RobotModelResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(robotModelService.getBySlug(slug)));
    }
}
