import processing.core.PApplet;
import processing.core.PFont;
import processing.event.MouseEvent;
import processing.opengl.PGraphicsOpenGL;


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
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

public class Sketch extends PApplet implements MetaEventListener{
	
	float BPM;
	int pitch = 0;
	int pitchcheat = 0;
	int songkey;
	int mode = 0;
	int songChoice = 0;
	
	long view;
	int wheelOff;
	boolean channelHeld;
	
	ArrayList<Song> library = new ArrayList<Song>();
	Song song;
	
	Sequencer sequencer;
	public Sequence sequence;
	public Synthesizer synthesizer;
	
	Channel[] channels = new Channel[16];
	int activeCount = 0;
	int topNote = 108;
	int botNote = 21;
	
	boolean bright = true;
	
	// Whole Whole Half Whole Whole Whole Half
	int[] majStep = new int[] {2,2,1,2,2,2,1};
	
	//UI stuff
	int topBar = 40*2;
	int chanBar = 200;
	int jourBar = 70;
	
	int GIVE = 10;
	int[] barW = new int[] {0,0};
	
	int channelThick = 160;
	int pchannelScroll;
	int channelScroll;
	
	int tutCount = 60*60;
	Bubble tut;
	

	Button bSongName;
	Button bTempoUp;
	Button bTempo;
	Button bTempoDown;
	Button bPitchUp;
	Button bPitch;
	Button bPitchDown;
	Button bToMajor;
	Button bToMinor;
	Button bNextSong;
	Button bPrevSong;
	Button bInspiration;
	Button bUIMode;
	
	Button bRestart;
	Button bPause;
	Button bPlay;
	
	Button[][] bs;
	
	PFont f;
	
	
	ArrayList<Bubble> grab = new ArrayList<Bubble>();
	ArrayList<Bubble> bubs = new ArrayList<Bubble>();
	ArrayList<Bubble> jbubs = new ArrayList<Bubble>();
	Bubble kept;
	
	// this gets the PApplet sketch up and running
	public static void main(String[] args) {
		String[] processingArgs = {"Sketch"};
		Sketch mySketch = new Sketch();
		PApplet.runSketch(processingArgs, mySketch);
	}
	
	public void settings() {
		//size(1024, 576);
		fullScreen();
	}
	
