package at.ac.wu.seramis.rtlssimulator.gui.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.jfxconverter.JFXConverter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;

public class SVGWriter 
{
	public static void writeSVG(Node node, String svgPath)
	 {
		 try (Writer writer = new BufferedWriter(new FileWriter(svgPath)))
		 {
			 TranscoderOutput output = new TranscoderOutput(writer);
			 Bounds bounds = node.getBoundsInLocal();
			 Rectangle2D rec = new Rectangle2D(bounds.getMinX(), bounds.getMinY(),
			 bounds.getWidth(), bounds.getHeight());
			
			 Document doc = SVGDOMImplementation.getDOMImplementation().createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI,"svg", null);
			
			 SVGGraphics2D g2D = new SVGGraphics2D(doc);
			 JFXConverter converter = new JFXConverter();
			 converter.convert(g2D, node);
			
			 // get the root element and add size
			 String minX = Double.toString(rec.getMinX());
			 String minY = Double.toString(rec.getMinY());
			 String width = Double.toString(rec.getWidth());
			 String height = Double.toString(rec.getHeight());
			 String size = minX + " " + minY + " " + width + " " + height;
			
			 Element svgRoot = g2D.getRoot();
			 svgRoot.setAttributeNS(null, "viewBox", size);
			
			 Writer wr = output.getWriter();
			 if (wr != null)
			 {
				 try
				 {
					 g2D.stream(svgRoot, wr);
				 }
				 catch (SVGGraphics2DIOException e)
				 {
					 // TODO Auto-generated catch block
					 e.printStackTrace();
				 }
				 return;
			 }
			
			 writer.flush();
		 }
		 catch (IOException e)
		 {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
	 }
}
