package vn.ktt.shared;

import javax.sound.sampled.*;
import java.io.InputStream;

public class SoundPlayer implements ISoundPlayer {
    private Clip soundClip;

    public SoundPlayer(InputStream inputStream) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream);
            this.soundClip = AudioSystem.getClip();
            this.soundClip.open(audioStream);


        } catch (Exception e) {
            // @TODO: handle exception here
            e.printStackTrace();
        }
    }

    @Override
    public void play() {
        this.soundClip.start();
        // Wait for clip duration in milliseconds
        try {
            Thread.sleep(soundClip.getMicrosecondLength() / 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