	public void setup() {
		//String[] fontList = PFont.list();
		//println(fontList);
		f = createFont("Ariel",12);
		textFont(f);
		colorMode(HSB);
		for(int i = 0; i < channels.length; i++) {
			channels[i] = new Channel(this,i);
		}
		
		library.add(new Song("I Want To Hold Your Hand.mid",7,0));
		library.add(new Song("Beggin.mid",-1,-2));
		library.add(new Song("September.mid",-3,0));
		library.add(new Song("Toxic.mid",0,-2));
		//library.add(new Song("AngelIslandZone1.mid",0,4));
		//library.add(new Song("AngelIslandZone2.mid",0,2));
		library.add(new Song("Undertale-Megalovania.mid",2,-2));
		//library.add(new Song("FonsaMyma(Night).mid",4,-2));
		//library.add(new Song("PopStar.mid",5,0));
		//library.add(new Song("GreenHillZone.mid",0,0));
		//library.add(new Song("ffxiii_desperate.mid",6,-2));
		//library.add(new Song("Undertale-SpearOfJustice.mid",2,-2));
		//library.add(new Song("Dua Lipa - Don't Start Now.mid",12-1,-2));
		
		song = library.get(songChoice);
		
		loadSong(song);
		/*
		Tempo: Tempo is the speed of the music. Click ???+??? to increase speed and ???-??? to decrease it.

		Pitch: Pitch refers to individual sounds in a piece of music. Clicking ???+,??? moves all sounds higher 
		(higher pitch is like when a person inhales helium), and ???-??? moves all sounds lower. 

		Maj: If a song is not in a major key, this button changes just a few sounds throughout the music. Major keys are often associated with happy sounding music.

		Min: If a song is not in a minor key, this button changes just a few sounds throughout the music. Minor keys are often associated with sad sounding music.
		 */
		
		bSongName= new Button(this,0,0,"Playing: ","");
		bTempoUp = new Button(this,0,0,"+","tempoUp");
		bTempo = new Button(this,0,0,"Tempo: ","");
		bTempo.tip = "Tempo is the speed of the music";
		bTempoDown = new Button(this,0,0,"-","tempoDown");
		bPitchUp = new Button(this,0,0,"+","pitchUp");
		bPitch = new Button(this,0,0,"Pitch: ","");
		bPitch.tip = "Makes all of the notes go up or down by this many steps";
		bPitchDown = new Button(this,0,0,"-","pitchDown");
		bToMajor = new Button(this,0,0,"Maj","toMaj");
		bToMajor.tip = "If the song is in minor key, this changes some of the notes to make it major";
		bToMinor = new Button(this,0,0,"Min","toMin");
		bToMinor.tip = "If the song is in major key, this changes some of the notes to make it minor";
		bNextSong = new Button(this,0,0,"???","nextSong");
		bPrevSong = new Button(this,0,0,"???","prevSong");
		bInspiration = new Button(this,0,0,"Need Inspiration?","bubble");
		bInspiration.hue = 200;
		bInspiration.lithue = 200;
		bUIMode = new Button(this,0,0,"UI Mode","bright");
		bUIMode.invert = true;
		bRestart = new Button(this,0,0,"??????","toStart");
		bPause = new Button(this,0,0,"???","pause");
		bPlay = new Button(this,0,0,"???","play");
		bs = new Button[][] {
			{
				bUIMode,
				bTempoDown,
				bTempo,
				bTempoUp,
				bPitchDown,
				bPitch,
				bPitchUp,
				bToMajor,
				bToMinor,
				bInspiration,
			},
			{
				bPrevSong,
				bSongName,
				bNextSong,
				bRestart,
				bPause,
				bPlay,
			}
		};
		
		
		tut = new Bubble(this,200,200,"Bubbles","This is an\nInspiration Bubble!\nYou can click on it to pop it\nDrag it to keep it around\nOr save it on the sidebar\nwhen you're done!");
		
		grab.add(new Bubble(this,200,200,"Mood","Think a bit about\nhow this song makes you feel\nNow make it feel more relaxed\nMake it feel more frantic"));
		grab.add(new Bubble(this,200,200,"Lullaby","See if you can turn this song\ninto a lullaby"));
		grab.add(new Bubble(this,200,200,"Spooky","Can you make this\nsong sound more spooky?"));
		grab.add(new Bubble(this,200,200,"Minimal","How many tracks\ncan you mute and\nstill recognize the\nsong?"));
		//grab.add(new Bubble(this,200,200,"Mood","Think a bit about\nhow this song makes you feel\nNow make it feel more relaxed\nMake it feel more frantic"));
		//grab.add(new Bubble(this,200,200,"Mood","Think a bit about\nhow this song makes you feel\nNow make it feel more relaxed\nMake it feel more frantic"));
		
		//tut = new Bubble(this,200,200,"Hover\nOver\nMe","Bubble contain chalenges\nand are spawned when you\nclick the \"Need Inspiration\" button.\nIf you'd like to remove them from view\nclick to pop.\nIf you're interested in saving them\ndrag to the right sidebar.");
		//bubs.add(tut);
		
		//bubs.add(new Bubble(this,50,50,"Test","This is a test?\nWell ok then ..."));
		//bubs.add(new Bubble(this,450,50,"Test2","This is a second test?\nNow wait a minue ..."));
	}
	
