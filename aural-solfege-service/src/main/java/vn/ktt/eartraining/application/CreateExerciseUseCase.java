package vn.ktt.eartraining.application;

import org.springframework.stereotype.Service;
import vn.ktt.eartraining.application.dto.ExerciseDTO;
import vn.ktt.eartraining.application.inbound.IExerciseCreationPort;
import vn.ktt.eartraining.application.mapper.ExerciseMapper;
import vn.ktt.eartraining.domain.exercise.entity.Exercise;
import vn.ktt.eartraining.domain.exercise.repository.IExerciseRepository;
import vn.ktt.eartraining.domain.exercise.valueobject.TrainingMethodology;

@Service
public class CreateExerciseUseCase implements IExerciseCreationPort {
    private final IExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    public CreateExerciseUseCase(IExerciseRepository exerciseRepository,
                                  ExerciseMapper exerciseMapper) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseMapper = exerciseMapper;
    }

    public void createExercise(ExerciseDTO exerciseDTO) {
        Integer reps = exerciseDTO.getReps();
        if (reps == null) {
            throw new IllegalArgumentException("Repetitions must be between 1 and 10");
        }

        var domainExerciseActivities = exerciseDTO.getExerciseActivities().stream()
                .map(exerciseMapper::toDomain)
                .toList();

        var domainExercise = Exercise.create(
                TrainingMethodology.valueOf(exerciseDTO.getTrainingMethodology()),
                exerciseDTO.getTitle(),
                exerciseDTO.getDescription(),
                reps,
                exerciseDTO.getRest(),
                domainExerciseActivities);

        exerciseRepository.saveExercise(domainExercise);
    }
}
