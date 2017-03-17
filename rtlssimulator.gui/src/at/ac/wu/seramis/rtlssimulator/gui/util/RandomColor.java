package at.ac.wu.seramis.rtlssimulator.gui.util;

import java.util.Random;

import javafx.scene.paint.Color;


public class RandomColor 
{
	public static Color getRandomColor()
	{
		Random random = new Random();
		
		float hue = random.nextFloat() * 360;
		float saturation = 0.7f; // 1.0 for brilliant, 0.0 for dull
		float brightness = 1.0f; // 1.0 for brighter, 0.0 for black
		
		return Color.hsb(hue, saturation, brightness);		
	}
	
	public static Color getRandomGrayscale()
	{
		Random random = new Random();
		
		float hue = 0; // meaningless anyway
		float saturation = 0.0f; // 1.0 for brilliant, 0.0 for dull
		float brightness = random.nextFloat(); // 1.0 for gray, 0.0 for black
		
		return Color.hsb(hue, saturation, brightness);		
	}
}
