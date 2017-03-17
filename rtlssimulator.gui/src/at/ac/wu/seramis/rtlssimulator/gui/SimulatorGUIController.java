package at.ac.wu.seramis.rtlssimulator.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.controlsfx.control.CheckListView;
import org.controlsfx.control.NotificationPane;

import at.ac.wu.seramis.rtlssimulator.RTLSSimulator;
import at.ac.wu.seramis.rtlssimulator.gui.component.Map;
import at.ac.wu.seramis.rtlssimulator.gui.component.Map.MapLayer;
import at.ac.wu.seramis.rtlssimulator.gui.util.RandomColor;
import at.ac.wu.seramis.rtlssimulator.gui.util.SVGWriter;
import at.ac.wu.seramis.rtlssimulator.model.Item;
import at.ac.wu.seramis.rtlssimulator.model.ItemClass;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class SimulatorGUIController
{
	// area of 1000px width (e.g. 100m, 1px = 10cm) and 500px height (e.g. 50m)
	// for visualization purposes in the paper, a 400x400 area is created
	private final static int AREA_WIDTH = 1000, AREA_HEIGHT = 500;
	
	@FXML
	private Map rtlsMap;
	@FXML
	private TextField numberOfClassesTXT, numberOfItemsTXT, dispersionTXT, numberOfMisplacedItemsTXT, sensingAccuracyTXT, sensingMissingnessTXT, clustersRatioTXT;
	@FXML
	private Button generateInitialStateBTN, performMisplacementBTN, generateSnapshotBTN, svgSnapshotBTN, automaticGenerationBTN;
	@FXML
	private CheckListView<String> mapLayerCLV;
	@FXML
	private NotificationPane notificationPNE;

	private HashMap<String, Group> mapLayers = new HashMap<>();
	private TreeMap<Integer, Rectangle> itemMarkers = new TreeMap<>();
 
	private RTLSSimulator rtlsSimulator = new RTLSSimulator(AREA_WIDTH, AREA_HEIGHT);
 
	@FXML
	public void initialize()
	{
		this.rtlsMap.setSize(AREA_WIDTH, AREA_HEIGHT);
		
		for (MapLayer mapLayer : MapLayer.values())
		{
			this.mapLayers.put(mapLayer.name(), this.rtlsMap.getLayer(mapLayer));
			this.mapLayerCLV.getItems().add(mapLayer.name());
		}

		this.mapLayerCLV.getCheckModel().checkAll();

		this.mapLayerCLV.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) c ->
		{
			for (Entry<String, Group> layer : SimulatorGUIController.this.mapLayers.entrySet())
			{
				if (SimulatorGUIController.this.mapLayerCLV.getCheckModel().isChecked(layer.getKey()))
				{
					layer.getValue().setVisible(true);
				}
				else
				{
					layer.getValue().setVisible(false);
				}
			}
		});
		
		this.notificationPNE.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
	}

	@FXML
	public void handleGenerateInitialStateBTNAction()
	{
		this.rtlsMap.reset();
		this.itemMarkers.clear();

		int numberOfClasses = Integer.parseInt(this.numberOfClassesTXT.getText().split(",")[0]);
		int numberOfItems = Integer.parseInt(this.numberOfItemsTXT.getText().split(",")[0]);
		int dispersion = Integer.parseInt(this.dispersionTXT.getText().split(",")[0]);
		float extraClustersFactor = Float.parseFloat(this.clustersRatioTXT.getText().split(",")[0]);

		this.rtlsSimulator.calculateInitialState(numberOfClasses, numberOfItems, dispersion, extraClustersFactor);

		for (List<ItemClass> listItems : this.rtlsSimulator.getItemClasses().values())
		{
			for (Object element : listItems)
			{
				ItemClass itemClass = (ItemClass) element;
				// draw cell borders
				Polygon p = new Polygon();
				p.setFill(Color.TRANSPARENT);
				p.setStroke(Color.BLACK);
				p.setStrokeWidth(1);
				p.getPoints().addAll(itemClass.getCell().getPointsAsDoubleList());

				this.rtlsMap.getLayer(MapLayer.VoranoiCells).getChildren().add(p);

				Color classColor = RandomColor.getRandomColor();
//				Color classColor = RandomColor.getRandomGrayscale();

				Circle centroid = new Circle(3, classColor);
				centroid.setCenterX(itemClass.getInitialPoint().getX());
				centroid.setCenterY(itemClass.getInitialPoint().getY());

				this.rtlsMap.getLayer(MapLayer.ClassCentroids).getChildren().add(centroid);

				for (Item item : itemClass.getItems())
				{
					Rectangle marker = new Rectangle(2, 2);
					marker.setX(item.getOriginalPosition().getX() - marker.getWidth() / 2);
					marker.setY(item.getOriginalPosition().getY() - marker.getHeight() / 2);
					marker.setFill(classColor.deriveColor(0, 1, 1, 0.7)); // for color
//					marker.setFill(classColor); // for grayscale

					this.rtlsMap.getLayer(MapLayer.Items).getChildren().add(marker);
					this.itemMarkers.put(item.getID(), marker);
				}
			}
		}
		this.performMisplacementBTN.setDisable(false);
	}

	@FXML
	public void handlePerformMisplacementBTNAction()
	{
		// reset all original markers visibility
		for (Rectangle marker : this.itemMarkers.values())
		{
			marker.setVisible(true);
		}
		// clear previous misplacements
		this.rtlsMap.getLayer(MapLayer.MisplacedItems).getChildren().clear();
		this.rtlsMap.getLayer(MapLayer.MisplacedItemPaths).getChildren().clear();

		int numberOfMisplacedItems = Integer.parseInt(this.numberOfMisplacedItemsTXT.getText().split(",")[0]);

		this.rtlsSimulator.calculateMisplacement(numberOfMisplacedItems);

		for (Item misplacedItem : this.rtlsSimulator.getMisplacedItems().values())
		{
			// hide original position of misplaced item
			if (misplacedItem.isMisplaced())
			{
				Rectangle marker = this.itemMarkers.get(misplacedItem.getID());
				marker.setVisible(false);

				Rectangle misplacedMarker = new Rectangle(5, 5);
				misplacedMarker.setX(misplacedItem.getCurrentPosition().getX() - misplacedMarker.getWidth() / 2);
				misplacedMarker.setY(misplacedItem.getCurrentPosition().getY() - misplacedMarker.getHeight() / 2);
				misplacedMarker.setFill(Color.RED);
				this.rtlsMap.getLayer(MapLayer.MisplacedItems).getChildren().add(misplacedMarker);

				Line misplacedPath = new Line();
				misplacedPath.setStartX(misplacedItem.getOriginalPosition().getX());
				misplacedPath.setStartY(misplacedItem.getOriginalPosition().getY());
				misplacedPath.setEndX(misplacedItem.getCurrentPosition().getX());
				misplacedPath.setEndY(misplacedItem.getCurrentPosition().getY());
				misplacedPath.setStroke(Color.RED);
				misplacedPath.setStrokeWidth(1);
				this.rtlsMap.getLayer(MapLayer.MisplacedItemPaths).getChildren().add(misplacedPath);
			}
		}
		this.generateSnapshotBTN.setDisable(false);
	}

	@FXML
	public void handleGenerateSnapshotBTNAction()
	{
		// clear previous noisy & missing items
		this.rtlsMap.getLayer(MapLayer.NoisyItems).getChildren().clear();
		this.rtlsMap.getLayer(MapLayer.NoisyItemPaths).getChildren().clear();
		this.rtlsMap.getLayer(MapLayer.MissingItems).getChildren().clear();

		int sensingAccuracy = Integer.parseInt(this.sensingAccuracyTXT.getText().split(",")[0]);
		int sensingMissingness = Integer.parseInt(this.sensingMissingnessTXT.getText().split(",")[0]);
		
		this.rtlsSimulator.calculateSnapshot(sensingAccuracy, sensingMissingness);

		for (Item item : this.rtlsSimulator.getItems().values())
		{			
			// item is missing			
			if(!this.rtlsSimulator.getSnapshotItems().keySet().contains(item.getID()))
			{
				Circle missingMarker = new Circle(3);
				missingMarker.setCenterX(item.getCurrentPosition().getX());
				missingMarker.setCenterY(item.getCurrentPosition().getY());
				missingMarker.setFill(Color.TRANSPARENT);
				missingMarker.setStroke(Color.RED);
				missingMarker.setStrokeWidth(0.5);
				this.rtlsMap.getLayer(MapLayer.MissingItems).getChildren().add(missingMarker);
			}
			// item was read
			else
			{
				Circle noisyMarker = new Circle(1);
				noisyMarker.setCenterX(item.getCurrentPositionWithNoise().getX());
				noisyMarker.setCenterY(item.getCurrentPositionWithNoise().getY());
				noisyMarker.setFill(this.itemMarkers.get(item.getID()).getFill());
				this.rtlsMap.getLayer(MapLayer.NoisyItems).getChildren().add(noisyMarker);

				Line noisyPath = new Line();
				noisyPath.setStartX(item.getCurrentPosition().getX());
				noisyPath.setStartY(item.getCurrentPosition().getY());
				noisyPath.setEndX(item.getCurrentPositionWithNoise().getX());
				noisyPath.setEndY(item.getCurrentPositionWithNoise().getY());
				noisyPath.setStroke(Color.SILVER);
				noisyPath.setStrokeWidth(0.5);
				this.rtlsMap.getLayer(MapLayer.NoisyItemPaths).getChildren().add(noisyPath);
			}
		}
	}
	
	@FXML
	public void handleSVGSnapshotBTNAction()
	{
		String filename = this.rtlsSimulator.getCurrentFilename() + ".svg";
		
		SVGWriter.writeSVG(this.rtlsMap, filename);
		
		this.notificationPNE.show("SVG has been saved as " + filename);
	}
	
	@FXML
	public void handleAutomaticGenerationBTNAction()
	{
		String[] allNumberOfClasses = this.numberOfClassesTXT.getText().split(",");
		String[] allNumberOfItems = this.numberOfItemsTXT.getText().split(",");
		String[] allDispersion = this.dispersionTXT.getText().split(",");
		String[] allClustersRatio = this.clustersRatioTXT.getText().split(",");
		String[] allMisplacedItems = this.numberOfMisplacedItemsTXT.getText().split(",");
		String[] allSensingAccuracy = this.sensingAccuracyTXT.getText().split(",");
		String[] allSensingMissingness = this.sensingMissingnessTXT.getText().split(",");
		
		for(String automatedNumberOfClasses : allNumberOfClasses)
		{
			for(String automatedNumberOfItems : allNumberOfItems)
			{
				for(String automatedDispersion : allDispersion)
				{
					for(String automatedClustersRatio : allClustersRatio)
					{
						for(String automatedMisplacedItems : allMisplacedItems)
						{
							for(String automatedSensingAccuracy : allSensingAccuracy)
							{
								for(String automatedSensingMissingness : allSensingMissingness)
								{
									int numberOfClasses = Integer.parseInt(automatedNumberOfClasses.trim());
									int numberOfItems = Integer.parseInt(automatedNumberOfItems.trim());
									int dispersion = Integer.parseInt(automatedDispersion.trim());
									float clustersRatio = Float.parseFloat(automatedClustersRatio.trim());
									int misplacedItems = Integer.parseInt(automatedMisplacedItems.trim());
									int sensingAccuracy = Integer.parseInt(automatedSensingAccuracy.trim());
									int sensingMissingness = Integer.parseInt(automatedSensingMissingness.trim());
									
									this.numberOfClassesTXT.setText(numberOfClasses + "");
									this.numberOfItemsTXT.setText(numberOfItems + "");
									this.dispersionTXT.setText(dispersion + "");
									this.clustersRatioTXT.setText(clustersRatio + "");
									this.numberOfMisplacedItemsTXT.setText(misplacedItems + "");
									this.sensingAccuracyTXT.setText(sensingAccuracy + "");
									this.sensingMissingnessTXT.setText(sensingMissingness + "");
									
									this.handleGenerateInitialStateBTNAction();
									this.handlePerformMisplacementBTNAction();
									this.handleGenerateSnapshotBTNAction();
									this.handleSVGSnapshotBTNAction();
								}
							}
						}
					}
				}
			}
		}
		
	}
}
