package kr.robotmate.server.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRobotRequest {

    @NotBlank(message = "반려로봇 이름은 필수입니다.")
    private String nickname;

    @NotNull(message = "기종은 필수입니다.")
    private String robotModelId;

    private LocalDate startDate;
    private String profileImage;
    private boolean isPublic = true;
}
