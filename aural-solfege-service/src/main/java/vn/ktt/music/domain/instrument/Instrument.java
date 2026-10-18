package vn.ktt.music.domain.instrument;

import lombok.Getter;
import vn.ktt.music.domain.atom.Pitch;
import vn.ktt.music.domain.factory.IMusicalEntityFactory;

@Getter
public class Instrument {
    private final InstrumentType instrumentType;
    private final Pitch lowestPitch;
    private final Pitch highestPitch;

    // Not allow other class to use default initialization
    private Instrument(InstrumentType instrumentType, Pitch lowestPitch, Pitch highestPitch) {
        this.instrumentType = instrumentType;
        this.lowestPitch = lowestPitch;
        this.highestPitch = highestPitch;
    }

    public static Instrument reconstruct(IMusicalEntityFactory musicalEntityFactory, InstrumentType instrumentType, String lowestPitch, String highestPitch) {
        return new Instrument(instrumentType, musicalEntityFactory.getPitch(lowestPitch), musicalEntityFactory.getPitch(highestPitch));
    }
}
