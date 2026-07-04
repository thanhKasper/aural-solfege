package vn.ktt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import vn.ktt.musical_components_core.musical_domains.music_factory.IMusicalEntityFactory;
import vn.ktt.musical_components_core.musical_domains.music_factory.MusicalEntityFactory;
import vn.ktt.musical_components_core.musical_domains.music_services.IMusicalOperation;
import vn.ktt.musical_components_core.musical_domains.music_services.MusicalOperation;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }


    @Bean
    protected IMusicalOperation musicalOperation() {
        return new MusicalOperation();
    }
}
