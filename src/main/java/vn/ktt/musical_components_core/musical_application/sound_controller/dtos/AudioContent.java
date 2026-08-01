package vn.ktt.musical_components_core.musical_application.sound_controller.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioContent {
    private byte[] data;
    private int fileSize;
    private String fileName;
}
