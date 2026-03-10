package velog.velog.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import velog.velog.domain.comment.entity.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentDto {

    @Getter
    @NoArgsConstructor
    public static class Request {
        private Long postId;
        private String content;
        private Long parentId; // 최상위 댓글인 경우 null
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String content;
        private String nickname;
        private boolean isDeleted;
        private LocalDateTime createdAt;
        private boolean isOwner;
        private List<Response> children; // 대댓글

        public static Response from(Comment comment, Long currentUserId) {
            return Response.builder()
                    .id(comment.getId())
                    .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                    .nickname(comment.getUser().getFirstName()) // 우리 User 엔티티 필드에 맞춤
                    .isDeleted(comment.isDeleted())
                    .createdAt(comment.getCreatedAt())
                    .isOwner(currentUserId != null && currentUserId.equals(comment.getUser().getId()))
                    .children(new ArrayList<>())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SliceResponse {
        private List<Response> content;
        private boolean hasNext;
        private long totalCount; // 전체 댓글 수

        public static SliceResponse of(List<Response> content, boolean hasNext, long totalCount) {
            return SliceResponse.builder()
                    .content(content)
                    .hasNext(hasNext)
                    .totalCount(totalCount)
                    .build();
        }
    }
}
