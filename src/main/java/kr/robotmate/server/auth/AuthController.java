package kr.robotmate.server.auth;

import kr.robotmate.server.auth.dto.AuthResponse;
import kr.robotmate.server.auth.dto.GoogleLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.googleLogin(request.getIdToken());
        return ResponseEntity.ok(response);
    }
}
