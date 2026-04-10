package kr.robotmate.server.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.robotmate.server.post.PostType;
import kr.robotmate.server.post.PostVisibility;
import kr.robotmate.server.post.SaleType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreatePostRequest {

    @NotNull(message = "게시글 타입은 필수입니다.")
    private PostType type;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private String robotModelId;
    private String userRobotId;

    private List<String> tags = new ArrayList<>();

    @Size(max = 5, message = "이미지는 최대 5장까지 업로드 가능합니다.")
    private List<String> images = new ArrayList<>();

    private PostVisibility visibility = PostVisibility.PUBLIC;

    // 판매/나눔 전용
    private SaleType saleType;
    private Integer salePrice;
    private String condition;
    private String usagePeriod;
    private String tradeMethod;
    private String tradeLocation;
    private String contactInfo;
}
