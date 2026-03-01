package velog.velog.system.security.jwt.exception;

import velog.velog.system.exception.model.ErrorCode;
import velog.velog.system.exception.model.RestException;

public class JwtRestException extends RestException {
    public JwtRestException(ErrorCode errorCode) {
        super(errorCode);
    }
    public JwtRestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
