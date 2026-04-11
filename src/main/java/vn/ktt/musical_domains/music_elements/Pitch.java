package vn.ktt.musical_domains.music_elements;

import vn.ktt.sound_controller.ISoundPlayer;

public class Pitch implements Comparable<Pitch> {
    private final Note note;
    private final Accidental accidental;
    private final Octave octave;

    public Pitch(Note note, Accidental accidental, Octave octave) {
        this.note = note;
        this.accidental = accidental;
        this.octave = octave;
    }

    @Override
    public String toString() {
        return note.toString() + accidental.getAccidental() + octave.getIntegerOctave();
    }

    public Note getNote() {
        return note;
    }

    public Accidental getAccidental() {
        return accidental;
    }

    public Octave getOctave() {
        return octave;
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
        if (this.octave.getIntegerOctave() > pitch.getOctave().getIntegerOctave()) {
            return 1;
        } else if (this.octave.getIntegerOctave() < pitch.getOctave().getIntegerOctave()) {
            return -1;
        }
        return 0;
    }

    private int compareNote(Pitch pitch) {
        if (this.note.getIntegerNote() > pitch.getNote().getIntegerNote()) {
            return 1;
        } else if (this.note.getIntegerNote() < pitch.getNote().getIntegerNote()) {
            return -1;
        }
        return 0;
    }

    private int compareAccidental(Pitch pitch) {
        if (pitch.accidental == pitch.getAccidental()) {
            return 0;
        } else if (this.accidental == Accidental.FLAT && pitch.accidental != Accidental.FLAT) {
            return -1;
        } else if (this.accidental == Accidental.SHARP && pitch.accidental != Accidental.SHARP) {
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
            } catch(IllegalArgumentException ex) {
                return false;
            }
        }
    }

    public enum Accidental {
        SHARP("#"), FLAT("♭"), NONE("");
        private final String accidental;

        Accidental(String accidental) {
            this.accidental = accidental;
        }

        public String getAccidental() {
            return accidental;
        }
    }
}
