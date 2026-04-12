package vn.ktt.musical_components_core.musical_application.sound_controller;

import vn.ktt.musical_components_core.musical_application.sound_controller.inbound.IMusicalElementsSoundHandler;
import vn.ktt.musical_components_core.musical_application.sound_controller.outbound.ISoundPlayer;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;

import java.util.Arrays;

public class MusicalElementsSoundHandler implements IMusicalElementsSoundHandler {
    private final ISoundPlayer soundPlayer;
    private final IMusicalEntityFactory musicalElementFactory;
    public MusicalElementsSoundHandler(ISoundPlayer soundPlayer, IMusicalEntityFactory factory) {
        this.soundPlayer = soundPlayer;
        this.musicalElementFactory = factory;
    }


    @Override
    public void playStackedInterval(String interval, String pitch) {
        var selectedInterval = this.musicalElementFactory.getInterval(interval);
        var firstPitch = this.musicalElementFactory.getPitch(pitch);
        var secondPitch = selectedInterval.upwardPitch(firstPitch);
        this.soundPlayer.playStackedSound(Arrays.asList(firstPitch.toNotation(), secondPitch.toNotation()));
    }

    @Override
    public void playBrokenInterval(String interval, String anchoredPitch, boolean isUpward) {
        var selectedInterval = this.musicalElementFactory.getInterval(interval);
        var startPitch = this.musicalElementFactory.getPitch(anchoredPitch);
        var endPitch = isUpward ? selectedInterval.upwardPitch(startPitch) : selectedInterval.downwardPitch(startPitch);
        this.soundPlayer.playSingleSound(startPitch.toNotation());
        this.soundPlayer.playSingleSound(endPitch.toNotation());
    }
}
