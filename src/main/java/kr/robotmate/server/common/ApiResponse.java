package kr.robotmate.server.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final String code;

    private ApiResponse(boolean success, T data, String message, String code) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.code = code;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, null, null, null);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, message, null);
    }

    public static ApiResponse<Map<String, String>> suspended(String suspendReason) {
        return new ApiResponse<>(false, Map.of("suspendReason", suspendReason != null ? suspendReason : ""),
                "계정이 정지되었습니다.", "USER_SUSPENDED");
    }
}
