package kr.robotmate.server.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AdminModelRequest {
    @NotBlank
    private String slug;
    @NotBlank
    private String name;
    private String maker;
    private String price;
    private String emoji;
    private String description;
    private List<String> keywords;
    private String officialSite;
    private String imageUrl;
}
