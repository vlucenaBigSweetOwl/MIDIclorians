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
	int pitchcheat = 0;
	int songkey;
	int mode = 0;
	int songChoice = 1;
	
	long view;
	
	ArrayList<Song> library = new ArrayList<Song>();
	Song song;
	
	Sequencer sequencer;
	public Sequence sequence;
	
	Channel[] channels = new Channel[16];
	int activeCount = 0;
	
	// Whole Whole Half Whole Whole Whole Half
	int[] majStep = new int[] {2,2,1,2,2,2,1};
	
	// this gets the PApplet sketch up and running
	public static void main(String[] args) {
		String[] processingArgs = {"Sketch"};
		Sketch mySketch = new Sketch();
		PApplet.runSketch(processingArgs, mySketch);
	}
	
	public void settings() {
		//size(800,600);
		fullScreen();
	}
	
	public void setup() {
		colorMode(HSB);
		for(int i = 0; i < channels.length; i++) {
			channels[i] = new Channel();
		}
		
		library.add(new Song("Toxic.mid",0,-2));
		library.add(new Song("September.mid",-3,0));
		library.add(new Song("AngelIslandZone1.mid",0,4));
		library.add(new Song("AngelIslandZone2.mid",0,2));
		library.add(new Song("Undertale-Megalovania.mid",2,-2));
		library.add(new Song("FonsaMyma(Night).mid",4,-2));
		library.add(new Song("PopStar.mid",5,0));
		library.add(new Song("GreenHillZone.mid",0,0));
		library.add(new Song("ffxiii_desperate.mid",6,-2));
		//library.add(new Song("Undertale-SpearOfJustice.mid",2,-2));
		
		song = library.get(songChoice);
		
		loadSong(song);
		
	}
	
	public void loadSong(Song s) {
		// load the song and start playing
		sequencer = s.loadSong(this);
		songkey = s.ogKey;
		mode = s.ogMode;
		sequence = sequencer.getSequence();
        view = sequence.getResolution()*8;
        BPM = sequencer.getTempoInBPM();
    	pitch = 0;
    	sequencer.addMetaEventListener(this);
    	
    	cleanMIDI();
    	loadNotes();
    	
        sequencer.start();
	}
	
	public void unloadSong() {
		sequencer.close();
		for(int i = 0; i < channels.length; i++) {
			channels[i].clear();
		}
	}
	
	public void draw() {
		background(0);
		
		
		long now = sequencer.getTickPosition();
		now -= (160.0 * BPM * sequence.getResolution() / 60000);
		int xoff = (int)map((now-view/2), 0, view, 0, width);
		
		int count = 0;
		for(int i = 0; i < 16; i++) {
			if(channels[i].active) {
				float y = map(count,0,activeCount, 0, height);
				image(channels[i].roll,-xoff,y);
				count++;
			}
		}
		
		/*
		for(int i = 0; i < 16; i++) {
			if(channels[i].active) {
				drawPianoRollChannel(i, 0,height/activeCount * i,width,height/activeCount,now - view/2, now + view/2);
			}
		}
		*/
		
		
		stroke(255);
		line(width/2,0,width/2,height);
		
		
		/*
		// rough gui
		fill(255);
		textSize(50);
		text("song: "+song.filename, 100, 100);
		text("pitch shift: "+pitch, 100, 150);
		text("tempo: "+BPM, 100, 200);
		
		noStroke();
		fill(100);
		rect(100,400, width - 200, 10);
		fill(255);
		rect(100,400, map(sequencer.getTickPosition(), 0, sequencer.getTickLength(), 0, width - 200), 10);
		*/
	}
	
	public void drawPianoRollChannel(int i, float inx, float iny, float inw, float inh, long start, long end){
		Channel c = channels[i];
		int pitchslack = 3;
		
		float x;
		float y;
		float w;
		//float h = inh/128;
		float h = inh/(c.maxPitch+pitchslack - c.minPitch-pitchslack) * 2;
		
		//lines
		long beat = sequence.getResolution();
		int count = 0;
		noStroke();
		for(long l = start/(beat*4) * (beat*4); l < end; l+= beat/4 ) {
			x = map(l, start, end, inx, inx+inw);
			if(count%(4*4) == 0) {
				fill(75);
				rect(x,0,2,height);
			} else if(count%4 == 0) {
				fill(50);
				rect(x,0,1,height);
			} else {
				fill(25);
				rect(x,0,1,height);
			}
			
			count++;
		}
		
		stroke(i*20,155,100);
		noFill();
		rect(inx,iny,inw,inh);
		
		for(Note n: c) {
			x = map(n.start, start, end, inx, inx+inw);
			//y = map(n.pitch,0,128, iny+inh, iny);
			y = map(n.pitch,c.minPitch-pitchslack,c.maxPitch+pitchslack, iny+inh, iny);
			w = map(n.length,0, end-start, 0, inw);
			//w = 2;
			
			//stroke(i*20,155,255);
			//strokeWeight(.5f);
			noStroke();
			fill(((n.pitch+pitch)%12)*20,255,n.velocity*2);
			rect(x,y,w,h);
		}
		
		
		
	}
	
	
	
	public void keyPressed() {;
		if(key == 'w') {
			println("ok");
			BPM += 10;
			sequencer.setTempoInBPM(BPM);
		} else if(key == 's') {
			BPM -= 10;
			sequencer.setTempoInBPM(BPM);
		} else if(key == 'd') {
			pitchShift(1);
			pitch++;
			pitchcheat++;
			songkey++;
			createChannelCanvases();
			recalculateAllRange();
		} else if(key == 'a') {
			pitchShift(-1);
			pitch--;
			pitchcheat--;
			songkey--;
			createChannelCanvases();
			recalculateAllRange();
		} else if(key == 'n') {
			unloadSong();
			songChoice = (songChoice-1+library.size())%library.size();
			song = library.get(songChoice);
			loadSong(song);
		} else if(key == 'm') {
			unloadSong();
			songChoice = (songChoice+1)%library.size();
			song = library.get(songChoice);
			loadSong(song);
		}
		
		int num = key - '0';
		if(num >= 0 && num <= 6) {
			modeToMode(mode,num);
			mode = num;
			createChannelCanvases();
			recalculateAllRange();
			//loadNotes();
		}
		
	}
	
	
	// list used to hold notes to be replaced
	ArrayList<MidiEvent> list;
	public void editByLookup(int[] lookup){
		sequencer.stop();
		for(Track t : sequence.getTracks()) {
			list = new ArrayList<MidiEvent>();
			MidiEvent me;
			MidiMessage mm;
			for(Channel c: channels) {
				c.writeInt = 0;
			}
			
			for(int i = 0; i < t.size(); i++) {
				int by = 0;
				me = t.get(i);
				mm = me.getMessage();
				int stat = mm.getStatus();
				if( stat >= 0x80 && stat <= 0x9F && stat != 0x89 && stat != 0x99) {
					
					ShortMessage you = new ShortMessage();
					//println(mm.getMessage()[1] + " " + (mm.getMessage()[1]-songkey+24)%12);
					by = lookup[(mm.getMessage()[1]-songkey+24)%12];
					if(by == 0) {
						continue;
					}
					try {
						t.remove(me);
						i--;
						you.setMessage(stat, mm.getMessage()[1]+by, mm.getMessage()[2]);
						list.add(new MidiEvent(you,me.getTick()));
						if(stat >= 0x90) {
							int channel = stat- 0x90;
							Channel c = channels[channel];
							Note n;
							while(c.writeInt < c.size()) {
								n = c.get(c.writeInt);
								if(n.start == me.getTick()) {
									n.pitch += by;
								}
								c.writeInt++;
							}
						}
					} catch (InvalidMidiDataException e) {
						try {
							you.setMessage(stat, mm.getMessage()[1], mm.getMessage()[2]);
						} catch (InvalidMidiDataException e1) {
							e1.printStackTrace();
						}
					}
					
				} else {
					//println(String.format("%02X",stat));
				}
			}
			for(MidiEvent m: list) {
				t.add(m);
			}
			list.clear();
		}
		sequencer.start();
		sequencer.setTempoInBPM(BPM);
	}
	
	// increase pitch of all notes by "by"
	public void pitchShift(int by) {
		editByLookup(new int[]{by,by,by,by,by,by,by,by,by,by,by,by});
	}
	
	public void modeToMode(int from, int to) {
		sequencer.stop();
		int count = 0;
		int[] lookup = new int[12];
		int fromCount = 0;
		int toCount = 0;
		int j = 0;
		while(fromCount < 12) {
			lookup[fromCount] = toCount - fromCount;
			fromCount += majStep[(j+from+7)%7];
			toCount += majStep[(j+to+7)%7];
			j++;
		}
		
		// SPECIAL CASE: de-blues-ify the minor to major
		if((from+7)%7 == 5 && (to+7)%7 == 0) {
			lookup[6] = 1;
		}
		
		//println(from);
		//println(to);
		//println(lookup);
		editByLookup(lookup);
	}
	
	// pitch shift only notes to make maj to min
	// TODO: get rid of this, modeToMode does the same thing better, but this shows a bit more concretely how to shift from maj to min
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
					//println(mm.getMessage()[1] + " " + mm.getMessage()[1]%12);
					if(mm.getMessage()[1]%12 == (songkey+4)%12 ) {
						by = -1;
					} else if(mm.getMessage()[1]%12 == (songkey+9)%12 ) {
						by = -1;
					} else if(mm.getMessage()[1]%12 == (songkey+11)%12 ) {
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
		
		//println(count);
		sequencer.start();
		sequencer.setTempoInBPM(BPM);
	}
	

	// pitch shift only notes to make min to maj
	// TODO: get rid of this, modeToMode does the same thing better, but this shows a bit more concretely how to shift from min to maj
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
					if(mm.getMessage()[1]%12 == (songkey+3)%12 ) {
						by = 1;
					} else if(mm.getMessage()[1]%12 == (songkey+8)%12 ) {
						by = 1;
					} else if(mm.getMessage()[1]%12 == (songkey+10)%12 ) {
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
		//check for a Set Tempo message
		if(meta.getType() == 0x51) {
			print("AYE!");
			sequencer.setTempoInBPM(BPM);
		}
		//TODO: scale new tempo rather than set it to current BPM
		
		if(meta.getType() == 0x59) {
			print(meta.getStatus());
		}
	}
	

	// get rid of onNote messages that should be offNote
	public void cleanMIDI() {
		for(Track t : sequence.getTracks()) {
			list = new ArrayList<MidiEvent>();
			MidiEvent me;
			MidiMessage mm;
			for(int i = 0; i < t.size(); i++) {
				me = t.get(i);
				mm = me.getMessage();
				int stat = mm.getStatus();
				// find only MIDI messages that are either on or off (but avoid channel 10, where drums usually are)
				if( stat >= 0x90 && stat <= 0x9F && mm.getMessage()[2] == 0x00) {
					t.remove(me);
					i--;
					ShortMessage you = new ShortMessage();
					
					// find "onNote" messages that should be "offNote" and swap it
					stat -= 16;
					
					try {
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
	}
	
	public void loadNotes() {
		pitchcheat = 0;
		for(Channel c: channels) {
			c.clear();
		}
		activeCount = 0;
		for(Track t : sequence.getTracks()) {
			MidiEvent me;
			MidiMessage mm;
			for(int i = 0; i < t.size(); i++) {
				me = t.get(i);
				mm = me.getMessage();
				int stat = mm.getStatus();
				// find only MIDI messages that are either on or off (but avoid channel 10, where drums usually are)
				if( stat >= 0x90 && stat <= 0x9F) {
					channels[stat - 0x90].add(new Note(t,i,sequence));
				}
			}
		}
		
		for(Channel c: channels) {
			c.calculateRange();
			if(c.active) {
				activeCount++;
			}
		}
		
		createChannelCanvases();
	}
	
	public void createChannelCanvases() {
		Channel c;
		
		for(int i = 0; i < 16; i++) {
			c = channels[i];
			if(!c.active) {
				continue;
			}
			long end = sequencer.getTickLength();
			int inw = (int)map(sequencer.getTickLength() ,0, view , 0, width);
			int inh = (int)map(1,0,activeCount,0,height);
	
			c.roll = createGraphics(inw,inh);
			c.roll.beginDraw();
			c.roll.colorMode(HSB);
			int pitchslack = 3;
			
			float x;
			float y;
			float w;
			//float h = inh/128;
			float h = inh/(c.maxPitch+pitchslack - c.minPitch-pitchslack) * 2;
			
			//lines
			long beat = sequence.getResolution();
			int count = 0;
			c.roll.noStroke();
			for(long l = 0; l < end; l+= beat/4 ) {
				x = map(l, 0, end, 0, inw);
				if(count%(4*4) == 0) {
					c.roll.fill(75);
					c.roll.rect(x,0,2,height);
				} else if(count%4 == 0) {
					c.roll.fill(50);
					c.roll.rect(x,0,1,height);
				} else {
					c.roll.fill(25);
					c.roll.rect(x,0,1,height);
				}
				
				count++;
			}
			
			c.roll.stroke(i*20,155,100);
			c.roll.strokeWeight(2);
			c.roll.noFill();
			c.roll.rect(0,0,inw,inh);
			
			c.roll.strokeWeight(1);
			
			for(Note n: c) {
				x = map(n.start, 0, end, 0, inw);
				//y = map(n.pitch,0,128, iny+inh, iny);
				y = map(n.pitch+pitchcheat,c.minPitch-pitchslack,c.maxPitch+pitchslack, inh, 0);
				w = map(n.length,0, end, 0, inw);
				//w = 2;
				
				//stroke(i*20,155,255);
				//strokeWeight(.5f);
				c.roll.noStroke();
				c.roll.fill(((n.pitch+pitchcheat)%12)*20,255,n.velocity*2);
				c.roll.rect(x,y,w,h);
			}
	
			c.roll.endDraw();
		}
	}
	
	public void recalculateAllRange() {
		for(Channel c: channels) {
			c.calculateRange();
		}
	}
	

}
