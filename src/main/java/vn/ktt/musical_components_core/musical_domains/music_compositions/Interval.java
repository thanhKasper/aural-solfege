package vn.ktt.musical_components_core.musical_domains.music_compositions;

import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;

public class Interval implements Comparable<Interval> {
    private final IntervalNumber intervalNumber;

    public Interval(IntervalNumber intervalNumber) {
        this.intervalNumber = intervalNumber;
    }

    public Interval(String intervalNotation) {
        this.intervalNumber = IntervalNumber.fromNotation(intervalNotation);
    }

    public String toString() {
        return intervalNumber.toNotation();
    }

    public Pitch upwardPitch(Pitch basePitch) {
        return basePitch.getPitchAfterHalfSteps(this.intervalNumber.getHalfSteps());
    }

    public Pitch downwardPitch(Pitch basePitch) {
        return basePitch.getPitchAfterHalfSteps(-this.intervalNumber.getHalfSteps());
    }

    @Override
    public int compareTo(Interval interval) {
        if (this.intervalNumber.getHalfSteps() > interval.intervalNumber.getHalfSteps()) {
            return 1;
        } else if (this.intervalNumber.getHalfSteps() < interval.intervalNumber.getHalfSteps()) {
            return -1;
        }
        return 0;
    }

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

        public String toNotation() {
            return switch (this) {
                case UNISON -> "P0";
                case PERFECT_4TH -> "P4";
                case PERFECT_5TH -> "P5";
                case PERFECT_OCTAVE -> "P8";
                case MAJOR_2ND -> "M2";
                case MINOR_2ND -> "m2";
                case MINOR_3RD -> "m3";
                case MAJOR_3RD -> "M3";
                case MINOR_6TH -> "m6";
                case MAJOR_6TH -> "M6";
                case MINOR_7TH -> "m7";
                case MAJOR_7TH -> "M7";
                case DIMINISHED_5TH -> "d5";
                case AUGMENTED_4TH  -> "A4";
            };
        }

        public static IntervalNumber fromNotation(String notation) {
            return switch (notation) {
                case "P0" -> UNISON;
                case "P4" -> PERFECT_4TH;
                case "P5" -> PERFECT_5TH;
                case "P8" -> PERFECT_OCTAVE;
                case "M2" -> MAJOR_2ND;
                case "m2" -> MINOR_2ND;
                case "m3" -> MINOR_3RD;
                case "M3" -> MAJOR_3RD;
                case "m6" -> MINOR_6TH;
                case "M6" -> MAJOR_6TH;
                case "m7" -> MINOR_7TH;
                case "M7" -> MAJOR_7TH;
                case "A4" -> AUGMENTED_4TH;
                case "d5" -> DIMINISHED_5TH;
                default -> throw new IllegalArgumentException(
                        "Unknown interval notation: " + notation
                );
            };
        }
    }
}
