package kr.robotmate.server.admin.dto;

import kr.robotmate.server.user.User;
import kr.robotmate.server.user.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {
    private String id;
    private String email;
    private String nickname;
    private String profileImage;
    private String role;
    private String status;
    private String suspendReason;
    private LocalDateTime createdAt;

    public static AdminUserResponse from(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .status(user.getStatus() != null ? user.getStatus().name() : UserStatus.ACTIVE.name())
                .suspendReason(user.getSuspendReason())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
