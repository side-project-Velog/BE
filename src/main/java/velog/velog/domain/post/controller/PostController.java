package velog.velog.domain.post.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import velog.velog.common.util.ClientUtils;
import velog.velog.domain.post.dto.PostDto;
import velog.velog.domain.post.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 글 작성
    @PostMapping
    public ResponseEntity<Long> create(@RequestBody @Valid PostDto.CreateRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.create(request, userDetails.getUsername()));
    }

    // 글 목록 조회
    @GetMapping
    public ResponseEntity<Page<PostDto.ListResponse>> getList(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.findAll(pageable));
    }

    // 글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto.DetailResponse> getDetail(
            @PathVariable(name = "postId") Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String email = (userDetails != null) ? userDetails.getUsername() : null;
        String ip = ClientUtils.getClientIp(request);

        return ResponseEntity.ok(postService.getDetail(postId, email, ip));
    }

    // 글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<Void> update(@PathVariable(name = "postId") Long postId,
                                       @RequestBody @Valid PostDto.CreateRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        postService.update(postId, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // 글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable(name = "postId") Long postId,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        postService.delete(postId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}