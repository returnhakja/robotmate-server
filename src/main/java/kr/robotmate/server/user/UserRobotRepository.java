package kr.robotmate.server.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRobotRepository extends JpaRepository<UserRobot, String> {

    @Query("SELECT COUNT(ur) FROM UserRobot ur WHERE ur.robotModel.id = :robotModelId")
    long countByRobotModelId(@Param("robotModelId") String robotModelId);

    List<UserRobot> findByUserIdOrderByCreatedAtDesc(String userId);

    List<UserRobot> findByUserIdAndIsPublicTrueOrderByCreatedAtDesc(String userId);

    Optional<UserRobot> findByIdAndUserId(String id, String userId);
}
