package kr.robotmate.server.upload;

import kr.robotmate.server.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    // 게시글 이미지
    @PostMapping("/post")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadPostImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = uploadService.upload(file, "posts");
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
    }

    // 프로필 이미지
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = uploadService.upload(file, "profiles");
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
    }
}
