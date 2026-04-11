package vn.ktt.musical_components.sound_generator;

import vn.ktt.musical_components.music_elements.Pitch;
import vn.ktt.musical_components.music_elements.Pitch.*;

public interface ISoundRepository {
    Pitch getPitch(Note note, Accidental accidental, Octave octave);
}
