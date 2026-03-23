package kr.robotmate.server.robot.dto;

import kr.robotmate.server.robot.RobotModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RobotModelResponse {
    private String id;
    private String slug;
    private String name;
    private String maker;
    private String price;
    private String emoji;
    private String description;
    private String imageUrl;
    private String officialSite;
    private List<String> keywords;

    public static RobotModelResponse from(RobotModel model) {
        return RobotModelResponse.builder()
                .id(model.getId())
                .slug(model.getSlug())
                .name(model.getName())
                .maker(model.getMaker())
                .price(model.getPrice())
                .emoji(model.getEmoji())
                .description(model.getDescription())
                .imageUrl(model.getImageUrl())
                .officialSite(model.getOfficialSite())
                .keywords(model.getKeywords())
                .build();
    }
}
