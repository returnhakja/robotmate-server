package kr.robotmate.server.news.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PinRequest {

    @NotNull
    private Boolean isPinned;
}
