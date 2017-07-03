package module5;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MarkerManager;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PApplet;
import processing.core.PConstants;

public class Snake {
	private HashMap<Integer, float[]> body;
	//private int bodySize;
	private float sX;
	private float sY; 
	private float sD; //diameter of body segments 
	private boolean killed = true;
	private long timeOfDeath;
	private long unhideTiming;
	private long removalTiming;
	
	private final int snakeFPS = 10; //map frame rate when snake is on (much lower than default so as not to overload a CPU)
	private final float offsetX;
	private final float offsetY;
	private final float width;
	private final float height;
	
	//snake directions (possible displacement vectors)
	private final float[] left = {-1, 0};
	private final float[] right = {1, 0};
	private final float[] down = {0, 1};
	private final float[] up = {0, -1};
	//snake current direction
	private float[] dir = right;
	
	private long moves;
	
	private PApplet pa;  
	private UnfoldingMap map;
	
	private List<Marker> markers;
	
	private Random random = new Random();
	
	
	public Snake (PApplet applet, UnfoldingMap uMap) {
		pa = applet;
		map = uMap;
		markers = uMap.getMarkerManager(0).getMarkers();
		body = new HashMap<Integer, float[]>();
		offsetX = map.mapDisplay.offsetX;
		offsetY = map.mapDisplay.offsetY;
		width = map.mapDisplay.getWidth();
		height = map.mapDisplay.getHeight();
		sD = (height < width) ? pa.height/40 : pa.width/40;
		sX  = pa.width/2-sD/2;
		sY = pa.height/2-sD/2;
		
	}
	
	private void goDir() {
		//if (!killed) {
		if (body.get(0) != null) {
			
			
			float [] oldO = body.get(0);
			float [] newO = {oldO[0] + dir[0]*sD, oldO[1] + dir[1]*sD};

			for (int i = body.size()-1; i > 0; i--) {
				body.put(i, body.get(i-1));
			}
			body.put(0, newO);
		}
		
	}
	
	private void addOne() {
		if (moves == 0) {
			float [] start = {sX, sY};
			body.put(0, start);
		} else {
			float[] added = body.get(body.size()-1);
			body.put(body.size(), added);
		}
		//bodySize++;
	}
	
	private void removeOne() {
		//bodySize--;
		if (isKilled()) {
			kill();
			return;
		}
		body.remove(body.size()-1);
		
	}
	
	public void draw() {
		pa.pushStyle();
		pa.fill(0xAAFF8888);
		pa.ellipseMode(PConstants.CENTER);
		
		
		
		
		if (!killed) {
			if ((System.nanoTime()-unhideTiming) / 1000000000 >= 3) {
				unhideTiming = System.nanoTime(); 
				markers.get(random.nextInt(markers.size())).setHidden(false);
			}
			if ((System.nanoTime()-removalTiming) / 1000000000 >= 7) {
				removalTiming = System.nanoTime();
				removeOne();
			}
			if (pa.frameCount%snakeFPS == 1 || pa.frameCount%snakeFPS == snakeFPS/2+1) { //snake only moves 2 times per sec.
				if (moves < 4) {
					if (moves == 0) {breed();}
					addOne();
				}
				goDir();
				eat();
				moves++;
				if (isKilled()) {kill();}
			}
			body.forEach((k, v) -> pa.ellipse(v[0], v[1], sD, sD));	
		} else {
			if ((System.nanoTime() - timeOfDeath)/Math.pow(10, 9) < 5) {
				body.forEach((k, v) -> pa.ellipse(v[0], v[1], sD, sD));
				body.forEach((k, v) -> pa.line(v[0]+sD/2, v[1]+sD/2, v[0]-sD/2, v[1]-sD/2));
				body.forEach((k, v) -> pa.line(v[0]-sD/2, v[1]+sD/2, v[0]+sD/2, v[1]-sD/2));
			} else {
				body = new HashMap<Integer, float[]>();
			}
		}
		pa.popStyle();
	}
	
	private void kill() {
		map.setActive(true); 
		pa.frameRate(60); //default frame rate
		timeOfDeath = System.nanoTime(); //to make snake disappear after 3 seconds
		killed = true;
		
	}
	
	private boolean isKilled() {
		if (body.size() <= 0) {System.out.println("killed"); return true;}
		if (body.get(0)[0] > offsetX+width-sD || body.get(0)[0] < offsetX+sD || body.get(0)[1] > offsetY+height-sD || 
				body.get(0)[1] < offsetY+sD) {return true;}
		/*if(body.get(0)[0] > pa.width-sR || body.get(0)[0] < 0+sR || body.get(0)[1] > pa.height-sR || 
				body.get(0)[1] < 0+sR) {return true;}*/
		for(int i = 1; i < body.size(); i++) {
			if (body.get(i)[0] == body.get(0)[0] && body.get(i)[1] == body.get(0)[1]) {return true;}
		}
		
		
		return false;
	}
	
	private void eat() {
		if (body.get(0) != null) {
			for (Marker m : markers) {
				ScreenPosition mPos = ((CommonMarker) m).getScreenPosition(map);
				if (m.getProperties().containsKey("radius")) {
					//below I'm checking whether snakes head overlap (approximately) any marker 
					if (Math.sqrt(Math.pow(mPos.x - body.get(0)[0], 2) + Math.pow(mPos.y - body.get(0)[1], 2)) <= sD / 2
							+ (Float) m.getProperty("radius")) {
						if (!m.isHidden()) {
							m.setHidden(true);
							if (m instanceof CityMarker) {
								removeOne();
							}
							if (m instanceof EarthquakeMarker) {
								addOne();
							}
						}
					}
				}
			} 
		}
		
	}
	
	private void runSnake() {
		map.setActive(false); //so hitting arrows wouldn't change map panning
		map.zoomToLevel(UnfoldingMap.DEFAULT_ZOOM_LEVEL);
		map.panTo(UnfoldingMap.PRIME_MERIDIAN_EQUATOR_LOCATION);
		pa.frameRate(snakeFPS); 
		for (Marker m : markers) {m.setHidden(true);}
		markers.get(random.nextInt(markers.size())).setHidden(false);
		
		moves = 0;
		body = new HashMap<Integer, float[]>();
		dir = right;
		killed = false;
		unhideTiming = System.nanoTime();
		removalTiming = System.nanoTime();
		
	}
	
	private void breed() {
		float [] start = {sX, sY};
		body.put(0, start);
		//bodySize = 1;
	}
	
	public void keyAction() {
		if (pa.key == PConstants.CODED) {
			if (pa.keyCode == PConstants.LEFT) {dir = left;}
			else if (pa.keyCode == PConstants.RIGHT) {dir = right;}
			else if (pa.keyCode == PConstants.DOWN) {dir = down;}
			else if (pa.keyCode == PConstants.UP) {dir = up;}
		}
		else {
			if (pa.key == 'r') {
				runSnake();
				//hideMarkers();
			}
			if (killed == false && pa.key == 'q') {kill();}
		}
	}

}
