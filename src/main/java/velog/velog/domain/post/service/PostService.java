package velog.velog.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velog.velog.domain.post.dto.PostDto;
import velog.velog.domain.post.entity.Post;
import velog.velog.domain.post.repository.PostQueryRepository;
import velog.velog.domain.post.repository.PostRepository;
import velog.velog.domain.user.entity.User;
import velog.velog.domain.user.repository.UserRepository;
import velog.velog.system.exception.model.ErrorCode;
import velog.velog.system.exception.model.RestException;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostQueryRepository postQueryRepository;

    @Transactional
    public Long create(PostDto.CreateRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        return postRepository.save(post).getId();
    }

    public Page<PostDto.ListResponse> findAll(Pageable pageable) {
        return postQueryRepository.findAllPaged(pageable)
                .map(PostDto.ListResponse::from);
    }

    public PostDto.DetailResponse findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RestException(ErrorCode.POST_NOT_FOUND));
        return PostDto.DetailResponse.from(post);
    }

    @Transactional
    public void update(Long id, PostDto.CreateRequest request, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RestException(ErrorCode.POST_NOT_FOUND));

        // 권한 확인: 작성자와 수정 요청자가 일치하는지 검증
        validateAuthor(post, email);

        post.update(request.getTitle(), request.getContent());
    }

    @Transactional
    public void delete(Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RestException(ErrorCode.POST_NOT_FOUND));

        validateAuthor(post, email);

        postRepository.delete(post);
    }

    private void validateAuthor(Post post, String email) {
        if (!post.getUser().getEmail().equals(email)) {
            throw new RestException(ErrorCode.POST_FORBIDDEN);
        }
    }
}
