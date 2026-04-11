package vn.ktt.musical_domains.music_compositions;

import vn.ktt.musical_domains.music_elements.Pitch;
import vn.ktt.sound_controller.ISoundPlayer;

public class Interval {
    private IntervalNumber intervalNumber;
    private Pitch basePitch;
    private ISoundPlayer soundPlayer;

    public Interval(Pitch basePitch, IntervalNumber intervalNumber, ISoundPlayer soundPlayer) {
        this.basePitch = basePitch;
        this.intervalNumber =  intervalNumber;
        this.soundPlayer = soundPlayer;
    }

    public void makeUpwardSound() {

    }

    public void makeDownwardSound() {}

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
}
