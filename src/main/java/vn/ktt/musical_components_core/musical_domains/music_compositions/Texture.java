package vn.ktt.musical_components_core.musical_domains.music_compositions;

public enum Texture {
    ASCENDING,
    DESCENDING,
    STACKED;

    public static Texture fromString(String texture) {
        for (Texture value : values()) {
            if (value.name().equalsIgnoreCase(texture)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown texture: " + texture);
    }
}
