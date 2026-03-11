package kr.robotmate.server.robot;

import kr.robotmate.server.common.exception.NotFoundException;
import kr.robotmate.server.robot.dto.RobotModelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RobotModelService {

    private final RobotModelRepository robotModelRepository;

    public List<RobotModelResponse> getAll() {
        return robotModelRepository.findAll().stream()
                .map(RobotModelResponse::from)
                .toList();
    }

    public RobotModelResponse getBySlug(String slug) {
        RobotModel model = robotModelRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 기종입니다: " + slug));
        return RobotModelResponse.from(model);
    }
}
