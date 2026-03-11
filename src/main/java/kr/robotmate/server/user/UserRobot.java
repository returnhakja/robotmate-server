package kr.robotmate.server.user;

import jakarta.persistence.*;
import kr.robotmate.server.common.BaseEntity;
import kr.robotmate.server.robot.RobotModel;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_robots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRobot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "robot_model_id")
    private RobotModel robotModel;

    private String nickname;
    private LocalDate startDate;
}
