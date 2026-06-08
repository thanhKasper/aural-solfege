package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseCreationPort;
import vn.ktt.ear_training_system.domain.factory.ExerciseBuilder;
import vn.ktt.ear_training_system.domain.repository.IExerciseRepository;
import vn.ktt.ear_training_system.domain.TrainingMethodology;

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
        var methodology = TrainingMethodology.valueOf(exerciseDTO.getTrainingMethodology());

        var domainExerciseFormats = exerciseDTO.getExerciseFormats().stream()
                .map(dto -> exerciseMapper.toDomain(dto, methodology))
                .toList();

        var domainExercise = exerciseBuilder.buildExercise(
                exerciseDTO.getTrainingMethodology(),
                exerciseDTO.getTitle(),
                exerciseDTO.getDescription(),
                exerciseDTO.isLoop(),
                exerciseDTO.isLoop() ? 0 : exerciseDTO.getReps(),
                exerciseDTO.getRest(),
                domainExerciseFormats);

        exerciseRepository.saveExercise(domainExercise);
    }
}
