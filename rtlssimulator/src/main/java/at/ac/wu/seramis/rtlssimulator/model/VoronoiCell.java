package at.ac.wu.seramis.rtlssimulator.model;

import java.util.ArrayList;
import java.util.LinkedList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import javafx.geometry.Point2D;

public class VoronoiCell 
{
	private LinkedList<Point2D> points = new LinkedList<>();
	
	private Geometry geometry;
	
	public VoronoiCell(Geometry geometry)
	{
		this.geometry = geometry;
		
		for(Coordinate coordinate : this.geometry.getCoordinates())
		{
			this.points.add(new Point2D(coordinate.x, coordinate.y));
		}
	}
		
	public Geometry getGeometry()
	{
		return this.geometry;
	}
	
	public LinkedList<Point2D> getPointsAsList()
	{
		return this.points;
	}
	
	public LinkedList<Double> getPointsAsDoubleList()
	{
		LinkedList<Double> pointsDouble = new LinkedList<>();
		
		for(Point2D point : this.points)
		{
			pointsDouble.add(point.getX());
			pointsDouble.add(point.getY());
		}
		
		return pointsDouble;
	}
}
