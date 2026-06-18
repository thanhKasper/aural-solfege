package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseCreationPort;
import vn.ktt.ear_training_system.domain.exercise.service.ExerciseBuilder;
import vn.ktt.ear_training_system.domain.exercise.repository.IExerciseRepository;

@Service
public class CreateExerciseUseCase implements ExerciseCreationPort {
    private final IExerciseRepository exerciseRepository;
    private final ExerciseBuilder exerciseBuilder;
    private final ExerciseMapper exerciseMapper;

    public CreateExerciseUseCase(IExerciseRepository exerciseRepository,
                                  ExerciseBuilder exerciseBuilder,
                                  ExerciseMapper exerciseMapper) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseBuilder = exerciseBuilder;
        this.exerciseMapper = exerciseMapper;
    }

    public void createExercise(ExerciseDTO exerciseDTO) {
        var domainExerciseActivities = exerciseDTO.getExerciseActivities().stream()
                .map(exerciseMapper::toDomain)
                .toList();

        var domainExercise = exerciseBuilder.buildExercise(
                exerciseDTO.getTrainingMethodology(),
                exerciseDTO.getTitle(),
                exerciseDTO.getDescription(),
                exerciseDTO.isLoop(),
                exerciseDTO.isLoop() ? 0 : exerciseDTO.getReps(),
                exerciseDTO.getRest(),
                domainExerciseActivities);

        exerciseRepository.saveExercise(domainExercise);
    }
}
