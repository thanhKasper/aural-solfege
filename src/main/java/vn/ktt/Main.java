package vn.ktt;

import vn.ktt.musical_components.music_elements.Accidental;
import vn.ktt.musical_components.music_elements.Note;
import vn.ktt.musical_components.music_elements.Octave;
import vn.ktt.musical_components.sound_generator.ResourceBasedSoundRepository;

public class Main {
    static void main() {
        ResourceBasedSoundRepository repo = new ResourceBasedSoundRepository();
        var result = repo.getSound(Note.C, Accidental.NONE, Octave.FOURTH);
        result.makeSound();
    }
}
