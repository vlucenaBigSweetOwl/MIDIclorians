import processing.core.*;


public class Button {
	float x;
	float y;
	float h;
	float w;
	String val = "";
	String text;
	String action;
	Sketch p;

	int GIVE = 10;
	
	Button(Sketch p, float x, float y, String text, String action){
		this.p = p;
		this.x = x;
		this.y = y;
		this.text = text;
		this.action = action;
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
		h = p.textDescent() + GIVE*2;
		w = p.textWidth(text + val) + GIVE*2;
	}
	
	void display() {
		p.textSize(20);
		p.textAlign(p.LEFT,p.TOP);
		if(p.overRect(x,y,w,h) && action != "") {
			p.fill(100);
		} else {
			p.fill(50);
		}
		p.noStroke();
		p.rect(x,y,w,h,10);
		p.fill(255);
		p.text(text+val,x+GIVE,y);
	}
	
	public void onMousePressed() {
		if(p.overRect(x,y,w,h)) {
			p.doAction(action);
		}
	}
	
	public float getX() {return x;}
	public float getY() {return y;}
	public float getW() {return w;}
	public float getH() {return h;}
	
}
