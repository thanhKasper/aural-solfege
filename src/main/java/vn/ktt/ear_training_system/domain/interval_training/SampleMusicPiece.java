package vn.ktt.ear_training_system.domain.interval_training;

public class SampleMusicPiece {
    private String musicPiece;
    private final MusicalInterval dominantInterval;
    private final IntervalTexture intervalTexture;
    private int startRange;
    private int endRange;

    public SampleMusicPiece(String musicPiece, MusicalInterval interval, IntervalTexture texture, int startRange, int endRange) {
        updateMusicPiece(musicPiece);
        this.dominantInterval = interval;
        this.intervalTexture = texture;
        updateRange(startRange, endRange);
    }

    private void updateMusicPiece(String musicPiece) {
        if (musicPiece.isBlank()) throw new IllegalArgumentException("Music piece cannot be empty");
        this.musicPiece = musicPiece;
    }

    private void updateRange(int startRange, int endRange) {
        if (endRange < startRange) throw new IllegalArgumentException("start range cannot larger than end range");
        else if (startRange < 0) throw new IllegalArgumentException("start range cannot be a negative number");
        this.startRange = startRange;
        this.endRange = endRange;
    }

    public String getMusicPiece() {
        return musicPiece;
    }

    public MusicalInterval getDominantInterval() {
        return dominantInterval;
    }

    public IntervalTexture getIntervalTexture() {
        return intervalTexture;
    }

    public int getStartRange() {
        return startRange;
    }

    public int getEndRange() {
        return endRange;
    }
}
