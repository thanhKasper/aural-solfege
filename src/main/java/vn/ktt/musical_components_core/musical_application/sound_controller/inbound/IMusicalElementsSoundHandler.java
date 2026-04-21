package vn.ktt.musical_components_core.musical_application.sound_controller.inbound;

public interface IMusicalElementsSoundHandler {
    void playStackedInterval(String interval, String pitch);
    void playBrokenInterval(String interval, String anchoredPitch, boolean isUpward);
    void playAscendingSequentially(String interval);
    void playDescendingSequentially(String interval);
    void playStackSequentially(String interval);
}
