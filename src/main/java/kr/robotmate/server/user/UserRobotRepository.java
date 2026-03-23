package kr.robotmate.server.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRobotRepository extends JpaRepository<UserRobot, String> {

    @Query("SELECT COUNT(ur) FROM UserRobot ur WHERE ur.robotModel.id = :robotModelId")
    long countByRobotModelId(@Param("robotModelId") String robotModelId);
}
