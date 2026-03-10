package velog.velog.domain.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import velog.velog.domain.comment.dto.CommentDto;
import velog.velog.domain.comment.service.CommentService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글/대댓글 생성
    @PostMapping("/comments")
    public ResponseEntity<Void> create(@RequestBody @Valid CommentDto.Request request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        commentService.createComment(request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // 특정 게시글의 댓글 목록 조회 (무한 스크롤)
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDto.SliceResponse> getList(
            @PathVariable(name = "postId") Long postId,
            @RequestParam(required = false) Long lastId,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = (userDetails != null) ? userDetails.getUsername() : null;
        return ResponseEntity.ok(commentService.getComments(postId, lastId, pageable, email));
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable(name = "commentId") Long commentId,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}