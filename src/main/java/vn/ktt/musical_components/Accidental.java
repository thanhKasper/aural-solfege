package vn.ktt.musical_components;

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
