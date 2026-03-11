package kr.robotmate.server.robot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RobotModelRepository extends JpaRepository<RobotModel, String> {
    Optional<RobotModel> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
