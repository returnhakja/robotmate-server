package kr.robotmate.server.seed;

import kr.robotmate.server.robot.RobotModel;
import kr.robotmate.server.robot.RobotModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final RobotModelRepository robotModelRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedRobotModels();
    }

    private void seedRobotModels() {
        List<RobotModel> models = List.of(
                RobotModel.builder()
                        .slug("loona")
                        .name("LOONA")
                        .maker("KEYi Tech")
                        .price("₩990,000")
                        .description("고양이형 AI 반려 로봇. 얼굴 인식과 감정 표현, 자율 이동 기능을 갖춘 스마트 반려 로봇.")
                        .imageUrl("https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400")
                        .officialSite("https://www.keyirobot.com")
                        .keywords(List.of("얼굴인식", "감정표현", "자율이동", "앱연동"))
                        .build(),
                RobotModel.builder()
                        .slug("moflin")
                        .name("Moflin")
                        .maker("Vanguard Industries")
                        .price("₩550,000")
                        .description("털복숭이 감정 반려 로봇. 감정이 진화하고 촉각 센서로 교감하는 힐링 로봇.")
                        .imageUrl("https://images.unsplash.com/photo-1535378917042-10a22c95931a?w=400")
                        .officialSite("https://moflin.com")
                        .keywords(List.of("감정진화", "촉각센서", "힐링", "소프트"))
                        .build(),
                RobotModel.builder()
                        .slug("lovot")
                        .name("LOVOT")
                        .maker("GROOVE X")
                        .price("₩3,500,000")
                        .description("체온이 느껴지는 교감 로봇. 따뜻한 체온과 자율주행으로 진정한 반려감을 주는 프리미엄 로봇.")
                        .imageUrl("https://images.unsplash.com/photo-1546776310-eef45dd6d63c?w=400")
                        .officialSite("https://lovot.life")
                        .keywords(List.of("체온감지", "자율주행", "프리미엄", "교감"))
                        .build(),
                RobotModel.builder()
                        .slug("ebo-air")
                        .name("Ebo Air")
                        .maker("Enabot")
                        .price("₩250,000")
                        .description("펫 모니터링 구형 로봇. 원격 카메라와 자동 놀이 기능을 갖춘 컴팩트한 홈 로봇.")
                        .imageUrl("https://images.unsplash.com/photo-1563396983906-b3795482a59a?w=400")
                        .officialSite("https://www.enabot.com")
                        .keywords(List.of("원격카메라", "자동놀이", "컴팩트", "모니터링"))
                        .build(),
                RobotModel.builder()
                        .slug("eilik")
                        .name("Eilik")
                        .maker("Energize Lab")
                        .price("₩190,000")
                        .description("데스크탑 감성 로봇. 감성 반응과 멀티 교감 기능을 갖춘 가성비 좋은 소형 로봇.")
                        .imageUrl("https://images.unsplash.com/photo-1518770660439-4636190af475?w=400")
                        .officialSite("https://www.energizelab.com")
                        .keywords(List.of("감성반응", "멀티교감", "가성비", "데스크탑"))
                        .build(),
                RobotModel.builder()
                        .slug("vector")
                        .name("Vector 2.0")
                        .maker("Digital Dream Labs")
                        .price("₩350,000")
                        .description("소형 음성비서 로봇. 음성 비서와 스마트홈 연동 기능을 갖춘 클래식 AI 로봇.")
                        .imageUrl("https://images.unsplash.com/photo-1531746790731-6c087fecd65a?w=400")
                        .officialSite("https://www.digitaldreamlabs.com")
                        .keywords(List.of("음성비서", "스마트홈", "클래식", "AI"))
                        .build()
        );

        for (RobotModel model : models) {
            if (!robotModelRepository.existsBySlug(model.getSlug())) {
                robotModelRepository.save(model);
                log.info("Seeded RobotModel: {}", model.getName());
            }
        }
    }
}
