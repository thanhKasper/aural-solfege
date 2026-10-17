package vn.ktt.music.infrastructure.audio.renderer;

public record PcmSamples(float[] samples, float sampleRate, int channels) {
}
