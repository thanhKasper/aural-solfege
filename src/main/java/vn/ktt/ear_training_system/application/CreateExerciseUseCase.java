package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseCreationPort;
import vn.ktt.ear_training_system.application.mappers.ExerciseMapper;
import vn.ktt.ear_training_system.domain.exercise.entity.Exercise;
import vn.ktt.ear_training_system.domain.exercise.repository.IExerciseRepository;
import vn.ktt.ear_training_system.domain.exercise.value_object.TrainingMethodology;

@Service
public class CreateExerciseUseCase implements ExerciseCreationPort {
    private final IExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    public CreateExerciseUseCase(IExerciseRepository exerciseRepository,
                                  ExerciseMapper exerciseMapper) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseMapper = exerciseMapper;
    }

    public void createExercise(ExerciseDTO exerciseDTO) {
        var domainExerciseActivities = exerciseDTO.getExerciseActivities().stream()
                .map(exerciseMapper::toDomain)
                .toList();

        var domainExercise = Exercise.create(
                TrainingMethodology.valueOf(exerciseDTO.getTrainingMethodology()),
                exerciseDTO.getTitle(),
                exerciseDTO.getDescription(),
                exerciseDTO.isLoop(),
                exerciseDTO.isLoop() ? 0 : exerciseDTO.getReps(),
                exerciseDTO.getRest(),
                domainExerciseActivities);

        exerciseRepository.saveExercise(domainExercise);
    }
}
