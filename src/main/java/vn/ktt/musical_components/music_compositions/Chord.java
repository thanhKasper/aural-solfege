package vn.ktt.musical_components.music_compositions;

import vn.ktt.musical_components.music_elements.Pitch;
import vn.ktt.shared.sound_player.ISoundPlayer;

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
        soundPlayer.play();
    };

    public String toString() {
        return rootKey.toString() + quality.getStringNotation();
    }
}
