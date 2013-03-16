
package com.ledpixelart.pc;

import ioio.lib.api.RgbLedMatrix;
import ioio.lib.api.exception.ConnectionLostException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * @author rmarquez
 */
public abstract class PixelTilePanel extends JPanel implements ActionListener
{
    protected List<JButton> buttons;
    
    protected byte[] BitmapBytes;
    
    protected boolean pixelFound;
    
    protected InputStream BitmapInputStream;
    
    protected short[] frame_;
    
    protected RgbLedMatrix matrix_;
    
    protected RgbLedMatrix.Matrix KIND;   
    
    public PixelTilePanel(RgbLedMatrix matrix, RgbLedMatrix.Matrix KIND)
    {
	GridLayout experimentLayout = new GridLayout(0, 5);
	experimentLayout.setHgap(5);	
	experimentLayout.setVgap(5);
	
	setLayout(experimentLayout);
	
	buttons = new ArrayList();
	
	BitmapBytes = new byte[KIND.width * KIND.height * 2]; //512 * 2 = 1024 or 1024 * 2 = 2048
	
	frame_ = new short[KIND.width * KIND.height];
	
	pixelFound = false;
	
	this.KIND = KIND;
	
	this.matrix_ = matrix;
        System.out.println("matrix in PixelTilePanel: " + this.matrix_);
    }
    
    protected void loadRGB565(String raw565ImagePath) throws ConnectionLostException 
    {

	BitmapInputStream = PixelApp.class.getClassLoader().getResourceAsStream(raw565ImagePath);

	try 
	{   
	    int n = BitmapInputStream.read(BitmapBytes, 0, BitmapBytes.length); // reads
	    // the
	    // input
	    // stream
	    // into
	    // a
	    // byte
	    // array
	    Arrays.fill(BitmapBytes, n, BitmapBytes.length, (byte) 0);
	} 
	catch (IOException e) 
	{
	    System.err.println("An error occured while trying to load " + raw565ImagePath + ".");
	    System.err.println("Make sure " + raw565ImagePath + "is included in the executable JAR.");
	    e.printStackTrace();
	}

	int y = 0;
	for (int f = 0; f < frame_.length; f++) 
	{
	    frame_[f] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
	    y = y + 2;
	}

	matrix_.frame(frame_);
    }
    
    private void loadRGB565PNG() throws ConnectionLostException 
    {
	int y = 0;
	for (int f = 0; f < frame_.length; f++) 
	{   
	    frame_[f] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
	    y = y + 2;
	}

	matrix_.frame(frame_);
    }    
    
    /**
     * This method needs calling to place the icons on the panel.
     */
    public void populate()
    {
	List<String> filenames;
	try 
	{
	    filenames = imageNames();
	    for(String file : filenames)
	    {
		ImageIcon icon = getImageIcon(file);
		JButton button = new JButton(icon);
		int i = file.lastIndexOf(".");
		String command = file.substring(0, i);
		button.setActionCommand(command);
		button.addActionListener(this);
		add(button);
		buttons.add(button);
	    }
	} 
	catch (Exception ex) 
	{
	    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
	}
	
    }
    
    protected abstract ImageIcon getImageIcon(String path);
    
    protected abstract List<String> imageNames() throws Exception;
    
    protected abstract String imagePath();
    
    protected void setPixelFound(boolean found)
    {
	pixelFound = found;
    }
    
    private void writeImagetoMatrix(String imagePath) throws ConnectionLostException 
    {  
	//here we'll take a PNG, BMP, or whatever and convert it to RGB565 via a canvas, also we'll re-size the image if necessary
	
	URL url = PixelApp.class.getClassLoader().getResource(imagePath);

	try 
	{
	    BufferedImage originalImage = ImageIO.read(url);
	    int width_original = originalImage.getWidth();
	    int height_original = originalImage.getHeight();

	    if (width_original != KIND.width || height_original != KIND.height) 
	    {  
		//the image is not the right dimensions, ie, 32px by 32px				
		BufferedImage ResizedImage = new BufferedImage(KIND.width, KIND.height, originalImage.getType());
		Graphics2D g = ResizedImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(originalImage, 0, 0, KIND.width, KIND.height, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);
		g.dispose();
		originalImage = ResizedImage;		
	    }

	    int numByte = 0;
	    int i = 0;
	    int j = 0;

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
		    byte byteH = (byte) ((pixel_to_send >> 8) & 0x0FF);
		    byte byteL = (byte) (pixel_to_send & 0x0FF);

		    //Writing it to array - High-byte is the first

		    BitmapBytes[numByte + 1] = byteH;
		    BitmapBytes[numByte] = byteL;
		    numByte += 2;
		}
	    }
	} 
	catch (IOException e) 
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	loadRGB565PNG();
    }
    
    /**
     * 
     * //************ this part of code writes to the LED matrix in code without any external file *********
	//  writeTest(); //this just writes a test pattern to the LEDs in code without using any external file, uncomment out this line if you want to see that and then comment out the next two lines
	//***************************************************************************************************
     */
    private void writeTest() 
    {
	for (int i = 0; i < frame_.length; i++) 
	{
	    //	frame_[i] = (short) (((short) 0x00000000 & 0xFF) | (((short) (short) 0x00000000 & 0xFF) << 8));  //all black
	    frame_[i] = (short) (((short) 0xFFF5FFB0 & 0xFF) | (((short) (short) 0xFFF5FFB0 & 0xFF) << 8));  //pink
	    //frame_[i] = (short) (((short) 0xFFFFFFFF & 0xFF) | (((short) (short) 0xFFFFFFFF & 0xFF) << 8));  //all white
	}
    } 
	    
}
