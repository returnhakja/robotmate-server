package kr.robotmate.server.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    private String nickname;
    private String profileImage;
    @Size(max = 100, message = "한 줄 소개는 100자 이하여야 합니다.")
    private String bio;
}
