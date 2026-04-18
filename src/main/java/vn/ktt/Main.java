package vn.ktt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import vn.ktt.ear_training_system.domain.ExerciseBuilder;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }

    @Bean
    protected ExerciseBuilder exerciseBuilder() {
        return new ExerciseBuilder();
    }
}
