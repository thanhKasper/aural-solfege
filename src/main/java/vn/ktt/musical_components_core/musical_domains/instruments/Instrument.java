package vn.ktt.musical_components_core.musical_domains.instruments;

import lombok.Getter;
import vn.ktt.musical_components_core.musical_domains.music_atom.Pitch;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;

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
