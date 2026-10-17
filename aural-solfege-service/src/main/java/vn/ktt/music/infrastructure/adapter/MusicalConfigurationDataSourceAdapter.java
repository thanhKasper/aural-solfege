package vn.ktt.music.infrastructure.adapter;

import org.springframework.stereotype.Repository;
import vn.ktt.music.application.instrument.outbound.IInstrumentConfigurationPort;
import vn.ktt.music.domain.instrument.Instrument;
import vn.ktt.music.domain.factory.IMusicalEntityFactory;
import vn.ktt.music.infrastructure.persistence.MusicalConfigurationRepository;
import vn.ktt.music.infrastructure.persistence.entity.InstrumentEntity;

@Repository
public class MusicalConfigurationDataSourceAdapter implements IInstrumentConfigurationPort {
    private final IMusicalEntityFactory musicalEntityFactory;
    private final MusicalConfigurationRepository musicalConfigurationRepository;

    public MusicalConfigurationDataSourceAdapter(IMusicalEntityFactory musicalEntityFactory, MusicalConfigurationRepository musicalConfigurationRepository) {
        this.musicalEntityFactory = musicalEntityFactory;
        this.musicalConfigurationRepository = musicalConfigurationRepository;
    }

    @Override
    public Instrument getActiveInstrument() {
        var result = musicalConfigurationRepository.findAll().getFirst();
        InstrumentEntity activeInstrument = result.getActiveInstrument();
        return Instrument.reconstruct(
                musicalEntityFactory,
                activeInstrument.getInstrumentType(),
                activeInstrument.getLowestPitch(),
                activeInstrument.getHighestPitch()
        );
    }
}
