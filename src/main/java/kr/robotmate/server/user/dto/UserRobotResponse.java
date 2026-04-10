package kr.robotmate.server.user.dto;

import kr.robotmate.server.user.UserRobot;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserRobotResponse {

    private String id;
    private String nickname;
    private String robotModelId;
    private String robotModelName;
    private String robotModelSlug;
    private String robotModelEmoji;
    private LocalDate startDate;
    private String profileImage;
    private boolean isPublic;
    private LocalDateTime createdAt;

    public static UserRobotResponse from(UserRobot ur) {
        return UserRobotResponse.builder()
                .id(ur.getId())
                .nickname(ur.getNickname())
                .robotModelId(ur.getRobotModel() != null ? ur.getRobotModel().getId() : null)
                .robotModelName(ur.getRobotModel() != null ? ur.getRobotModel().getName() : null)
                .robotModelSlug(ur.getRobotModel() != null ? ur.getRobotModel().getSlug() : null)
                .robotModelEmoji(ur.getRobotModel() != null ? ur.getRobotModel().getEmoji() : null)
                .startDate(ur.getStartDate())
                .profileImage(ur.getProfileImage())
                .isPublic(ur.isPublic())
                .createdAt(ur.getCreatedAt())
                .build();
    }
}
