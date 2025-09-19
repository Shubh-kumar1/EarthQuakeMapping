package module4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

public class EarthquakeCityMap extends PApplet {
	
	private static final long serialVersionUID = 1L;
	private static final boolean offline = false;
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	private UnfoldingMap map;
	private List<Marker> cityMarkers;
	private List<Marker> quakeMarkers;
	private List<Marker> countryMarkers;
	
	public void setup() {		
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    printQuakes();
	 		
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	}
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
	}
	
	private void addKey() {	
		fill(255, 250, 240);
		rect(25, 50, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", 50, 75);
		
		fill(255, 125, 0);
		triangle(45, 100, 55, 100, 50, 110);
		fill(0);
		text("City Marker", 75, 105);

		fill(255, 255, 255);
		ellipse(50, 135, 10, 10);
		fill(0);
		text("Land Quake", 75, 135);
		
		fill(255, 255, 255);
		rect(45, 155, 10, 10);
		fill(0);
		text("Ocean Quake", 75, 160);
		
		text("Size ~ Magnitude", 50, 190);
		
		fill(255, 0, 0);
		ellipse(50, 215, 10, 10);
		fill(0);
		text("Shallow", 75, 215);

		fill(255, 255, 0);
		ellipse(50, 235, 10, 10);
		fill(0);
		text("Intermediate", 75, 235);

		fill(0, 0, 255);
		ellipse(50, 255, 10, 10);
		fill(0);
		text("Deep", 75, 255);
	}

	private boolean isLand(PointFeature earthquake) {
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		return false;
	}
	
	private void printQuakes() {
		HashMap<String, Integer> quakeCounts = new HashMap<String, Integer>();
		int oceanQuakes = 0;

		for (Marker m : quakeMarkers) {
			if (m instanceof LandQuakeMarker) {
				String country = (String) m.getProperty("country");
				quakeCounts.put(country, quakeCounts.getOrDefault(country, 0) + 1);
			} else if (m instanceof OceanQuakeMarker) {
				oceanQuakes++;
			}
		}

		System.out.println("--- Earthquake Counts by Country ---");
		for (String country : quakeCounts.keySet()) {
			System.out.println(country + ": " + quakeCounts.get(country));
		}
		System.out.println("OCEAN QUAKES: " + oceanQuakes);
		System.out.println("------------------------------------");
	}
	
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		Location checkLoc = earthquake.getLocation();
		if(country.getClass() == MultiMarker.class) {
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
					return true;
				}
			}
		}
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			return true;
		}
		return false;
	}
}