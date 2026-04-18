package vn.ktt.ear_training_system.application;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.dtos.ExerciseDTO;
import vn.ktt.ear_training_system.application.dtos.PassiveExerciseFormatDTO;
import vn.ktt.ear_training_system.application.inbound.ExerciseRetrievalPort;

import java.util.List;

@Service
public class ExerciseRetrievalUseCase implements ExerciseRetrievalPort {
    @Override
    public List<ExerciseDTO> getAllExercises() {
        var sampleExercise = new ExerciseDTO();
        var samplePassiveListingAscP5 = new PassiveExerciseFormatDTO();
        samplePassiveListingAscP5.setInterval("PERFECT_5TH");
        samplePassiveListingAscP5.setTexture("ASCENDING");
        var samplePassiveListeningDescP5 = new PassiveExerciseFormatDTO();
        samplePassiveListeningDescP5.setInterval("PERFECT_5TH");
        samplePassiveListeningDescP5.setTexture("DESCENDING");
        sampleExercise.setTitle("Practice perfect 5th");
        sampleExercise.setDescription("Practicing listening to ascending and descending perfect 5");
        sampleExercise.setReps(1);
        sampleExercise.setTrainingMethodology("INTERVAL_TRAINING");
        sampleExercise.setExerciseFormats(List.of(samplePassiveListingAscP5, samplePassiveListeningDescP5));
        return List.of(sampleExercise);
    }
}
