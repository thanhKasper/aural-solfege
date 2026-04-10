package vn.ktt.musical_components.music_compositions;

public enum IntervalNumber {
    UNISON(0),
    MINOR_2ND(1),
    MAJOR_2ND(2),
    MINOR_3RD(3),
    MAJOR_3RD(4),
    PERFECT_4TH(5),
    AUGMENTED_4TH(5),
    DIMINISHED_5TH(6),
    PERFECT_5TH(7),
    MINOR_6TH(8),
    MAJOR_6TH(9),
    MINOR_7TH(10),
    MAJOR_7TH(11),
    PERFECT_OCTAVE(12);

    private final int halfSteps;

    IntervalNumber(int halfSteps) {
        this.halfSteps = halfSteps;
    }

    public int getHalfSteps() {
        return halfSteps;
    }
}
