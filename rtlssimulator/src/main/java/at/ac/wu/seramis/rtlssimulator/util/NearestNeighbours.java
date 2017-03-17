package at.ac.wu.seramis.rtlssimulator.util;

import java.util.ArrayList;
import java.util.TreeMap;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.internal.EntryDefault;

public class NearestNeighbours
{
	private static RTree<String, Point> _tree = RTree.create();
	private static TreeMap<String, Entry<String, Point>> _treeEntries = new TreeMap<>();
	private static TreeMap<String, String> _itemClasses = new TreeMap<>();
	private static TreeMap<String, Long> _itemTimestamps = new TreeMap<>();

	public NearestNeighbours()
	{
		
	}

	public static synchronized void updatePosition(String ID, String itemClass, double x, double y, boolean updateDuplicates)
	{
		if (_treeEntries.containsKey(ID) && !updateDuplicates)
		{
			return;
		}

		updatePosition(ID, itemClass, x, y);
	}
	
	public static synchronized void updatePosition(String ID, String itemClass, double x, double y, long timestamp)
	{
		updatePosition(ID, itemClass, x, y);
		_itemTimestamps.put(ID, timestamp);
	}

	public static synchronized void updatePosition(String ID, String itemClass, double x, double y)
	{
		if (_treeEntries.containsKey(ID))
		{
			_tree = _tree.delete(_treeEntries.get(ID));
		}

		Entry<String, Point> newPoint = EntryDefault.entry(ID, Geometries.point(x, y));
		_treeEntries.put(ID, newPoint);
		_itemClasses.put(ID, itemClass);

		_tree = _tree.add(newPoint);
	}

	public static ArrayList<String> getNearestNeighbours(String ID, int n)
	{
		Entry<String, Point> entry = _treeEntries.get(ID);
		ArrayList<String> nearestNeighbours = new ArrayList<>();

		if (entry != null && entry.geometry() != null)
		{
			// get n+1 nearest neighbours, because the item sees itself as a neighbour
			_tree.nearest(entry.geometry(), Double.POSITIVE_INFINITY, (n + 1)).subscribe(s ->
			{
				if(entry != s && nearestNeighbours.size() < 10)
				{
					nearestNeighbours.add(s.value());
				}
			});
		}

		return nearestNeighbours;
	}

	public static TreeMap<String, Integer> getNearestNeighbourClasses(String ID, int n)
	{
		TreeMap<String, Integer> neighbourClasses = new TreeMap<>();
		ArrayList<String> nearestNeighbours = getNearestNeighbours(ID, n);

		for (String epc : nearestNeighbours)
		{
			neighbourClasses.putIfAbsent(_itemClasses.get(epc), 0);
			neighbourClasses.put(_itemClasses.get(epc), neighbourClasses.get(_itemClasses.get(epc)) + 1);
		}

		return neighbourClasses;
	}

	public static double getNearestNeighbourQuota(String ID, int n)
	{
		TreeMap<String, Integer> neighbourClasses = getNearestNeighbourClasses(ID, n);

		int count = neighbourClasses.getOrDefault(_itemClasses.getOrDefault(ID, ""), 0);

		return (double) count / n;
	}
	
	public static TreeMap<String, Entry<String, Point>> getTreeEntries()
	{
		return _treeEntries;
	}
	
	public static TreeMap<String, String> getItemClasses()
	{
		return _itemClasses;
	}
	
	public static TreeMap<String, Long> getItemTimestamps()
	{
		return _itemTimestamps;
	}

}
