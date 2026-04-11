package vn.ktt.musical_domains.music_factory;

import vn.ktt.musical_domains.music_compositions.Chord;
import vn.ktt.musical_domains.music_compositions.Interval;
import vn.ktt.musical_domains.music_elements.Pitch;
import vn.ktt.musical_domains.music_services.MusicalValidator;

public class MusicalEntityFactory {
    private final MusicalValidator validator;
    public MusicalEntityFactory(MusicalValidator validator) {
        this.validator = validator;
    }
    public Pitch getPitch(String noteNotation) {
        return null;
    }
    public Chord getChord(String chordNotation) {
        return null;
    }
    public Interval getInterval(String intervalNotation) {
        return null;
    }
}
