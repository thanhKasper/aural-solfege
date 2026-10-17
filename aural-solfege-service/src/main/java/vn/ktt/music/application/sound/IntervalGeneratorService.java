package vn.ktt.music.application.sound;

import org.springframework.stereotype.Service;
import vn.ktt.music.application.instrument.outbound.IInstrumentConfigurationPort;
import vn.ktt.music.application.sound.dto.AudioContent;
import vn.ktt.music.application.sound.dto.IntervalRangeParameters;
import vn.ktt.music.application.sound.inbound.IIntervalGeneratorPort;
import vn.ktt.music.application.sound.outbound.ISoundGeneratorPort;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.service.IMusicalOperation;

@Service
public class IntervalGeneratorService implements IIntervalGeneratorPort {

    private final ISoundGeneratorPort soundGenerator;
    private final IMusicalOperation musicalOperation;
    private final IInstrumentConfigurationPort instrumentConfigurationPort;

    public IntervalGeneratorService(ISoundGeneratorPort soundGenerator,
                                            IMusicalOperation musicalOperation,
                                            IInstrumentConfigurationPort instrumentConfigurationPort) {
        this.soundGenerator = soundGenerator;
        this.musicalOperation = musicalOperation;
        this.instrumentConfigurationPort = instrumentConfigurationPort;
    }

    @Override
    public AudioContent generateUpwardInterval(Interval interval, Interval.Texture texture) {
        return generateIntervalRange(interval, texture, false);
    }

    @Override
    public AudioContent generateDownwardInterval(Interval interval, Interval.Texture texture) {
        return generateIntervalRange(interval, texture, true);
    }

    @Override
    public AudioContent generateInterval(Interval interval, Interval.Texture texture) {
        Pitch highestPitchForInterval = musicalOperation.getLowerBoundPitchFromInterval(
                getActiveInstrument().getHighestPitch(),
                interval.getIntervalType());
        Pitch startingPitch = musicalOperation.getRandomPitch(getLowestPitch(), highestPitchForInterval);
        return toAudioContent(
                soundGenerator.createIntervalSound(startingPitch, interval, texture),
                "interval-" + interval + ".wav");
    }

    private AudioContent generateIntervalRange(Interval interval, Interval.Texture texture, boolean reverse) {
        Pitch lowestPitch = getLowestPitch();
        IntervalRangeParameters parameters = new IntervalRangeParameters();
        parameters.setLowestPitch(lowestPitch);
        parameters.setHighestPitch(musicalOperation.getUpperBoundPitchFromInterval(lowestPitch, interval.getIntervalType()));
        parameters.setInterval(interval);
        parameters.setIntervalTexture(texture);
        parameters.setReverse(reverse);
        return toAudioContent(soundGenerator.createIntervalRangeSound(parameters),
                "interval-range-" + interval + ".wav");
    }

    private Instrument getActiveInstrument() {
        return instrumentConfigurationPort.getActiveInstrument();
    }

    private Pitch getLowestPitch() {
        return getActiveInstrument().getLowestPitch();
    }

    private AudioContent toAudioContent(byte[] data, String fileName) {
        AudioContent audioContent = new AudioContent();
        audioContent.setData(data);
        audioContent.setFileSize(data.length);
        audioContent.setFileName(fileName);
        return audioContent;
    }
}
