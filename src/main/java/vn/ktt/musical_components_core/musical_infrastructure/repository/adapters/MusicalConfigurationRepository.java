package vn.ktt.musical_components_core.musical_infrastructure.repository.adapters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ktt.musical_components_core.musical_application.sound_configuration.outbound.InstrumentConfigurationPort;
import vn.ktt.musical_components_core.musical_domains.instruments.Instrument;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;
import vn.ktt.musical_components_core.musical_infrastructure.repository.entities.InstrumentEntity;
import vn.ktt.musical_components_core.musical_infrastructure.repository.entities.MusicalConfigurationEntity;

import java.util.UUID;

@Repository
public abstract class MusicalConfigurationRepository implements InstrumentConfigurationPort, JpaRepository<MusicalConfigurationEntity, UUID> {
    private final IMusicalEntityFactory musicalEntityFactory;

    public MusicalConfigurationRepository(IMusicalEntityFactory musicalEntityFactory) {
        this.musicalEntityFactory = musicalEntityFactory;
    }

    @Override
    public Instrument getActiveInstrument() {
        var result = this.findAll().getFirst();
        InstrumentEntity activeInstrument = result.getActiveInstrument();
        return Instrument.reconstruct(
                musicalEntityFactory,
                activeInstrument.getInstrumentType(),
                activeInstrument.getLowestPitch(),
                activeInstrument.getHighestPitch()
        );
    }
}