	public void loadSong(Song s) {
		// load the song and start playing
    	try {
			synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sequencer = s.loadSong(this);
		songkey = s.ogKey;
		mode = s.ogMode;
		sequence = sequencer.getSequence();
        view = sequence.getResolution()*16*2;
        BPM = s.ogBPM;
    	pitch = 0;
    	sequencer.addMetaEventListener(this);
    	cleanMIDI();
    	loadNotes();
    	
	}
	
	public void unloadSong() {
		sequencer.close();
		for(int i = 0; i < channels.length; i++) {
			channels[i].clear();
		}
	}
	
	public void draw() {
		
		for(int i = 0; i < bubs.size(); i++) {
			for(int j = i+1; j < bubs.size(); j++) {
				bubs.get(i).checkCol(bubs.get(j));
			}
		}
		
		if(bright) {
			background(40,50,180);
		} else {
			background(0);
		}
		
		
		tutCount--;
		if(tutCount == 0) {
			bubs.add(tut);
		}
		
		// tracks
		
		if(channelHeld) {
			channelScroll += pmouseY - mouseY;
		}
		
		channelScroll = constrain(channelScroll,0,activeCount*channelThick - height + topBar);
		if(pchannelScroll/channelThick != channelScroll/channelThick) {
			createChannelCanvases();
		}
		pchannelScroll = channelScroll;
		
		long now = sequencer.getTickPosition();
		now -= (160.0 * BPM * sequence.getResolution() / 60000);
		int xoff = (int)map((now-view/2), 0, view, 0, width);
		
		int count = 0;
		for(int i = 0; i < 16; i++) {
			if(channels[i].active) {
				float y = count*channelThick + topBar - channelScroll;
				channels[i].display(this, y, xoff);
				count++;
			}
		}
		if(bright) {
			stroke(0);
		} else {
			stroke(255);
		}
		line(width/2,0,width/2,height);
		
		
		// top bar
		if(bright) {
			fill(40, 40, 220);
			stroke(40, 40, 150);
		} else {
			fill(20);
			stroke(0);
		}
		strokeWeight(3);
		rect(0,0,width,topBar);
		
		float fixed = ((int)(BPM*100/song.ogBPM)/100.0f);
		String fixeds = "" + fixed;
		if(fixeds.length() == 3) {
			fixeds = fixeds + "0";
		}
		bTempo.setVal("x"+ fixeds);
		bPitch.setVal(""+pitch);
		bSongName.setVal(""+song.filename);
		for(int i = 0; i < bs.length; i++) {
			float x = GIVE;
			for(Button b: bs[i]) {
				b.setPos(x + width*.5f - barW[i]*.5f, topBar*.5f*i + (topBar*.5f - b.getH())/2.0f);
				b.update();
				b.display();
				x += b.getW() + GIVE;
				if(b == bTempoDown || b == bTempo || b == bPitchDown || b == bPitch || b == bToMajor
						|| b == bPrevSong || b == bSongName || b == bPause || b == bRestart) {
					x -= GIVE;
				}
			}
			barW[i] = (int)x;
		}
		
		
		//journal bar
		if(mouseX > width - jourBar) {
			if(bright) {
				fill(40,40,140);
				stroke(40,40,100);
			} else {
				fill(120);
				stroke(100);
			}
		} else {
			if(bright) {
				fill(40,40,230);
				stroke(40,40,150);
			} else {
				fill(20);
				stroke(0);
			}
		}
		strokeWeight(3);
		rect(width,0,-jourBar,height);
		fill(255);
		for(int i = 0; i < jbubs.size(); i++) {
			float y = jourBar*.5f + jourBar*i;
			
			stroke(220,150,255,200);
			strokeWeight(3);
			fill(220,50,80,200);
			ellipse(width-jourBar*.5f,y,jourBar-GIVE,jourBar-GIVE);
			
			textSize(16);
			fill(220,150,255,255);
			textAlign(CENTER,CENTER);
			text(jbubs.get(i).title, width-jourBar*.5f,y-4);
		}
		if(bright) {
			stroke(40,50,200);
			fill(40,50,150);
		} else {
			stroke(100);
			fill(50);
		}
		strokeWeight(3);
		ellipse(width-jourBar*.5f, jourBar*.5f + jourBar*jbubs.size(),jourBar-GIVE,jourBar-GIVE);
		textSize(30);
		fill(40,50,200);
		textAlign(CENTER,CENTER);
		text("+",width-jourBar*.5f, jourBar*.5f + jourBar*jbubs.size()-4);
		
		
		
		//bubbles
		if(kept != null) {
			kept.state = 100;
			kept.statePos = 100;
			kept.x = width-kept.br-jourBar;
			kept.y = height-kept.br;
			kept.display();
		}
		for(int i = 0; i < bubs.size(); i++) {
			bubs.get(i).update();
			bubs.get(i).display();
			if(bubs.get(i).popped > 100) {
				bubs.get(i).popped = -1;
				grab.add(bubs.remove(i));
				println(grab.size());
				i--;
			}
		}
		
		//tool tip
		for(int i = 0; i < bs.length; i++) {
			for(Button b: bs[i]) {
				b.toolTip();
			}
		}
		
		/*
		for(int i = 0; i < 16; i++) {
			if(channels[i].active) {
				drawPianoRollChannel(i, 0,height/activeCount * i,width,height/activeCount,now - view/2, now + view/2);
			}
		}
		*/
		
		
		
		
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
	
	public void mousePressed() {
		if(mouseX > width - jourBar) {
			for(int i = 0; i < jbubs.size(); i++) {
				float y = jourBar*.5f + jourBar*i;
				if(dist(mouseX,mouseY,width-jourBar*.5f,y) < jourBar*.5f-GIVE) {
					Bubble b = jbubs.remove(i);
					b.held = true;
					b.x = width-jourBar*.5f;
					b.y = y;
					bubs.add(b);
				}
			}
		}
		if(kept != null && kept.mousePressed()) {
			bubs.add(kept);
			kept = null;
			return;
		}
		for(Bubble b: bubs) {
			if(b.mousePressed()) {
				return;
			}
		}
		for(Button[] barr: bs) {
			for(Button b: barr) {
				b.onMousePressed();
			}
		}
		if(mouseY > topBar) {
			for(Channel c: channels) {
				for(Button[] barr: c.bs) {
					for(Button b: barr) {
						b.onMousePressed();
					}
				}
			}
			if(mouseX > chanBar) {
				channelHeld = true;
			}
		}
	}
	
	public void mouseReleased() {
		Bubble temp = null;
		channelHeld = false;
		for(Bubble b: bubs) {
			if(b.held&& mouseX > width-jourBar) {
				temp = b;
			}
			if(b.mouseReleased()) {
				if(kept != null) {
					temp = kept;
				}
				kept = b;
				temp = null;
			}
		}
		if(kept != null) {
			bubs.remove(kept);
		}
		if(temp != null) {
			if(mouseX > width-jourBar) {
				jbubs.add(temp);
				bubs.remove(temp);
				if(kept != null && temp == kept) {
					kept = null;
				}
				return;
			}
			bubs.add(temp);
			temp.statePos = 0;
			temp.vx = -25;
			temp.vy = -25;
		}
	}
	
	public void mouseWheel(MouseEvent e) {
		channelScroll += pow(e.getCount(),3)*5;
	}
	
	public boolean overRect(float x, float y, float w, float h) {
		float mx = mouseX;
		float my = mouseY;
		return mx > x && mx < x+w && my > y && my < y+h;
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
			doAction("tempoUp");
		} else if(key == 's') {
			doAction("tempoDown");
		} else if(key == 'd') {
			doAction("pitchUp");
		} else if(key == 'a') {
			doAction("pitchDown");
		} else if(key == 'n') {
			doAction("prevSong");
		} else if(key == 'm') {
			doAction("nextSong");
		}
		
		int num = key - '0';
		if(num >= 0 && num <= 6) {
			doAction("toMode", new String[] {"" + num});
		}
		
	}
	public void doAction(String action) {
		doAction(action,null);
	}
	
	public void doAction(String action, String[] args) {
		if(action == "tempoUp") {
			BPM += 5;
			sequencer.setTempoInBPM(BPM);
		} else if(action == "tempoDown") {
			BPM -= 5;
			sequencer.setTempoInBPM(BPM);
		} else if(action == "pitchUp") {
			pitchShift(1);
			pitch++;
			pitchcheat++;
			songkey++;
			createChannelCanvases();
			recalculateAllRange();
		} else if(action == "pitchDown") {
			pitchShift(-1);
			pitch--;
			pitchcheat--;
			songkey--;
			createChannelCanvases();
			recalculateAllRange();
		} else if(action == "toMaj") {
			modeToMode(mode,0);
			mode = 0;
			createChannelCanvases();
			recalculateAllRange();
			sequencer.setTempoInBPM(BPM);
		} else if(action == "toMin") {
			modeToMode(mode,5);
			mode = 5;
			createChannelCanvases();
			recalculateAllRange();
		} else if(action == "toMode") {
			int to = Integer.parseInt(args[0]);
			modeToMode(mode,to);
			mode = to;
			createChannelCanvases();
			recalculateAllRange();
		} if(action == "nextSong") {
			unloadSong();
			songChoice = (songChoice+1)%library.size();
			song = library.get(songChoice);
			loadSong(song);
		} else if(action == "prevSong") {
			unloadSong();
			songChoice = (songChoice-1+library.size())%library.size();
			song = library.get(songChoice);
			loadSong(song);
		} else if(action == "pause") {
			sequencer.stop();
		} else if(action == "play") {
			sequencer.start();
		} else if(action == "toStart") {
			sequencer.setTickPosition(0);;
		} else if(action == "mute") {
			int c = Integer.parseInt(args[0]);
			println(c);
			sequencer.setTrackMute(c+1, !sequencer.getTrackMute(c+1));
		} else if(action == "solo") {
			int c = Integer.parseInt(args[0]);
			println(c);
			sequencer.setTrackSolo(c+1, !sequencer.getTrackSolo(c+1));
		} else if(action == "prevInst") {
			int c = Integer.parseInt(args[0]);
			println(c);
			//sequencer.
		} else if (action == "bubble") {
			if(tutCount > 0) {
				bubs.add(tut);
				tutCount = -1;
			} else if (grab.size() > 0){
				Bubble temp = grab.remove((int)random(grab.size()));
				temp.x = random(200,width-200);
				temp.y = random(200,height-200);
				bubs.add(temp);
				
			}
			
		} else if (action == "bright") {
			bright = !bright;
			createChannelCanvases();
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
		for(int i = 0; i < 16; i++) {
			channels[i].clear();
			channels[i].bMute.on = false;
			channels[i].bSolo.on = false;
			channels[i].mc = synthesizer.getChannels()[i];
		}

		topNote = 0;
		botNote = 200;
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
				} else if(stat >= 0xC0 && stat <= 0xCF) {
					channels[stat - 0xC0].prog = ((ShortMessage)mm).getData1();
				}
			}
		}
		
