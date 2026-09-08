package vn.ktt.eartraining.application;

import org.springframework.stereotype.Service;
import vn.ktt.eartraining.application.dto.ExerciseDTO;
import vn.ktt.eartraining.application.inbound.IExerciseCreationPort;
import vn.ktt.eartraining.application.mapper.ExerciseMapper;
import vn.ktt.eartraining.domain.exercise.entity.Exercise;
import vn.ktt.eartraining.domain.exercise.valueobject.activity.CoolDownRestActivity;
import vn.ktt.eartraining.domain.exercise.repository.IExerciseRepository;
import vn.ktt.eartraining.domain.exercise.valueobject.TrainingMethodology;

import java.util.ArrayList;

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
        var domainExerciseActivities = new ArrayList<>(exerciseDTO.getExerciseActivities().stream()
                .map(exerciseMapper::toDomain)
                .toList());
        domainExerciseActivities.add(new CoolDownRestActivity(domainExerciseActivities.size(), exerciseDTO.getRest()));

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
