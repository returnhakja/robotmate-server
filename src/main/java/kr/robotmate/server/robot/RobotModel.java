package kr.robotmate.server.robot;

import jakarta.persistence.*;
import kr.robotmate.server.common.BaseEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "robot_models")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(nullable = false)
    private String name;

    private String maker;
    private String price;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;
    private String officialSite;

    @ElementCollection
    @CollectionTable(name = "robot_model_keywords", joinColumns = @JoinColumn(name = "robot_model_id"))
    @Column(name = "keyword")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
}
