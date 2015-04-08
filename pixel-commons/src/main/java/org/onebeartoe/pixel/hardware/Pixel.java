
package org.onebeartoe.pixel.hardware;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.RgbLedMatrix;

import ioio.lib.api.IOIO.VersionType;
import ioio.lib.api.exception.ConnectionLostException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import org.gifdecoder.GifDecoder;
import org.onebeartoe.pixel.sound.meter.SoundReading;

/**
 * @author Roberto Marquez
 * @author Al Linke
 */
public class Pixel 
{
    /**
     * This is for the animations.
     */
    private int i;
        
    public static IOIO ioiO;
    
    public RgbLedMatrix matrix;

//TODO: rename this matrix_type    
    public final RgbLedMatrix.Matrix KIND;
    
    public static AnalogInput analogInput1;
    
    public static  AnalogInput analogInput2;
    
    protected byte[] BitmapBytes;
    
    protected InputStream BitmapInputStream;
    
    protected short[] frame_;
    
    private float fps;
    
    public String fileType;
    
    public String gifNameNoExt;
    
    private static String localFileImagePath;
    
    private static VersionType v;
    
    private String userHome;
    
    private String pixelHome;
  
    private String animationsPath;
    
    private String decodedAnimationsPath;

    private String imagesPath;
    
    private int currentResolution;
    
// Is this a dup of currentResolution?    
    private static int GIFresolution;
    
    /**
     * the path to the source gifs in the jar
     */
    private String gifSourcePath = "animations/gifsource/";
    
    /**
     * This is for the animations.
     */
    private int GIFnumFrames;
    
    private volatile Timer timer;
    
    private String animationFilename;
    
    private PixelModes mode;

//TODO: rename for scrolling text
    private int x;
    
    private HashMap<String, Font> fonts;

//TODO: Why does this need to be static?   
//    public static final String [] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    private String scrollingText;
    
    /**
     * This is length in milliseconds of the delay between each scrolling text redraw.
     */
    private long scrollDelay = 500; 
    
    private Color scrollingTextColor = Color.ORANGE;
    
    private Logger logger;
    
