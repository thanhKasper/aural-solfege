package vn.ktt.musical_components_core.musical_application.sound_controller;

import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.AudioContent;
import vn.ktt.musical_components_core.musical_application.sound_controller.inbound.IntervalSoundGenerator;
import vn.ktt.musical_components_core.musical_application.sound_controller.outbound.SoundGeneratorPort;

public class IntervalSoundGeneratorServiceImpl implements IntervalSoundGenerator {

    private SoundGeneratorPort soundGenerator;

    public IntervalSoundGeneratorServiceImpl(SoundGeneratorPort soundGenerator) {
        this.soundGenerator = soundGenerator;
    }

    @Override
    public AudioContent generateUpwardInterval(String interval, String texture) {
        return null;
    }

    @Override
    public AudioContent generateDownwardInterval(String interval, String texture) {
        return null;
    }

    @Override
    public AudioContent generateInterval(String interval, String texture) {
        return null;
    }
}
