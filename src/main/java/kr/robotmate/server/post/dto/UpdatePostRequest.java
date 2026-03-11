package kr.robotmate.server.post.dto;

import jakarta.validation.constraints.Size;
import kr.robotmate.server.post.SaleType;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePostRequest {
    private String title;
    private String content;
    private String robotModelId;
    private List<String> tags;

    @Size(max = 5, message = "이미지는 최대 5장까지 업로드 가능합니다.")
    private List<String> images;

    // 판매/나눔 전용
    private SaleType saleType;
    private Integer salePrice;
    private String condition;
    private String usagePeriod;
    private String tradeMethod;
    private String contactInfo;
    private Boolean sold;
}
