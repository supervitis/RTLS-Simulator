package at.ac.wu.seramis.rtlssimulator.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SimulatorGUI extends Application
{
	private Scene primaryScene;
	private BorderPane rootLayout;
	
	@Override
	public void start(Stage primaryStage) throws Exception 
	{
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(this.getClass().getResource(this.getClass().getSimpleName() + ".fxml"));
			
			this.rootLayout = (BorderPane) fxmlLoader.load();

			this.primaryScene = new Scene(this.rootLayout);
			primaryStage.setScene(this.primaryScene);
			primaryStage.setTitle("RTLSSimulator");
			
			primaryStage.setMaximized(true);
			primaryStage.show();
	}
	
	public static void main(String[] args) 
	{
		launch(args);
	}
}
