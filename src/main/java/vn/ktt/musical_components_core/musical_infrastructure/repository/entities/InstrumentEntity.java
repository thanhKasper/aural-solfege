package vn.ktt.musical_components_core.musical_infrastructure.repository.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.ktt.musical_components_core.musical_domains.instruments.InstrumentType;

import java.util.UUID;

@Entity
@Table("instrument")
@Getter
@Setter
@NoArgsConstructor
public class InstrumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false)
    private InstrumentType instrumentType;

    @Column(name = "lowest_pitch", nullable = false)
    private String lowestPitch;

    @Column(name = "highest_pitch", nullable = false)
    private String highestPitch;
}
