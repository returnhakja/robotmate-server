package kr.robotmate.server.auth;

import kr.robotmate.server.auth.dto.AuthResponse;
import kr.robotmate.server.auth.dto.GoogleTokenInfo;
import kr.robotmate.server.auth.dto.UserResponse;
import kr.robotmate.server.user.User;
import kr.robotmate.server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RestTemplate restTemplate;

    @Transactional
    public AuthResponse googleLogin(String idToken) {
        log.info("[Auth] Google 로그인 요청 수신");
        GoogleTokenInfo tokenInfo = verifyGoogleToken(idToken);
        log.info("[Auth] Google 토큰 검증 성공 - email={}, sub={}", tokenInfo.getEmail(), tokenInfo.getSub());

        User user = userRepository.findByGoogleId(tokenInfo.getSub())
                .orElseGet(() -> userRepository.findByEmail(tokenInfo.getEmail())
                        .map(existing -> {
                            log.info("[Auth] 기존 유저에 Google 계정 연결 - userId={}", existing.getId());
                            existing.setGoogleId(tokenInfo.getSub());
                            if (existing.getProfileImage() == null) {
                                existing.setProfileImage(tokenInfo.getPicture());
                            }
                            return userRepository.save(existing);
                        })
                        .orElseGet(() -> createUser(tokenInfo)));

        log.info("[Auth] 유저 확인 완료 - userId={}, nickname={}", user.getId(), user.getNickname());

        String accessToken = jwtProvider.generateToken(user.getId());
        String refreshTokenValue = issueRefreshToken(user.getId());
        log.info("[Auth] 토큰 발급 완료 - userId={}", user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .user(UserResponse.from(user))
                .build();
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        String userId = refreshToken.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 기존 refresh token 교체 (Refresh Token Rotation)
        refreshTokenRepository.delete(refreshToken);
        String newRefreshTokenValue = issueRefreshToken(userId);
        String newAccessToken = jwtProvider.generateToken(userId);

        log.info("[Auth] 토큰 갱신 완료 - userId={}", userId);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .user(UserResponse.from(user))
                .build();
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    log.info("[Auth] 로그아웃 - userId={}", token.getUserId());
                    refreshTokenRepository.delete(token);
                });
    }

    private String issueRefreshToken(String userId) {
        String tokenValue = jwtProvider.generateRefreshTokenValue();
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(tokenValue)
                .expiresAt(jwtProvider.getRefreshTokenExpiresAt())
                .build();
        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    private GoogleTokenInfo verifyGoogleToken(String idToken) {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        log.debug("[Auth] Google tokeninfo 요청: {}", url);
        try {
            GoogleTokenInfo info = restTemplate.getForObject(url, GoogleTokenInfo.class);
            if (info == null || info.getSub() == null) {
                throw new RuntimeException("Invalid Google token");
            }
            if (!googleClientId.equals(info.getAud())) {
                log.warn("[Auth] Token audience 불일치 - expected={}, actual={}", googleClientId, info.getAud());
                throw new RuntimeException("Token audience mismatch");
            }
            return info;
        } catch (Exception e) {
            log.error("[Auth] Google 토큰 검증 실패: {}", e.getMessage());
            throw new RuntimeException("Google token verification failed: " + e.getMessage());
        }
    }

    private User createUser(GoogleTokenInfo tokenInfo) {
        String baseNickname = tokenInfo.getName() != null
                ? tokenInfo.getName().replaceAll("\\s+", "") : "user";
        String nickname = baseNickname;
        int suffix = 1;
        while (userRepository.existsByNickname(nickname)) {
            nickname = baseNickname + suffix++;
        }

        User user = User.builder()
                .email(tokenInfo.getEmail())
                .googleId(tokenInfo.getSub())
                .nickname(nickname)
                .profileImage(tokenInfo.getPicture())
                .build();
        log.info("[Auth] 신규 유저 생성 - email={}, nickname={}", tokenInfo.getEmail(), nickname);
        return userRepository.save(user);
    }
}
