import processing.core.*;


public class Button {
	float x;
	float y;
	float h;
	float w;
	String val = "";
	String text;
	String action;
	String[] args;
	Sketch p;
	int hue = -1;

	int GIVE = 5;
	
	Button(Sketch p, float x, float y, String text, String action){
		this.p = p;
		this.x = x;
		this.y = y;
		this.text = text;
		this.action = action;
	}
	
	Button(Sketch p, float x, float y, String text, String action, String[] args){
		this.p = p;
		this.x = x;
		this.y = y;
		this.text = text;
		this.action = action;
		this.args = args;
	}
	
	public void setPos(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public void setVal(String val) {
		this.val = val;
	}
	
	void update() {
		p.textSize(20);
		h = p.textDescent() + p.textAscent();
		w = p.textWidth(text + val) + GIVE*2;
	}
	
	void display() {
		p.textSize(20);
		p.textAlign(p.LEFT,p.CENTER);
		if(p.overRect(x,y,w,h) && action != "") {
			if(hue == -1) {
				p.fill(150);
			} else {
				p.fill(hue,150,150);
			}
		} else {
			if(hue == -1) {
				p.fill(80);
			} else {
				p.fill(hue,150,100);
			}
		}
		p.noStroke();
		p.rect(x,y,w,h,10);
		if(hue == -1) {
			p.fill(255);
		} else {
			p.fill(hue,50,255);
		}
		p.text(text+val,x+GIVE,y+h*.5f-GIVE);
	}
	
	public void onMousePressed() {
		if(p.overRect(x,y,w,h)) {
			if(args != null) {
				p.doAction(action,args);
			} else {
				p.doAction(action);
			}
		}
	}
	
	public float getX() {return x;}
	public float getY() {return y;}
	public float getW() {return w;}
	public float getH() {return h;}
	
}
