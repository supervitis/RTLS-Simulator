package at.ac.wu.seramis.rtlssimulator.gui.component;

import java.util.TreeMap;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Map extends AnchorPane
{
	private Rectangle mapBackground = new Rectangle(0, 0);

	private TreeMap<MapLayer, Group> mapLayers = new TreeMap<>();
	
	private Group zoomGroup = new Group();
	private ScrollPane zoomPane;

	public Map()
	{
		super();

		FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource(this.getClass().getSimpleName() + ".fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try
		{
			fxmlLoader.load();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		for (MapLayer mapLayer : MapLayer.values())
		{
			Group layer = new Group();

			this.mapLayers.put(mapLayer, layer);
		}

		this.mapBackground.setStroke(Color.BLACK);
		this.mapBackground.setStrokeWidth(1);
		this.mapBackground.setFill(Color.TRANSPARENT);
		
		this.zoomGroup.getChildren().add(this.mapBackground);
		this.zoomGroup.getChildren().addAll(this.mapLayers.values());
		
		this.zoomPane = new ZoomPane().createZoomPane(this.zoomGroup, "white");
		this.zoomPane.setStyle("-fx-box-border: transparent; -fx-focus-color: transparent;");
		
		AnchorPane.setTopAnchor(this.zoomPane, 0.0);
		AnchorPane.setLeftAnchor(this.zoomPane, 0.0);
		AnchorPane.setBottomAnchor(this.zoomPane, 0.0);
		AnchorPane.setRightAnchor(this.zoomPane, 0.0);
		
		this.getChildren().add(this.zoomPane);
	}

	public Group getLayer(MapLayer layer)
	{
		return this.mapLayers.get(layer);
	}
	
	public Group getZoomPane()
	{
		return this.zoomGroup;
	}
	
	public void setSize(double width, double height)
	{
		this.mapBackground.setWidth(width);
		this.mapBackground.setHeight(height);
	}

	public void reset()
	{
		for (Group layer : this.mapLayers.values())
		{
			layer.getChildren().clear();
		}
	}

	public enum MapLayer
	{
		ClassCentroids, VoranoiCells, Items, MisplacedItems, MisplacedItemPaths, NoisyItems, NoisyItemPaths, MissingItems
	}
}
