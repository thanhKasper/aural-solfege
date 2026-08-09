package vn.ktt.musical_components_core.musical_infrastructure.adapters;

import org.springframework.stereotype.Repository;
import vn.ktt.musical_components_core.musical_application.sound_configuration.outbound.InstrumentConfigurationPort;
import vn.ktt.musical_components_core.musical_domains.instruments.Instrument;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;
import vn.ktt.musical_components_core.musical_infrastructure.repository.MusicalConfigurationRepository;
import vn.ktt.musical_components_core.musical_infrastructure.repository.entities.InstrumentEntity;

@Repository
public class MusicalConfigurationDataSourceAdapter implements InstrumentConfigurationPort {
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
