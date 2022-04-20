import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

public class Song {
	String filename;
	int ogKey;
	int ogMode;
	int ogBPM;
	
	public Song(String filename, int key, int mode){
		this.filename = filename;
		this.ogKey = (60 + key)%12;
		this.ogMode = mode;
		this.ogBPM = -1;
	}
	
	public Sequencer loadSong(Sketch mel) {
		Sequencer sequencer = null;
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			File f = new File("assets/"+filename);
	        InputStream is = new BufferedInputStream(new FileInputStream(f));
	        sequencer.setSequence(is);
	        ogBPM = (int)sequencer.getTempoInBPM();
	        //mel.cleanMIDI();
	        
		} catch (MidiUnavailableException | IOException e) {
			e.printStackTrace();
		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sequencer;
	}
}
