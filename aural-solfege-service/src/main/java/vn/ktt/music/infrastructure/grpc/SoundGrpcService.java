package vn.ktt.music.infrastructure.grpc;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import vn.ktt.grpc.sound.CompareIntervalsRequest;
import vn.ktt.grpc.sound.CompareIntervalsResponse;
import vn.ktt.grpc.sound.SoundServiceGrpc;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.factory.IMusicalEntityFactory;

@Service
public class SoundGrpcService extends SoundServiceGrpc.SoundServiceImplBase {
    private final IMusicalEntityFactory factory;

    public SoundGrpcService(IMusicalEntityFactory factory) {
        this.factory = factory;
    }

    @Override
    public void compareIntervals(CompareIntervalsRequest request, StreamObserver<CompareIntervalsResponse> observer) {
        try {
            Interval first = factory.getInterval(request.getFirstIntervalNotation());
            Interval second = factory.getInterval(request.getSecondIntervalNotation());
            observer.onNext(CompareIntervalsResponse.newBuilder()
                    .setComparison(first.compareTo(second))
                    .build());
            observer.onCompleted();
        } catch (IllegalArgumentException e) {
            observer.onError(e);
        }
    }
}
