package velog.velog.domain.comment.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import velog.velog.domain.comment.event.CommentCountEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentCountEventHandler {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "comment:count:";

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCountEvent(CommentCountEvent event) {
        String key = KEY_PREFIX + event.getPostId();

        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                redisTemplate.opsForValue().increment(key, event.getDelta());
                log.info("[Redis] Comment Count Updated -> Post: {}, Delta: {}", event.getPostId(), event.getDelta());
            }
        } catch (Exception e) {
            log.error("[Redis] Comment Count Update Failed", e);
        }
    }
}