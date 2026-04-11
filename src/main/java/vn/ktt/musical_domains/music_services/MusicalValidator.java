package vn.ktt.musical_domains.music_services;


import vn.ktt.musical_domains.music_elements.Pitch.*;

public class MusicalValidator {
    boolean validPitchNotation(String notation) {
        if (notation.isBlank()) return false;
        else if (notation.length() > 3) return false;

        var note = notation.substring(0,1);
        if (!Note.isNote(note.toUpperCase())) return false;
        if (notation.length() == 2) {
            var octavePos = notation.substring(notation.length() - 1);
            return isOctavePositionValid(octavePos);
        }
        else {
            var accidental = notation.substring(1,2);
            var octavePos = notation.substring(notation.length() - 1);
            if (!accidental.equals("#") && !accidental.equals("b")) return false;
            return isOctavePositionValid(octavePos);
        }
    }

    private boolean isOctavePositionValid(String octavePosition) {
        try {
            int intOctavePos = Integer.parseInt(octavePosition);
            return Octave.isValid(intOctavePos);
        }
        catch (NumberFormatException ex) {
            return false;
        }
    }

    boolean validChordNotation(String notation) {
        return true;
    }

    boolean validIntervalNotation(String notation) {
        return true;
    }
}
