package vn.ktt.musical_components.music_elements;

public enum Octave {
    ZEROTH(0),
    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    FIFTH(5),
    SIXTH(6),
    SEVENTH(7),
    EIGHT(8);

    private final int octavePosition;
    Octave(int octavePosition) {
        this.octavePosition = octavePosition;
    }

    int getIntegerOctave() {
        return octavePosition;
    }
}
