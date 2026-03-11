package kr.robotmate.server.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleTokenInfo {
    private String sub;       // Google 유저 고유 ID
    private String email;
    private String name;
    private String picture;
    private String aud;       // client_id (검증용)

    @JsonProperty("email_verified")
    private String emailVerified;
}
