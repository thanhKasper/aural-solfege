package vn.ktt.musical_components_core.musical_application.sound_controller;

import org.springframework.stereotype.Service;
import vn.ktt.musical_components_core.musical_application.sound_configuration.outbound.InstrumentConfigurationPort;
import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.AudioContent;
import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.IntervalRangeParameters;
import vn.ktt.musical_components_core.musical_application.sound_controller.inbound.IntervalSoundGenerator;
import vn.ktt.musical_components_core.musical_application.sound_controller.outbound.SoundGeneratorPort;
import vn.ktt.musical_components_core.musical_domains.instruments.Instrument;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;
import vn.ktt.musical_components_core.musical_domains.music_services.IMusicalOperation;

@Service
public class IntervalSoundGeneratorServiceImpl implements IntervalSoundGenerator {

    private final SoundGeneratorPort soundGenerator;
    private final IMusicalOperation musicalOperation;
    private final InstrumentConfigurationPort instrumentConfigurationPort;

    public IntervalSoundGeneratorServiceImpl(SoundGeneratorPort soundGenerator,
                                             IMusicalOperation musicalOperation,
                                             InstrumentConfigurationPort instrumentConfigurationPort) {
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
        return null;
    }

    private AudioContent generateIntervalRange(Interval interval, Interval.Texture texture, boolean reverse) {
        IntervalRangeParameters parameters = new IntervalRangeParameters();
        Instrument activeInstrument = instrumentConfigurationPort.getActiveInstrument();

        parameters.setLowestPitch(musicalOperation.getLowestPitch(activeInstrument));
        parameters.setHighestPitch(musicalOperation.getHighestLowerBoundIntervalPitch(activeInstrument, interval.getIntervalType()));
        parameters.setInterval(interval);
        parameters.setIntervalTexture(texture);
        parameters.setReverse(reverse);

        byte[] data = soundGenerator.createIntervalRange(parameters);

        AudioContent audioContent = new AudioContent();
        audioContent.setData(data);
        audioContent.setFileSize(data.length);
        audioContent.setFileName("interval-" + interval + ".wav");
        return audioContent;
    }
}
