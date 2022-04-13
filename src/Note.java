import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

public class Note {
	public long start;
	public int pitch;
	public int velocity;
	public long length;
	
	
	Note(Track t, int i, Sequence s){
		MidiEvent me = t.get(i);
		MidiMessage mm = me.getMessage();
		int stat = mm.getStatus();
		
		pitch = mm.getMessage()[1];
		velocity = mm.getMessage()[2];
		start = me.getTick();
		
		for(++i; i < t.size(); i++){
			if(t.get(i).getMessage().getMessage()[1] == pitch) {
				if( t.get(i).getMessage().getStatus() == stat-0x10) {
					length = t.get(i).getTick() - start;
					if(length < s.getResolution()/16) {
						length = s.getResolution()/16;
					}
					break;
				}
			}
		}
	}
	
}
