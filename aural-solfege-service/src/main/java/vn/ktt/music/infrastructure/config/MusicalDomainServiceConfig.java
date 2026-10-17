package vn.ktt.music.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.ktt.music.domain.service.IMusicalOperation;
import vn.ktt.music.domain.service.MusicalOperation;

@Configuration
public class MusicalDomainServiceConfig {

    @Bean
    public IMusicalOperation musicalOperation() {
        return new MusicalOperation();
    }
}
