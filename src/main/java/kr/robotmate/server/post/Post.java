package kr.robotmate.server.post;

import jakarta.persistence.*;
import kr.robotmate.server.common.BaseEntity;
import kr.robotmate.server.robot.RobotModel;
import kr.robotmate.server.user.User;
import kr.robotmate.server.user.UserRobot;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private PostType type;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PostVisibility visibility = PostVisibility.PUBLIC;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "robot_model_id")
    private RobotModel robotModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_robot_id")
    private UserRobot userRobot;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Builder.Default
    private int viewCount = 0;

    // 판매/나눔 전용 (type=SALE)
    @Enumerated(EnumType.STRING)
    private SaleType saleType;

    private Integer salePrice;
    private String condition;
    private String usagePeriod;
    private String tradeMethod;
    private String tradeLocation;
    private String contactInfo;

    @Builder.Default
    private boolean isSold = false;
}
