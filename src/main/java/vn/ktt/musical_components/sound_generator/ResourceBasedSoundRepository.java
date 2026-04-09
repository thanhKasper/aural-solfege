package vn.ktt.musical_components.sound_generator;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import vn.ktt.musical_components.music_elements.Accidental;
import vn.ktt.musical_components.music_elements.Note;
import vn.ktt.musical_components.music_elements.Octave;
import vn.ktt.musical_components.music_elements.Pitch;

import java.io.InputStream;

public class ResourceBasedSoundRepository implements ISoundRepository {
    private static final String resourceMap = "sound_map.json";
    private JsonNode chosenInstrumentNode;
    private JsonNode sounds;
    private static final String chosenInstrument = "piano"; // Subject to change, currently chosen as piano for easy implementation


    public ResourceBasedSoundRepository() {
        var objectMapper = new ObjectMapper();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceMap)) {
            var instruments = objectMapper.readTree(inputStream);
            for (JsonNode instrument : instruments) {
                if (chosenInstrument.equals(instrument.get("instrument").asString())) {
                    this.chosenInstrumentNode = instrument;
                    break;
                }
            }
            this.sounds = chosenInstrumentNode.get("sounds");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Pitch getSound(Note note, Accidental accidental, Octave octave) {
        String rootPath = chosenInstrumentNode.get("rootDir").asString();
        JsonNode selectedSound = sounds.valueStream()
                .filter(sound ->
                        note.toString()
                                .equals(sound.get("note").asString())
                                && accidental.toString()
                                .equals(sound.get("accidental").asString())
                                && octave.getIntegerOctave() == sound.get("octavePosition").asInt())
                .findFirst().orElseThrow();
        System.out.println(selectedSound.get("soundFile").asString());
        return null;
    }
}

