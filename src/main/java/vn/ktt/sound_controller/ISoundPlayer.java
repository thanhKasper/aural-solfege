package vn.ktt.sound_controller;

import vn.ktt.musical_components.music_compositions.Chord;
import vn.ktt.musical_components.music_compositions.Interval;
import vn.ktt.musical_components.music_elements.Pitch;

public interface ISoundPlayer {
    void playPitch(Pitch pitch);
    void playTriad(Chord chord);
    void playInterval(Interval interval);
}
