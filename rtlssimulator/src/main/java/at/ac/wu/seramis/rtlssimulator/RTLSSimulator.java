package at.ac.wu.seramis.rtlssimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.Random;
import java.util.TreeMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.shape.random.RandomPointsBuilder;

import at.ac.wu.seramis.rtlssimulator.model.Item;
import at.ac.wu.seramis.rtlssimulator.model.ItemClass;
import at.ac.wu.seramis.rtlssimulator.model.VoronoiCell;
import at.ac.wu.seramis.rtlssimulator.util.Export;
import at.ac.wu.seramis.rtlssimulator.util.VoronoiDiagram;
import javafx.geometry.Point2D;

public class RTLSSimulator
{
	private double width = 0, height = 0;
	
	private int lastNumberOfClasses = -1, lastNumberOfItems = -1, lastDispersion = -1, lastNumberOfMisplacedItems = -1, lastAccuracy = -1, lastMissingness = -1;
	private float lastClustersRatio = -1;

	private TreeMap<Integer, Item> items;
	private TreeMap<Integer, List<ItemClass>> itemClasses;
	private TreeMap<Integer, Item> misplacedItems;
	private TreeMap<Integer, Item> snapshotItems;
	private TreeMap<Integer, Item> snapshotMisplacedItems;
	
	private VoronoiDiagram diagram;

	public RTLSSimulator(double width, double height)
	{
		this.width = width;
		this.height = height;
	}

	public void calculateInitialState(int numberOfClasses, int numberOfItems, int dispersion, float clustersRatio)
	{		
		this.lastNumberOfClasses = numberOfClasses;
		this.lastNumberOfItems = numberOfItems;
		this.lastDispersion = dispersion;
		this.lastClustersRatio = clustersRatio;
		
		Random rng = new Random();
		
		this.items = new TreeMap<>();
		this.itemClasses = new TreeMap<>();

		this.diagram = new VoronoiDiagram(this.width, this.height);

		// generate random coordinates for the classes' centroids
		for (int i = 0; i < numberOfClasses; i++)
		{
			int x = (int) (rng.nextDouble() * this.width);
			int y = (int) (rng.nextDouble() * this.height);

			this.diagram.addCentroid(x, y);
			
			List<ItemClass> list = new ArrayList<ItemClass>();
			list.add(new ItemClass((i + 1), new Point2D(x, y)));
			
			this.itemClasses.put((i + 1), list);
		}

		// depending on the extraClustersFactor, generate more clusters for some of the classes
		for (int i = numberOfClasses; i < numberOfClasses * clustersRatio; i++)
		{
			int x = (int) (rng.nextDouble() * this.width);
			int y = (int) (rng.nextDouble() * this.height);
			
			this.diagram.addCentroid(x, y);
			
			int j = rng.nextInt(numberOfClasses);
			List<ItemClass> list = this.itemClasses.get(j + 1);
			list.add(new ItemClass((j + 1), new Point2D(x, y)));
			
			this.itemClasses.put((j + 1), list);
		}

		this.diagram.calculateDiagram();

		// extract geometry of classes from diagram
		for (VoronoiCell cell : this.diagram.getCells())
		{
			for (List<ItemClass> list : this.itemClasses.values())
			{
				for (Object element : list)
				{
					ItemClass itemClass = (ItemClass) element;
					// rematch generated cells with corresponding class (to retain initial points)
					// this used to fail (some item classes had null as cell) before replacing "within" by "coveredBy" as method for comparison
					if (new Point(new CoordinateArraySequence(new Coordinate[] { new Coordinate(itemClass.getInitialPoint().getX(), itemClass.getInitialPoint().getY()) }), VoronoiDiagram.GEOMETRY_FACTORY).coveredBy(cell.getGeometry()))
					{
						itemClass.setCell(cell);
					}
				}
			}
		}

		TreeMap<String, Geometry> dispersionBuffer = new TreeMap<>();

		// randomly assign specified number of items to a class and place it in
		// the corresponding cell
		for (int i = 0; i < numberOfItems; i++)
		{
			int randomClass = rng.nextInt(numberOfClasses) + 1;
			int randomCluster = rng.nextInt(this.itemClasses.get(randomClass).size());
			ItemClass targetClass = this.itemClasses.get(randomClass).get(randomCluster);

			// "simple" approach to add dispersion by extending the boundaries
			// of the cell for the random positioning of items
			// the amount is calculated by multiplying the average of the cells
			// height + width with the specified dispersion amount (taken as
			// percentage)
			// TODO: more sophisticated approach?
			if (!dispersionBuffer.containsKey(randomClass + "/" + randomCluster))
			{
				Geometry targetGeometry = targetClass.getCell().getGeometry();
				Envelope targetEnvelope = targetGeometry.getEnvelopeInternal();

				double dispersionPixels = ((targetEnvelope.getHeight() + targetEnvelope.getWidth()) / 2) * (dispersion / 100.0);

				dispersionBuffer.put(randomClass + "/" + randomCluster, targetGeometry.buffer(dispersionPixels).intersection(this.diagram.getClip()));
			}
						
			Geometry extent = dispersionBuffer.get(randomClass + "/" + randomCluster);

			RandomPointsBuilder rpb = new RandomPointsBuilder(VoronoiDiagram.GEOMETRY_FACTORY);
			rpb.setExtent(extent);
			rpb.setNumPoints(1);
			Geometry randomGeometry = rpb.getGeometry();

			Item randomItem = new Item(i, targetClass, randomCluster, randomGeometry.getCoordinate().x, randomGeometry.getCoordinate().y);
			targetClass.addItem(randomItem);
			this.items.put(i, randomItem);
		}
	}

