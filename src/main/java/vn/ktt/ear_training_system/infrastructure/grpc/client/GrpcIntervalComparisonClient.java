package vn.ktt.ear_training_system.infrastructure.grpc.client;

import org.springframework.stereotype.Service;
import vn.ktt.ear_training_system.application.outbound.IIntervalComparisonPort;
import vn.ktt.ear_training_system.domain.exercise.value_object.MusicalInterval;
import vn.ktt.grpc.sound.CompareIntervalsRequest;
import vn.ktt.grpc.sound.SoundServiceGrpc;

@Service
public class GrpcIntervalComparisonClient implements IIntervalComparisonPort {

    private final SoundServiceGrpc.SoundServiceBlockingStub soundService;

    public GrpcIntervalComparisonClient(SoundServiceGrpc.SoundServiceBlockingStub soundService) {
        this.soundService = soundService;
    }

    @Override
    public int compare(MusicalInterval firstInterval, MusicalInterval secondInterval) {
        return soundService.compareIntervals(CompareIntervalsRequest.newBuilder()
                        .setFirstIntervalNotation(toNotation(firstInterval))
                        .setSecondIntervalNotation(toNotation(secondInterval))
                        .build())
                .getComparison();
    }

    private String toNotation(MusicalInterval interval) {
        return switch (interval) {
            case UNISON -> "P0";
            case MINOR_2ND -> "m2";
            case MAJOR_2ND -> "M2";
            case MINOR_3RD -> "m3";
            case MAJOR_3RD -> "M3";
            case PERFECT_4TH -> "P4";
            case TRITONE -> "TT";
            case PERFECT_5TH -> "P5";
            case MINOR_6TH -> "m6";
            case MAJOR_6TH -> "M6";
            case MINOR_7TH -> "m7";
            case MAJOR_7TH -> "M7";
            case PERFECT_8TH -> "P8";
        };
    }
}