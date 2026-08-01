package vn.ktt.musical_components_core.musical_application.sound_controller;

import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.AudioContent;
import vn.ktt.musical_components_core.musical_application.sound_controller.inbound.IntervalSoundGenerator;
import vn.ktt.musical_components_core.musical_application.sound_controller.outbound.SoundGeneratorPort;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Texture;

public class IntervalSoundGeneratorServiceImpl implements IntervalSoundGenerator {

    private SoundGeneratorPort soundGenerator;

    public IntervalSoundGeneratorServiceImpl(SoundGeneratorPort soundGenerator) {
        this.soundGenerator = soundGenerator;
    }

    @Override
    public AudioContent generateUpwardInterval(Interval interval, Texture texture) {
        return null;
    }

    @Override
    public AudioContent generateDownwardInterval(Interval interval, Texture texture) {
        return null;
    }

    @Override
    public AudioContent generateInterval(Interval interval, Texture texture) {
        return null;
    }
}