	public void calculateMisplacement(int numberOfMisplacedItems)
	{
		this.lastNumberOfMisplacedItems = numberOfMisplacedItems;
		
		Random rng = new Random();
		
		this.misplacedItems = new TreeMap<>();

		int numberOfItems = this.items.size();

		// TODO: misplaced items need to be at least 0.5% (?) of whole shop away from original position
		RandomPointsBuilder rpb = new RandomPointsBuilder(VoronoiDiagram.GEOMETRY_FACTORY);
		rpb.setExtent(this.diagram.getClip());
		rpb.setNumPoints(numberOfMisplacedItems);
		Geometry randomGeometry = rpb.getGeometry();

		// reset position of (probably) previously misplaced items
		for(Item item : this.items.values())
		{
			item.setCurrentPosition(item.getOriginalPosition().getX(), item.getOriginalPosition().getY(), false);
		}
		
		for (int i = 0; i < numberOfMisplacedItems; i++)
		{
			int randomItem = rng.nextInt(numberOfItems) + 1;
			Item misplacedItem = this.items.get(randomItem);
			misplacedItem.setCurrentPosition(randomGeometry.getCoordinates()[i].x, randomGeometry.getCoordinates()[i].y, true);
			this.misplacedItems.put(misplacedItem.getID(), misplacedItem);
		}
	}

	public void calculateSnapshot(int accuracy, int missingness)
	{
		this.lastAccuracy = accuracy;
		this.lastMissingness = missingness;
		
		// reset items from (eventual) previous snapshots 
		this.snapshotItems = new TreeMap<>();
		this.snapshotMisplacedItems = new TreeMap<>();
		
		// add noise to all items
		for (Entry<Integer, Item> entry : this.items.entrySet())
		{
			entry.getValue().addNoise(accuracy);
		}
		
		// perform pass one (baseline snapshot without misplaced items)
		this.snapshotItems = this.performRead(missingness);
				
		// perform pass two (snapshot with misplaced items)
		this.snapshotMisplacedItems = this.performRead(missingness);
		
		Export.snapshotExport(this.snapshotItems, this.snapshotMisplacedItems, this.getCurrentFilename() + ".csv");
	}
	
	public String getCurrentFilename()
	{
		return "./output/simulator_" 
				+ this.lastNumberOfItems + "i_" 
				+ this.lastNumberOfClasses + "c_" 
				+ this.lastDispersion + "d_"
				+ this.lastClustersRatio + "r_"
				+ this.lastNumberOfMisplacedItems + "mi_"
				+ this.lastAccuracy + "a_"
				+ this.lastMissingness + "m";
	}
	
	private TreeMap<Integer, Item> performRead(int missingness)
	{
		TreeMap<Integer, Item> read = new TreeMap<>();
		
		ArrayList<Integer> keys = new ArrayList<>(this.items.keySet());
		Collections.shuffle(keys);
		
		// only read (100 - missingness) percent of the available items		
		for(int i = 0; i < keys.size() * ((100 - missingness) / 100.0); i++)
		{
			read.put((Integer) keys.get(i), items.get(keys.get(i)));
		}	
		
		return read;
	}

	public TreeMap<Integer, List<ItemClass>> getItemClasses()
	{
		return this.itemClasses;
	}

	public TreeMap<Integer, Item> getItems()
	{
		return this.items;
	}

	public TreeMap<Integer, Item> getMisplacedItems()
	{
		return this.misplacedItems;
	}

	public TreeMap<Integer, Item> getSnapshotItems()
	{
		return this.snapshotItems;
	}
}
