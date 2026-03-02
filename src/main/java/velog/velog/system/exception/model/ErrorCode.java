package velog.velog.system.exception.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Global
    GLOBAL_BAD_REQUEST(HttpStatus.BAD_REQUEST, "GLOBAL_BAD_REQUEST", "잘못된 요청입니다."),
    GLOBAL_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_INTERNAL_SERVER_ERROR", "서버 내부에 오류가 발생했습니다."),
    GLOBAL_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "GLOBAL_INVALID_PARAMETER", "요청 파라미터가 올바르지 않습니다."),
    GLOBAL_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "GLOBAL_METHOD_NOT_ALLOWED", "허용되지 않는 메서드입니다."),

    // JWT
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT_EXPIRED", "인증 세션이 만료되었습니다."),
    JWT_INVALID(HttpStatus.UNAUTHORIZED, "JWT_INVALID", "유효하지 않은 인증 토큰입니다."),
    JWT_MISSING(HttpStatus.UNAUTHORIZED, "JWT_MISSING", "인증 토큰이 누락되었습니다."),
    JWT_BLACKLIST(HttpStatus.UNAUTHORIZED, "JWT_BLACKLIST", "로그아웃된 토큰입니다."),

    // AUTH
    AUTH_PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "AUTH_PASSWORD_NOT_MATCH", "비밀번호가 일치하지 않습니다."),
    AUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
    AUTH_EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "AUTH_EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다."),
    AUTH_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다."),

    // MAIL
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL_SEND_FAILED", "이메일 전송에 실패했습니다."),
    MAIL_TEMPLATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL_TEMPLATE_ERROR", "메일 템플릿 처리 중 오류가 발생했습니다."),
    AUTH_EMAIL_CODE_INVALID(HttpStatus.BAD_REQUEST, "AUTH_EMAIL_CODE_INVALID", "인증 코드가 만료되었거나 올바르지 않습니다."),
    AUTH_EMAIL_CODE_NOT_MATCHED(HttpStatus.BAD_REQUEST, "AUTH_EMAIL_CODE_NOT_MATCHED", "인증 코드가 일치하지 않습니다."),

    // DB
    DB_DATA_TOO_LONG(HttpStatus.BAD_REQUEST, "DB_DATA_TOO_LONG", "데이터 길이가 너무 깁니다."),
    DB_NOT_NULL_VIOLATION(HttpStatus.BAD_REQUEST, "DB_NOT_NULL_VIOLATION", "필수 항목이 누락되었습니다."),
    DB_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "DB_CONNECTION_FAILED", "데이터베이스 연결에 실패했습니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER NOT FOUND", "존재하지 않는 사용자입니다."),
    USER_USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER USERNAME ALREADY EXISTS", "중복되는 아이디입니다."),
    USER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER EMAIL ALREADY EXISTS", "중복되는 이메일입니다."),
    USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "USER UNAUTHORIZED", "인증되지 않은 유저입니다."),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST NOT FOUND", "존재하지 않는 게시글입니다."),
    POST_FORBIDDEN(HttpStatus.FORBIDDEN, "POST_FORBIDDEN", "권한이 없습니다."),
    POST_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "POST CREATION FAILED", "게시글 생성에 실패했습니다."),
    POST_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "POST UPDATE FAILED", "게시글 수정에 실패했습니다."),
    POST_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "POST DELETE FAILED", "게시글 삭제에 실패했습니다.");

    private final HttpStatus status;
    private final String error;
    private final String message;
}