		for(Channel c: channels) {
			c.calculateRange();
			topNote = max(c.maxPitch,topNote);
			botNote = min(c.minPitch,botNote);
			if(c.active) {
				activeCount++;
			}
		}
		
		createChannelCanvases();
	}
	
	public void createChannelCanvases() {
		Channel c;
		int account = 0;
		for(int i = 0; i < 16; i++) {
			c = channels[i];
			if(!c.active) {
				continue;
			}
			float cy = account*channelThick + topBar - channelScroll;
			long end = sequencer.getTickLength();
			int inw = (int)map(sequencer.getTickLength() ,0, view , 0, width);
			//int inh = (int)map(1,0,activeCount,0,height);
			int inh = channelThick;
			
			if(cy < -channelThick || cy > height+channelThick) {
				c.roll = null;
				account++;
				continue;
			}
			
			c.roll = createGraphics(inw,inh);
			c.roll.beginDraw();
			c.roll.colorMode(HSB);
			
			if(bright) {
				c.roll.fill(220);
			} else {
				c.roll.fill(0);
			}
			c.roll.rect(0,0,inw,inh);
			
			int pitchslack = 2;
			
			float x;
			float y;
			float w;
			//float h = inh/128;
			float h = inh/(topNote+pitchslack - botNote-pitchslack) * 2;
			
			//lines
			long beat = sequence.getResolution();
			int count = 0;
			c.roll.noStroke();
			for(long l = 0; l < end; l+= beat/4 ) {
				x = map(l, 0, end, 0, inw);
				if(count%(4*4) == 0) {
					if(bright) {
						c.roll.fill(150);
					} else {
						c.roll.fill(75);
					}
					c.roll.rect(x,0,2,height);
				} else if(count%4 == 0) {
					if(bright) {
						c.roll.fill(175);
					} else {
						c.roll.fill(50);
					}
					c.roll.rect(x,0,1,height);
				} else {
					if(bright) {
						c.roll.fill(200);
					} else {
						c.roll.fill(25);
					}
					c.roll.rect(x,0,1,height);
				}
				
				count++;
			}
			
			if(bright) {
				c.roll.fill(i*15,150,200);
			} else {
				c.roll.fill(i*15,85,130);
			}
			c.roll.noStroke();
			c.roll.rect(0,0,inw,2);
			c.roll.rect(0,inh,inw,-2);
			
			c.roll.strokeWeight(1);
			
			for(Note n: c) {
				x = map(n.start, 0, end, 0, inw);
				//y = map(n.pitch+pitchcheat,0,128, inh, 0);
				y = map(n.pitch+pitchcheat,botNote-pitchslack,topNote+pitchslack, inh, 0);
				w = map(n.length,0, end, 0, inw);
				//w = 2;
				
				//stroke(i*20,155,255);
				//strokeWeight(.5f);
				c.roll.noStroke();
				if(bright) {
					c.roll.fill(i*15,n.velocity*2,220);
				} else {
					c.roll.fill(i*15,255,n.velocity*2);
				}
				c.roll.rect(x,y,w,h);
			}
	
			c.roll.endDraw();
			account++;
		}
	}
	
	public void recalculateAllRange() {
		for(Channel c: channels) {
			c.calculateRange();
		}
	}
	

}
