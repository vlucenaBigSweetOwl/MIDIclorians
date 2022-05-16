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
	int lithue = -1;
	boolean togglable;
	boolean on;
	boolean invert = false;
	
	int overTick;
	String tip = "";

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
		
		//button
		boolean over = p.overRect(x,y,w,h);
		if((over && action != "")) {
			if(lithue == -1) {
				
				p.fill(150);
			} else {
				p.fill(lithue,150,150);
			}
		} else if (togglable && on)  {
			if(lithue == -1) {
				if(p.bright ^ invert) {
					p.fill(40,50,180);
				} else {
					p.fill(120);
				}
			} else {
				if(p.bright ^ invert) {
					p.fill(lithue,150,180);
				} else {
					p.fill(lithue,150,130);
				}
			}
		} else {
			if(hue == -1) {
				if(p.bright ^ invert) {
					p.fill(40,50,170);
				} else {
					p.fill(80);
				}
			} else {
				if(p.bright ^ invert) {
					p.fill(hue,150,170);
				} else {
					p.fill(hue,150,100);
				}
			}
		}
		
		//text
		p.noStroke();
		p.rect(x,y,w,h,10);
		if(hue == -1) {
			if(p.bright ^ invert) {
				p.fill(0);
			} else {
				p.fill(255);
			}
		} else {
			if(p.bright ^ invert) {
				p.fill(hue,50,0);
			} else {
				p.fill(hue,50,255);
			}
		}
		p.text(text+val,x+GIVE,y+h*.5f-GIVE);
		
		
		
	}
	
	public void toolTip() {
		boolean over = p.overRect(x,y,w,h);
		if(over) {
			overTick++;
		} else {
			overTick = 0;
		}
		//tooltip
		if(overTick > 60 && tip.length() > 0) {
			p.textSize(16);
			p.textAlign(p.LEFT,p.TOP);
			
			p.fill(200,125,200);
			p.strokeWeight(1);
			p.stroke(0);
			
			
			float x = p.mouseX + 10;
			float y = p.mouseY + 16;
			float h = p.textDescent() + p.textAscent();
			float w = p.textWidth(tip) + 6;
			
			p.rect(x,y,w,h);

			p.fill(0);
			p.text(tip, x+3, y);
			
			
		}
	}
	
	public void onMousePressed() {
		if(p.overRect(x,y,w,h)) {
			on = !on;
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
