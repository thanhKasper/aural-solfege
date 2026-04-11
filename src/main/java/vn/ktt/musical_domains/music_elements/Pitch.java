package vn.ktt.musical_domains.music_elements;

public record Pitch(Note note, Accidental accidental, Octave octave) implements Comparable<Pitch> {

    @Override
    public String toString() {
        return note.toString() + accidental.getAccidental() + octave.getIntegerOctave();
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
        if (this.note.getIntegerNote() > pitch.note().getIntegerNote()) {
            return 1;
        } else if (this.note.getIntegerNote() < pitch.note().getIntegerNote()) {
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
        C(1),
        D(2),
        E(3),
        F(4),
        G(5),
        A(6),
        B(7);

        private final int noteNumber;

        Note(int noteNumber) {
            this.noteNumber = noteNumber;
        }

        public int getIntegerNote() {
            return this.noteNumber;
        }

        public static boolean isNote(String note) {
            try {
                Note.valueOf(note);
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
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
    }
}
