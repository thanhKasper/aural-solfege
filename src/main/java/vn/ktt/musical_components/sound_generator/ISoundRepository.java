package vn.ktt.musical_components.sound_generator;

import vn.ktt.musical_components.music_elements.Pitch;

public interface ISoundRepository {
    public Pitch getSound(Pitch.Note note, Pitch.Accidental accidental, Pitch.Octave octave);
}
