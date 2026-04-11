package vn.ktt.musical_components.music_compositions;

import vn.ktt.musical_components.music_elements.Pitch;
import vn.ktt.sound_controller.ISoundPlayer;

public class Chord {
    private final Pitch rootKey;
    private final Quality quality;
    private final ISoundPlayer soundPlayer;

    public Chord(Pitch rootKey, Quality quality, ISoundPlayer soundPlayer) {
        this.rootKey = rootKey;
        this.quality = quality;
        this.soundPlayer =  soundPlayer;
    }

    public void makeSound() {
        soundPlayer.playTriad(this);
    };

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
