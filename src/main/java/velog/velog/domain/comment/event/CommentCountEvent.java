package velog.velog.domain.comment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class CommentCountEvent {
    private final Long postId;
    private final long delta; // +1(작성), -1(삭제), -N(부모삭제시 자식수만큼)
}
