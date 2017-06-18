package module5;

import java.util.List;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/** Implements a visual marker for ocean earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 *
 */
public class OceanQuakeMarker extends EarthquakeMarker {
	private List<float[]> threatened;
	
	public OceanQuakeMarker(PointFeature quake) {
		super(quake);
		
		// setting field in earthquake marker
		isOnLand = false;
	}
	

	/** Draw the earthquake as a square */
	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		pg.rect(x-radius, y-radius, 2*radius, 2*radius);
		pg.fill(0);
		if (!threatened.isEmpty()) {
			threatened.forEach(t -> pg.line(x, y, t[0], t[1]));
		}
	}
	
	public void addThreatened(float[] city) {
		threatened.add(city);
	}

	

}
