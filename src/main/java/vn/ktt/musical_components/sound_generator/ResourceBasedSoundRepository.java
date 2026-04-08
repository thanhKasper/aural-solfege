package vn.ktt.musical_components.sound_generator;

import vn.ktt.musical_components.music_elements.Accidental;
import vn.ktt.musical_components.music_elements.Note;
import vn.ktt.musical_components.music_elements.Octave;
import vn.ktt.musical_components.music_elements.Pitch;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ResourceBasedSoundRepository implements ISoundRepository {
    private static final String resourceMap = "sound_map.json";

    public ResourceBasedSoundRepository() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceMap)) {
            // Read lines as a single String (Java 8+)
            assert inputStream != null;
            String result = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining("\n"));
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Pitch getSound(Note note, Accidental accidental, Octave octave) {
        return null;
    }
}

