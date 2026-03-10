package velog.velog.domain.comment.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import velog.velog.domain.comment.entity.Comment;

import java.util.List;

import static velog.velog.domain.comment.entity.QComment.comment;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Slice<Comment> findAllByPostId(Long postId, Long lastId, Pageable pageable) {
        // 1. 부모 댓글 ID만 먼저 조회 (페이징)
        List<Long> parentIds = queryFactory
                .select(comment.id)
                .from(comment)
                .where(comment.post.id.eq(postId),
                        comment.parent.isNull(),
                        gtLastId(lastId))
                .orderBy(comment.id.asc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        if (parentIds.isEmpty()) return new SliceImpl<>(List.of(), pageable, false);

        boolean hasNext = parentIds.size() > pageable.getPageSize();
        if (hasNext) parentIds.remove(parentIds.size() - 1);

        // 2. 부모 ID에 속한 본인 + 자식(대댓글) 한꺼번에 조회
        List<Comment> content = queryFactory
                .selectFrom(comment)
                .join(comment.user).fetchJoin()
                .where(comment.id.in(parentIds).or(comment.parent.id.in(parentIds)))
                .orderBy(comment.parent.id.coalesce(comment.id).asc(),
                        comment.createdAt.asc())
                .fetch();

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression gtLastId(Long lastId) {
        return lastId == null ? null : comment.id.gt(lastId);
    }
}
