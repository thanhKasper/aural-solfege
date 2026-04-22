package vn.ktt.musical_components_core.musical_application.sound_controller;

import org.springframework.stereotype.Service;
import vn.ktt.musical_components_core.musical_application.sound_controller.inbound.IMusicalElementsSoundHandler;
import vn.ktt.musical_components_core.musical_application.sound_controller.outbound.ISoundPlayer;
import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval.*;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;
import vn.ktt.musical_components_core.musical_domains.music_services.IMusicalOperation;
import java.util.Arrays;

@Service
public class MusicalElementsSoundHandler implements IMusicalElementsSoundHandler {
    private final ISoundPlayer soundPlayer;
    private final IMusicalEntityFactory musicalElementFactory;
    private final IMusicalOperation musicalOperation;

    public MusicalElementsSoundHandler(ISoundPlayer soundPlayer, IMusicalEntityFactory factory, IMusicalOperation musicalOperation) {
        this.soundPlayer = soundPlayer;
        this.musicalElementFactory = factory;
        this.musicalOperation = musicalOperation;
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

    @Override
    public void playAscendingSequentially(String interval) {
        Pitch lowestPitch = musicalOperation.getLowestPitch();
        Pitch highestIntervalBasePitch = musicalOperation.getHighestLowerBoundIntervalPitch(IntervalNumber.fromNotation(interval));
        while (lowestPitch.compareTo(highestIntervalBasePitch) <= 0) {
            var intervalDomain = musicalElementFactory.getInterval(interval);
            soundPlayer.playSingleSound(lowestPitch.toNotation());
            soundPlayer.playSingleSound(intervalDomain.upwardPitch(lowestPitch).toNotation());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException exception) {
                System.out.println("Cannot sleep. ZZZZZ");
            }
            lowestPitch = lowestPitch.getPitchAfterHalfSteps(1);
        }
    }

    @Override
    public void playDescendingSequentially(String interval) {
        Pitch lowestPitch = musicalOperation.getLowestUpperBoundIntervalPitch(IntervalNumber.fromNotation(interval));
        Pitch highestIntervalBasePitch = musicalOperation.getHighestPitch();
        while (lowestPitch.compareTo(highestIntervalBasePitch) <= 0) {
            var intervalDomain = musicalElementFactory.getInterval(interval);
            soundPlayer.playSingleSound(lowestPitch.toNotation());
            soundPlayer.playSingleSound(intervalDomain.downwardPitch(lowestPitch).toNotation());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException exception) {
                System.out.println("Cannot sleep. ZZZZZ");
            }
            lowestPitch = lowestPitch.getPitchAfterHalfSteps(1);
        }
    }

    @Override
    public void playStackSequentially(String interval) {

    }
}
