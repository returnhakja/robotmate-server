package kr.robotmate.server.auth;

import kr.robotmate.server.auth.dto.AuthResponse;
import kr.robotmate.server.auth.dto.GoogleLoginRequest;
import kr.robotmate.server.auth.dto.UserResponse;
import kr.robotmate.server.user.User;
import kr.robotmate.server.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "Google 로그인 및 토큰 검증")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Operation(summary = "Google 로그인", description = "Google idToken을 검증하고 JWT를 발급합니다. 신규 유저는 자동 가입됩니다.")
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.googleLogin(request.getIdToken());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "토큰 검증", description = "JWT가 유효하면 유저 정보를 반환합니다. 무효하면 401.")
    @GetMapping("/verify")
    public ResponseEntity<UserResponse> verify(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findById(userDetails.getUsername())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
