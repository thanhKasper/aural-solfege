package vn.ktt.ear_training_system.application.outbound;

import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;

public interface IIntervalComparisonPort {
    int compare(MusicalInterval firstInterval, MusicalInterval secondInterval);
}