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
		} catch (MidiUnavailableException | IOException | InvalidMidiDataException e) {
			e.printStackTrace();
		}
        
	}
	
	public void draw() {
		background(0);
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
	ArrayList<MidiEvent> list;
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
				if( stat >= 0x80 && stat <= 0x9F && stat != 0x89 && stat != 0x99) {
					t.remove(me);
					i--;
					//t.add(new MidiEvent(new MidiMessage(), me.getTick()));
					//me.getMessage().setMessage(me.getMessage().getMessage(), me.getMessage().getLength());
					ShortMessage you = new ShortMessage();
					//println(String.format("%02X",mm.getMessage()[0]) + " " + 
					//		String.format("%02X",mm.getMessage()[1]) + " " + 
					//		String.format("%02X",mm.getMessage()[2]) + " "
					//);
					
					if(mm.getMessage()[2] == 0x00 && stat > 0x8F) {
						stat -= 16;
						//println(String.format("%02X",stat));
					}
					
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
					
					if(mm.getMessage()[2] == 0x00 && stat > 0x8F) {
						stat -= 16;
					}
					
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
					
					if(mm.getMessage()[2] == 0x00 && stat > 0x8F) {
						stat -= 16;
					}
					
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
	
	public void meta(MetaMessage meta) {
		if(meta.getType() == 0x51) {
			print("AYE!");
			sequencer.setTempoInBPM(BPM);
		}
		
	}
	
	public static void main(String[] args) {
		String[] processingArgs = {"Sketch"};
		Sketch mySketch = new Sketch();
		PApplet.runSketch(processingArgs, mySketch);
		
		
	}

}
