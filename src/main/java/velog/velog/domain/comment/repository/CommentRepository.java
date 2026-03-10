package velog.velog.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import velog.velog.domain.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByPostId(Long postId);

    // 게시글 삭제 시: 대댓글 먼저 삭제
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId AND c.parent IS NOT NULL")
    void deleteRepliesByPostId(@Param("postId") Long postId);

    // 게시글 삭제 시: 부모 댓글 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL")
    void deleteParentsByPostId(@Param("postId") Long postId);
}
