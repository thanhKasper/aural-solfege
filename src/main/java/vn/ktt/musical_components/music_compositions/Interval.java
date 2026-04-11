package vn.ktt.musical_components.music_compositions;

import vn.ktt.musical_components.music_elements.Pitch;
import vn.ktt.shared.sound_player.ISoundPlayer;

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
}
