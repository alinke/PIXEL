
package com.ledpixelart.pcpixelart;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.RgbLedMatrix;
import ioio.lib.api.RgbLedMatrix.Matrix;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.pc.IOIOSwingApp;
import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Window;
import javax.swing.JFileChooser;

import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.JFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JLabel;

public class TouchPaintPC extends IOIOSwingApp implements ActionListener 
{    
    private CountDownTimer connectTimer;    

    private int deviceFound = 0;    
    private int matrix_model;    
    
    private static IOIO ioiO;   
    private static RgbLedMatrix matrix_;
    private static RgbLedMatrix.Matrix KIND;  //have to do it this way because there is a matrix library conflict

    private static short[] frame_;
    private static byte[] BitmapBytes;

    private static int width_original;
    private static int height_original;
    
    private static ActionListener animateTimer = null;
    
    private static BufferedImage originalImage;

    private static int i = 0;
    private static boolean pixelFound = false;
    private static int numFrames = 0;

    private int selectedFileTotalFrames;
    private int selectedFileDelay;

    private Timer timer;
    
    private static String decodedDirPath;
    private static String selectedFileName;
    private static InputStream decodedFile;
    private static BufferedReader br;
    private static String line;
    private static String fdelim;
    private static String fileAttribs;
    private static String[] fileAttribs2;

    public TouchPaintPC() 
    {      		       
	setPreferences();        
        
	connectTimer = new ConnectTimer(30000,5000); //pop up a message if it's not connected by this timer
	connectTimer.run();//this timer will pop up a message box if the device is not found 		 		
	 
	KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32;
	frame_ = new short[KIND.width * KIND.height];
	BitmapBytes = new byte[KIND.width * KIND.height * 2]; //512 * 2 = 1024 or 1024 * 2 = 2048
    }

    public static void main(String[] args) throws Exception 
    {  		
	TouchPaintPC app = new TouchPaintPC();
	app.go(args); //ui stuff		

	animateTimer = new ActionListener() 
	{
	    public void actionPerformed(ActionEvent evt) 
	    {
		if (!pixelFound) 
		{  
		    // only go here if PIXEL wa found, other leave the timer
		    return;
		}		

		i++;

		if (i >= numFrames - 1) 
		{
		    i = 0;
		}
		
		try 
		{
		    loadRGB565();

		} 
		catch (ConnectionLostException e1) 
		{

		    e1.printStackTrace();
		}		
	    }
	};
    }

    private static void loadRGB565() throws ConnectionLostException 
    {
	int y = 0;
	for (int i = 0; i < frame_.length; i++) 
	{
	    frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
	    y = y + 2;
	}

        RgbLedMatrix matrix_ = getMatrix();
	if(matrix_ != null)
	{
	    matrix_.frame(frame_);
	}	
    }

    private static void loadRGB565PNG() throws ConnectionLostException 
    {
	int y = 0;
	for (int i = 0; i < frame_.length; i++) 
	{
	    frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
	    y = y + 2;
	}

        RgbLedMatrix matrix_ = getMatrix();
	matrix_.frame(frame_);
    }
    
    
    private void setPreferences() //here is where we read the shared preferences into variables
    {         
	matrix_model = 3;      
	
	switch (matrix_model) 
	{  
	    case 0:
		KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x16;

		break;
	    case 1:
		KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x16;

		break;
	    case 2:
		KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32_NEW; //v1

		break;
	    case 3:
		KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2

		break;
	    default:	    		 
		KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2 as the default	
	}


	frame_ = new short [KIND.width * KIND.height];
	BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048
	 
//	try 
	{
//	    loadRGB565(); //this function loads a raw RGB565 image to the matrix
	} 
//	catch (ConnectionLostException ex) 
	{
//	    Logger.getLogger(TouchPaintPC.class.getName()).log(Level.SEVERE, null, ex);
	}          
    }

