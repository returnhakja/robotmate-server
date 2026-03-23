package kr.robotmate.server.admin.dto;

import jakarta.validation.constraints.NotNull;
import kr.robotmate.server.user.UserStatus;
import lombok.Data;

@Data
public class ChangeStatusRequest {
    @NotNull
    private UserStatus status;
    private String suspendReason;
}
