package vn.ktt.music.application.sound.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioContent {
    private byte[] data;
    private int fileSize;
    private String fileName;
}
