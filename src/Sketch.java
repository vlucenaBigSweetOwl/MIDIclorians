import processing.core.PApplet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class Sketch extends PApplet implements MetaEventListener{
	
	float BPM;
	int pitch = 0;
	String midiFile = "Toxic.mid";
	int ogKey = (60 - 0)%12;

	Sequencer sequencer;
	Sequence sequence;
	

	public void settings() {
		size(800,600);
	}
	
	public void setup() {
		// load the song and start playing
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			File f = new File("assets/"+midiFile);
			println(f.getCanonicalPath());
	        InputStream is = new BufferedInputStream(new FileInputStream(f));
	        sequencer.setSequence(is);
	 
	        sequencer.start();
	        sequencer.addMetaEventListener(this);
	        BPM = sequencer.getTempoInBPM();
	        sequence = sequencer.getSequence();
	        cleanMIDI();
		} catch (MidiUnavailableException | IOException | InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public void draw() {
		background(0);
		
		// rough gui
		fill(255);
		textSize(50);
		text("song: "+midiFile, 100, 100);
		text("pitch shift: "+pitch, 100, 150);
		text("tempo: "+BPM, 100, 200);
		
		noStroke();
		fill(100);
		rect(100,400, width - 200, 10);
		fill(255);
		rect(100,400, map(sequencer.getTickPosition(), 0, sequencer.getTickLength(), 0, width - 200), 10);
		
	}
	
	public void keyPressed() {
		if(key == 'w') {
			BPM += 10;
			sequencer.setTempoInBPM(BPM);
		} else if(key == 's') {
			BPM -= 10;
			sequencer.setTempoInBPM(BPM);
		} else if(key == 'd') {
			pitchShift(1);
			pitch++;
		} else if(key == 'a') {
			pitchShift(-1);
			pitch--;
		} else if(key == 'm') {
			majToMin();
		} else if(key == 'n') {
			minToMaj();
		}
	}
	
	
	// list used to hold notes to be replaced
	ArrayList<MidiEvent> list;
	// get rid of onNote messages that should be offNote
	public void cleanMIDI() {
		sequencer.stop();
		int count = 0;
		for(Track t : sequence.getTracks()) {
			list = new ArrayList<MidiEvent>();
			MidiEvent me;
			MidiMessage mm;
			for(int i = 0; i < t.size(); i++) {
				me = t.get(i);
				mm = me.getMessage();
				int stat = mm.getStatus();
				// find only MIDI messages that are either on or off (but avoid channel 10, where drums usually are)
				if( stat >= 0x80 && stat <= 0x9F && stat != 0x89 && stat != 0x99) {
					t.remove(me);
					i--;
					ShortMessage you = new ShortMessage();
					
					// find "onNote" messages that should be "offNote" and swap it
					if(mm.getMessage()[2] == 0x00 && stat > 0x8F) {
						stat -= 16;
					}
					
					try {
						count++;
						you.setMessage(stat, mm.getMessage()[1], mm.getMessage()[2]);
					} catch (InvalidMidiDataException e) {
						// TODO Auto-generated catch block
						try {
							you.setMessage(stat, mm.getMessage()[1], mm.getMessage()[2]);
						} catch (InvalidMidiDataException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					
					list.add(new MidiEvent(you,me.getTick()));
				}
			}
			for(MidiEvent m: list) {
				t.add(m);
			}
			list.clear();
		}
		
		println(count);
		sequencer.start();
		sequencer.setTempoInBPM(BPM);
	}
	
	// increase pitch of all notes by "by"
	public void pitchShift(int by) {
		sequencer.stop();
		int count = 0;
		for(Track t : sequence.getTracks()) {
			list = new ArrayList<MidiEvent>();
			MidiEvent me;
			MidiMessage mm;
			for(int i = 0; i < t.size(); i++) {
				me = t.get(i);
				mm = me.getMessage();
				int stat = mm.getStatus();
				// find only MIDI messages that are either on or off (but avoid channel 10, where drums usually are)
				if( stat >= 0x80 && stat <= 0x9F && stat != 0x89 && stat != 0x99) {
					t.remove(me);
					i--;
					ShortMessage you = new ShortMessage();
					
					try {
						count++;
						you.setMessage(stat, mm.getMessage()[1]+by, mm.getMessage()[2]);
					} catch (InvalidMidiDataException e) {
						// TODO Auto-generated catch block
						try {
							you.setMessage(stat, mm.getMessage()[1], mm.getMessage()[2]);
						} catch (InvalidMidiDataException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					
					list.add(new MidiEvent(you,me.getTick()));
				} else {
					//println(String.format("%02X",stat));
				}
			}
			for(MidiEvent m: list) {
				t.add(m);
			}
			list.clear();
		}
		
		println(count);
		sequencer.start();
		sequencer.setTempoInBPM(BPM);
	}
	
	// pitch shift only notes to make maj to min
	public void majToMin() {
		sequencer.stop();
		int count = 0;
		for(Track t : sequence.getTracks()) {
			list = new ArrayList<MidiEvent>();
			MidiEvent me;
			MidiMessage mm;
			for(int i = 0; i < t.size(); i++) {
				int by = 0;
				me = t.get(i);
				mm = me.getMessage();
				int stat = mm.getStatus();
				if( stat >= 0x80 && stat <= 0x9F && stat != 0x89 && stat != 0x99) {
					t.remove(me);
					i--;
					ShortMessage you = new ShortMessage();
					
					// flat 3rd, flat 6, flat 7
					println(mm.getMessage()[1] + " " + mm.getMessage()[1]%12);
					if(mm.getMessage()[1]%12 == (ogKey+4)%12 ) {
						by = -1;
					} else if(mm.getMessage()[1]%12 == (ogKey+9)%12 ) {
						by = -1;
					} else if(mm.getMessage()[1]%12 == (ogKey+11)%12 ) {
						by = -1;
					}
					
					try {
						count++;
						you.setMessage(stat, mm.getMessage()[1]+by, mm.getMessage()[2]);
					} catch (InvalidMidiDataException e) {
						try {
							you.setMessage(stat, mm.getMessage()[1], mm.getMessage()[2]);
						} catch (InvalidMidiDataException e1) {
							e1.printStackTrace();
						}
					}
					
					list.add(new MidiEvent(you,me.getTick()));
				} else {
					//println(String.format("%02X",stat));
				}
			}
			for(MidiEvent m: list) {
				t.add(m);
			}
			list.clear();
		}
		
		println(count);
		sequencer.start();
		sequencer.setTempoInBPM(BPM);
	}
	

	// pitch shift only notes to make min to maj
	public void minToMaj() {
		sequencer.stop();
		int count = 0;
		for(Track t : sequence.getTracks()) {
			list = new ArrayList<MidiEvent>();
			MidiEvent me;
			MidiMessage mm;
			for(int i = 0; i < t.size(); i++) {
				int by = 0;
				me = t.get(i);
				mm = me.getMessage();
				int stat = mm.getStatus();
				if( stat >= 0x80 && stat <= 0x9F && stat != 0x89 && stat != 0x99) {
					t.remove(me);
					i--;
					ShortMessage you = new ShortMessage();
					
					println(mm.getMessage()[1] + " " + mm.getMessage()[1]%12);
					if(mm.getMessage()[1]%12 == (ogKey+3)%12 ) {
						by = 1;
					} else if(mm.getMessage()[1]%12 == (ogKey+8)%12 ) {
						by = 1;
					} else if(mm.getMessage()[1]%12 == (ogKey+10)%12 ) {
						by = 1;
					}
					
					try {
						count++;
						you.setMessage(stat, mm.getMessage()[1]+by, mm.getMessage()[2]);
					} catch (InvalidMidiDataException e) {
						try {
							you.setMessage(stat, mm.getMessage()[1], mm.getMessage()[2]);
						} catch (InvalidMidiDataException e1) {
							e1.printStackTrace();
						}
					}
					
					list.add(new MidiEvent(you,me.getTick()));
				} else {
					//println(String.format("%02X",stat));
				}
			}
			for(MidiEvent m: list) {
				t.add(m);
			}
			list.clear();
		}
		
		println(count);
		sequencer.start();
		sequencer.setTempoInBPM(BPM);
	}
	
	// this avoids messages MIDI files send to reset the tempo
	public void meta(MetaMessage meta) {
		if(meta.getType() == 0x51) {
			print("AYE!");
			sequencer.setTempoInBPM(BPM);
		}
		//TODO: scale new tempo rather than set it to current BPM
		
	}
	
	// this gets the PApplet skethc up and running
	public static void main(String[] args) {
		String[] processingArgs = {"Sketch"};
		Sketch mySketch = new Sketch();
		PApplet.runSketch(processingArgs, mySketch);
		
		
	}

}
