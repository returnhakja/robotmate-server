package kr.robotmate.server.common.exception;

import lombok.Getter;

@Getter
public class SuspendedException extends RuntimeException {

    private final String suspendReason;

    public SuspendedException(String suspendReason) {
        super("계정이 정지되었습니다.");
        this.suspendReason = suspendReason;
    }
}
