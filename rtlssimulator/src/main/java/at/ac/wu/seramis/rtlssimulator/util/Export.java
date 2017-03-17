package at.ac.wu.seramis.rtlssimulator.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import at.ac.wu.seramis.rtlssimulator.model.Item;

public class Export
{
	public static void snapshotExport(TreeMap<Integer, Item> itemsSnapshot, TreeMap<Integer, Item> misplacedItemsSnapshot, String filename)
	{
		long timestamp = System.currentTimeMillis();
		
		try
		{
			CSVPrinter csv = new CSVPrinter(new FileWriter(filename, true), CSVFormat.newFormat(';').withRecordSeparator('\n'));

			csv.printRecord("item", "item_class", "item_cluster", "timestamp", "firstQuota", "secondQuota", "distance", "misplaced");
			DistanceMeasure dist = new EuclideanDistance();

			for (Integer key : itemsSnapshot.keySet())
			{
				// fill the tree with the starting positions with noise
				NearestNeighbours.updatePosition(itemsSnapshot.get(key).getID() + "", itemsSnapshot.get(key).getItemClass().getID() + "", itemsSnapshot.get(key).getOriginalPositionWithNoise().getX(), itemsSnapshot.get(key).getOriginalPositionWithNoise().getY());
			}

			for (Integer key : misplacedItemsSnapshot.keySet())
			{
				// check all items inside one of the sets
				double quotaBefore = NearestNeighbours.getNearestNeighbourQuota(misplacedItemsSnapshot.get(key).getID() + "", 10);
				double distance = 0;
				double quotaAfter = 0;
				
				NearestNeighbours.updatePosition(misplacedItemsSnapshot.get(key).getID() + "", misplacedItemsSnapshot.get(key).getItemClass().getID() + "", misplacedItemsSnapshot.get(key).getCurrentPositionWithNoise().getX(), misplacedItemsSnapshot.get(key).getCurrentPositionWithNoise().getY());
				quotaAfter = NearestNeighbours.getNearestNeighbourQuota(misplacedItemsSnapshot.get(key).getID() + "", 10);

				if (!itemsSnapshot.containsKey(key))
				{
					// if the other subset does not contain the entry,
					// distance=0
					distance = 0;
				}
				else
				{
					distance = dist.compute(new double[] { itemsSnapshot.get(key).getOriginalPositionWithNoise().getX(), itemsSnapshot.get(key).getOriginalPositionWithNoise().getY(), 0 }, new double[] { misplacedItemsSnapshot.get(key).getCurrentPositionWithNoise().getX(), misplacedItemsSnapshot.get(key).getCurrentPositionWithNoise().getY(), 0 });
				}
				
				csv.printRecord(misplacedItemsSnapshot.get(key).getID(), misplacedItemsSnapshot.get(key).getItemClass().getID(), misplacedItemsSnapshot.get(key).getCluster(), timestamp, quotaBefore, quotaAfter, distance, misplacedItemsSnapshot.get(key).isMisplaced());
			}
			
			// Check the other items if some where not included and add a record
			// with d=0
			for (Integer key : itemsSnapshot.keySet())
			{
				if (!misplacedItemsSnapshot.containsKey(key))
				{
					double quotaBefore = NearestNeighbours.getNearestNeighbourQuota(itemsSnapshot.get(key).getID() + "", 10);
					csv.printRecord(itemsSnapshot.get(key).getID(), itemsSnapshot.get(key).getItemClass().getID(), itemsSnapshot.get(key).getCluster(), timestamp, quotaBefore, quotaBefore, 0, itemsSnapshot.get(key).isMisplaced());
				}
			}

			csv.flush();
			csv.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}