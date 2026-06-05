package vn.ktt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.ktt.ear_training_system.domain.factory.ExerciseBuilder;
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
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean
    protected ExerciseBuilder exerciseBuilder() {
        return new ExerciseBuilder();
    }

    @Bean
    protected IMusicalEntityFactory musicalEntityFactory() {
        return new MusicalEntityFactory();
    }

    @Bean
    protected IMusicalOperation musicalOperation() {
        return new MusicalOperation();
    }
}
