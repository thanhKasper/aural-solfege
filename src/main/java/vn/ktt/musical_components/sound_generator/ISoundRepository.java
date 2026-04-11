package vn.ktt.musical_components.sound_generator;

import vn.ktt.musical_components.music_compositions.Chord;
import vn.ktt.musical_components.music_compositions.Interval;
import vn.ktt.musical_components.music_elements.Pitch;

public interface ISoundRepository {
    Pitch getPitch(String noteNotation);
    Chord getChord(String chordNotation);
    Interval getInterval(String intervalNotation);
}
