package vn.ktt.ear_training_system.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ExerciseFormat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "training_methodology")
    @Enumerated(EnumType.STRING)
    private TrainingMethodology trainingMethodology;

    public ExerciseFormat(TrainingMethodology trainingMethodology) {
        this.trainingMethodology = trainingMethodology;
    }

    public ExerciseFormat() {}

    public TrainingMethodology getTrainingMethodology() {
        return trainingMethodology;
    }
}
