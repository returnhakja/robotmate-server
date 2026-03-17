package kr.robotmate.server.comment;

import jakarta.persistence.*;
import kr.robotmate.server.common.BaseEntity;
import kr.robotmate.server.user.User;
import lombok.*;

@Entity
@Table(name = "comment_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "comment_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
}
