package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.ActiveExerciseFormatDTO;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.SingleIntervalExerciseFormatDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseCreationPort;
import vn.ktt.ear_training_system.domain.ExerciseBuilder;
import vn.ktt.ear_training_system.domain.IExerciseRepository;

@Service
public class CreateExerciseUseCase implements ExerciseCreationPort {
    private final IExerciseRepository exerciseRepository;
    private final ExerciseBuilder exerciseBuilder;

    public CreateExerciseUseCase(IExerciseRepository exerciseRepository, ExerciseBuilder exerciseBuilder) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseBuilder = exerciseBuilder;
    }

    public void createExercise(ExerciseDTO exerciseDTO) {
        var domainExerciseFormats = exerciseDTO.getExerciseFormats().stream().map(exerciseFormatDTO -> {
            if (exerciseFormatDTO.type().equals("SINGLE_INTERVAL")) {
                SingleIntervalExerciseFormatDTO singleIntervalExerciseFormatDTO = (SingleIntervalExerciseFormatDTO) exerciseFormatDTO;
                return exerciseBuilder.buildSingleIntervalExerciseFormat(
                        exerciseDTO.getTrainingMethodology(),
                        singleIntervalExerciseFormatDTO.getInterval(),
                        singleIntervalExerciseFormatDTO.getTexture(),
                        singleIntervalExerciseFormatDTO.position()
                        );
            }
            else if (exerciseFormatDTO.type().equals("active")) {
                ActiveExerciseFormatDTO activeExerciseDTO = (ActiveExerciseFormatDTO) exerciseFormatDTO;
                return exerciseBuilder.buildActiveExerciseFormat(
                        exerciseDTO.getTrainingMethodology(),
                        activeExerciseDTO.getTrainingIntervals()
                );
            }
            else {
                throw new RuntimeException("Unknown exercise format, it should be 'SINGLE_INTERVAL' or 'active'");
            }
        }).toList();

        var domainExercise = exerciseBuilder.buildExercise(
                exerciseDTO.getTrainingMethodology(),
                exerciseDTO.getTitle(),
                exerciseDTO.getDescription(),
                exerciseDTO.getReps(),
                domainExerciseFormats);

        exerciseRepository.saveExercise(domainExercise);
    }
}
