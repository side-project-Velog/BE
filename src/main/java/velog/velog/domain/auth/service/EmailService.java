package velog.velog.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import velog.velog.domain.auth.dto.AuthDto;
import velog.velog.domain.auth.dto.EmailPurpose;
import velog.velog.system.exception.model.ErrorCode;
import velog.velog.system.exception.model.RestException;

import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Async("mailTaskExecutor")
    public void sendVerificationCode(String email, EmailPurpose purpose) {
        String normalizedEmail = email.trim().toLowerCase();
        // 인증 번호 랜덤으로 생성 10000~999999
        String code = String.format("%06d", secureRandom.nextInt(1000000));

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername);
            message.setTo(normalizedEmail);
            message.setSubject("[Velog] 회원가입 인증번호 - " + purpose);
            message.setText("인증번호 6자리: " + code);
            mailSender.send(message);

            // 발송 성공 시 -> Redis 등록
            String key = "EMAIL_CODE:" + purpose.name() + ":" + normalizedEmail;
            redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
            log.info("이메일 발송 성공: {}", normalizedEmail);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}, 이유: {}" + normalizedEmail, e.getMessage());
        }
    }

    public void verifyCode(AuthDto.VerifyEmailRequest request) {
        String email = request.getEmail();
        String key = "EMAIL_CODE:" + request.getPurpose().name() + ":" + email;
        String savedCode = redisTemplate.opsForValue().get(key);

        // 1. 코드가 없거나 만료
        if(savedCode == null || !savedCode.equals(request.getCode())) {
            throw new RestException(ErrorCode.AUTH_EMAIL_CODE_INVALID);
        }

        // 2. 코드가 일치하지 않는 경우
        if (!savedCode.equals(request.getCode())) {
            throw new RestException(ErrorCode.AUTH_EMAIL_CODE_NOT_MATCHED);
        }

        // 인증 완료
        redisTemplate.opsForValue().set("EMAIL_VERIFIED:" + request.getPurpose().name() + ":" + email, "TRUE", Duration.ofMinutes(10));

        redisTemplate.delete(key);
    }
}