    /**
     * @param KIND
     * @param resolution 
     */
    public Pixel(RgbLedMatrix.Matrix KIND, int resolution)
    {
        String name = getClass().getName();
        logger = Logger.getLogger(name);
        
        mode = PixelModes.STILL_IMAGE;
        
	this.KIND = KIND;
        
        this.currentResolution = resolution;
	
	BitmapBytes = new byte[KIND.width * KIND.height * 2]; //512 * 2 = 1024 or 1024 * 2 = 2048
	
	frame_ = new short[KIND.width * KIND.height];
        
        x = 0;
                
        fonts = new HashMap();
        
        scrollingText = "Scolling Text Initial Value";
        
        try
        {
            userHome = System.getProperty("user.home");
            
            pixelHome = userHome + "/pixel/";
            
            animationsPath = pixelHome + "animations/";            
            decodedAnimationsPath = animationsPath + "decoded/";
            
            imagesPath = pixelHome + "images/";
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Read the input stream into a byte array
     * @param raw565ImagePath
     * @throws ConnectionLostException 
     */
    public void loadRGB565(String raw565ImagePath) throws ConnectionLostException 
    {
	BitmapInputStream = getClass().getClassLoader().getResourceAsStream(raw565ImagePath);
//	BitmapInputStream = PixelApp.class.getClassLoader().getResourceAsStream(raw565ImagePath);

	try 
	{   
	    int n = BitmapInputStream.read(BitmapBytes, 0, BitmapBytes.length);
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

	matrix.frame(frame_);
    }
    
    private void loadRGB565PNG() throws ConnectionLostException 
    {
	int y = 0;
	for (int f = 0; f < frame_.length; f++) 
	{   
	    frame_[f] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
	    y = y + 2;
	}

	if(matrix != null)
	{
	    matrix.frame(frame_);
	}
    }
    
    /**
     * tells PIXEL to play the local files
     */
    public void playLocalMode() 
    {
    	try 
        {
    		matrix.playFile();
        } 
        catch (ConnectionLostException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }
    }    

    public String getHardwareVersion() 
    {
        String pixelHardwareVersion = null;
		if (ioiO != null) 
                {
	  	  	try {
				pixelHardwareVersion = ioiO.getImplVersion(v.HARDWARE_VER);
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.out.println("PIXEL was not found...");
			pixelHardwareVersion = "0";
		}
                
        return pixelHardwareVersion;
    }

    public String getImagesPath()
    {
        return imagesPath;
    }
    
    public String getFirmwareVersion() 
    {
        String pixelFirmware = null;
		if (ioiO != null) 
                {
	  	  	try {
				pixelFirmware = ioiO.getImplVersion(v.APP_FIRMWARE_VER);
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.out.println("PIXEL was not found...");
			pixelFirmware = "0";
		}
        return pixelFirmware;
    }
    
    public PixelModes getMode()
    {
        return mode;
    }

    public String getPixelHome()
    {
        return pixelHome;
    }
    
    //*** Al added, this code is to support the SD card and local animations
    public void interactiveMode() {  //puts PIXEL into interactive mode
    	try {
			matrix.interactive();
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void writeMode(float frameDelay) {  //puts PIXEL into write mode
    	try {
    		 matrix.writeFile(frameDelay); //put PIXEL in write mode
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    

    
    private static AnalogInput getAnalogInput(int pinNumber) 
    {
	if(ioiO != null)
	{
	    try 
	    {
		analogInput1 = ioiO.openAnalogInput(pinNumber);
	    } 
	    catch (ConnectionLostException ex) 
	    {
		String message = "The PIXEL connection was lost.";
		Logger.getLogger("Pixel").log(Level.SEVERE, message, ex);
	    }		
	}
        
        return analogInput1;
    }
    
    public static AnalogInput getAnalogInput1() 
    {
        if (analogInput1 == null) 
	{
	    analogInput1 = getAnalogInput(31);			    
        }
        
        return analogInput1;
    }
    
    public static AnalogInput getAnalogInput2() 
    {
        if (analogInput2 == null) 
	{
	    analogInput2 = getAnalogInput(32);
        }
        
        return analogInput2;
    }

    public String getAnimationsPath()
    {
        return animationsPath;
    }
    
    public String getDecodedAnimationsPath()
    {
        return decodedAnimationsPath;
    }
    
    private int[] getDecodedMetadata(String currentDir, String gifName) {  //not using this one right now
    	
    	String gifNamePath = currentDir + "/decoded/" + gifName + ".txt";
    	
    	File filemeta = new File(gifNamePath);
    	int[] decodedMetadata = null; //array    	
    	FileInputStream decodedFile = null; //fix this
    	try {
			decodedFile = new FileInputStream(gifNamePath);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	String line = "";

		    try 
		    {
				InputStreamReader streamReader = new InputStreamReader(decodedFile);
				BufferedReader br = new BufferedReader(streamReader);
				line = br.readLine();
		    } 
		    catch (IOException e) 
		    {
			    //You'll need to add proper error handling here
		    }

		    String fileAttribs = line.toString();  //now convert to a string	 
		    String fdelim = "[,]"; //now parse this string considering the comma split  ie, 32,60
		    String[] fileAttribs2 = fileAttribs.split(fdelim);
		    int selectedFileTotalFrames = Integer.parseInt(fileAttribs2[0].trim());
		    int selectedFileDelay = Integer.parseInt(fileAttribs2[1].trim());
		    
		    int resolution = Integer.parseInt(fileAttribs2[1].trim());	  //TO DO FIX THIS
		    
		    decodedMetadata[0] = selectedFileTotalFrames;
		    decodedMetadata[1] = selectedFileDelay;
		    decodedMetadata[2] = resolution;
		    
		    
		    if (selectedFileDelay != 0) {  //then we're doing the FPS override which the user selected from settings
	    	    fps = 1000.f / selectedFileDelay;
			} else { 
	    		fps = 0;
	    	}

        return (decodedMetadata); //we are returning an array here
    }

public boolean GIFNeedsDecoding(String decodedDir, String gifName, int currentResolution) {
	
	/*In this method we will first check if the decoded files are there
	if they are present, then let's read them and make sure the resolution in the decoded file matches the current matrix
	if no match, then we need to re-encode
	if the files are not there, then we need to re-encode anyway*/
	
	/*GIFName will be tree
	GIF Path will be c:\animations\tree.gif
	decdoed path will be c:\animations\tree.gif\decoded\tree.rgb565 and tree.txt
	*/
	
	gifName = FilenameUtils.removeExtension(gifName); //with no extension
	
	System.out.println("PIXEL LED panel resolution is: " + currentResolution);
	
	//String decodedGIFPathTXT = currentDir + "/decoded/" + gifName + ".txt";
	//String decodedGIFPath565 = currentDir + "/decoded/" + gifName + ".rgb565";
	
	String decodedGIFPathTXT = decodedDir + gifName + ".txt";
	String decodedGIFPath565 = decodedDir + gifName + ".rgb565";
	
	File filetxt = new File(decodedGIFPathTXT);
	File file565 = new File(decodedGIFPath565);
	
	if (filetxt.exists() && file565.exists()) { //need to ensure both files are there
		   
			if (getDecodedresolution(decodedDir, gifName) == currentResolution) {  //does the resolution in the encoded txt file match the current matrix
				
				return false;
			}
			else {
				return true;
			}
	}
	else {
		return true;
	}
}
    
    public float getDecodedfps(String decodedDir, String gifName) {  //need to return the meta data
    	
    	gifName = FilenameUtils.removeExtension(gifName); //with no extension, ex. tree instead of tree.gif
    	//String gifNamePath = currentDir + "/decoded/" + gifName + ".txt"; 
    	String gifNamePath = decodedDir + gifName + ".txt"; 
    	File filemeta = new File(gifNamePath);
    	
    	FileInputStream decodedFile = null; //fix this
    	try {
			decodedFile = new FileInputStream(gifNamePath);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	String line = "";

		    try 
		    {
				InputStreamReader streamReader = new InputStreamReader(decodedFile);
				BufferedReader br = new BufferedReader(streamReader);
				line = br.readLine();
		    } 
		    catch (IOException e) 
		    {
			    //You'll need to add proper error handling here
		    }

		    String fileAttribs = line.toString();  //now convert to a string	 
		    String fdelim = "[,]"; //now parse this string considering the comma split  ie, 32,60
		    String[] fileAttribs2 = fileAttribs.split(fdelim);
		    int selectedFileDelay = Integer.parseInt(fileAttribs2[1].trim());	
		    
		    if (selectedFileDelay != 0) {  //then we're doing the FPS override which the user selected from settings
	    	    fps = 1000.f / selectedFileDelay;
			} else { 
	    		fps = 0;
	    	}

		   return (fps);
	}
    
    public int getDecodednumFrames(String decodedDir, String gifName) {  //need to return the meta data
    	
    	//decodeddir is: userHome + "/pixel/animations/decoded/";  
    	
    	
    	gifName = FilenameUtils.removeExtension(gifName); //with no extension
    	//String framestring = "animations/decoded/" + animation_name + ".rgb565";
    	//String gifNamePath = gifName + ".txt";
    	//String gifNamePath = currentDir + "/decoded/" + gifName + ".txt"; 
    	
    	String gifNamePath = decodedDir + gifName + ".txt"; 
    	
    	
    	File filemeta = new File(gifNamePath);    	
    	FileInputStream decodedFile = null; //fix this
    	try {
			decodedFile = new FileInputStream(gifNamePath);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	String line = "";

		    try 
		    {
				InputStreamReader streamReader = new InputStreamReader(decodedFile);
				BufferedReader br = new BufferedReader(streamReader);
				line = br.readLine();
		    } 
		    catch (IOException e) 
		    {
			    //You'll need to add proper error handling here
		    }

		    String fileAttribs = line.toString();  //now convert to a string	 
		    String fdelim = "[,]"; //now parse this string considering the comma split  ie, 32,60
		    String[] fileAttribs2 = fileAttribs.split(fdelim);
		    int selectedFileTotalFrames = Integer.parseInt(fileAttribs2[0].trim());
		  
		   return (selectedFileTotalFrames);
	}
    
 public int getDecodedresolution(String decodedDir, String gifName) {  //need to return the meta data
    	
	    gifName = FilenameUtils.removeExtension(gifName); //with no extension
	    //String framestring = "animations/decoded/" + animation_name + ".rgb565";
	   // String gifNamePath = gifName + ".txt";
	    //String gifNamePath = currentDir + "/decoded/" + gifName + ".txt"; 
    	String gifNamePath = decodedDir + gifName + ".txt"; 
	    
    	File filemeta = new File(gifNamePath);
    	
    	FileInputStream decodedFile = null; //fix this
    	try {
			decodedFile = new FileInputStream(gifNamePath);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	String line = "";

		    try 
		    {
				InputStreamReader streamReader = new InputStreamReader(decodedFile);
				BufferedReader br = new BufferedReader(streamReader);
				line = br.readLine();
		    } 
		    catch (IOException e) 
		    {
			    //You'll need to add proper error handling here
		    }

		    String fileAttribs = line.toString();  //now convert to a string	 
		    String fdelim = "[,]"; //now parse this string considering the comma split  ie, 32,60,32  where last 32 is the resolution
		    String[] fileAttribs2 = fileAttribs.split(fdelim);
		    int resolution = Integer.parseInt(fileAttribs2[2].trim());	
		  
		   return (resolution);
	}
    
    public int getDecodedframeDelay(String decodedDir, String gifName) {  //need to return the meta data
    	
    	
    	gifName = FilenameUtils.removeExtension(gifName); //with no extension
    	//String gifNamePath = currentDir + "/decoded/" + gifName + ".txt"; 
    	String gifNamePath = decodedDir + gifName + ".txt"; 
    	
    	File filemeta = new File(gifNamePath);
    	
    	FileInputStream decodedFile = null; //fix this
    	try {
			decodedFile = new FileInputStream(gifNamePath);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	String line = "";

		    try 
		    {
				InputStreamReader streamReader = new InputStreamReader(decodedFile);
				BufferedReader br = new BufferedReader(streamReader);
				line = br.readLine();
		    } 
		    catch (IOException e) 
		    {
			    //You'll need to add proper error handling here
		    }

		    String fileAttribs = line.toString();  //now convert to a string	 
		    String fdelim = "[,]"; //now parse this string considering the comma split  ie, 32,60
		    String[] fileAttribs2 = fileAttribs.split(fdelim);
		    int selectedFileDelay = Integer.parseInt(fileAttribs2[1].trim());

		   return (selectedFileDelay);
	}
    
    public boolean GIFRGB565Exists(String decodedDir, String selectedFileName) {
    	
    	System.out.println("selected file name: " + selectedFileName);
    	int i = selectedFileName.lastIndexOf(".");
    	selectedFileName = selectedFileName.substring(0, i);
    	System.out.println("corrected file name: " + selectedFileName);
    	
    	//now let's check if this file exists
    	
    	File file565 = new File(decodedDir + selectedFileName + ".rgb565");
    	
    	if (file565.exists()) return true;
    	else return false;
    }
    
    public boolean gifTxtExists(String decodedDir, String selectedFileName) {
    	
    	//System.out.println("selected file name: " + selectedFileName);
    	int i = selectedFileName.lastIndexOf(".");
    	selectedFileName = selectedFileName.substring(0, i);
    	//System.out.println("corrected file name: " + selectedFileName);
    	
    	//now let's check if this file exists
    	
    	File filetxt = new File(decodedDir + selectedFileName + ".txt");
    	
//TODO: BRACKETS        
    	if (filetxt.exists()) return true;
    	else return false;
    }
    
    public void sendPixelDecodedFrame(String decodedDir, String gifName, int x, int selectedFileTotalFrames, int selectedFileResolution, int frameWidth, int frameHeight) 
    {
		 
    	BitmapBytes = new byte[frameWidth * frameHeight * 2]; //512 * 2 = 1024 or 1024 * 2 = 2048
		frame_ = new short[frameWidth * frameHeight];
		
		gifName = FilenameUtils.removeExtension(gifName); //with no extension
    	String gifNamePath = decodedDir + gifName + ".rgb565";  //  ex. c:\animations\decoded\tree.rgb565
    	String gifname2 = gifName;
    
    	
    	File file = new File(gifNamePath);
			if (file.exists()) 
                        {
				
				/*Because the decoded gif is one big .rgb565 file that contains all the frames, we need
			to use the raf pointer and extract just a single frame at a time and then we'll move the 
			pointer to get the next frame until we reach the end of the file*/
				
     		RandomAccessFile raf = null;
			
			//let's setup the seeker object and set it at the beginning of the rgb565 file
			try {
				raf = new RandomAccessFile(file, "r");
				try {
					raf.seek(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}  // "r" means open the file for reading
			
			int frame_length;
			
			 switch (selectedFileResolution) {
	            case 16: frame_length = 1024;
	                     break;
	            case 32: frame_length = 2048;
	                     break;
	            case 64: frame_length = 4096;
	                     break;
	            case 128: frame_length = 8192;
                		break;
	            default: frame_length = 2048;
	                     break;
	          }
			
			//now let's see forward to a part of the file
			try {
				raf.seek(x*frame_length);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} 
			
   			 
   			if (frame_length > Integer.MAX_VALUE) {
   			    try {
					throw new IOException("The file is too big");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
   			}
   			 
   			// Create the byte array to hold the data
   			BitmapBytes = new byte[(int)frame_length];
   			
   			// Read in the bytes
   			int offset = 0;
   			int numRead = 0;
   			try {
				while (offset < BitmapBytes.length && (numRead=raf.read(BitmapBytes, offset, BitmapBytes.length-offset)) >= 0) {
				    offset += numRead;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
   			 
   			// Ensure all the bytes have been read in
   			if (offset < BitmapBytes.length) {
   			    try {
					throw new IOException("The file was not completely read: "+file.getName());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
   			}
   			 
   			// Close the input stream, all file contents are in the bytes variable
   			try {
   				raf.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
   			
   			//now that we have the byte array loaded, load it into the frame short array
   			
   			int y = 0;
     		for (int i = 0; i < frame_.length; i++) 
                {
     			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
     			y = y + 2;
     		}
     		
		   	try 
                        {
		   		matrix.frame(frame_);
				
			} 
                        catch (ConnectionLostException e) 
                        {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
        }
        else 
        {
            // do nothing huh?
        }
    }
    
    public void scrollText()
    {
        stopExistingTimer();

        timer = new Timer();

        TimerTask drawTask = new TextScroller();

        Date firstTime = new Date();

        timer.schedule(drawTask, firstTime, scrollDelay);
    }
    
    public void setDecodedAnimationsPath(String decodedAnimationsPath)
    {
        this.decodedAnimationsPath = decodedAnimationsPath;
    }
    
    public void setMode(PixelModes mode)
    {
        if( this.mode.equals(mode) )
        {
            System.out.println("Pixel is ignoring a setMode() call.  The mode is already " + this.mode + "/" + mode);
        }
        else
        {
            // the mode has changed
            stopExistingTimer();
        }

        this.mode = mode;
    }
	
    public void decodeGIF(String decodedDir, String gifFilePath, int currentResolution, int pixelMatrix_width, int pixelMatrix_height) 
    {  //pass the matrix type
		
		//we should add another flag here if we're decoding from the jar or user supplied gif
		
		//we're going to decode a native GIF into our RGB565 format
	    //we'll need to know the resolution of the currently selected matrix type: 16x32, 32x32, 32x64, or 64x64
		//and then we will receive the gif accordingly as we decode
		//we also need to get the original width and vuHeight of the gif which is easily done from the gif decoder class
		//String gifName = FilenameUtils.removeExtension(gifName); //with no extension
		
	    String selectedFileName = FilenameUtils.getName(gifFilePath); 
		fileType = FilenameUtils.getExtension(gifFilePath);
	    gifNameNoExt = FilenameUtils.removeExtension(selectedFileName); //with no extension
		
		System.out.println("User selected file name: " + selectedFileName);
		//System.out.println("User selected file type: " + fileType);
		//System.out.println("User selected file name no extension: " + gifNameNoExt);
		
		
		//String gifNamePath = currentDir + "/" + gifName + ".gif";  //   ex. c:\animation\tree.gif
		System.out.println("User selected file name path: " + gifFilePath);
		File file = new File(gifFilePath);
		if (file.exists()) {
			
			  //since we are decoding, we need to first make sure the .rgb565 and .txt decoded file is not there and delete if so.
			  String gifName565Path =  decodedDir + gifNameNoExt + ".rgb565";  //   ex. c:\animation\decoded\tree.rgb565
			  String gifNameTXTPath = decodedDir + gifNameNoExt + ".txt";  //   ex. c:\animation\decoded\tree.txt
			  
			  //since we are decoding, we need to first make sure the .rgb565 and .txt decoded file is not there and delete if so.
			//  String gifName565Path = decodedAnimationsPath + gifName + ".rgb565";  //   ex. c:\animation\decoded\tree.rgb565
			//  String gifNameTXTPath = decodedAnimationsPath + gifName + ".txt";  //   ex. c:\animation\decoded\tree.txt
			  
			  File file565 = new File(gifName565Path);
			  File fileTXT = new File(gifNameTXTPath);
			  
			  if (file565.exists()) file565.delete();
			  if (fileTXT.exists()) file565.delete();
			  //*******************************************************************************************
			
			  GifDecoder d = new GifDecoder();
			  d.read(gifFilePath);
	         // d.read(getClass().getClassLoader().getResourceAsStream("animations/" + gifName + ".gif")); //read the soruce gif from the jar
	          //InputStream stream = Pixel.class.getResourceAsStream("animations/" + jarGIFName); //TO DO maybe later we'll change this if we use pngs instead of gifs on the image tile
	          int numFrames = d.getFrameCount(); 
	          int frameDelay = d.getDelay(1); //even though gifs have a frame delay for each frmae, pixel doesn't support this so we'll take the frame rate of the second frame and use this for the whole animation. We take the second frame because often times the frame delay of the first frame in a gif is much longer than the rest of the frames
	          
	          Dimension frameSize = d.getFrameSize();
	          int frameWidth = frameSize.width;
	          int frameHeight = frameSize.height;
	         
	          System.out.println("frame count: " + numFrames);
	          System.out.println("frame delay: " + frameDelay);
	          System.out.println("frame height: " + frameHeight);
	          System.out.println("frame width: " + frameWidth);
	          	          
	          for (int i = 0; i < numFrames; i++) { //loop through all the frames
	             BufferedImage rotatedFrame = d.getFrame(i);  
	            // rotatedFrame = Scalr.rotate(rotatedFrame, Scalr.Rotation.CW_90, null); //fixed bug, no longer need to rotate the image
	            // rotatedFrame = Scalr.rotate(rotatedFrame, Scalr.Rotation.FLIP_HORZ, null); //fixed bug, no longer need to flip the image
	             
	             // These worked too but using the scalr library gives quicker results
	             //rotatedFrame = getFlippedImage(rotatedFrame); //quick hack, for some reason the code below i think is flipping the image so we have to flip it here as a hack
	             //rotatedFrame = rotate90ToLeft(rotatedFrame);  //quick hack, same as above, have to rotate
	              
	    		 if (frameWidth != pixelMatrix_width || frameHeight != pixelMatrix_height) {
	    			 System.out.println("Resizing and encoding " + selectedFileName + " frame " + i);
	    			// rotatedFrame = Scalr.resize(rotatedFrame, pixelMatrix_width, pixelMatrix_height); //resize it, need to make sure we do not anti-alias
	    			 
	    			 try {
						rotatedFrame = getScaledImage(rotatedFrame, pixelMatrix_width,pixelMatrix_height);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			 
	    			 
	    		 }
	    		 else {
	    			 System.out.println("Encoding " + selectedFileName + " frame " + i);
	    		 }
	            
	             //this code here to convert a java image to rgb565 taken from stack overflow http://stackoverflow.com/questions/8319770/java-image-conversion-to-rgb565/
	    		 BufferedImage sendImg  = new BufferedImage(pixelMatrix_width, pixelMatrix_height, BufferedImage.TYPE_USHORT_565_RGB);
	             sendImg.getGraphics().drawImage(rotatedFrame, 0, 0, pixelMatrix_width, pixelMatrix_height, null);    

	             int numByte=0;
	             BitmapBytes = new byte[pixelMatrix_width*pixelMatrix_height*2];

	                int x=0;
	                int y=0;
	                int len = BitmapBytes.length;

	                for (x=0 ; x < pixelMatrix_height; x++) { //TO DO double check to make this is right
	                    for (y=0; y < pixelMatrix_width; y++) {

	                        Color c = new Color(sendImg.getRGB(y, x));  // x and y were switched in the original code which was causing the image to rotate by 90 degrees and was flipped horizontally, switching x and y fixes this bug
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
	                        byte byteH=(byte)((pixel_to_send >> 8) & 0x0FF);
	                        byte byteL=(byte)(pixel_to_send & 0x0FF);

	                        //Writing it to array - High-byte is the first, big endian byte order
	                        BitmapBytes[numByte]=byteL;
	                        BitmapBytes[numByte+1]=byteH;
	                        
	                        numByte+=2;
	                    }
	                }
			   		    
			   		 File decodeddir = new File(decodedDir); //this could be gif, gif64, or usergif
					    if(decodeddir.exists() == false)
			             {
					    	decodeddir.mkdirs();
			             }
					
				   			try {
							
								appendWrite(BitmapBytes, decodedDir + gifNameNoExt + ".rgb565"); //this writes one big file instead of individual ones
								
								
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								//Log.e("PixelAnimate", "Had a problem writing the original unified animation rgb565 file");
								e1.printStackTrace();
							}
				  
	             
	          } //end for, we are done with the loop so let's now write the file
	          
	           //********** now let's write the meta-data text file
		   		
		   		if (frameDelay == 0 || numFrames == 1) {  //we can't have a 0 frame delay so if so, let's add a 100ms delay by default
		   			frameDelay = 100;
		   		}
		   		
		   		String filetag = String.valueOf(numFrames) + "," + String.valueOf(frameDelay) + "," + String.valueOf(currentResolution); //current resolution may need to change to led panel type
		   				
	     		   File myFile = new File(decodedDir + gifNameNoExt + ".txt");  				       
	     		   try {
					myFile.createNewFile();
					FileOutputStream fOut = null;
					fOut = new FileOutputStream(myFile);
			        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
					myOutWriter.append(filetag); 
					myOutWriter.close();
					fOut.close();	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("ERROR, could not write " + selectedFileName);
					e.printStackTrace();
				}
		}
		else {
			System.out.println("ERROR  Could not find file " + gifFilePath);
		}
	} 
	

//TODO: pass the matrix type, OR BETTER YET, USE THE INSTANCE'S PIXEL ENVIRONMENT OBJECT
    public void decodeGIFJar(final String decodedDir, String gifSourcePath, String gifName, int currentResolution, final int pixelMatrix_width, final int pixelMatrix_height) 
    {  
	//BitmapBytes = new byte[pixelMatrix_width * pixelMatrix_height * 2]; //512 * 2 = 1024 or 1024 * 2 = 2048
	//frame_ = new short[pixelMatrix_width * pixelMatrix_height];	
	
	//we're going to decode a native GIF into our RGB565 format
	    //we'll need to know the resolution of the currently selected matrix type: 16x32, 32x32, 32x64, or 64x64
		//and then we will receive the gif accordingly as we decode
		//we also need to get the original width and vuHeight of the gif which is easily done from the gif decoder class
	//String str3 = new String(str1); 
	    
		 gifName = FilenameUtils.removeExtension(gifName); //with no extension
		//String gifNamePath = currentDir + "/" + gifName + ".gif";  //   ex. c:\animation\tree.gif

	    InputStream GIFStream = null; 
	   // GIFStream = getClass().getClassLoader().getResourceAsStream("animations/gifsource/" + gifName + ".gif"); //since we changed the thumbnails to pngs instead of gifs for performance reasons
	      GIFStream = getClass().getClassLoader().getResourceAsStream(gifSourcePath + gifName + ".gif"); //since we changed the thumbnails to pngs instead of gifs for performance reasons
	    
		if (GIFStream != null) 
                {	
			
			//since we are decoding, we need to first make sure the .rgb565 and .txt decoded file is not there and delete if so.
			  //String gifName565Path = currentDir + "/decoded/" + gifName + ".rgb565";  //   ex. c:\animation\decoded\tree.rgb565
			  //String gifNameTXTPath = currentDir + "/decoded/" + gifName + ".txt";  //   ex. c:\animation\decoded\tree.txt
			  
			  String gifName565Path = decodedDir + gifName + ".rgb565";  //   ex. c:\animation\decoded\tree.rgb565
			  String gifNameTXTPath = decodedDir + gifName + ".txt";  //   ex. c:\animation
			  
			  File file565 = new File(gifName565Path);
			  File fileTXT = new File(gifNameTXTPath);
			  
			  if (file565.exists()) file565.delete();
                          
//TODO: Do we really want to delete file565 if fileTXT.exists()?
			  if (fileTXT.exists()) file565.delete();
                          
                          
			  //*******************************************************************************************
			
			  final GifDecoder d = new GifDecoder();
	          d.read(getClass().getClassLoader().getResourceAsStream(gifSourcePath + gifName + ".gif"));
	          final int numFrames = d.getFrameCount(); 
	          int frameDelay = d.getDelay(1); //even though gifs have a frame delay for each frmae, pixel doesn't support this so we'll take the frame rate of the second frame and use this for the whole animation. We take the second frame because often times the frame delay of the first frame in a gif is much longer than the rest of the frames
	          
	          Dimension frameSize = d.getFrameSize();
	          final int frameWidth = frameSize.width;
	          final int frameHeight = frameSize.height;
	         
	          System.out.println("frame count: " + numFrames);
	          System.out.println("frame delay: " + frameDelay);
	          System.out.println("frame height: " + frameHeight);
	          System.out.println("frame width: " + frameWidth);
	          
	          
                for (int i = 0; i < numFrames; i++)
                { 
                    //loop through all the frames
    	             
                    BufferedImage rotatedFrame = d.getFrame(i);  
    	             //in case we want to add an option to rotate the image, we could use this code later, a user requested this for the 16x32 matrix
    	            // rotatedFrame = Scalr.rotate(rotatedFrame, Scalr.Rotation.CW_90, null); //fixed bug, no longer need to rotate the image
    	            // rotatedFrame = Scalr.rotate(rotatedFrame, Scalr.Rotation.FLIP_HORZ, null); //fixed bug, no longer need to flip the image
    	             
    	             // These worked too but using the scalr library gives quicker results
    	             //rotatedFrame = getFlippedImage(rotatedFrame); //quick hack, for some reason the code below i think is flipping the image so we have to flip it here as a hack
    	             //rotatedFrame = rotate90ToLeft(rotatedFrame);  //quick hack, same as above, have to rotate
    	              
    	    		 if (frameWidth != pixelMatrix_width || frameHeight != pixelMatrix_height) {
    	    			 System.out.println("Resizing and encoding " + gifName + ".gif" + " frame " + i);
    	    			// rotatedFrame = Scalr.resize(rotatedFrame, pixelMatrix_width, pixelMatrix_height); //resize it, need to make sure we do not anti-alias
    	    			 
    	    			 try {
    						rotatedFrame = getScaledImage(rotatedFrame, pixelMatrix_width,pixelMatrix_height);
    					} catch (IOException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    	    			 
    	    			 
    	    		 }
    	    		 else {
    	    			 System.out.println("DO NOT INTERRUPT: Encoding " + gifName + ".gif" + " frame " + i);
    	    		 }
    	            
    	             //this code here to convert a java image to rgb565 taken from stack overflow http://stackoverflow.com/questions/8319770/java-image-conversion-to-rgb565/
    	    		 BufferedImage sendImg  = new BufferedImage(pixelMatrix_width, pixelMatrix_height, BufferedImage.TYPE_USHORT_565_RGB);
    	             sendImg.getGraphics().drawImage(rotatedFrame, 0, 0, pixelMatrix_width, pixelMatrix_height, null);    

    	             int numByte=0;
    	             BitmapBytes = new byte[pixelMatrix_width*pixelMatrix_height*2];

    	                int x=0;
    	                int y=0;
    	                int len = BitmapBytes.length;

    	                for (x=0 ; x < pixelMatrix_height; x++) {
    	                    for (y=0; y < pixelMatrix_width; y++) {

    	                        Color c = new Color(sendImg.getRGB(y, x));  // x and y were switched in the original code which was causing the image to rotate by 90 degrees and was flipped horizontally, switching x and y fixes this bug
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
    	                        byte byteH=(byte)((pixel_to_send >> 8) & 0x0FF);
    	                        byte byteL=(byte)(pixel_to_send & 0x0FF);

    	                        //Writing it to array - High-byte is the first, big endian byte order
    	                        BitmapBytes[numByte]=byteL;
    	                        BitmapBytes[numByte+1]=byteH;
    	                        
    	                        numByte+=2;
    	                    }
    	                }
    			   		    
    			   		 File decodeddir = new File(decodedDir); //this could be gif, gif64, or usergif
    					    if(decodeddir.exists() == false)
    			             {
    					    	decodeddir.mkdirs();
    			             }
    					
    				   			try {
    							
    								appendWrite(BitmapBytes, decodedDir + gifName + ".rgb565"); //one big file to user home/pixel/animations/decoded/gifname.gif
    								
    								
    							} catch (IOException e1) {
    								// TODO Auto-generated catch block
    								//Log.e("PixelAnimate", "Had a problem writing the original unified animation rgb565 file");
    								e1.printStackTrace();
    							}
    				  
    	             
    	          } //end for, we are done with the loop so let's now write the file
	          
	           //********** now let's write the meta-data text file
		   		
		   		if (frameDelay == 0 || numFrames == 1) {  //we can't have a 0 frame delay so if so, let's add a 100ms delay by default
		   			frameDelay = 100;
		   		}
		   		
		   		String filetag = String.valueOf(numFrames) + "," + String.valueOf(frameDelay) + "," + String.valueOf(currentResolution); //current resolution may need to change to led panel type
		   				
	     		   File myFile = new File(decodedDir + gifName + ".txt");  				       
	     		   try {
					myFile.createNewFile();
					FileOutputStream fOut = null;
					fOut = new FileOutputStream(myFile);
			        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
					myOutWriter.append(filetag); 
					myOutWriter.close();
					fOut.close();	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("ERROR, could not write " + gifName);
					e.printStackTrace();
				}
		}
		else 
                {
			System.out.println("ERROR  Could not find " + gifSourcePath + gifName + ".gif in the JAR file");
		
                }
	
    }  

    public void drawEqualizer(double [] values) throws ConnectionLostException
    {
        int w = KIND.width;
        int h = KIND.height;

        BufferedImage vuImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        Color textColor = Color.RED;
        
        Graphics2D g2d = vuImage.createGraphics();
        g2d.setPaint(textColor);
        
        double COLUMN_WIDTH = w / (double) values.length;        
        int x = 0;
        
        for(double f : values)
        {
            double vuHeight = h * f;
                    
            double y = (double) h - vuHeight;
                    
            g2d.fillRect(x, (int) y, (int) COLUMN_WIDTH, (int) vuHeight);
            
            x += COLUMN_WIDTH;
        }

        writeImagetoMatrix(vuImage, w, h);        
    }

    /**
     * Override this to perform any additional background drawing on the image that get sent to the PIXEL
     * @param g2d 
     */
    protected void additionalBackgroundDrawing(Graphics2D g2d) throws Exception
    {
        
    }    
    
    /**
     * Override this to perform any additional foreground drawing on the image that get sent to the PIXEL
     * @param g2d 
     */
    protected void additionalForegroundDrawing(Graphics2D g2d) throws Exception
    {
        
    }
          
    public static void appendWrite(byte[] data, String filename) throws IOException 
    {
        FileOutputStream fos = new FileOutputStream(filename, true);  //true means append, false is over-write
        fos.write(data);
        fos.close();
    }
  
  public static String getSelectedFilePath(Component command) {
	    String path = command.toString();
		//System.out.println("image comamand: " + path);	
		path = path.replaceAll(",", "\r\n");
		Properties properties = new Properties();
		
		try {
		    properties.load(new StringReader(path));
		} catch (IOException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		}

		localFileImagePath = properties.getProperty("defaultIcon");
		
		String selectedFileName = FilenameUtils.getName(localFileImagePath); //with no extension
		//System.out.println("Selected File Name: " + selectedFileName);
		String fileType = FilenameUtils.getExtension(selectedFileName);
		String gifNameNoExt = FilenameUtils.removeExtension(selectedFileName); //with no extension
		
		//System.out.println("Local File Image Path: "+ localFileImagePath);
		//System.out.println("User selected file name: " + selectedFileName);
		//System.out.println("User selected file type: " + fileType);
		//System.out.println("User selected file name no extension: " + gifNameNoExt);
		
      return localFileImagePath;
  }
  
    /**
     * resizes our image and preserves hard edges we need for pixel art
     * @param image
     * @param width
     * @param height
     * @return
     * @throws IOException 
     */
    public static BufferedImage getScaledImage(BufferedImage image, int width, int height) throws IOException 
    {
	    int imageWidth  = image.getWidth();
	    int imageHeight = image.getHeight();

	    double scaleX = (double)width/imageWidth;
	    double scaleY = (double)height/imageHeight;
	    AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
	    AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

	    return bilinearScaleOp.filter(
	        image,
	        new BufferedImage(width, height, image.getType()));	
    }
    
    public void setScrollDelay(long delay)
    {
        scrollDelay = delay;
    }
    
    public void setScrollingText(String text)
    {
        scrollingText = text;
    }
    
    public void setScrollTextColor(Color color)
    {
        scrollingTextColor = color;
    }
    
    public void stopExistingTimer()
    {
        System.out.println("checking PIXEL activity in " + getClass().getSimpleName() + ".");

        if(timer == null)
        {
            System.out.println("No timer stop needed at Pixel mode change.");
        }
        else
        {
            System.out.println("Pixel is stopping PIXEL activity in " + getClass().getSimpleName() + ".");
            timer.cancel();
        }
    }    
  
    public void writeAnimation(String selectedFileName, boolean writeMode)
    {
        animationFilename = selectedFileName;
        if(gifTxtExists(decodedAnimationsPath,selectedFileName) == true && GIFRGB565Exists(decodedAnimationsPath,selectedFileName) == true) 
        {
            System.out.println("This GIF was already decoded");
        }
        else 
        {
            System.out.println("Decoding " + selectedFileName);
            // the text file is not there so we cannot continue and we must decode, let's first copy the file to home dir
            decodeGIFJar(decodedAnimationsPath, gifSourcePath,selectedFileName, currentResolution, KIND.width, KIND.height);
        }
			    
        if (GIFNeedsDecoding(decodedAnimationsPath, selectedFileName, currentResolution) == true) 
        {
            System.out.println("Selected LED panel is different than the encoded GIF, need to re-encode...");
            decodeGIFJar(decodedAnimationsPath, gifSourcePath, selectedFileName, currentResolution, KIND.width, KIND.height);
        }
	
        //****** Now let's setup the animation ******

// TODO: replace animation_name with selectedFileName
        String animation_name = selectedFileName;

        float GIFfps = getDecodedfps(decodedAnimationsPath, animation_name); //get the fps //to do fix this later becaause we are getting from internal path
        GIFnumFrames = getDecodednumFrames(decodedAnimationsPath, animation_name);
        int gifSelectedFileDelay = getDecodedframeDelay(decodedAnimationsPath, animation_name);
        
        currentResolution = getDecodedresolution(decodedAnimationsPath, animation_name);
        GIFresolution = currentResolution;

        System.out.println("Selected GIF Resolution: " + GIFresolution);
        System.out.println("Current LED Panel Resolution: " + currentResolution);
        System.out.println("GIF Width: " + KIND.width);
        System.out.println("GIF Height: " + KIND.height);
        
        System.out.println("The existing timer was stopped");
		
        String pixelHardwareId = "not found";
        try 
        {
            pixelHardwareId = ioiO.getImplVersion(v.HARDWARE_VER);
        } 
        catch (ConnectionLostException ex) 
        {
            Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        stopExistingTimer();
        
        if (pixelHardwareId.substring(0,4).equals("PIXL") && writeMode == true) 
        {
            interactiveMode();
            
            // need to tell PIXEL the frames per second to use, how fast to play the animations
            writeMode(GIFfps); 
            System.out.println("Now writing to PIXEL's SD card, the screen will go blank until writing has been completed..."); 

            // we'll run this in the background and also update the UI with progress
            System.out.println("The Pixel animation writter is being created");
            Date now = new Date();
            SendGifAnimationTask wp = new SendGifAnimationTask();
            timer = new Timer();
            timer.schedule(wp, now);
            System.out.println("The Pixel animation writter was created");
        }
        else 
        {
            System.out.println("A non PIXL, version of the timer is starting.");
            
            stopExistingTimer();
            System.out.println("stopped the existing timer again.");
            
            TimerTask animateTimer = new AnimateTimer();
            timer = new Timer();
            Date firstTime = new Date();
            timer.schedule(animateTimer, firstTime, gifSelectedFileDelay);
        
            System.out.println("A non PIXL, version of the timer has started.");
        } 
    }
  
    /**
     * This method is used to write a single frame to the pixel.
     * @param originalImage
     * @param pixelMatrix_width
     * @param pixelMatrix_height
     * @throws ConnectionLostException 
     */
    public void writeImagetoMatrix(BufferedImage originalImage,  int pixelMatrix_width, int pixelMatrix_height) throws ConnectionLostException     
    {        
        BitmapBytes = new byte[pixelMatrix_width * pixelMatrix_height * 2]; //512 * 2 = 1024 or 1024 * 2 = 2048
        frame_ = new short[pixelMatrix_width * pixelMatrix_height];
	  
	  //here we'll take a PNG, BMP, or whatever and convert it to RGB565 via a canvas, also we'll re-size the image if necessary
        int width_original = originalImage.getWidth();
        int height_original = originalImage.getHeight();

        if (width_original != pixelMatrix_width || height_original != pixelMatrix_height) 
        {  
        	//the image is not the right dimensions so we need to resize			
            BufferedImage ResizedImage = new BufferedImage(pixelMatrix_width, pixelMatrix_height, originalImage.getType());
            Graphics2D g = ResizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);  //IMPORTANT to use nearest neighbor or you'll get anti-aliasing which is BAD for pixel art
            g.drawImage(originalImage, 0, 0, pixelMatrix_width, pixelMatrix_height, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);
            g.dispose();
            originalImage = ResizedImage;		
        }

        int numByte = 0;
        int i = 0;
        int j = 0;

        for (i = 0; i < pixelMatrix_height; i++) 
        {
            for (j = 0; j < pixelMatrix_width; j++) 
            {
                Color c = new Color(originalImage.getRGB(j, i));  //i and j were reversed which was rotationg the image by 90 degrees
//                int aRGBpix = originalImage.getRGB(j, i);  //i and j were reversed which was rotationg the image by 90 degrees
//                int alpha;
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

	loadRGB565PNG();
    }
    
    /**          
     * this part of code writes to the LED matrix in code without any external file
     * this just writes a test pattern to the LEDs in code without using any external 
     * file	
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


//TODO: this is not a Timer, where is this used?
    private class AnimateTimer extends TimerTask
    {
        @Override
        public void run()
        {
            i++;

            if (i >= GIFnumFrames - 1) 
            {
                i = 0;
            }
            sendPixelDecodedFrame(decodedAnimationsPath, animationFilename, i, GIFnumFrames, GIFresolution, KIND.width,KIND.height);
        }
    }
    
//TODO: Did this not exist before?
    public enum PixelModes
    {
        ANIMATED_GIF,
        SCROLLING_TEXT,
        STILL_IMAGE
    }
    
    /**
     * When this task is executed, it sends an animated GIF to the the Pixel.
     */
    class SendGifAnimationTask extends TimerTask
    {
        @Override
        public void run()
        {
            String message = "Pixel is writing an animation to the hardware.";
            System.out.println(message);
            
            //let's loop through and send frame to PIXEL with no delay
            for(int y=0; y<GIFnumFrames; y++) 
            { 
                //Al removed the -1, make sure to test that!!!!!
                sendPixelDecodedFrame(decodedAnimationsPath, animationFilename, y, GIFnumFrames, GIFresolution, KIND.width,KIND.height);
            }

            message = "Pixel is done writing the animation, setting PIXEL to local playback mode.";
            System.out.println(message);
            playLocalMode();
            
            message = "Pixel is in local playback mode.";
            System.out.println(message);  

            //TODO UPDATE THE BROWSER/CLIENTS SOMEHOW
        }
    }
    
    private class TextScroller extends TimerTask
    {
        @Override
        public void run()
        {
	    int delay = 200;//scrollSpeedSlider.getValue();	
	    delay = 710 - delay;                            // al linke: added this so the higher slider value means faster scrolling
	    
//	    ChangeModeServlet.this.timer.setDelay(scrollDelay);
	    
            int w = 64;
            int h = 64;
	    
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            
            
//	    Color textColor = Color.GREEN;//colorPanel.getBackground();
	    
            Graphics2D g2d = img.createGraphics();
            g2d.setPaint(scrollingTextColor);
                      
//               Font tr = new Font("Arial", Font.PLAIN, scrollingTextFontSize_);
            String fontFamily = "Arial";
//            String fontFamily = fontNames[0];
            
            Font font = fonts.get(fontFamily);
            if(font == null)
            {
                font = new Font(fontFamily, Font.PLAIN, 32);
                fonts.put(fontFamily, font);
            }            
            
            g2d.setFont(font);
            
            FontMetrics fm = g2d.getFontMetrics();
            
            int y = fm.getHeight();            

            try 
            {
                additionalBackgroundDrawing(g2d);
            } 
            catch (Exception ex) 
            {
                logger.log(Level.SEVERE, null, ex);
            }
            
//            set intial value on scrollingText
            g2d.drawString(scrollingText, x, y);
            
            try 
            {
                additionalForegroundDrawing(g2d);
            } 
            catch (Exception ex) 
            {
                logger.log(Level.SEVERE, null, ex);
            }
            
            g2d.dispose();

//            System.out.print(".");

            if(matrix == null)
            {
                logger.log(Level.INFO, "There is no matrix for the text scrolller.");
            }
            else
            {
                try 
                {  
                    writeImagetoMatrix(img, KIND.width, KIND.height);
                } 
                catch (ConnectionLostException ex) 
                {
                    logger.log(Level.SEVERE, null, ex);
                }                
            }
                        
            int messageWidth = fm.stringWidth(scrollingText);            
            int resetX = 0 - messageWidth;
            
            if(x == resetX)
            {
                x = w;
            }
            else
            {
                x--;
            }
        }
    }
}
