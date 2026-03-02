package velog.velog.system.security.jwt.exception;

import velog.velog.system.exception.model.ErrorCode;

public class JwtInvalidException extends JwtRestException {
    public JwtInvalidException() {
        super(ErrorCode.JWT_INVALID);
    }
}
