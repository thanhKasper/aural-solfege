package vn.ktt.ear_training_system.application;

import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.PassiveExerciseFormatDTO;
import vn.ktt.ear_training_system.domain.ExerciseBuilder;
import vn.ktt.ear_training_system.domain.IExerciseRepository;

public class CreateExerciseUseCase {
    private final IExerciseRepository exerciseRepository;
    private final ExerciseBuilder exerciseBuilder;

    public CreateExerciseUseCase(IExerciseRepository exerciseRepository, ExerciseBuilder exerciseBuilder) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseBuilder = exerciseBuilder;
    }

    public void createExercise(ExerciseDTO exerciseDTO) {
        var domainExerciseFormats = exerciseDTO.getExerciseFormats().stream().map(exerciseFormatDTO -> {
            if (exerciseFormatDTO.type().equals("passive")) {
                PassiveExerciseFormatDTO passiveExerciseDTO = (PassiveExerciseFormatDTO) exerciseFormatDTO;
                return exerciseBuilder.buildPassiveExerciseFormat(
                        exerciseDTO.getTrainingMethodology(),
                        passiveExerciseDTO.getInterval(),
                        passiveExerciseDTO.getIntervalProperty());
            }
            return null;
        }).toList();

        var domainExercise = exerciseBuilder.buildExercise(
                exerciseDTO.getTrainingMethodology(),
                exerciseDTO.getTitle(),
                exerciseDTO.getDescription(),
                exerciseDTO.getReps(),
                domainExerciseFormats);

        exerciseRepository.save(domainExercise);
    }
}
