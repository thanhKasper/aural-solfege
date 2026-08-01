package vn.ktt.musical_components_core.musical_infrastructure.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ktt.musical_components_core.musical_domains.music_services.IMusicalOperation;
import vn.ktt.musical_components_core.musical_domains.music_services.MusicalOperation;

@Configuration
public class MusicalDomainServiceConfig {

    @Bean
    public IMusicalOperation musicalOperation() {
        return new MusicalOperation();
    }
}
