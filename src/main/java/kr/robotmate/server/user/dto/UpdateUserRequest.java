package kr.robotmate.server.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nickname;
    private String profileImage;
}
