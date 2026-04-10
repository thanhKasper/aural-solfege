package vn.ktt.musical_components.music_elements;

import vn.ktt.shared.ISoundPlayer;

public class Pitch implements Comparable<Pitch> {
    private final Note note;
    private final Accidental accidental;
    private final Octave octave;
    private final ISoundPlayer soundPlayer;

    public Pitch(Note note, Accidental accidental, Octave octave, ISoundPlayer soundPlayer) {
        this.note = note;
        this.accidental = accidental;
        this.octave = octave;
        this.soundPlayer = soundPlayer;
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

    public void makeSound() {
        soundPlayer.play();
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
}
