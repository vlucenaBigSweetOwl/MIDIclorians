import java.util.ArrayList;

import javax.sound.midi.Sequencer;

import processing.core.PGraphics;

public class Channel extends ArrayList<Note> {
	public int maxPitch;
	public int minPitch;
	public boolean active;
	
	public int writeInt = 0;
	
	PGraphics roll;
	
	Channel(){
		super();
		maxPitch = 0;
		minPitch = 128;
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
	
	
	
	
}
