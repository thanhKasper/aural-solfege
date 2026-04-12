package vn.ktt.musical_components_core.musical_domains.music_compositions;

import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;

public class Chord {
    private final Pitch rootKey;
    private final Quality quality;

    public Chord(Pitch rootKey, Quality quality) {
        this.rootKey = rootKey;
        this.quality = quality;
    }

    public String toString() {
        return rootKey.toString() + quality.getStringNotation();
    }

    public enum Quality {
        MAJOR(new int[]{0, 4, 7}),
        MINOR(new int[]{0, 3, 7});

        private final int[] intervals;

        Quality(int[] intervals) {
            this.intervals = intervals;
        }

        public int[] getIntervals() {
            return intervals;
        }

        public String getStringNotation() {
            if (this == Quality.MINOR) {
                return "m";
            }
            return "";
        }
    }
}
