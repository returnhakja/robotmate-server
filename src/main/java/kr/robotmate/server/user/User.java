package kr.robotmate.server.user;

import jakarta.persistence.*;
import kr.robotmate.server.common.BaseEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;  // Google 로그인 유저는 null

    @Column(unique = true)
    private String googleId;

    @Column(unique = true, nullable = false)
    private String nickname;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<UserRobot> ownedRobots = new ArrayList<>();
}
