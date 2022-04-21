import java.util.ArrayList;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.Sequencer;

import processing.core.PGraphics;

public class Channel extends ArrayList<Note> {
	public int maxPitch;
	public int minPitch;
	public boolean active;
	
	int index = -1;
	public int writeInt = 0;
	
	MidiChannel mc;
	int prog = -1;
	
	PGraphics roll;
	
	
	Button bInstrument;
	Button bMute;
	Button bSolo;
	Button[][] bs;
	int[] barW = new int[] {0,0};
	
	int GIVE = 5;
	
	Channel(Sketch s, int i){
		super();
		maxPitch = 0;
		minPitch = 128;
		index = i;
		
		bInstrument = new Button(s,0,0,"","");
		bMute = new Button(s,0,0,"M","mute", new String[] {""+i});
		bSolo = new Button(s,0,0,"S","solo", new String[] {""+i});
		
		bs = new Button[][] {
			{
				bInstrument,
			},
			{
				bMute,
				bSolo,
			},
		};
		
	}
	
	public void calculateRange() {
		maxPitch = 0;
		minPitch = 128;
		for(Note n: this) {
			maxPitch = Math.max(maxPitch,n.pitch);
			minPitch = Math.min(minPitch,n.pitch);
		}
		
		active = this.size() > 0;
	}
	
	public void display(Sketch s, float y, float xoff) {
		if(roll != null) {
			s.image(roll,-xoff,y,roll.width,s.channelThick);
		}
		
		s.fill(50);
		s.stroke(index*15,85,130);
		s.strokeWeight(3);
		s.rect(0,y,s.sideBar,s.channelThick);
		
		
		String inst = "UNKNOWN";
		if(prog >= 0) {
			inst = s.synthesizer.getAvailableInstruments()[prog].getName();
		}
		bInstrument.setVal(inst);
		for(int i = 0; i < bs.length; i++) {
			float x = GIVE;
			for(Button b: bs[i]) {
				b.setPos(x + s.sideBar*.5f - barW[i]*.5f, y + s.channelThick*.5f*i + (s.channelThick*.5f - b.getH())/2.0f);
				b.update();
				b.display();
				x += b.getW() + GIVE;
				if(false) {
					x -= GIVE;
				}
			}
			barW[i] = (int)x;
		}
	}
	
	
	
	
}
