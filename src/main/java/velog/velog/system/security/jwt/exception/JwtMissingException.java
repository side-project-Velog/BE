package velog.velog.system.security.jwt.exception;

import velog.velog.system.exception.model.ErrorCode;

public class JwtMissingException extends JwtRestException {
  public JwtMissingException() {
    super(ErrorCode.JWT_MISSING);
  }
}
