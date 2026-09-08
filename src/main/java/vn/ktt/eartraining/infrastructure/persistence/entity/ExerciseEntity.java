package vn.ktt.eartraining.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import vn.ktt.eartraining.domain.exercise.valueobject.TrainingMethodology;
import vn.ktt.eartraining.infrastructure.persistence.converter.ExerciseActivitiesConverter;
import vn.ktt.eartraining.infrastructure.persistence.entity.activity.ExerciseActivityEntity;

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

    @Convert(converter = ExerciseActivitiesConverter.class)
    @Column(name = "exercise_activities", columnDefinition = "TEXT")
    private List<ExerciseActivityEntity> exerciseActivities;
}
