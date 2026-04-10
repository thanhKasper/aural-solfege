package vn.ktt.musical_components.music_compositions;

public enum Quality {
    MAJOR(new int[]{0, 4, 7}),
    MINOR(new int[]{0, 3, 7});

    private final int[] intervals;

    Quality(int[] intervals) {
        this.intervals = intervals;
    }

    public int[] getIntervals() {
        return intervals;
    }

    public String getStringNotation() {
        if (this == Quality.MINOR) {
            return "m";
        }
        return "";
    }
}
