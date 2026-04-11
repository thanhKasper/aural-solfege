package vn.ktt;

import vn.ktt.musical_components.music_elements.Pitch;
import vn.ktt.musical_components.sound_generator.ResourceBasedSoundRepository;

public class Main {
    static void main() {
        ResourceBasedSoundRepository repo = new ResourceBasedSoundRepository();
        var result = repo.getSound(Pitch.Note.C, Pitch.Accidental.NONE, Pitch.Octave.FOURTH);
        result.makeSound();
    }
}
