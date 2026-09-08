package vn.ktt.music.domain.factory;

import vn.ktt.music.domain.composition.Interval;
import vn.ktt.music.domain.atom.Pitch;

public class MusicalEntityFactory implements IMusicalEntityFactory{

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

    @Override
    public Interval getInterval(String intervalNotation) {
        return new Interval(intervalNotation);
    }
}
