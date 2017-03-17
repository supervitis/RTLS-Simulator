package at.ac.wu.seramis.rtlssimulator.util;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

import at.ac.wu.seramis.rtlssimulator.model.VoronoiCell;

public class VoronoiDiagram
{
	public final static GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
	
	ArrayList<Coordinate> centroids = new ArrayList<>();
	ArrayList<VoronoiCell> voronoiCells = new ArrayList<>();
	
	Envelope envelope;
	Geometry diagram, clip;
	
	public VoronoiDiagram(double width, double height)
	{
		this.envelope = new Envelope(0, width, 0, height);

		Coordinate[] coordinates = new Coordinate[] { new Coordinate(0, 0), new Coordinate(width, 0), new Coordinate(width, height), new Coordinate(0, height), new Coordinate(0, 0)};
	    this.clip = new Polygon( new LinearRing(new CoordinateArraySequence(coordinates), GEOMETRY_FACTORY), null, GEOMETRY_FACTORY);
	}
	
	public void addCentroid(double x, double y)
	{
        this.centroids.add(new Coordinate(x, y));
	}
	
	public void addCentroid(double x, double y, double z)
	{
        this.centroids.add(new Coordinate(x, y, z));
	}
	
	public void calculateDiagram()
	{
		VoronoiDiagramBuilder voronoi = new VoronoiDiagramBuilder();
	    voronoi.setSites(this.centroids);
	    voronoi.setClipEnvelope(this.envelope); // this is (for some reason) internally extended by a buffer, the actual clipping needs to be done by intersecting with the intended shape...
	    voronoi.setTolerance(0);
	    
	    this.diagram = voronoi.getDiagram(GEOMETRY_FACTORY).intersection(this.getClip());
	    
	    for(int i = 0; i < this.diagram.getNumGeometries(); i++)
		{
			Geometry cell = this.diagram.getGeometryN(i);
			VoronoiCell voronoiCell = new VoronoiCell(cell);			
			voronoiCells.add(voronoiCell);			
		}
	}
	
	public ArrayList<VoronoiCell> getCells()
	{
		return this.voronoiCells;
	}
	
	public Geometry getClip()
	{
		return this.clip;
	}
}
