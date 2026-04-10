package kr.robotmate.server.news.dto;

import kr.robotmate.server.robot.RobotModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RobotModelRef {
    private String slug;
    private String name;
    private String emoji;

    public static RobotModelRef from(RobotModel model) {
        if (model == null) return null;
        return RobotModelRef.builder()
                .slug(model.getSlug())
                .name(model.getName())
                .emoji(model.getEmoji())
                .build();
    }
}
