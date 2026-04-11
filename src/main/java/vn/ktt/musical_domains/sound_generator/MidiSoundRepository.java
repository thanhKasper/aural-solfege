package vn.ktt.musical_domains.sound_generator;

import vn.ktt.musical_domains.music_compositions.Chord;
import vn.ktt.musical_domains.music_compositions.Interval;
import vn.ktt.musical_domains.music_elements.Pitch;
import vn.ktt.sound_controller.ISoundPlayer;

public class MidiSoundRepository implements ISoundRepository{
    private final ISoundPlayer soundPlayer;

    public MidiSoundRepository(ISoundPlayer soundPlayer) {
        this.soundPlayer = soundPlayer;
    }

    @Override
    public Pitch getPitch(String noteNotation) {
        return null;
    }

    @Override
    public Chord getChord(String chordNotation) {
        return null;
    }

    @Override
    public Interval getInterval(String intervalNotation) {
        return null;
    }
}
