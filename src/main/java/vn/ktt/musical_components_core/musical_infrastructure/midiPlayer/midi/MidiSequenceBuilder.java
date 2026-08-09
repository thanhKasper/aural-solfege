package vn.ktt.musical_components_core.musical_infrastructure.midiPlayer.midi;

import org.springframework.stereotype.Component;
import vn.ktt.musical_components_core.musical_application.sound_controller.dtos.IntervalRangeParameters;
import vn.ktt.musical_components_core.musical_domains.music_compositions.Interval;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

@Component
public class MidiSequenceBuilder {

    private static final int PPQ = 480;
    private static final int NOTE_CHANNEL = 0;
    private static final int VELOCITY = 90;
    private static final int MELODIC_NOTE_TICKS = 180;
    private static final int STACKED_NOTE_TICKS = 360;
    private static final int GAP_TICKS = 20;
    private static final int HIGHEST_MIDI_NOTE = 108;
    private static final int PREROLL_TICKS = 60;

    public Sequence build(IntervalRangeParameters parameters) {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, PPQ);
            Track track = sequence.createTrack();

            int halfSteps = parameters.getInterval().getIntervalType().getHalfSteps();
            int baseStart = parameters.getLowestPitch().toMidiNumber();
            int baseEnd = Math.min(parameters.getHighestPitch().toMidiNumber(), HIGHEST_MIDI_NOTE - halfSteps);

            Interval.Texture texture = parameters.getIntervalTexture();
            boolean stacked = texture == Interval.Texture.STACKED;
            boolean descending = texture == Interval.Texture.DESCENDING;
            int noteTicks = stacked ? STACKED_NOTE_TICKS : MELODIC_NOTE_TICKS;
            int sweepStep = parameters.isReverse() ? -1 : 1;

            long tick = PREROLL_TICKS;
            for (int base = parameters.isReverse() ? baseEnd : baseStart;
                 parameters.isReverse() ? base >= baseStart : base <= baseEnd;
                 base += sweepStep) {
                int high = base + halfSteps;

                if (stacked) {
                    scheduleNote(track, base, tick, noteTicks);
                    scheduleNote(track, high, tick, noteTicks);
                    tick += noteTicks + GAP_TICKS;
                } else if (descending) {
                    scheduleNote(track, high, tick, noteTicks);
                    tick += noteTicks + GAP_TICKS;
                    scheduleNote(track, base, tick, noteTicks);
                    tick += noteTicks + GAP_TICKS;
                } else {
                    scheduleNote(track, base, tick, noteTicks);
                    tick += noteTicks + GAP_TICKS;
                    scheduleNote(track, high, tick, noteTicks);
                    tick += noteTicks + GAP_TICKS;
                }
            }
            return sequence;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build MIDI sequence", e);
        }
    }

    private void scheduleNote(Track track, int note, long startTick, int durationTicks) throws InvalidMidiDataException {
        track.add(new MidiEvent(
                new ShortMessage(ShortMessage.NOTE_ON, NOTE_CHANNEL, note, VELOCITY), startTick));
        track.add(new MidiEvent(
                new ShortMessage(ShortMessage.NOTE_OFF, NOTE_CHANNEL, note, 0), startTick + durationTicks));
    }
}
