package kr.robotmate.server.upload;

import kr.robotmate.server.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Tag(name = "이미지 업로드", description = "Cloudflare R2에 이미지를 업로드하고 URL을 반환")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @Operation(summary = "게시글 이미지 업로드", description = "이미지 파일을 업로드하고 URL을 반환합니다. 반환된 url을 게시글 작성 시 사용하세요. (최대 20MB)")
    @PostMapping("/post")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadPostImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = uploadService.upload(file, "posts");
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
    }

    @Operation(summary = "프로필 이미지 업로드", description = "프로필 이미지를 업로드하고 URL을 반환합니다. 반환된 url을 PUT /api/users/me의 profileImage 필드에 사용하세요. (최대 20MB)")
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = uploadService.upload(file, "profiles");
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
    }
}
