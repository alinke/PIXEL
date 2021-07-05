package com.ledpixelart.pcpixelart;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import ioio.lib.api.RgbLedMatrix;
import ioio.lib.api.exception.ConnectionLostException;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DrawingCanvas extends JPanel implements MouseListener, MouseMotionListener 
{
    private static final long serialVersionUID = 1L;
    
    private final int CANVAS_WIDTH = 200;
    private final int CANVAS_HEIGHT = 200;
    
    // last mouse position
    private int lastX;
    private int lastY;   
    
    private ioio.lib.api.RgbLedMatrix.Matrix KIND;
    
//    private ioio.lib.api.RgbLedMatrix matrix;
    
    private short [] frame;
    private static byte[] BitmapBytes;
    
    private static BufferedImage originalImage;    
    private static BufferedImage ResizedImage;
          
    public DrawingCanvas(RgbLedMatrix matrix, short [] frame, RgbLedMatrix.Matrix KIND) 
    {
//	this.matrix = matrix;
	this.frame = frame;
	
	BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048

	addMouseListener(this);         
	addMouseMotionListener(this);   

	this.KIND = KIND;

	setBackground(new Color(255, 255, 160)); // pale yellow

	Dimension d = new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
	setPreferredSize(d);
    }

    private void loadRGB565PNG() throws ConnectionLostException 
    {		
	if(TouchPaintPC.getMatrix() == null)
	{
	    throw new ConnectionLostException();
	}
	else
	{
	    int y = 0;
	    for (int i = 0; i < frame.length; i++) 
	    {
		    frame[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
		    y = y + 2;
	    }
	    TouchPaintPC.getMatrix().frame(frame);
	}
    }
    
    /**
     * Determines the starting point of the next line
     * @param event 
     */
    public void mousePressed(MouseEvent event) 
    {
	Point first = event.getPoint(); // where's the mouse?
	lastX = first.x; // save the mouse position
	lastY = first.y;
    }

    /**
     * Draws a line from the last point to the current point.
     * @param event 
     */    
    public void mouseDragged(MouseEvent event) 
    {
	// get the mouse position
	Point current = event.getPoint(); 

	Graphics page = getGraphics(); 
	
	page.drawLine(lastX, lastY, current.x, current.y);

	lastX = current.x; 
	lastY = current.y; 
	
	Dimension d = new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
	Rectangle canvas = new Rectangle(getLocation(), d);	
	BufferedImage screenCapture = null;
	try 
	{
	    screenCapture = new Robot().createScreenCapture(canvas);
	    originalImage = screenCapture;	    
	    try 
	    {
		WriteImagetoMatrix();
		loadRGB565PNG();
	    } 
	    catch (ConnectionLostException ex) 
	    {
		String message = "The connection to the IOIO was lost.";
		Logger.getLogger(DrawingCanvas.class.getName()).log(Level.SEVERE, message, ex);
	    }	    
	} 
	catch (AWTException ex) 
	{
	    String message = "An exception occured while screen captureing.";
	    Logger.getLogger(DrawingCanvas.class.getName()).log(Level.SEVERE, message, ex);
	}
    }

    public void mouseReleased(MouseEvent event) {
    }

    public void mouseClicked(MouseEvent event) {
    }

    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
    }

    public void mouseMoved(MouseEvent event) {
    }
    
    private void WriteImagetoMatrix() throws ConnectionLostException 
    {  
	//here we'll take a PNG, BMP, or whatever and convert it to RGB565 via a canvas, also we'll re-size the image if necessary
		
	int width_original = originalImage.getWidth();
	int height_original = originalImage.getHeight();

	if (width_original != KIND.width || height_original != KIND.height) 
	{  
	    //the image is not the right dimensions, ie, 32px by 32px	
	    ResizedImage = new BufferedImage(KIND.width, KIND.height, originalImage.getType());
	    Graphics2D g = ResizedImage.createGraphics();
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g.drawImage(originalImage, 0, 0, KIND.width, KIND.height, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);
	    g.dispose(); 
	    originalImage = ResizedImage;
	}						

	int numByte=0;
	int i = 0;
	int j = 0;
	int len = BitmapBytes.length;

	for (i = 0; i < KIND.height; i++) 
	{                 
	    for (j = 0; j < KIND.width; j++) 		 
	    {            
		Color c = new Color(originalImage.getRGB(j, i));  //i and j were reversed which was rotationg the image by 90 degrees
                
		int aRGBpix = originalImage.getRGB(j, i);  //i and j were reversed which was rotationg the image by 90 degrees
                
		int alpha;
                
		int red = c.getRed();
                
		int green = c.getGreen();
                
		int blue = c.getBlue();

		//RGB888
		//  red = (aRGBpix >> 16) & 0x0FF;
		//  green = (aRGBpix >> 8) & 0x0FF;
		//  blue = (aRGBpix >> 0) & 0x0FF; 
		//  alpha = (aRGBpix >> 24) & 0x0FF;

		//RGB565
		red = red >> 3;
		green = green >> 2;
		blue = blue >> 3;   
		//A pixel is represented by a 4-byte (32 bit) integer, like so:
		//00000000 00000000 00000000 11111111
		//^ Alpha  ^Red     ^Green   ^Blue
		//Converting to RGB565

		short pixel_to_send = 0;
		int pixel_to_send_int = 0;
		pixel_to_send_int = (red << 11) | (green << 5) | (blue);
		pixel_to_send = (short) pixel_to_send_int;

		//dividing into bytes
		byte byteH=(byte)((pixel_to_send >> 8) & 0x0FF);
		byte byteL=(byte)(pixel_to_send & 0x0FF);

		//Writing it to array - High-byte is the first
                    
		BitmapBytes[numByte+1]=byteH;
		BitmapBytes[numByte]=byteL;                    
		numByte+=2;
	    }
	}						 	
    }
    
}
