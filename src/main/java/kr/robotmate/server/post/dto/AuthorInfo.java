package kr.robotmate.server.post.dto;

import kr.robotmate.server.user.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorInfo {
    private String id;
    private String nickname;
    private String profileImage;

    public static AuthorInfo from(User user) {
        return AuthorInfo.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .build();
    }
}
