package vn.ktt.musical_domains.music_factory;

import vn.ktt.musical_domains.music_elements.Pitch;

public class MusicalEntityFactory {

    public Pitch getPitch(String noteNotation) {
        if (Pitch.isNotValid(noteNotation)) {
            throw new IllegalArgumentException("Unknown note with notation " + noteNotation);
        }
        return new Pitch(
                Pitch.extractNote(noteNotation),
                Pitch.extractAccidental(noteNotation),
                Pitch.extractOctave(noteNotation)
        );
    }
}
