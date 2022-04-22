
public class Bubble {
	float x,y;
	float vx,vy;
	float drag = 0.05f;
	float sr, br;
	boolean held;
	int heldCount = 0;
	String title;
	String body;
	
	int popped = -1;
	int state = 0;
	float statePos;
	float stateVel;
	float stateDrag = 0.15f;
	
	Sketch s;
	
	int GIVE = 8;
	
	Bubble(Sketch s, float x, float y, String title, String body) {
		this.s = s;
		this.x = x;
		this.y = y;
		this.title = title;
		this.body = body;
		
		s.textSize(30);
		sr = s.textWidth(title)*.5f + GIVE;
		
		s.textSize(20);
		br = Math.max(sr,s.textWidth(body)*.5f + GIVE*2);
	}
	
	void update() {
		float r = s.map(statePos,0,100,sr,br);
		if(!s.mousePressed && s.dist(s.mouseX,s.mouseY,x,y) < r ) {
			state = 100;
		} else {
			state = 0;
		}
		state = s.constrain(state,0,100);
		
		stateVel += (state - statePos)*.05;
		stateVel -= stateVel*stateDrag;
		statePos += stateVel;
		
		float smooth = 100.0f;
		vx += (.5f - s.noise(s.frameCount/smooth,x/smooth,y/smooth))*.1;
		vy += (.5f - s.noise(s.frameCount/smooth,x/smooth,(y+10000)/smooth))*.1;
		
		vx -= vx*drag*s.map(statePos,0,100,1,8);
		vy -= vy*drag*s.map(statePos,0,100,1,8);
		
		if(held) {
			vx = (s.mouseX - s.pmouseX);
			vy = (s.mouseY - s.pmouseY);
			heldCount++;
		} else {
			heldCount = -1;
		}
		
		x+=vx;
		y+=vy;
		
		if(x - r < 0) {
			vx *= -1;
			x=r;
		}
		if(x + r > s.width) {
			vx *= -1;
			x=s.width-r;
		}
		if(y - r < 0) {
			vy *= -1;
			y=r;
		}
		if(y + r > s.height) {
			vy *= -1;
			y=s.height-r;
		}
	}
	
	void display() {
		s.textAlign(s.CENTER,s.CENTER);
		
		float r = s.map(statePos,0,100,sr,br);
		
		if(popped >= 0) {
			r += popped*5;
			
			s.stroke(220,150,255,200 - popped*5);
			s.strokeWeight(3);
			s.fill(220,50,80,200 - popped*5);
			s.ellipse(x,y,r*2,r*2);
			
			popped+=10;
			return;
		}
		
		s.stroke(220,150,255,200);
		s.strokeWeight(3);
		s.fill(220,50,80,200 + statePos);
		s.ellipse(x,y,r*2,r*2);
		
		s.textSize(30 + statePos*.2f);
		s.fill(220,150,255,255-statePos*(300.0f/100));
		s.text(title, x, y-4);
		
		float fix = 1 + 20*statePos*.01f;
		if(fix <= 0) fix = 0.1f;
		s.textSize(fix);
		s.fill(220,100,255,statePos*(300.0f/100));
		s.text(body, x, y-4);
		
		// to here
		if(s.kept == null ) {
			float here = statePos;
			float mr = r;
			if(held) {
				here = 100;
				mr = br;
			}
			s.stroke(220,150,255,here);
			s.strokeWeight(3);
			s.fill(220,50,80,here);
			s.ellipse(s.width-br-s.jourBar,s.height-br,mr*2,mr*2);
			s.textSize(30);
			s.fill(220,100,255,here*(300.0f/100));
			s.text("KEEP", s.width-br-s.jourBar,s.height-br-4);
		}
		
	}
	
	boolean mousePressed() {
		float r = s.map(statePos,0,100,sr,br);
		
		if(s.dist(s.mouseX,s.mouseY,x,y) < r ) {
			held = true;
		}
		
		return held;
	}
	
	boolean mouseReleased() {
		if(held) {
			held = false;
			if(heldCount < 10) {
				popped = 0;
				return false;
			}
			if(s.dist(s.width-br-s.jourBar,s.height-br, s.mouseX, s.mouseY) < br*2) {
				return true;
			}
			
		}
		return false;
	}
	
}
