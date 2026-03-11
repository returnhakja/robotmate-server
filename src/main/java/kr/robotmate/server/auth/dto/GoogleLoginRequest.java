package kr.robotmate.server.auth.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String idToken;
}
