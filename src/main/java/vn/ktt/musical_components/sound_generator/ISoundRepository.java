package vn.ktt.musical_components.sound_generator;

import vn.ktt.musical_components.music_elements.Accidental;
import vn.ktt.musical_components.music_elements.Note;
import vn.ktt.musical_components.music_elements.Octave;
import vn.ktt.musical_components.music_elements.Pitch;

public interface ISoundRepository {
    public Pitch getSound(Note note, Accidental accidental, Octave octave);
}
