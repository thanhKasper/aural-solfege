package vn.ktt.musical_components_core.musical_domains.music_atom;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record Pitch(Note note, Accidental accidental, Octave octave) implements Comparable<Pitch> {

    private static final int BASE_MIDI_NOTE = 12;
    private static final int SOUND_PER_OCTAVE = 12;
    private static final int HALF_STEP = 1;

    @Override
    public String toString() {
        return note.toString() + accidental.getAccidental() + octave.getIntegerOctave();
    }

    public int toMidiNumber() {
        return BASE_MIDI_NOTE + SOUND_PER_OCTAVE * this.octave.getIntegerOctave() + this.note.getHalfStepDistanceFromDo() + this.accidental.getIntegerAccidental();
    }

    public Pitch getPitchAfterHalfSteps(int halfSteps) {
        var newMidiNumber = this.toMidiNumber() + halfSteps;
        return Pitch.convertFromMidiNumber(newMidiNumber);
    }

    public static Pitch convertFromMidiNumber(int currentMidiNumber) {
        var starterMidiNumber = currentMidiNumber - BASE_MIDI_NOTE;
        var octavePosition = starterMidiNumber / SOUND_PER_OCTAVE;
        var octave = Octave.fromInt(octavePosition);
        var halfStepsFromDo = starterMidiNumber % SOUND_PER_OCTAVE;
        if (Note.isValidHalfStepFromDo(halfStepsFromDo)) {
            var note = Note.getNoteFromHalfStepDistanceFromDo(halfStepsFromDo);
            return new Pitch(note, Accidental.NONE, octave);
        }
        else {
            var validHalfStepFromDo = halfStepsFromDo - HALF_STEP;
            var note = Note.getNoteFromHalfStepDistanceFromDo(validHalfStepFromDo);
            return new Pitch(note, Accidental.SHARP, octave);
        }
    }

    public String toNotation() {
        return this.note.toString() + this.accidental.toNotation() + this.octave.getIntegerOctave();
    }

    public static boolean isNotValid(String pitchNotation) {
        if (pitchNotation.isBlank()) return true;
        else if (pitchNotation.length() > 3 || pitchNotation.length() == 1) return true;

        var note = pitchNotation.substring(0, 1);
        if (!Note.isNote(note.toUpperCase())) return true;
        if (pitchNotation.length() == 2) {
            var octavePos = pitchNotation.substring(pitchNotation.length() - 1);
            return isOctavePositionInvalid(octavePos);
        } else {
            var accidental = pitchNotation.substring(1, 2);
            var octavePos = pitchNotation.substring(pitchNotation.length() - 1);
            if (!accidental.equals("#") && !accidental.equals("b")) return true;
            return isOctavePositionInvalid(octavePos);
        }
    }

    private static boolean isOctavePositionInvalid(String octavePosition) {
        try {
            int intOctavePos = Integer.parseInt(octavePosition);
            return !Octave.isValid(intOctavePos);
        } catch (NumberFormatException ex) {
            return true;
        }
    }

    public static Note extractNote(String pitchNotation) {
        if (isNotValid(pitchNotation)) throw new IllegalArgumentException("Unknown pitch notation " + pitchNotation);
        return Note.valueOf(pitchNotation.substring(0, 1).toUpperCase());
    }

    public static Accidental extractAccidental(String pitchNotation) {
        if (isNotValid(pitchNotation)) throw new IllegalArgumentException("Unknown pitch notation " + pitchNotation);
        String accidental = pitchNotation.substring(1, pitchNotation.length() - 1);
        return Accidental.getFromString(accidental);
    }

    public static Octave extractOctave(String pitchNotation) {
        if (isNotValid(pitchNotation)) throw new IllegalArgumentException("Unknown pitch notation " + pitchNotation);
        int intOctavePosition = Integer.parseInt(pitchNotation.substring(pitchNotation.length() - 1));
        return Octave.fromInt(intOctavePosition);
    }

    @Override
    public int compareTo(Pitch pitch) {
        if (pitch == null) {
            throw new NullPointerException();
        }
        if (this.compareOctavePosition(pitch) != 0) {
            return this.compareOctavePosition(pitch);
        } else if (this.compareNote(pitch) != 0) {
            return this.compareNote(pitch);
        }
        return compareAccidental(pitch);
    }

    private int compareOctavePosition(Pitch pitch) {
        if (this.octave.getIntegerOctave() > pitch.octave().getIntegerOctave()) {
            return 1;
        } else if (this.octave.getIntegerOctave() < pitch.octave().getIntegerOctave()) {
            return -1;
        }
        return 0;
    }

    private int compareNote(Pitch pitch) {
        if (this.note.getHalfStepDistanceFromDo() > pitch.note().getHalfStepDistanceFromDo()) {
            return 1;
        } else if (this.note.getHalfStepDistanceFromDo() < pitch.note().getHalfStepDistanceFromDo()) {
            return -1;
        }
        return 0;
    }

    private int compareAccidental(Pitch pitch) {
        if (this.accidental == pitch.accidental) {
            return 0;
        } else if (this.accidental == Accidental.FLAT) {
            return -1;
        } else if (this.accidental == Accidental.SHARP) {
            return 1;
        } else if (this.accidental == Accidental.NONE) {
            if (pitch.accidental == Accidental.SHARP) {
                return -1;
            } else if (pitch.accidental == Accidental.FLAT) {
                return 1;
            }
        }
        return 0;
    }

    public enum Octave {
        ZEROTH(0),
        FIRST(1),
        SECOND(2),
        THIRD(3),
        FOURTH(4),
        FIFTH(5),
        SIXTH(6),
        SEVENTH(7),
        EIGHT(8);

        private final int octavePosition;

        Octave(int octavePosition) {
            this.octavePosition = octavePosition;
        }

        public int getIntegerOctave() {
            return octavePosition;
        }

        public static Octave fromInt(int octavePosition) {
            for (Octave octave : values()) {
                if (octave.octavePosition == octavePosition) {
                    return octave;
                }
            }
            throw new IllegalArgumentException("No octave found for position: " + octavePosition);
        }

        public static boolean isValid(int checkOctavePos) {
            for (Octave octave : values()) {
                if (checkOctavePos == octave.getIntegerOctave()) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum Note {
        C(0),
        D(2),
        E(4),
        F(5),
        G(7),
        A(9),
        B(11);

        private final int halfStepDistance;

        Note(int halfStepDistance) {
            this.halfStepDistance = halfStepDistance;
        }

        public int getHalfStepDistanceFromDo() {
            return this.halfStepDistance;
        }

        private static final Set<String> NOTE_NAMES = Arrays.stream(Note.values())
                .map(Note::name)
                .collect(Collectors.toUnmodifiableSet());

        private static final Map<Integer, Note> DISTANCE_MAP = Arrays.stream(Note.values())
                .collect(Collectors.toMap(n -> n.halfStepDistance, n -> n));

        public static boolean isNote(String note) {
            return note != null && NOTE_NAMES.contains(note);
        }

        public static Note getNoteFromHalfStepDistanceFromDo(int halfStepDistance) {
            Note note = DISTANCE_MAP.get(halfStepDistance);
            if (note == null) {
                throw new IllegalArgumentException("No natural note exists at distance: " + halfStepDistance);
            }
            return note;
        }

        public static boolean isValidHalfStepFromDo(int distance) {
            return DISTANCE_MAP.containsKey(distance);
        }
    }

    public enum Accidental {
        SHARP("♯"), FLAT("♭"), NONE("");
        private final String accidental;

        Accidental(String accidental) {
            this.accidental = accidental;
        }

        public String getAccidental() {
            return accidental;
        }

        public static Accidental getFromString(String stringAccidental) {
            return switch (stringAccidental) {
                case "#" -> Accidental.SHARP;
                case "b" -> Accidental.FLAT;
                case "" -> Accidental.NONE;
                default -> throw new IllegalArgumentException("Unknown accidental " + stringAccidental);
            };
        }

        public int getIntegerAccidental() {
            return switch (this) {
                case SHARP -> 1;
                case FLAT -> -1;
                case NONE -> 0;
            };
        }

        public String toNotation() {
            return switch (this) {
                case SHARP -> "#";
                case FLAT -> "b";
                case NONE -> "";
            };
        }
    }
}
