package vn.ktt.musical_components_core.musical_domains.instruments;

import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;

public class Piano implements Instrument {
    @Override
    public InstrumentType getInstrumentType() {
        return InstrumentType.PIANO;
    }

    @Override
    public Pitch getLowestPitch() {
        return new Pitch(Pitch.Note.A, Pitch.Accidental.NONE, Pitch.Octave.ZEROTH);
    }

    @Override
    public Pitch getHighestPitch() {
        return new Pitch(Pitch.Note.C, Pitch.Accidental.NONE, Pitch.Octave.EIGHT);
    }
}
