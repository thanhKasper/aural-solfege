package vn.ktt.ear_training_system.infrastructure.repository.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import vn.ktt.ear_training_system.domain.ExerciseActivity;
import vn.ktt.ear_training_system.domain.TrainingMethodology;
import vn.ktt.ear_training_system.infrastructure.repository.converter.ExerciseFormatsConverter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
public class ExerciseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID exerciseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_methodology")
    private TrainingMethodology trainingMethodology;

    @Column(name = "title", length = 256)
    private String title;

    @Column(name = "description", length = 3000)
    private String description;

    @Check(constraints = "repetitions IS NULL OR (repetitions >= 1 AND repetitions <= 10)")
    @Column(name = "repetitions")
    private Integer repetitions;

    @Check(constraints = "rest >= 0 AND rest <= 1800")
    @Column(name = "rest", nullable = false)
    private Integer rest = 0;

    @Convert(converter = ExerciseFormatsConverter.class)
    @Column(name = "exercise_formats", columnDefinition = "TEXT")
    private List<ExerciseActivity> exerciseActivities;
}
