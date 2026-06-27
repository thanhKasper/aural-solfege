package vn.ktt.ear_training_system.infrastructure.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ktt.ear_training_system.domain.exercise.service.ExerciseBuilder;
import vn.ktt.ear_training_system.domain.guard.ExerciseModificationGuard;
import vn.ktt.ear_training_system.domain.practice_session.repository.IPracticeSessionRepository;
import vn.ktt.ear_training_system.domain.practice_session.service.SingleIntervalStepGeneration;
import vn.ktt.ear_training_system.domain.practice_session.service.StepGeneration;
import vn.ktt.ear_training_system.domain.practice_session.service.StepGenerationService;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;
import vn.ktt.musical_components_core.musical_domains.music_factory.MusicalEntityFactory;

import java.util.List;

@Configuration
public class DomainServiceConfig {

    @Bean
    public ExerciseModificationGuard exerciseModificationGuard(
            IPracticeSessionRepository sessionRepository) {
        return new ExerciseModificationGuard(sessionRepository);
    }

    @Bean
    public StepGenerationService stepGenerationService(
            List<StepGeneration> generators) {
        return new StepGenerationService(generators);
    }

    @Bean
    public StepGeneration singleIntervalStepGeneration() {
        return new SingleIntervalStepGeneration();
    }

    @Bean
    protected ExerciseBuilder exerciseBuilder() {
        return new ExerciseBuilder();
    }

    @Bean
    protected IMusicalEntityFactory musicalEntityFactory() {
        return new MusicalEntityFactory();
    }
}
