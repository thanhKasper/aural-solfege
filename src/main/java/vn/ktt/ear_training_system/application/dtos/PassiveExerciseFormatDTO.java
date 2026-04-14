package vn.ktt.ear_training_system.application.dtos;

public final class PassiveExerciseFormatDTO implements ExerciseFormatDTO {
    private String trainingMethodology;
    private String interval;
    private String intervalProperty;
    @Override
    public String type() {
        return "passive";
    }


}
