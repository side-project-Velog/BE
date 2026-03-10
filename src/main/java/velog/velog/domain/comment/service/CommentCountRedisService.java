package velog.velog.domain.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import velog.velog.domain.comment.repository.CommentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentCountRedisService {

    private final StringRedisTemplate redisTemplate;
    private final CommentRepository commentRepository;
    private static final String KEY_PREFIX = "comment:count:";

    /**
     * 댓글 수 조회 (Redis 우선, 없으면 DB)
     */
    public long getCommentCount(Long postId) {
        String key = KEY_PREFIX + postId;

        try {
            // SEQ 1. Redis 조회
            String cachedCount = redisTemplate.opsForValue().get(key);
            if (cachedCount != null) {
                return Long.parseLong(cachedCount);
            }

            // SEQ 2. Cache Miss -> DB 조회
            long count = commentRepository.countByPostId(postId);

            // SEQ 3. Redis 캐시 채우기
            redisTemplate.opsForValue().set(key, String.valueOf(count));
            return count;

        } catch (Exception e) {
            log.warn("[Redis] Error fetching count, falling back to DB: {}", e.getMessage());
            return commentRepository.countByPostId(postId);
        }
    }

    /**
     * 게시글 삭제 시 캐시도 삭제
     */
    public void deleteCache(Long postId) {
        redisTemplate.delete(KEY_PREFIX + postId);
    }
}
