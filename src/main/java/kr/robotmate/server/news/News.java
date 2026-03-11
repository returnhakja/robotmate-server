package kr.robotmate.server.news;

import jakarta.persistence.*;
import kr.robotmate.server.common.BaseEntity;
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

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String sourceUrl;
    private String sourceName;
    private LocalDateTime publishedAt;
}
