package velog.velog.system.security.jwt.exception;

import velog.velog.system.exception.model.ErrorCode;

public class JwtExpiredException extends JwtRestException {
  public JwtExpiredException() {
    super(ErrorCode.JWT_EXPIRED);
  }
}
