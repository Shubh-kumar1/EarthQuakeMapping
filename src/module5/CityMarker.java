package module5;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import processing.core.PConstants;
import processing.core.PGraphics;

public class CityMarker extends CommonMarker {
	
	public static int TRI_SIZE = 5;  // The size of the triangle marker
	
	public CityMarker(Location location) {
		super(location);
	}
	
	public CityMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	}
	
	/**
	 * Implementation of the abstract method to draw the marker on the map.
	 */
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		// Save previous drawing style
		pg.pushStyle();
		
		// Draw a triangle for each city
		pg.fill(150, 30, 30);
		pg.triangle(x, y-TRI_SIZE, x-TRI_SIZE, y+TRI_SIZE, x+TRI_SIZE, y+TRI_SIZE);
		
		// Restore previous drawing style
		pg.popStyle();
	}
	
	/** Show the title of the city if this marker is selected */
	@Override
	public void showTitle(PGraphics pg, float x, float y) {
		// Format the title string
		String name = getCity() + ", " + getCountry();
		String pop = "Pop: " + getPopulation() + " M";
		
		// Calculate the width of the text box
		float textWidth = Math.max(pg.textWidth(name), pg.textWidth(pop));
		
		// Define the position and size of the text box
		float boxX = x + 15;
		float boxY = y - 15;
		float boxW = textWidth + 6; // Add some padding
		float boxH = 35; // Height for two lines of text
		
		// Draw the background box
		pg.fill(255, 255, 240); // Off-white color
		pg.rect(boxX, boxY, boxW, boxH);
		
		// Draw the text
		pg.fill(0); // Black text
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.textSize(12);
		pg.text(name, boxX + 3, boxY + 3); // Position text inside the box
		pg.text(pop, boxX + 3, boxY + 18);
	}
	
	/* Local getters for some city properties.  */
	public String getCity()
	{
		return getStringProperty("name");
	}
	
	public String getCountry()
	{
		return getStringProperty("country");
	}
	
	public float getPopulation()
	{
		return Float.parseFloat(getStringProperty("population"));
	}
}