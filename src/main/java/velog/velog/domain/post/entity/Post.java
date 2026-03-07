package velog.velog.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import velog.velog.common.auditor.TimeBaseEntity;
import velog.velog.domain.user.entity.User;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_created_at", columnList = "created_at DESC")
})
public class Post extends TimeBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; // 제목

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content; // 내용

    @Column(length = 100)
    private String summary; // 요약문

    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Post(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.viewCount = 0L;
        this.summary = generateSummary(content);
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.summary = generateSummary(content);
    }

    private String generateSummary(String content) {
        if (content == null || content.isBlank()) return "";
        return content.length() > 50
                ? content.substring(0, 50) + "..."
                : content;
    }

    @PrePersist
    private void onCreate() {
        this.summary = generateSummary(this.content);
    }
}
