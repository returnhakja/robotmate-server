package kr.robotmate.server.post.dto;

import kr.robotmate.server.user.UserRobot;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRobotInfo {
    private String id;
    private String nickname;
    private String profileImage;

    public static UserRobotInfo from(UserRobot userRobot) {
        return UserRobotInfo.builder()
                .id(userRobot.getId())
                .nickname(userRobot.getNickname())
                .profileImage(userRobot.getProfileImage())
                .build();
    }
}
