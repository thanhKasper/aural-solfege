package vn.ktt.musical_components_core.musical_infrastructure.repository.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "musical_config")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class MusicalConfigurationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(targetEntity = InstrumentEntity.class, optional = false)
    private InstrumentEntity activeInstrument;
}
