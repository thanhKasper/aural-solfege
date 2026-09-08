package vn.ktt.eartraining.application.outbound;

import vn.ktt.eartraining.domain.exercise.valueobject.MusicalInterval;

public interface IIntervalComparisonPort {
    int compare(MusicalInterval firstInterval, MusicalInterval secondInterval);
}