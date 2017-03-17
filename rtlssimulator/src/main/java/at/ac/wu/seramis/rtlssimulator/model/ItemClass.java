package at.ac.wu.seramis.rtlssimulator.model;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import javafx.geometry.Point2D;

public class ItemClass
{
	private int id;
	private VoronoiCell cell;
	private Point2D initialPoint;

	private ArrayList<Item> items = new ArrayList<>();

	public ItemClass(int id)
	{
		this.id = id;
	}

	public ItemClass(int id, Point2D initialPoint)
	{
		this.id = id;
		this.initialPoint = initialPoint;
	}

	public void setCell(VoronoiCell cell)
	{
		this.cell = cell;
	}

	public VoronoiCell getCell()
	{
		return this.cell;
	}

	public void addItem(Item item)
	{
		this.items.add(item);
	}

	public ArrayList<Item> getItems()
	{
		return this.items;
	}

	public int getID()
	{
		return this.id;
	}

	public Point2D getCentroid()
	{
		return new Point2D(this.cell.getGeometry().getCentroid().getX(), this.cell.getGeometry().getCentroid().getY());
	}

	public Point2D getInitialPoint()
	{
		return this.initialPoint;
	}
}
