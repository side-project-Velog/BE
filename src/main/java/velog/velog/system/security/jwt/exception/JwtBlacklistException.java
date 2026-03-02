package velog.velog.system.security.jwt.exception;

import velog.velog.system.exception.model.ErrorCode;

public class JwtBlacklistException extends JwtRestException {
    public JwtBlacklistException() {
        super(ErrorCode.JWT_BLACKLIST);
    }
}
