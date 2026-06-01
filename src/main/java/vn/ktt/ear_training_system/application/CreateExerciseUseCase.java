package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.ExerciseFormatType;
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
            if (exerciseFormatDTO.type() == ExerciseFormatType.SINGLE_INTERVAL) {
                SingleIntervalExerciseFormatDTO singleIntervalExerciseFormatDTO = (SingleIntervalExerciseFormatDTO) exerciseFormatDTO;
                return exerciseBuilder.buildSingleIntervalExerciseFormat(
                        exerciseDTO.getTrainingMethodology(),
                        singleIntervalExerciseFormatDTO.getInterval(),
                        singleIntervalExerciseFormatDTO.getTexture(),
                        singleIntervalExerciseFormatDTO.position()
                        );
            } else {
                throw new RuntimeException("Unknown exercise format: " + exerciseFormatDTO.type());
            }
        }).toList();

        var domainExercise = exerciseBuilder.buildExercise(
                exerciseDTO.getTrainingMethodology(),
                exerciseDTO.getTitle(),
                exerciseDTO.getDescription(),
                exerciseDTO.getReps(),
                exerciseDTO.getRest(),
                domainExerciseFormats);

        exerciseRepository.saveExercise(domainExercise);
    }
}
