package module5;

import java.util.HashMap;

import de.fhpotsdam.unfolding.UnfoldingMap;
import processing.core.PApplet;
import processing.core.PConstants;

public class Snake {
	private HashMap<Integer, float[]> body;
	private float sX;
	private float sY; 
	private float sR;
	private boolean killed = true;
	private long died;
	
	private final int snakeFPS = 10;
	//snake directions (possible displacement vectors)
	private final float[] left = {-1, 0};
	private final float[] right = {1, 0};
	private final float[] down = {0, 1};
	private final float[] up = {0, -1};
	//snake current direction
	private float[] dir = right;
	
	private int moves;
	
	private PApplet pa;  
	private UnfoldingMap map;
	
	
	public Snake (PApplet applet, UnfoldingMap uMap) {
		pa = applet;
		body = new HashMap<Integer, float[]>();
		sR = (pa.height < pa.width) ? pa.height/40 : pa.width/40;
		sX  = pa.width/2-sR;
		sY = pa.height/2-sR;
		map = uMap;
		/*float [] start = {sX, sY};
		body.put(0, start);*/
	}
	
	public HashMap<Integer, float[]> getBody() {
		return body;
	}
	
	public void goDir(/*float [] xy*/) {
		if (!killed) {
			
			
			float [] oldO = body.get(0);
			float [] newO = {oldO[0] + dir[0]*2*sR, oldO[1] + dir[1]*2*sR};

			for (int i = body.size()-1; i > 0; i--) {
				body.put(i, body.get(i-1));
			}
			body.put(0, newO);
		}
		
	}
	
	public void addOne() {
		if (moves == 0) {
			float [] start = {sX, sY};
			body.put(0, start);
		} else {
			float[] added = body.get(body.size()-1);
			body.put(body.size(), added);
		}
	}
	
	public void draw() {
		pa.pushStyle();
		pa.fill(0xAAFF8888);
		pa.ellipseMode(PConstants.RADIUS);
		
		if(pa.keyPressed) {
			keyAction();
		}
		
		/*if (pa.keyCode == PConstants.LEFT) {dir = left;}
		else if (pa.keyCode == PConstants.RIGHT) {dir = right;}
		else if (pa.keyCode == PConstants.DOWN) {dir = down;}
		else if (pa.keyCode == PConstants.UP) {dir = up;}*/
		
		
		
		if (!killed) {
			if (pa.frameCount%snakeFPS == 1 || pa.frameCount%snakeFPS == snakeFPS/2+1) { //snake only moves 2 times per sec.
				//System.out.println(pa.frameCount + "\t" + pa.frameCount%Math.round(pa.frameRate) + "\t" + Math.round(pa.frameRate) + "\t" + pa.frameRate);
				if (moves < 5) {
					if (moves == 0) {breed();}
					addOne();
				}
				goDir();
				moves++;
				if (isKilled()) {kill();}
			}
			body.forEach((k, v) -> pa.ellipse(v[0], v[1], sR, sR));
		} else {
			if ((System.nanoTime() - died)/Math.pow(10, 9) < 3) {
				body.forEach((k, v) -> pa.ellipse(v[0], v[1], sR, sR));
				body.forEach((k, v) -> pa.line(v[0]+sR/2, v[1]+sR/2, v[0]-sR/2, v[1]-sR/2));
				body.forEach((k, v) -> pa.line(v[0]-sR/2, v[1]+sR/2, v[0]+sR/2, v[1]-sR/2));
			} else {
				body = new HashMap<Integer, float[]>();
			}
		}
		pa.popStyle();
	}
	
	public void kill() {
		map.setActive(true);
		pa.frameRate(60);
		died = System.nanoTime(); //to make snake disappear after 3 seconds
		killed = true;
		
	}
	
	private boolean isKilled() {
		if(body.get(0)[0] > pa.width-sR || body.get(0)[0] < 0+sR || body.get(0)[1] > pa.height-sR || 
				body.get(0)[1] < 0+sR) {return true;}
		for(int i = 1; i < body.size(); i++) {
			if (body.get(i)[0] == body.get(0)[0] && body.get(i)[1] == body.get(0)[1]) {return true;}
		}
		/*if (body.size() == 0) {return true;}*/
		
		return false;
	}
	
	public void resetSnake() {
		map.setActive(false);
		pa.frameRate(snakeFPS);
		moves = 0;
		body = new HashMap<Integer, float[]>();
		dir = right;
		killed = false;
	}
	
	public void breed() {
		float [] start = {sX, sY};
		body.put(0, start);
	}
	
	private void keyAction() {
		if (pa.key == PConstants.CODED) {
			if (pa.keyCode == PConstants.LEFT) {dir = left;}
			else if (pa.keyCode == PConstants.RIGHT) {dir = right;}
			else if (pa.keyCode == PConstants.DOWN) {dir = down;}
			else if (pa.keyCode == PConstants.UP) {dir = up;}
		}
		else {
			if (pa.key == 'r') {resetSnake();}
			if (killed == false && pa.key == 'q') {kill();}
		}
	}

}
