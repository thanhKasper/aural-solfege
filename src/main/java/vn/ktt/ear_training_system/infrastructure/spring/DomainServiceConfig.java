package vn.ktt.ear_training_system.infrastructure.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ktt.ear_training_system.domain.exercise.entity.ExerciseActivity;
import vn.ktt.ear_training_system.domain.guard.ExerciseModificationGuard;
import vn.ktt.ear_training_system.domain.practice_session.repository.IPracticeSessionRepository;
import vn.ktt.ear_training_system.domain.practice_session.service.CoolDownStepGeneration;
import vn.ktt.ear_training_system.domain.practice_session.service.SingleIntervalStepGeneration;
import vn.ktt.ear_training_system.domain.practice_session.service.StepGeneration;
import vn.ktt.ear_training_system.domain.practice_session.service.StepGenerationService;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;
import vn.ktt.musical_components_core.musical_domains.music_factory.MusicalEntityFactory;
import vn.ktt.shared.IServiceIndex;

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
            List<IServiceIndex<ExerciseActivity, StepGeneration>> generators) {
        return new StepGenerationService(generators);
    }

    @Bean
    public StepGeneration singleIntervalStepGeneration() {
        return new SingleIntervalStepGeneration();
    }

    @Bean
    public StepGeneration coolDownStepGeneration() {
        return new CoolDownStepGeneration();
    }

    @Bean
    protected IMusicalEntityFactory musicalEntityFactory() {
        return new MusicalEntityFactory();
    }
}
