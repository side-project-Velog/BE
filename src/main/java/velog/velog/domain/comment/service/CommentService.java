package velog.velog.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velog.velog.domain.comment.dto.CommentDto;
import velog.velog.domain.comment.entity.Comment;
import velog.velog.domain.comment.event.CommentCountEvent;
import velog.velog.domain.comment.repository.CommentQueryRepository;
import velog.velog.domain.comment.repository.CommentRepository;
import velog.velog.domain.post.entity.Post;
import velog.velog.domain.post.repository.PostRepository;
import velog.velog.domain.user.entity.User;
import velog.velog.domain.user.repository.UserRepository;
import velog.velog.system.exception.model.ErrorCode;
import velog.velog.system.exception.model.RestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentQueryRepository commentQueryRepository;
    private final CommentCountRedisService commentCountRedisService;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 댓글 생성
     */
    @Transactional
    public void createComment(CommentDto.Request request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RestException(ErrorCode.POST_NOT_FOUND));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RestException(ErrorCode.COMMENT_NOT_FOUND));

            // 부모 댓글이 같은 게시글인지
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new RestException(ErrorCode.GLOBAL_BAD_REQUEST);
            }

            // 2-Depth 제한
            if (parent.getParent() != null) {
                throw new RestException(ErrorCode.GLOBAL_BAD_REQUEST);
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(request.getContent())
                .parent(parent)
                .build();

        commentRepository.save(comment);

        // 댓글 수 증가(Redis 카운트 업데이트를 위한 이벤트 발행)
        eventPublisher.publishEvent(CommentCountEvent.of(post.getId(), 1L));
    }

    /**
     * 댓글 조회
     */
    @Transactional(readOnly = true)
    public CommentDto.SliceResponse getComments(Long postId, Long lastCommentId, Pageable pageable, String email) {
        Long currentUserId = (email != null) ?
                userRepository.findByEmail(email).map(User::getId).orElse(null) : null;

        // SEQ 1. Repository에서 Slice<Entity> 조회
        Slice<Comment> slice = commentQueryRepository.findAllByPostId(postId, lastCommentId, pageable);

        // SEQ 2. 조회된 Entity 리스트를 계층형 DTO 구조로
        List<CommentDto.Response> hierarchy = convertToHierarchy(slice.getContent(), currentUserId);

        // SEQ 3. Redis에서 전체 댓글 수 가져오기
        long totalCount = commentCountRedisService.getCommentCount(postId);

        return CommentDto.SliceResponse.of(hierarchy, slice.hasNext(), totalCount);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RestException(ErrorCode.COMMENT_NOT_FOUND));

        // SEQ 1. 권한 체크
        if (!comment.getUser().getEmail().equals(email)) {
            throw new RestException(ErrorCode.AUTH_FORBIDDEN);
        }

        // SEQ 2. 이미 삭제된 댓글인지 체크
        if (comment.isDeleted()) {
            throw new RestException(ErrorCode.GLOBAL_BAD_REQUEST);
        }

        // SEQ 3. 댓글 수 감소
        long deletedCount = 1 + comment.getChildren().size();

        // SEQ 4. 게시글 ID 미리 저장
        Long postId = comment.getPost().getId();

        // SEQ 5. 댓글 삭제
        commentRepository.delete(comment);

        // SEQ 6. 댓글 수 감소 이벤트 발행
        eventPublisher.publishEvent(CommentCountEvent.of(postId, -deletedCount));
    }

    private List<CommentDto.Response> convertToHierarchy(List<Comment> comments, Long currentUserId) {
        List<CommentDto.Response> result = new ArrayList<>();
        Map<Long, CommentDto.Response> map = new HashMap<>();

        comments.forEach(c -> {
            CommentDto.Response dto = CommentDto.Response.from(c, currentUserId);
            map.put(dto.getId(), dto);
            if (c.getParent() != null) {
                CommentDto.Response parentDto = map.get(c.getParent().getId());
                if (parentDto != null) parentDto.getChildren().add(dto);
            } else {
                result.add(dto);
            }
        });
        return result;
    }
}
