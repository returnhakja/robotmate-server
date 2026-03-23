package kr.robotmate.server.admin.dto;

import jakarta.validation.constraints.NotNull;
import kr.robotmate.server.user.Role;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    @NotNull
    private Role role;
}
