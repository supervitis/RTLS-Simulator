package at.ac.wu.seramis.rtlssimulator.model;

import java.util.Random;

import javafx.geometry.Point2D;

public class Item
{
	private int id;
	private ItemClass itemClass;
	private int cluster;
	private boolean misplaced = false;
	
	private Point2D originalPosition, originalPositionNoise, currentPosition, currentPositionNoise;
	
	private static final Random _RNG = new Random();

	public Item(int id)
	{
		this.id = id;
	}

	public Item(int id, ItemClass itemClass, int cluster, double x, double y)
	{
		this.id = id;
		this.itemClass = itemClass;
		this.setCluster(cluster);	

		this.originalPosition = new Point2D(x, y);
		this.currentPosition = new Point2D(x, y);
	}

	public int getID()
	{
		return this.id;
	}

	public ItemClass getItemClass()
	{
		return this.itemClass;
	}

	public int getCluster()
	{
		return this.cluster;
	}

	public void setCluster(int cluster)
	{
		this.cluster = cluster;
	}

	public boolean isMisplaced()
	{
		return this.misplaced;
	}

	public Point2D getOriginalPosition()
	{
		return this.originalPosition;
	}
	
	public Point2D getOriginalPositionWithNoise()
	{
		return this.originalPositionNoise;
	}

	public Point2D getCurrentPosition()
	{
		return this.currentPosition;
	}
	
	public Point2D getCurrentPositionWithNoise()
	{
		return this.currentPositionNoise;
	}
	
	public void setCurrentPosition(double x, double y)
	{
		this.currentPosition = new Point2D(x, y);
	}
	
	public void setCurrentPosition(double x, double y, boolean misplaced)
	{
		this.setCurrentPosition(x, y);
		this.misplaced = misplaced;
	}
	
	public void addNoise(int accuracy)
	{
		double factorX1 = _RNG.nextGaussian();
		double factorY1 = _RNG.nextGaussian();
		
		this.currentPositionNoise = new Point2D((factorX1 * accuracy) + this.currentPosition.getX(), factorY1 * accuracy + this.currentPosition.getY());
		
		double factorX2 = _RNG.nextGaussian();
		double factorY2 = _RNG.nextGaussian();
		
		this.originalPositionNoise = new Point2D((factorX2 * accuracy) + this.originalPosition.getX(), factorY2 * accuracy + this.originalPosition.getY());
	}
}
