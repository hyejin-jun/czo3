package org.mbc.czo.function.apiSecurity.jwt.exception;

public class InvalidTokenException extends RuntimeException {

    // 기본 생성자
    public InvalidTokenException() {
        super("Invalid or expired token");
    }

    // 메시지를 직접 지정
    public InvalidTokenException(String message) {
        super(message);
    }

    // 메시지 + 원인 예외 포함
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
