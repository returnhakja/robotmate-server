package kr.robotmate.server.news;

import jakarta.persistence.*;
import kr.robotmate.server.common.BaseEntity;
import kr.robotmate.server.robot.RobotModel;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NewsType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String thumbnailUrl;

    @Builder.Default
    private boolean isPinned = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "robot_model_id")
    private RobotModel robotModel;

    private String sourceUrl;
    private String sourceName;

    private LocalDateTime publishedAt;
}
