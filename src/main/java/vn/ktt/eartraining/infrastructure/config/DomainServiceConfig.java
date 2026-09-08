package vn.ktt.eartraining.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ktt.eartraining.domain.exercise.valueobject.ExerciseActivity;
import vn.ktt.eartraining.domain.guard.ExerciseModificationGuard;
import vn.ktt.eartraining.domain.session.repository.IPracticeSessionRepository;
import vn.ktt.eartraining.domain.session.service.CoolDownStepGeneration;
import vn.ktt.eartraining.domain.session.service.IntervalSoundComparisonStepGeneration;
import vn.ktt.eartraining.domain.session.service.SingleIntervalStepGeneration;
import vn.ktt.eartraining.domain.session.service.IStepGeneration;
import vn.ktt.eartraining.domain.session.service.StepGenerationService;
import vn.ktt.music.domain.factory.IMusicalEntityFactory;
import vn.ktt.music.domain.factory.MusicalEntityFactory;
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
            List<IServiceIndex<ExerciseActivity, IStepGeneration>> generators) {
        return new StepGenerationService(generators);
    }

    @Bean
    public IStepGeneration singleIntervalStepGeneration() {
        return new SingleIntervalStepGeneration();
    }

    @Bean
    public IStepGeneration intervalSoundComparisonStepGeneration() {
        return new IntervalSoundComparisonStepGeneration();
    }

    @Bean
    public IStepGeneration coolDownStepGeneration() {
        return new CoolDownStepGeneration();
    }

    @Bean
    protected IMusicalEntityFactory musicalEntityFactory() {
        return new MusicalEntityFactory();
    }
}
