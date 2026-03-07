package velog.velog.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import velog.velog.common.enums.ViewDomain;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisViewService {

    private final StringRedisTemplate redisTemplate;

    // 10분 쿨다운(중복 조회 방지)
    private static final long COOLDOWN_MINUTES = 10;

    // Key 패턴: view:log:{domain}:{id}:{user/ip}
    private static final String LOG_KEY_FORMAT = "view:log:%s:%s:%s";
    // Key 패턴: view:count:{domain}:{id}
    private static final String COUNT_KEY_FORMAT = "view:count:%s:%s";

    /**
     * 조회 수 증가 로직
     */
    public void incrementViewCount(ViewDomain domain, Long id, Long userId, String clientIp) {
        String identifier = (userId != null) ? "user:" + userId : "ip:" + clientIp;

        // SEQ 1. 로그 키 생성 -> 누가, 어떤 도메인의, 어떤 글을 봤는지
        String logKey = String.format(LOG_KEY_FORMAT, domain.getPrefix(), id, identifier);

        // SEQ 2. 중복 조회 방지(NX 옵션)
        Boolean isFirstView = redisTemplate.opsForValue()
                .setIfAbsent(logKey, "1", Duration.ofMinutes(COOLDOWN_MINUTES));

        // SEQ 3. 처음 조회인 경우 -> 실시간으로 증가
        if(Boolean.TRUE.equals(isFirstView)) {
            String countKey = String.format(COUNT_KEY_FORMAT, domain.getPrefix(), id);
            redisTemplate.opsForValue().increment(countKey);

            log.debug("[Redis] 조회 수 증가 - {}: {}, 식별자: {}", domain.getPrefix(), id, identifier);
        }
    }

    /**
     * Redis에 쌓인 데이터 확인
     */
    public Map<Long, Long> getAndFlushViewCount(ViewDomain domain, int limit) {
        Map<Long, Long> viewCounts = new HashMap<>();
        String pattern = String.format("view:count:%s:*", domain.getPrefix());

        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(options)) {
            while (cursor.hasNext() && viewCounts.size() < limit) {
                String key = new String(cursor.next());
                String value = redisTemplate.opsForValue().get(key);

                if (value != null) {
                    // Key 파싱: view:count:post:{id} -> id 추출
                    Long id = Long.parseLong(key.split(":")[3]);
                    viewCounts.put(id, Long.parseLong(value));
                }
            }
        } catch (Exception e) {
            log.error("[Redis] Redis Scan Error ({})", domain.getPrefix(), e);
        }
        return viewCounts;
    }

    /**
     * Redis 카운트 키 삭제
     */
    public void deleteViewCountKey(ViewDomain domain, Long id) {
        String key = String.format(COUNT_KEY_FORMAT, domain.getPrefix(), id);
        redisTemplate.delete(key);
    }
}
