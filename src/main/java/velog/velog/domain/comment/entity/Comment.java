package velog.velog.domain.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import velog.velog.common.auditor.TimeBaseEntity;
import velog.velog.domain.post.entity.Post;
import velog.velog.domain.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_post", columnList = "post_id"),
        @Index(name = "idx_comment_parent", columnList = "parent_id")
})
public class Comment extends TimeBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3000)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;// 삭제 됐는지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent",  cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Builder
    public Comment(String content, Post post, User user, Comment parent) {
        this.content = content;
        this.post = post;
        this.user = user;
        this.parent = parent;
    }

    public void changeDeletedStatus(boolean status) {
        this.isDeleted = status;
    }
}
