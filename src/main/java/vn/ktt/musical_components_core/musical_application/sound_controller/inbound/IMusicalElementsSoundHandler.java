package vn.ktt.musical_components_core.musical_application.sound_controller.inbound;

public interface IMusicalElementsSoundHandler {
    void playStackedInterval(String interval);
    void playBrokenInterval(String interval, String anchoredPitch, boolean isUpward);
}
