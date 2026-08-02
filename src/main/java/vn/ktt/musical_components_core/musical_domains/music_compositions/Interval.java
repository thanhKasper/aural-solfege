package vn.ktt.musical_components_core.musical_domains.music_compositions;

import lombok.Getter;
import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;

import java.util.HashMap;
import java.util.Map;

@Getter
@SuppressWarnings("ClassCanBeRecord")
public class Interval implements Comparable<Interval> {
    private final IntervalNumber intervalNumber;

    public Interval(IntervalNumber intervalNumber) {
        this.intervalNumber = intervalNumber;
    }

    public Interval(String intervalNotation) {
        this.intervalNumber = IntervalNumber.fromNotation(intervalNotation);
    }

    @Override
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
        return Integer.compare(this.intervalNumber.getHalfSteps(), interval.intervalNumber.getHalfSteps());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interval interval)) return false;
        return this.intervalNumber.getHalfSteps() == interval.intervalNumber.getHalfSteps();
    }

    @Override
    public int hashCode() {
        return this.intervalNumber.getHalfSteps();
    }

    public enum Texture {
        ASCENDING,
        DESCENDING,
        STACKED;

        public static Texture fromString(String texture) {
            for (Texture value : values()) {
                if (value.name().equalsIgnoreCase(texture)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown texture: " + texture);
        }
    }

    public enum IntervalNumber {
        UNISON(0, "P0"),
        MINOR_2ND(1, "m2"),
        MAJOR_2ND(2, "M2"),
        MINOR_3RD(3, "m3"),
        MAJOR_3RD(4, "M3"),
        PERFECT_4TH(5, "P4"),
        AUGMENTED_4TH(6, "A4"),
        TRITONE(6, "TT"),
        DIMINISHED_5TH(6, "d5"),
        PERFECT_5TH(7, "P5"),
        MINOR_6TH(8, "m6"),
        MAJOR_6TH(9, "M6"),
        MINOR_7TH(10, "m7"),
        MAJOR_7TH(11, "M7"),
        PERFECT_OCTAVE(12, "P8");

        private static final Map<String, IntervalNumber> BY_NOTATION = buildLookup();

        @Getter
        private final int halfSteps;
        private final String notation;

        IntervalNumber(int halfSteps, String notation) {
            this.halfSteps = halfSteps;
            this.notation = notation;
        }

        private static Map<String, IntervalNumber> buildLookup() {
            Map<String, IntervalNumber> lookup = new HashMap<>();
            for (IntervalNumber value : values()) {
                lookup.put(value.notation, value);
            }
            return Map.copyOf(lookup);
        }

        public String toNotation() {
            return notation;
        }

        public static IntervalNumber fromNotation(String notation) {
            IntervalNumber intervalNumber = BY_NOTATION.get(notation);
            if (intervalNumber == null) {
                throw new IllegalArgumentException("Unknown interval notation: " + notation);
            }
            return intervalNumber;
        }
    }
}