    /**
     * Here we'll take a PNG, BMP, or whatever and convert it to RGB565 via a 
     * canvas, also we'll re-size the image if necessary.
     * @param imagePath
     * @throws ConnectionLostException 
     */
    private static void WriteImagetoMatrix(String imagePath) throws ConnectionLostException 
    {  	
	//   originalImage = BitmapFactory.decodeFile(imagePath);  
	URL url = TouchPaintPC.class.getClassLoader().getResource(imagePath);

	try 
	{
	    originalImage = ImageIO.read(url);
	    width_original = originalImage.getWidth();
	    height_original = originalImage.getHeight();
	    //Log.w(TAG, "width: " + width_original);

	    if (width_original != KIND.width || height_original != KIND.height) 
	    {  
		// the image is not the right dimensions, ie, 32px by 32px
				
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
	    e.printStackTrace();
	}

	loadRGB565PNG();
    }

    private static byte[] Image2Byte(BufferedImage image) 
    {
	WritableRaster raster = image.getRaster();
	DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
	return buffer.getData();
    }

    public static byte[] extractBytes(BufferedImage image) throws IOException {

	// get DataBufferBytes from Raster
	WritableRaster raster = image.getRaster();
	DataBufferByte data = (DataBufferByte) raster.getDataBuffer();

	return (data.getData());
    }

    //return file (image) names in the chosen directory
    public ArrayList<String> getFileNames() 
    {
	//widget to let users select a directory or file
	JFileChooser chooser = new JFileChooser();
	//holds all file (image) names in the chosen directory
	ArrayList<String> myArr = new ArrayList<String>();

	//only allow directory selection
	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	//current directory is set
	chooser.setCurrentDirectory(new java.io.File("."));

	//pops up file chooser dialog, user chooses a directory
	int returnVal = chooser.showOpenDialog(null);

	//if the selected option was approved
	if (returnVal == JFileChooser.APPROVE_OPTION) 
	{
	    
	    File folder = chooser.getSelectedFile();
	    
	    String directory = chooser.getSelectedFile() + "\\";
	    
	    File[] listOfFiles = folder.listFiles();

	    //put all the names of the file objects into myArr
	    for (int i = 0; i < listOfFiles.length; i++) 
	    {
		if (listOfFiles[i].isFile()) {
		    myArr.add(directory + listOfFiles[i].getName());
		}
	    }
	}	
	else 
	{
	    System.out.println("No Selection ");
	}

	return myArr;
    }

    public static RgbLedMatrix getMatrix() 
    {        
        if (matrix_ == null) 
        {
            try 
            {
                matrix_ = ioiO.openRgbLedMatrix(KIND);
            } 
            catch (ConnectionLostException ex) 
            {
                String message = "The IOIO connection was lost.";
                Logger.getLogger(TouchPaintPC.class.getName()).log(Level.SEVERE, message, ex);
            }
        }
        
        return matrix_;
    }
        
    private static void writeTest() 
    {
	for (int i = 0; i < frame_.length; i++) 
	{
	    //	frame_[i] = (short) (((short) 0x00000000 & 0xFF) | (((short) (short) 0x00000000 & 0xFF) << 8));  //all black
	    frame_[i] = (short) (((short) 0xFFF5FFB0 & 0xFF) | (((short) (short) 0xFFF5FFB0 & 0xFF) << 8));  //pink
	    //frame_[i] = (short) (((short) 0xFFFFFFFF & 0xFF) | (((short) (short) 0xFFFFFFFF & 0xFF) << 8));  //all white
	}
    }
    protected boolean ledOn_;

    @Override
    protected Window createMainWindow(String args[]) 
    {
	try 
	{
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} 
	catch (Exception ex) 
	{
	    String message = "The native look and feel could not be set.";
	    Logger.getLogger(TouchPaintPC.class.getName()).log(Level.SEVERE, message, ex);
	}	

	JFrame frame = new JFrame("Click an Animation");
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	Container contentPane = frame.getContentPane();
	LayoutManager experimentLayout = new BorderLayout();

	contentPane.setLayout(experimentLayout);
	JLabel label = new JLabel("PixelTouchPC");
//        RgbLedMatrix matrix_ = getMatrix();
	DrawingCanvas canvas = new DrawingCanvas(matrix_, frame_, KIND);
	contentPane.add(canvas, BorderLayout.CENTER);

	// Display the window.
	frame.setSize(300, 400);
	frame.setLocationRelativeTo(null); // center it
	frame.setVisible(true);
	return frame;
    }

    @Override
    public IOIOLooper createIOIOLooper(String connectionType, Object extra) 
    {
	return new BaseIOIOLooper() 
	{
	    private DigitalOutput led_;

	    @Override
	    protected void setup() throws ConnectionLostException, InterruptedException 
	    {
		led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
                
//                RgbLedMatrix matrix_ = getMatrix();
		matrix_ = ioio_.openRgbLedMatrix(KIND);
                
                TouchPaintPC.this.ioiO = ioio_;
                
		pixelFound = true;
		
		System.out.println("Found PIXEL\n");
		System.out.println("You may now select one of the animations\n");

		loadRGB565(); //show select picture on PIXEL		
	    }	    
	};
    }

    @Override
    public void actionPerformed(ActionEvent event) 
    {
	selectedFileName = event.getActionCommand();
	decodedDirPath = "animations/decoded";	

	decodedFile = TouchPaintPC.class.getClassLoader().getResourceAsStream(decodedDirPath + "/" + selectedFileName + "/" + selectedFileName + ".txt"); //decoded/rain/rain.text
	//note can't use file operator here as you can't reference files from a jar file

	if (decodedFile != null) 
	{
	    // ok good, now let's read it, we need to get the total numbers of frames and the frame speed
	    try 
	    {
		br = new BufferedReader( new InputStreamReader(decodedFile) );
		line = br.readLine();		
	    } 
	    catch (IOException e) 
	    {
		//You'll need to add proper error handling here
	    }

	    fileAttribs = line.toString();  //now convert to a string	 
	    
	    fdelim = "[,]"; //now parse this string considering the comma split  ie, 32,60
	    fileAttribs2 = fileAttribs.split(fdelim);
	    selectedFileTotalFrames = Integer.parseInt(fileAttribs2[0].trim());

	    selectedFileDelay = Integer.parseInt(fileAttribs2[1].trim());
	}

	//****** Now let's setup the animation ******
	i = 0;

	numFrames = selectedFileTotalFrames;

	timer = new Timer(selectedFileDelay, animateTimer);

	if (timer.isRunning() == true) 
	{
	    timer.stop();
	}
	timer.start();
    }
    
    public void showNotFound()
    {
	
    }
    
    class IOIOThread extends BaseIOIOLooper 
    {  
	
	private void showToast(String message)
	{
	    System.out.println("message");
	}

	@Override
	protected void setup() throws ConnectionLostException 
	{
//            RgbLedMatrix matrix_ = getMatrix();
//	    matrix_ = ioio_.openRgbLedMatrix(KIND);
	    deviceFound = 1; 

	    //if we went here, then we are connected over bluetooth or USB

//	    connectTimer.cancel(); //we can stop this since it was found			

	    showToast("Bluetooth Connected");			
	}

	@Override
	public void loop() throws ConnectionLostException 
	{  		  		  					  			
	}	

	@Override
	public void disconnected() 
	{   			
	    System.err.println("IOIO disconnected");	
	    showToast("Bluetooth Disconnected");	    
	}

	@Override
	public void incompatible() 
	{  
	    //if the wrong firmware is there

	    showToast("Incompatbile firmware!");
	    showToast("This app won't work until you flash the IOIO with the correct firmware!");
	    showToast("You can use the IOIO Manager Android app to flash the correct firmware");

	    System.err.println("Incompatbile firmware!");
	}
    }
    
    public class ConnectTimer extends CountDownTimer
    {
	public ConnectTimer(long startTime, long interval)
	{
		super(startTime, interval);
	}

	@Override
	public void onFinish()
	{
		if (deviceFound == 0) 
		{
			showNotFound(); 					
		}
	}		

	@Override
	public void run() 
	{

	}   		
    }

    public abstract class CountDownTimer implements Runnable
    {
	public CountDownTimer(long startTime, long interval)
	{		
	}

	public abstract void onFinish();	    
    }
		
}
