package vn.ktt.musical_infrastructure;

import vn.ktt.musical_application.sound_controller.outbound.ISoundPlayer;
import vn.ktt.musical_domains.music_factory.IMusicalEntityFactory;

import javax.sound.midi.*;
import java.io.IOException;
import java.io.InputStream;

public class MidiSoundPlayer implements ISoundPlayer {
    private Synthesizer synthesizer;
    private final IMusicalEntityFactory factory;
    public MidiSoundPlayer(IMusicalEntityFactory musicalEntityFactory) {
        this.factory = musicalEntityFactory;
        try {
            this.synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            try (InputStream inputStream = getClass().getResourceAsStream("/Steinway Grand piano.sf2")) {
                if (inputStream == null) {
                    System.out.println("sf2 file not found, switch back to default file");
                    return;
                }
                Soundbank defaultSoundbank = MidiSystem.getSoundbank(inputStream);
                System.out.println("Test soundbank file load");
                for (javax.sound.midi.Instrument inst : defaultSoundbank.getInstruments()) {
                    System.out.println(inst.getName() + " " + inst.getPatch().getProgram());
                }
                synthesizer.unloadAllInstruments(synthesizer.getDefaultSoundbank());
                synthesizer.loadAllInstruments(defaultSoundbank);
            }
        }
        catch (MidiUnavailableException ex) {
            ex.printStackTrace();
        } catch (InvalidMidiDataException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void playPitch(String pitch) {
        var domainPitch = this.factory.getPitch(pitch);
        try {
            MidiChannel channel = synthesizer.getChannels()[3];
            channel.noteOn(domainPitch.toMidiNumber(), 100);
            Thread.sleep(1000);
            channel.noteOff(60);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
