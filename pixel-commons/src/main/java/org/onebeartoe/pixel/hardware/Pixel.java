
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
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
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
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.FilenameUtils;
import org.gifdecoder.GifDecoder;
import org.onebeartoe.pixel.LogMe;
import java.util.Queue; 
import java.util.LinkedList;
import javax.imageio.ImageIO;

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
    
    private int z;
    
    private Boolean timerRunningFlag = false;
        
    public static IOIO ioiO;
    
    public RgbLedMatrix matrix;

//TODO: rename this matrix_type    
    public final RgbLedMatrix.Matrix KIND;
    
    public static AnalogInput analogInput1;
    
    public static  AnalogInput analogInput2;
    
    protected byte[] BitmapBytes;
    
    protected byte[] BitmapBytesLast;
    
    protected byte[] BitmapBytesOverlay;
    
    protected InputStream BitmapInputStream;
    
    protected short[] frame_;
    
    private float fps;
    
    public String fileType;
    
    public String gifNameNoExt;
    
    private static String localFileImagePath;
    
    private static VersionType v;
    
    private String userHome;
    
    //private static String pixelHome = "/home/pi/pixelcade/";
    
    private static String pixelHome = System.getProperty("user.home") + File.separator + "pixelcade" + File.separator; //this means "location of pixelcade resources, art, etc"
  
    private String animationsPath = ""; 
    
    private String decodedAnimationsPath = "";
    
    private String imagesPath = "";
    
    private String decodedArcadePath;//to do look into
    
    private int currentResolution;
    
    ScheduledExecutorService streamPinballservice;
    
    ScheduledExecutorService streamGIFservice;
    
// Is this a dup of currentResolution?    
    private static int GIFresolution;
    
    /**
     * the path to the source gifs in the jar
     */
    private String gifSourcePath = "animations/gifsource/";
    
    private String gifFilePath="";
    //private String gifArcadeSourcePath = "arcade/";
    
    /**
     * This is for the animations.
     */
    private int GIFnumFrames;
    
    private volatile Timer timer;
    
    private String animationFilename;
    
    private PixelModes mode;

//TODO: rename for scrolling text
    private int x ; 
    
    private HashMap<String, Font> fonts;

    private String scrollingText;
    
    private String filetag;
    
    /**
     * This is length in milliseconds of the delay between each scrolling text redraw.
     */
    private long scrollDelay = 10; 
    
    private int scrollSmooth = 1; 
    
    private Color scrollingTextColor = Color.RED;
    
    private static int yScrollingTextOffset = 0;
    
    private Logger logger;
    
    public static LogMe logMe = null;
    
    private String pixelHardwareId = null;
    
    public static String OS = System.getProperty("os.name").toLowerCase();
    
     private static int framecount = 0;
  
    private static int fontSize = 32;
  
    private static int yOffset = 0;
  
    private static boolean doubleLine = false;
  
    private static boolean fourLine = false;

    private String maxMultiLine = null;

    private static String fontFamily = "Arial";

    private Font font;

    private Font customFont;
    
//    private int w = 128; 
//    
//    private int h = 32;       
//    
//    private BufferedImage textImg;
//    
//    private Graphics2D g2d;
//    
//    private Font font;
//    
//    private Font customFont;
//    
//    private GraphicsEnvironment ge;
//    
//     private int messageWidth;
//    
//    private int yText;
//    
//    private FontMetrics fm;
        
    private StreamGIFTask streamgifTask = new StreamGIFTask();
    private StreamPinballTask streampinballTask = new StreamPinballTask();
    private ScrollTextTask scollTextTask = new ScrollTextTask();
    private DrawAnalogClockTask clockTask = new DrawAnalogClockTask();
    private PNGLoopTask pngLoopTask = new PNGLoopTask();
    
    private  ScheduledFuture<?> future ;
    private  ScheduledFuture<?> futurescroll ;
    private  ScheduledFuture<?> futureclock ;
    private  ScheduledFuture<?> futureimage ;
    private  ScheduledFuture<?> futurepinball ;
    
    private final AtomicBoolean streamGIFTimerRunningFlag = new AtomicBoolean();
    private final AtomicBoolean scrollingTextTimerRunningFlag = new AtomicBoolean();
    private final AtomicBoolean clockTimerRunningFlag = new AtomicBoolean();
    private final AtomicBoolean PNGTimerRunningFlag = new AtomicBoolean();
    private final AtomicBoolean PinballAnimationRunningFlag = new AtomicBoolean();
    private final AtomicBoolean PinballAnimationInterrupt = new AtomicBoolean();
    private final AtomicBoolean PinballAnimationWasInterrupted = new AtomicBoolean();
    
    public Queue<String> PixelQueue = new LinkedList<>(); 
    
    private Boolean isLooping = false;   //we'll use this for the queue feature
    private int loopTimesGlobal = 0;
    private int loopScrollingTextCounter = 0;
    private int loopGIFCounter = 0;
    private int loopPNGCounter = 0;
    
    private int p = 0;
    private int scrollingTextMultiplier = 1;
    //private Boolean loop99999Flag = false;
    private Boolean loop99999FlagPNG = false;
    private Boolean loop99999FlagGIF = false;
    private Boolean loop99999FlagText = false;
    
    private static boolean isALU = System.getenv("PATH").contains("pixelcade/jre11/bin");
    
    
    private String lastGIFName = "";
    private ArrayList<String> GIFPlayed = new ArrayList<>();
    
    private String lastGIFDecodedPath = "";
    private ArrayList<String> GIFPlayedDecodedPath = new ArrayList<>();
    
    private int lastGIFLatestFrame = 0;
    private ArrayList<Integer> GIFLatestFrame = new ArrayList<>();
    
    private int lastGIFTotalFrames = 0;
    private ArrayList<Integer> GIFTotalFrames = new ArrayList<>();
    
    //private TimerTask animateTimer = new AnimateTimer();
    
    /**
     * @param KIND
     * @param resolution 
     */
    public Pixel(RgbLedMatrix.Matrix KIND, int resolution)
    {
        
        String name = getClass().getName();
        logger = Logger.getLogger(name);
        
        logMe = LogMe.getInstance();
        
        //PixelQueue = new LinkedList<>(); 
        
        /* String name = getClass().getName();
        logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        
        // define the logfile
        FileHandler fh = null;
        try {
            fh = new FileHandler("pixelcade.log");
        } catch (IOException ex) {
            Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
        }
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);
        */
        
        mode = PixelModes.STILL_IMAGE;
        
	this.KIND = KIND;
        
        this.currentResolution = resolution;
	
	BitmapBytes = new byte[KIND.width * KIND.height * 2]; //512 * 2 = 1024 or 1024 * 2 = 2048
        
        BitmapBytesLast = new byte[KIND.width * KIND.height * 2]; 
        
        BitmapBytesOverlay = new byte[KIND.width * KIND.height * 2]; 
	
	frame_ = new short[KIND.width * KIND.height];
        
        x = 0;
                
        fonts = new HashMap();
        
        scrollingText = "Scolling Text";
        
        if (isWindows()) {
            pixelHome = System.getProperty("user.dir") + File.separator;  //user dir is the folder where pixelweb.jar lives and would be placed there by the windows installer
            scrollingTextMultiplier = 3; //to do this may no longer be needed
        } else if (isALU){       
            pixelHome = "/opt/pixelcade/";
        }
        
        animationsPath = pixelHome + "animations" + File.separator;            
        decodedAnimationsPath = animationsPath + "decoded" + File.separator;
        imagesPath = pixelHome + "images" + File.separator;
        
//        if (isWindows()) {
//            pixelHome = System.getProperty("user.dir") + File.separator;  //user dir is the folder where pixelweb.jar lives and would be placed there by the windows installer
//            animationsPath = pixelHome + "animations\\";            
//            decodedAnimationsPath = animationsPath + "decoded\\";
//            imagesPath = pixelHome + "images\\";
//            scrollingTextMultiplier = 3; //to do this may no longer be needed
//        } else if (isALU){       
//            pixelHome = "/opt/pixelcade/";
//            animationsPath = pixelHome + "animations/";            
//            decodedAnimationsPath = animationsPath + "decoded/";
//            imagesPath = pixelHome + "images/";
//        }
    
        
//        try
//        {
//            
//            if (isWindows()) {
//                //pixelHome = userHome + "\\";
//                pixelHome = System.getProperty("user.dir") + "\\";  //user dir is the folder where pixelweb.jar lives and would be placed there by the windows installer
//                animationsPath = pixelHome + "animations\\";            
//                decodedAnimationsPath = animationsPath + "decoded\\";
//                imagesPath = pixelHome + "images\\";
//                scrollingTextMultiplier = 3; //to do this may no longer be needed
//            } 
//            else {
//                pixelHome = userHome + "/";                 
//                pixelHome = System.getProperty("user.home") + "/pixelcade/";  //let's force user.home since we don't have an installer for Pi or Mac
//                
////              this won't work with appimage
////                String path = Pixel.class.getProtectionDomain().getCodeSource().getLocation().getPath(); //get the path that pixelweb.jar is launched from 
////                String decodedPath = URLDecoder.decode(path, "UTF-8");
////                pixelHome = "/" + FilenameUtils.getPath(decodedPath) ;  //important won't work without the "/" in front
//                
//                animationsPath = pixelHome + "animations/";            
//                decodedAnimationsPath = animationsPath + "decoded/";
//                imagesPath = pixelHome + "images/";
//            }
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
    }

    /**
     * Read the input stream into a byte array
     * @param raw565ImagePath
     * @throws ConnectionLostException 
     */
    public void loadRGB565(String raw565ImagePath) throws ConnectionLostException 
    {
	BitmapInputStream = getClass().getClassLoader().getResourceAsStream(raw565ImagePath);

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
    
    public void playLocalModeCheck(int loop, Boolean pixelConnected, Boolean writeMode) {
         
        //pixel.playLocalModeCheck(loop_, localModeloop, WebEnabledPixel.pixelConnected);
        
        if (!pixelConnected) { //pixel not connected yet, should not be the case here actually
             
            if (loop == 0) loop = 1;
            //addtoQueue("localplayback",selectedPlatformName,selectedFileName,loop,writeMode,Color.red,1);
            addtoQueue("localplayback","","",loop, writeMode,Color.red,1);
            
        } else { //pixel is connected
        
        
             if (isLooping && loop == 99999 && !writeMode) {  //we were already looping and new command has a loop so let's write to the Q

                 //addtoQueue("gif",selectedPlatformName,selectedFileName,99999,writeMode,Color.red,1);
                 addtoQueue("localplayback","","",loop,writeMode,Color.red,1);
                 loop99999FlagGIF = true;
            } 
            
             
            else if (isLooping && loop != 0 && !writeMode) {  //we were already looping and new command has a loop and is not a write command so let's write to the Q

                 //addtoQueue("gif",selectedPlatformName,selectedFileName,loop,writeMode,Color.red,1); //but don't add to the Q if it's empty?
                  addtoQueue("localplayback","","",loop,writeMode,Color.red,1);
            } 
            
            else 

            {
                if (loop!=0 && !writeMode && loop != 99999) { //we just got a looping command but were not looping before so just start a new one 
                    isLooping = true;
                    loopGIFCounter = 0;
                    loopTimesGlobal = loop; 
                    //?
                    playLocalMode(); //and then let's play local mode
                }
                else {   //loop = 0 so we are not looping or we have a write command so let's reset everything and clear the Q
                    isLooping = false;
                    loopGIFCounter = 0;
                    loopTimesGlobal = 0;
                    PixelQueue.clear();
                    playLocalMode(); //and then let's play local mode
                }
            }
        }
    }
    
    
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

//    public String getPixelHome()
//    {
//        return pixelHome;
//    }
    
    public boolean getLoopStatus()
    {
        return isLooping;
    }
    
    //public String getArcadeHome() {
    //    return arcadePath;
    //}
    
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
    
//    public static String getHomePath() {
//        return pixelHome;
//    }
    
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
        logMe.aLogger.info("PIXEL LED panel resolution is: " + currentResolution);
	
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

public boolean GIFArcadeNeedsDecoding(String decodedDir, String gifName, int currentResolution, String gifFilePath) throws NoSuchAlgorithmException, IOException {
	
	/*In this method we will first check if the decoded files are there
	if they are present, then let's read them and make sure the resolution in the decoded file matches the current matrix
	if no match, then we need to re-encode
	if the files are not there, then we need to re-encode anyway*/
	
	/*GIFName will be tree
	GIF Path will be c:\animations\tree.gif
	decdoed path will be c:\animations\tree.gif\decoded\tree.rgb565 and tree.txt
	*/
	MessageDigest md = MessageDigest.getInstance("MD5");
        String SelectedFileMD5_ = checksum(gifFilePath, md);
    
	gifName = FilenameUtils.removeExtension(gifName); //with no extension
	
	//System.out.println("PIXEL LED panel resolution is: " + currentResolution);
	
	//String decodedGIFPathTXT = currentDir + "/decoded/" + gifName + ".txt";
	//String decodedGIFPath565 = currentDir + "/decoded/" + gifName + ".rgb565";
	
	String decodedGIFPathTXT = decodedDir + gifName + ".txt";
	String decodedGIFPath565 = decodedDir + gifName + ".rgb565";
	
	File filetxt = new File(decodedGIFPathTXT);
	File file565 = new File(decodedGIFPath565);
	
	if (filetxt.exists() && file565.exists()) { //need to ensure both files are there
		   
			if ((getDecodedresolution(decodedDir, gifName) == currentResolution) && (getDecodedmd5(decodedDir,gifName).equals(SelectedFileMD5_))) { 
				
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

private static String checksum(String filepath, MessageDigest md) throws IOException {

        // DigestInputStream is better, but you also can hash file like this.
        try (InputStream fis = new FileInputStream(filepath)) {
            byte[] buffer = new byte[1024];
            int nread;
            while ((nread = fis.read(buffer)) != -1) {
                md.update(buffer, 0, nread);
            }
        }

        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();

    }

 public String getDecodedmd5(String decodedDir, String gifName) {  //returns md5 of the target gif
 	
	    String gifDecodedTxtPath = decodedDir + gifName + ".txt"; 
	    
	    File filemeta = new File(gifDecodedTxtPath);
 	
 	FileInputStream decodedFile = null; //fix this
 	try {
			decodedFile = new FileInputStream(gifDecodedTxtPath);
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
		    String fdelim = "[,]"; //now parse this string considering the comma split  ie, 32,60,32, 241234123412341234  
		    String[] fileAttribs2 = fileAttribs.split(fdelim);	
		    
		    //since we added the MD5 check later, we need to check that the array has the MD5 in there and if not, we'll put in a dummy value
		    
		    String selectedFileMD5String = "999999999999";
		    
		    if (fileAttribs2.length > 3) {
		    	  selectedFileMD5String = fileAttribs2[3].trim(); 
		    }
		    
		    //System.out.println("Get Decoded MD5 is: " + selectedFileMD5String);
		    return (selectedFileMD5String);
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
    	String gifNamePath = decodedDir + gifName + ".txt";  //arcade/mame/decoded/pacman.txt
       
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
    	
    	//System.out.println("selected file name: " + selectedFileName);
    	int i = selectedFileName.lastIndexOf(".");
    	selectedFileName = selectedFileName.substring(0, i);
    	//System.out.println("corrected file name: " + selectedFileName);
    	
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
    
    /*
    private void runRepeatingTask(TimerTask drawTask, long repeatPeriod)  //**** NO LONGER USED ******
    {
        stopExistingTimer();

        if(timer == null)
        {
            timer = new Timer();
        }

        Date firstTime = new Date();

        timer.schedule(drawTask, firstTime, repeatPeriod);
        
    }
    */
    
    public void GetPixelDecodedFrameLast(String decodedDir, String gifName, int x, int selectedFileTotalFrames, int selectedFileResolution, int frameWidth, int frameHeight) 
    {
//        BitmapBytesLast = new byte[frameWidth * frameHeight * 2]; //512 * 2 = 1024 or 1024 * 2 = 2048
//        frame_ = new short[frameWidth * frameHeight];
        
        Arrays.fill(BitmapBytesLast, (byte)0); 
		
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
                                case 16:
                                    frame_length = 1024;
                                    break;
                                case 32:
                                    frame_length = 2048;
                                    break;
                                case 6416:
                                    frame_length = 2048;
                                    break;
                                case 12816:
                                    frame_length = 4096;
                                    break;
                                case 25616:
                                    frame_length = 8192;
                                    break;
                                case 64:
                                    frame_length = 4096;
                                    break;
                                case 64999:
                                    frame_length = 4096; //had to add unique ones (999) for mirror to force re-encoding when led panel is switched
                                    break;
                                case 128:
                                    frame_length = 8192;
                                    break;
                                case 12832:
                                    frame_length = 8192;
                                    break;
                                case 128999:
                                    frame_length = 8192; //had to add unique ones (999) for mirror to force re-encoding when led panel is switched
                                    break;
                                default:
                                    frame_length = 2048;
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
   			//BitmapBytesLast = new byte[(int)frame_length];
                        
                        //if merge, then create a new frame that is a merge of previous gif

   			// Read in the bytes
   			int offset = 0;
   			int numRead = 0;
   			try {
				while (offset < BitmapBytesLast.length && (numRead=raf.read(BitmapBytesLast, offset, BitmapBytesLast.length-offset)) >= 0) {
				    offset += numRead;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
   			 
   			// Ensure all the bytes have been read in
   			if (offset < BitmapBytesLast.length) {
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
        }
        
    }
    
    public void sendPixelDecodedFramePinball(String decodedDir, String gifName, int x, int selectedFileTotalFrames, int selectedFileResolution, int frameWidth, int frameHeight) 
    {
      
        //TO DO need to make sure this is no bigger than 3 or so
        GIFLatestFrame.add(x);
        if (GIFLatestFrame.size()>1) {
            lastGIFLatestFrame = GIFLatestFrame.get(GIFLatestFrame.size()-2);
            //System.out.println("Last Frame index: " + lastGIFLatestFrame);
        }
        
        GIFTotalFrames.add(selectedFileTotalFrames);
        if (GIFTotalFrames.size()>1) {
            lastGIFTotalFrames = GIFTotalFrames.get(GIFTotalFrames.size()-2);
            //System.out.println("Last GIF Total Frames: " + lastGIFTotalFrames);
        }
        
        //made a change to not create new arrays and re-use existing global one so we need to clear the array here before we use it
        Arrays.fill(BitmapBytes, (byte)0); 
        Arrays.fill(frame_, (byte)0); 
        //BitmapBytes = new byte[frameWidth * frameHeight * 2]; //512 * 2 = 1024 or 1024 * 2 = 2048
        //frame_ = new short[frameWidth * frameHeight];
		
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
                                case 16:
                                    frame_length = 1024;
                                    break;
                                case 32:
                                    frame_length = 2048;
                                    break;
                                case 6416:
                                    frame_length = 2048;
                                    break;
                                case 12816:
                                    frame_length = 4096;
                                    break;
                                case 25616:
                                    frame_length = 8192;
                                    break;
                                case 64:
                                    frame_length = 4096;
                                    break;
                                case 64999:
                                    frame_length = 4096; //had to add unique ones (999) for mirror to force re-encoding when led panel is switched
                                    break;
                                case 128:
                                    frame_length = 8192;
                                    break;
                                case 12832:
                                    frame_length = 8192;
                                    break;
                                case 128999:
                                    frame_length = 8192; //had to add unique ones (999) for mirror to force re-encoding when led panel is switched
                                    break;
                                default:
                                    frame_length = 2048;
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
   			//BitmapBytes = new byte[(int)frame_length];
                        
                        //if merge, then create a new frame that is a merge of previous gif

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
                 
                      //boolean removeme = true; //test code to disable overlay
                              
                      //if (removeme == false && PinballAnimationWasInterrupted.get() == true && GIFLatestFrame.size()>1 ) {   //if there is still a pinball animation running, then overaly
                      if (PinballAnimationWasInterrupted.get() == true && GIFLatestFrame.size()>1 ) {   //if there is still a pinball animation running, then overaly
                      
                        
                        //since the pinball gifs are one shot, we should only play the remaining frames left from the last one
                        
                        
                        GetPixelDecodedFrameLast(pixelHome + lastGIFDecodedPath + "/decoded/", lastGIFName, lastGIFLatestFrame, lastGIFTotalFrames, 12832, 128, 32); 
                               
                        for (int i = 0; i < BitmapBytes.length; i++) {
                            byte transparent = 0x00000000;
                            int output = Byte.compare(BitmapBytes[i], transparent);
                                
                            if (output == 0) {                                 //if the new byte is transparent, then take the last byte
                                BitmapBytesOverlay[i] =BitmapBytesLast[i];
                            }
                            else {                                          //if the new byte is not transparent, then take the new byte
                                BitmapBytesOverlay[i] = BitmapBytes[i];
                            }
                        }
//                   
                        int y = 0;
                        
                        for (int i = 0; i < frame_.length; i++) 
                        {
                                frame_[i] = (short) (((short) BitmapBytesOverlay[y] & 0xFF) | (((short) BitmapBytesOverlay[y + 1] & 0xFF) << 8));
                                y = y + 2;
                        }
                        
                    }
                    else {
                        int y = 0;
                        for (int i = 0; i < frame_.length; i++) 
                        {
                            frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
                            y = y + 2;
                        }
                    }
     		
                    try 
                    {
                            matrix.frame(frame_);  //send the frame to the LED matrix

                    } 
                    catch (ConnectionLostException e) 
                    {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                    }
			
        }
        else 
        {
            System.out.println("Decoded file does not exist");
        }
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
                                case 16:
                                    frame_length = 1024;
                                    break;
                                case 32:
                                    frame_length = 2048;
                                    break;
                                case 6416:
                                    frame_length = 2048;
                                    break;
                                case 12816:
                                    frame_length = 4096;
                                    break;
                                case 25616:
                                    frame_length = 8192;
                                    break;
                                case 64:
                                    frame_length = 4096;
                                    break;
                                case 64999:
                                    frame_length = 4096; //had to add unique ones (999) for mirror to force re-encoding when led panel is switched
                                    break;
                                case 128:
                                    frame_length = 8192;
                                    break;
                                case 12832:
                                    frame_length = 8192;
                                    break;
                                case 128999:
                                    frame_length = 8192; //had to add unique ones (999) for mirror to force re-encoding when led panel is switched
                                    break;
                                default:
                                    frame_length = 2048;
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
                        
                        //if merge, then create a new frame that is a merge of previous gif

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
		   		matrix.frame(frame_);  //send the frame to the LED matrix
				
			} 
                        catch (ConnectionLostException e) 
                        {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
        }
        else 
        {
            System.out.println("Decoded file does not exist");
        }
    }
    
 
  
   
//    public void scrollText(String text, int loop, long speed, Color color, boolean pixelConnected,int scrollsmooth)
//    {
//        
//        //System.out.println("Scrolling Text Loop Flag " + isLooping);
//        
//        if (!pixelConnected) {  //if pixel is still starting up and we get a command, let's add to Q
//             
//           if (loop == 0) {
//             loop = 1;
//           }
//           
//           text = text.replaceAll(";"," "); 
//           addtoQueue("text", text, Long.toString(speed), loop, false, color,scrollsmooth);
//            
//     } 
//     
//     else { 
//        
//             if (isLooping && loop == 99999) {  //we were already looping and new command has a loop and is not a write command so let's write to the Q
//            
//                 text = text.replaceAll(";"," "); 
//                 addtoQueue("text",text,Long.toString(speed),loop,false,color,scrollsmooth);
//                 loop99999FlagText = true;
//             }
//         
//             else if (isLooping && loop != 0) {  //we were already looping and new command has a loop so let's write to the Q
//                
//                 loopScrollingTextCounter = 0;
//                 loopTimesGlobal = loop; 
//                 text = text.replaceAll(";"," "); 
//                 addtoQueue("text",text,Long.toString(speed),loop,false,color,scrollsmooth);
//
//            } 
//        
//            else  {
//            
//            scrollDelay = speed;
//            
//            scrollingTextMultiplier = scrollsmooth;
//            
//            scrollingText = text;
//          
//            setScrollTextColor(color);
//        
//            if (loop !=0 && loop != 99999) {   //if it's 99999, then we're actually no longer looping   //we were not already looping but new command has a loop and is not a write so let's continue and loop the gif
//                x = KIND.width;
//                isLooping = true;
//                loopScrollingTextCounter = 0;
//                loopTimesGlobal = loop; 
//                //System.out.println("looping for first time:" + loop);
//            } else {                                            //we are not looping or we have a write command so let's reset everything and clear the Q
//                x = KIND.width;
//                isLooping = false;
//                loopScrollingTextCounter = 0;
//                loopTimesGlobal = 0;
//                PixelQueue.clear();
//                //System.out.println("NOT looping");
//            }
//
//            stopExistingTimer();
//            
//            interactiveMode();
//            
//            //***** below here is all new in case something goes wrong remove this
//            int w = KIND.width;
//            int h = KIND.height;
//            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//            	    
//            Graphics2D g2d = img.createGraphics();
//            g2d.setPaint(scrollingTextColor);
//       
//            Font font = fonts.get(fontFamily);
//            font = new Font(fontFamily, Font.PLAIN, fontSize);
//            
//            if (!fontFamily.equals("Arial")) {
//                try {
//                        Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts//" + fontFamily + ".ttf"));
//                           GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//                           ge.registerFont(customFont);
//
//                       } catch (IOException e) {
//                           System.out.println(fontFamily + " not fount, defaulting to Arial");
//                           font = new Font("Arial", Font.PLAIN, KIND.height);
//                           fonts.put(fontFamily, font);
//
//                       } catch(FontFormatException e) {
//                           System.out.println(fontFamily + " not fount, defaulting to Arial");
//                           font = new Font("Arial", Font.PLAIN, KIND.height);
//                           fonts.put(fontFamily, font);
//                       }
//            }
//            
//            if(font == null)  //then we have a custom font most likely
//                { //default font size is 32 but we also set it from WebEnabledPixel
//                font = new Font("Arial", Font.PLAIN, KIND.height);
//                fonts.put(fontFamily, font);
//            } 
//            
//            g2d.setFont(font);
//            FontMetrics fm = g2d.getFontMetrics();
//            
//            int yDifference = KIND.height - fm.getHeight() *  72 / Toolkit.getDefaultToolkit().getScreenResolution();
//           
//            int y = (fm.getHeight() *  72 / Toolkit.getDefaultToolkit().getScreenResolution()) + yOffset;
//
//            try 
//            {
//                additionalBackgroundDrawing(g2d);
//            } 
//            catch (Exception ex) 
//            {
//                logger.log(Level.SEVERE, null, ex);
//            }
//            
//            if (doubleLine == true) {
//                String[] words = scrollingText.split(" +"); // Split words by spaces
//                int count = (int) ((words.length / 2.0) + 0.5); // Number of words in part[0]
//                String[] part = new String[2];
//                Arrays.fill(part, ""); // Initialize to empty strings
//                for (int i = 0; i < words.length; i++) {
//                    if (i < count) { // First half of the words go into part[0]
//                        part[0] += words[i] + " ";
//                    } else { // Next half go into part[1]
//                        part[1] += words[i] + " ";
//                    }
//                }
//                part[1] = part[1].trim(); // Since there will be extra space at end of part[1]
//                g2d.drawString(part[0] + "\n", 0, y);
//                g2d.drawString(part[1] + "\n", 0, y + 12);
//                if (part[0].length() > part[1].length())   //we need to get the longest length for the scrolling end
//                    maxMultiLine = part[0];
//                else 
//                    maxMultiLine = part[1];
//            }
//            
//            else if (fourLine == true) {
//                
//                fontFamily = "Minimal4";  //since we have 4 lines, we need to hard code the specific font here or it won't fit
//                fontSize = 14;
//                font = fonts.get(fontFamily);
//                font = new Font(fontFamily, Font.PLAIN, fontSize);
//
//                    try {
//                            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts//" + fontFamily + ".ttf"));
//                               GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//                               ge.registerFont(customFont);
//                           } catch (IOException e) {
//                               System.out.println(fontFamily + " not fount, defaulting to Arial");
//                               font = new Font("Arial", Font.PLAIN, KIND.height);
//                               fonts.put(fontFamily, font);
//                           } catch(FontFormatException e) {
//                               System.out.println(fontFamily + " not fount, defaulting to Arial");
//                               font = new Font("Arial", Font.PLAIN, KIND.height);
//                               fonts.put(fontFamily, font);
//                           }
//                g2d.setFont(font);
//                fm = g2d.getFontMetrics();
//                
//                String[] words = scrollingText.split(" +"); // Split words by spaces
//                int quarter = (int) ((words.length / 4.0) + 0.5); // Number of words in part[0]
//                String[] part = new String[4];
//                Arrays.fill(part, ""); // Initialize to empty strings
//                for (int i = 0; i < words.length; i++) {
//                    
//                    if (i < quarter)  // First quarter of the words go into part[0]
//                        part[0] += words[i] + " ";
//                    else if (i >= quarter && i < quarter*2)  // Next quarter go into part[1]
//                        part[1] += words[i] + " ";
//                    else if (i >= quarter*2 && i < quarter*3)  // third quarter go into part[2]
//                        part[2] += words[i] + " ";
//                    else 
//                        part[3] += words[i] + " ";
//                }
//                part[1] = part[1].trim(); // Since there will be extra space at end of part[1]
//                g2d.drawString(part[0] + "\n", 0, y);
//                g2d.drawString(part[1] + "\n", 0, y + 6);
//                g2d.drawString(part[2] + "\n", 0, y + 12);
//                g2d.drawString(part[3] + "\n", 0, y + 18);
//                
//                int arr[] = {part[0].length(), part[1].length(), part[2].length(), part[3].length()}; 
//                int i; 
//                // Initialize maximum element 
//                int max = arr[0]; 
//                int maxElement = 0;
//                // Traverse array elements from second and 
//                // compare every element with current max   
//                for (i = 1; i < arr.length; i++) 
//                    if (arr[i] > max) 
//                        maxElement = i;
//                maxMultiLine = part[maxElement];
//            }
//            else {
//                 g2d.drawString(scrollingText, 0, y);
//            }
//             
//            try 
//            {
//                additionalForegroundDrawing(g2d);
//            } 
//            catch (Exception ex) 
//            {
//                logger.log(Level.SEVERE, null, ex);
//            }
//            
//            g2d.dispose();
//
//            // uncomment this to see how often the pixel is communicated with the host            
//            // System.out.print(".");
//
//            int messageWidth = fm.stringWidth(scrollingText);    
//            // System.out.println("message width: " + messageWidth);
//            
//            if (doubleLine == true || fourLine == true) 
//                 messageWidth = fm.stringWidth(maxMultiLine);  
//            
//            if (messageWidth < KIND.width) { //then it means we don't need to scroll
//            
//                if(matrix == null)
//                {
//                    // uncomment this for debugging
//                    logger.log(Level.INFO, "There is no matrix for the text scrolller.");
//                }
//                else
//                {
//
//                    //if (scrollingTextTimerRunningFlag.get() == true) {  //there was a timing issue on windows only so added this, if the timerflag has been set to false, then don't write this frame
//                        try 
//                        {  
//                            writeImagetoMatrix(img, KIND.width, KIND.height);
//                        } 
//                        catch (ConnectionLostException ex) 
//                        {
//                            logger.log(Level.SEVERE, null, ex);
//                        }   
//                    //}
//                }
//                        
//            }
//            
//            else {
//                
//            //***** above this is all new ********    
//                    
//                    ScheduledExecutorService scrollTextService = Executors.newScheduledThreadPool(1);
//                    futurescroll = scrollTextService.scheduleAtFixedRate(scollTextTask, 0, scrollDelay, TimeUnit.MILLISECONDS);
//                    scrollingTextTimerRunningFlag.set(true);  //atomic boolean , better for threads
//            }
//            
//            
//        }
//      }
//    }
    
     public void scrollText(String text, int loop, long speed, Color color, boolean pixelConnected, int scrollsmooth) {
    if (!pixelConnected) {
      if (loop == 0)
        loop = 1; 
      text = text.replaceAll(";", " ");
      addtoQueue("text", text, Long.toString(speed), loop, Boolean.valueOf(false), color, scrollsmooth);
    } else if (this.isLooping.booleanValue() && loop == 99999) {
      text = text.replaceAll(";", " ");
      addtoQueue("text", text, Long.toString(speed), loop, Boolean.valueOf(false), color, scrollsmooth);
      this.loop99999FlagText = Boolean.valueOf(true);
    } else if (this.isLooping.booleanValue() && loop != 0) {
      this.loopScrollingTextCounter = 0;
      this.loopTimesGlobal = loop;
      text = text.replaceAll(";", " ");
      addtoQueue("text", text, Long.toString(speed), loop, Boolean.valueOf(false), color, scrollsmooth);
    } else {
      this.scrollDelay = speed;
      this.scrollingTextMultiplier = scrollsmooth;
      this.scrollingText = text;
      setScrollTextColor(color);
      if (loop != 0 && loop != 99999) {
        this.x = this.KIND.width;
        this.isLooping = Boolean.valueOf(true);
        this.loopScrollingTextCounter = 0;
        this.loopTimesGlobal = loop;
      } else {
        this.x = this.KIND.width;
        this.isLooping = Boolean.valueOf(false);
        this.loopScrollingTextCounter = 0;
        this.loopTimesGlobal = 0;
        this.PixelQueue.clear();
      } 
      stopExistingTimer();
      interactiveMode();
      int w = this.KIND.width;
      int h = this.KIND.height;
      BufferedImage img = new BufferedImage(w, h, 2);
      Graphics2D g2d = img.createGraphics();
      g2d.setPaint(this.scrollingTextColor);
      if (doubleLine) {
        fontFamily = "Arial Narrow 7";
        fontSize = 14;
      } 
      if (fourLine) {
        fontFamily = "Minimal4";
        fontSize = 14;
      } 
      boolean fontExists = false;
      fontExists = Arrays.<String>asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).contains(fontFamily);
      if (fontExists) {
        this.font = this.fonts.get(fontFamily);
        this.font = new Font(fontFamily, 0, fontSize);
      } else {
        System.out.println(fontFamily + " is not registered on this OS, let's register it now...");
        this.logger.log(Level.INFO, fontFamily + " is not registered on this OS, let's register it now...");
        try {
          this.customFont = Font.createFont(0, new File("fonts//" + fontFamily + ".ttf"));
          GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
          ge.registerFont(this.customFont);
          this.font = this.fonts.get(fontFamily);
          this.font = new Font(fontFamily, 0, fontSize);
        } catch (IOException e) {
          System.out.println(fontFamily + " not found, please copy your TTF font into the fonts folder, defaulting to Arial Narrow 7");
          this.logger.log(Level.INFO, fontFamily + " not found, please copy your TTF font into the fonts folder, defaulting to Arial Narrow 7");
          this.font = new Font("Arial Narrow 7", 0, this.KIND.height - 4);
          this.fonts.put(fontFamily, this.font);
        } catch (FontFormatException e) {
          System.out.println(fontFamily + " not found, please copy your TTF font into the fonts folder, defaulting to Arial Narrow 7");
          this.logger.log(Level.INFO, fontFamily + " not found, please copy your TTF font into the fonts folder, defaulting to Arial Narrow 7");
          this.font = new Font("Arial Narrow 7", 0, this.KIND.height - 4);
          this.fonts.put(fontFamily, this.font);
        } 
      } 
      if (this.font == null) {
        this.font = new Font("Arial", 0, this.KIND.height - 4);
        this.fonts.put(fontFamily, this.font);
      } 
      g2d.setFont(this.font);
      FontMetrics fm = g2d.getFontMetrics();
      int yDifference = this.KIND.height - fm.getHeight() * 72 / 110;
      int y = yDifference / 2 + fm.getHeight() * 72 / 110 + yOffset;
      try {
        additionalBackgroundDrawing(g2d);
      } catch (Exception ex) {
        this.logger.log(Level.SEVERE, (String)null, ex);
      } 
      if (doubleLine == true) {
        int doublelineoffset = -7;
        String[] words = this.scrollingText.split(" +");
        int count = (int)(words.length / 2.0D + 0.5D);
        String[] part = new String[2];
        Arrays.fill((Object[])part, "");
        for (int i = 0; i < words.length; i++) {
          if (i < count) {
            part[0] = part[0] + words[i] + " ";
          } else {
            part[1] = part[1] + words[i] + " ";
          } 
        } 
        part[1] = part[1].trim();
        g2d.drawString(part[0] + "\n", 0, y + doublelineoffset);
        g2d.drawString(part[1] + "\n", 0, y + 14 + doublelineoffset);
        if (part[0].length() > part[1].length()) {
          this.maxMultiLine = part[0];
        } else {
          this.maxMultiLine = part[1];
        } 
      } else if (fourLine == true) {
        int FourLineOffset = -11;
        String[] words = this.scrollingText.split(" +");
        int quarter = (int)(words.length / 4.0D + 0.5D);
        String[] part = new String[4];
        Arrays.fill((Object[])part, "");
        for (int i = 0; i < words.length; i++) {
          if (i < quarter) {
            part[0] = part[0] + words[i] + " ";
          } else if (i >= quarter && i < quarter * 2) {
            part[1] = part[1] + words[i] + " ";
          } else if (i >= quarter * 2 && i < quarter * 3) {
            part[2] = part[2] + words[i] + " ";
          } else {
            part[3] = part[3] + words[i] + " ";
          } 
        } 
        part[1] = part[1].trim();
        g2d.drawString(part[0] + "\n", 0, y + FourLineOffset);
        g2d.drawString(part[1] + "\n", 0, y + 6 + FourLineOffset);
        g2d.drawString(part[2] + "\n", 0, y + 12 + FourLineOffset);
        g2d.drawString(part[3] + "\n", 0, y + 18 + FourLineOffset);
        int[] arr = { part[0].length(), part[1].length(), part[2].length(), part[3].length() };
        int max = arr[0];
        int maxElement = 0;
        for (int j = 1; j < arr.length; j++) {
          if (arr[j] > max)
            maxElement = j; 
        } 
        this.maxMultiLine = part[maxElement];
      } else {
        g2d.drawString(this.scrollingText, 0, y);
      } 
      try {
        additionalForegroundDrawing(g2d);
      } catch (Exception ex) {
        this.logger.log(Level.SEVERE, (String)null, ex);
      } 
      g2d.dispose();
      int messageWidth = fm.stringWidth(this.scrollingText);
      if (doubleLine == true || fourLine == true)
        messageWidth = fm.stringWidth(this.maxMultiLine); 
      if (messageWidth < this.KIND.width) {
        if (this.matrix == null) {
          this.logger.log(Level.INFO, "There is no matrix for the text scrolller.");
        } else {
          try {
            writeImagetoMatrix(img, this.KIND.width, this.KIND.height);
          } catch (ConnectionLostException ex) {
            this.logger.log(Level.SEVERE, (String)null, (Throwable)ex);
          } 
          if (this.isLooping.booleanValue()) {
            ScheduledExecutorService loopPNGservice = Executors.newScheduledThreadPool(1);
            this.futureimage = loopPNGservice.scheduleAtFixedRate(this.pngLoopTask, 0L, 1L, TimeUnit.SECONDS);
            this.PNGTimerRunningFlag.set(true);
          } 
        } 
      } else {
        ScheduledExecutorService scrollTextService = Executors.newScheduledThreadPool(1);
        this.futurescroll = scrollTextService.scheduleAtFixedRate(this.scollTextTask, 0L, this.scrollDelay, TimeUnit.MILLISECONDS);
        this.scrollingTextTimerRunningFlag.set(true);
      } 
    } 
  }
    
    public void setDecodedAnimationsPath(String decodedAnimationsPath)
    {
        this.decodedAnimationsPath = decodedAnimationsPath;
    }
    
    @Deprecated
    /**
     * @deprecated
     * where is this even used?
     */
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
            //stopExistingTimer("modechange",null,null,null,false,null);
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
		
		//System.out.println("User selected file name: " + selectedFileName);
		//System.out.println("User selected file type: " + fileType);
		//System.out.println("User selected file name no extension: " + gifNameNoExt);
		
		
		//String gifNamePath = currentDir + "/" + gifName + ".gif";  //   ex. c:\animation\tree.gif
		//System.out.println("User selected file name path: " + gifFilePath);
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
                  //logMe.aLogger.info("frame count: " + numFrames);
                  //logMe.aLogger.info("frame delay: " + frameDelay);
                  //logMe.aLogger.info("frame height: " + frameHeight);
                  //logMe.aLogger.info("frame width: " + frameWidth);
	          	          
	          for (int i = 0; i < numFrames; i++) { //loop through all the frames
	             BufferedImage rotatedFrame = d.getFrame(i);  
	            // rotatedFrame = Scalr.rotate(rotatedFrame, Scalr.Rotation.CW_90, null); //fixed bug, no longer need to rotate the image
	            // rotatedFrame = Scalr.rotate(rotatedFrame, Scalr.Rotation.FLIP_HORZ, null); //fixed bug, no longer need to flip the image
	             
	             // These worked too but using the scalr library gives quicker results
	             //rotatedFrame = getFlippedImage(rotatedFrame); //quick hack, for some reason the code below i think is flipping the image so we have to flip it here as a hack
	             //rotatedFrame = rotate90ToLeft(rotatedFrame);  //quick hack, same as above, have to rotate
	              
	    		 if (frameWidth != pixelMatrix_width || frameHeight != pixelMatrix_height) {
	    			 System.out.println("Resizing and encoding " + selectedFileName + " frame " + i);
                                  logMe.aLogger.info("Resizing and encoding " + selectedFileName + " frame " + i);
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
                                 logMe.aLogger.info("Encoding " + selectedFileName + " frame " + i);
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
		   		
		   		filetag = String.valueOf(numFrames) + "," + String.valueOf(frameDelay) + "," + String.valueOf(currentResolution); //current resolution may need to change to led panel type
		   				
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
                                        logMe.aLogger.severe("ERROR, could not write " + selectedFileName);
					e.printStackTrace();
				}
		}
		else {
			System.out.println("ERROR  Could not find file " + gifFilePath);
                        logMe.aLogger.severe("Could not find file " + gifFilePath);
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
                                 //logMe.aLogger.info("Resizing and encoding " + gifName + ".gif" + " frame " + i);
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
                                 //logMe.aLogger.info("DO NOT INTERRUPT: Encoding " + gifName + ".gif" + " frame " + i);
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
		   		
		   		filetag = String.valueOf(numFrames) + "," + String.valueOf(frameDelay) + "," + String.valueOf(currentResolution); //current resolution may need to change to led panel type
		   				
	     		   File myFile = new File(decodedDir + gifName + ".txt");  				       
	     		   try 
                           {
					myFile.createNewFile();
					FileOutputStream fOut = null;
					fOut = new FileOutputStream(myFile);
			        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
					myOutWriter.append(filetag); 
					myOutWriter.close();
					fOut.close();	
				
                           } 
                           catch (IOException e) 
                           {
					// TODO Auto-generated catch block
					System.out.println("ERROR, could not write " + gifName);
                                        //logMe.aLogger.severe("Could not write " + gifName);
					e.printStackTrace();
                           }
		}
		else 
                {
			System.out.println("ERROR  Could not find " + gifSourcePath + gifName + ".gif in the JAR file");
                        //logMe.aLogger.severe("Could not write " + gifName);
                }
    }
    
    public void decodeArcadeGIF(final String decodedDir, String gifFilePath, String gifName, int currentResolution, final int pixelMatrix_width, final int pixelMatrix_height) throws NoSuchAlgorithmException, IOException 
    {  
		//we should add another flag here if we're decoding from the jar or user supplied gif
		
		//we're going to decode a native GIF into our RGB565 format
	    //we'll need to know the resolution of the currently selected matrix type: 16x32, 32x32, 32x64, or 64x64
		//and then we will receive the gif accordingly as we decode
		//we also need to get the original width and vuHeight of the gif which is easily done from the gif decoder class
		//String gifName = FilenameUtils.removeExtension(gifName); //with no extension
		
       
                String selectedFileName = FilenameUtils.getName(gifFilePath); 
                fileType = FilenameUtils.getExtension(gifFilePath);
                gifNameNoExt = FilenameUtils.removeExtension(selectedFileName); //with no extension
		
		System.out.println("Arcade file name: " + selectedFileName);
		System.out.println("Arcade file name path: " + gifFilePath);
                logMe.aLogger.info("Arcade file name: " + selectedFileName);
                logMe.aLogger.info("Arcade file name path: " + gifFilePath);
               
		File file = new File(gifFilePath);
		if (file.exists()) {
                    
                           MessageDigest md = MessageDigest.getInstance("MD5");
                           String md5 = checksum(gifFilePath, md);
                            //since we are decoding, we need to first make sure the .rgb565 and .txt decoded file is not there and delete if so.
			  String gifName565Path =  decodedDir + gifNameNoExt + ".rgb565";  //   ex. c:\animation\decoded\tree.rgb565
			  String gifNameTXTPath = decodedDir + gifNameNoExt + ".txt";  //   ex. c:\animation\decoded\tree.txt
			 
                          
			  //since we are decoding, we need to first make sure the .rgb565 and .txt decoded file is not there and delete if so.
			  
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
                  logMe.aLogger.info("frame count: " + numFrames);
                  logMe.aLogger.info("frame delay: " + frameDelay);
                  logMe.aLogger.info("frame height: " + frameHeight);
                  logMe.aLogger.info("frame width: " + frameWidth);
	          	          
	          if (numFrames == 1) {  //ok this is a hack, for some reason only on raspberry pi, single frame gifs are not writing so the work around is to write 2 frames for a single frame GIF
                           for (int i = 0; i < 2; i++) { 
                           BufferedImage rotatedFrame = d.getFrame(0);  
                               if (frameWidth != pixelMatrix_width || frameHeight != pixelMatrix_height) {
                                       System.out.println("Resizing and encoding " + selectedFileName + " frame " + i);
                                       logMe.aLogger.info("Resizing and encoding " + selectedFileName + " frame " + i);

                                       try {
                                                      rotatedFrame = getScaledImage(rotatedFrame, pixelMatrix_width,pixelMatrix_height);
                                                     
                                              } catch (IOException e) {
                                                      // TODO Auto-generated catch block
                                                      e.printStackTrace();
                                              }
                               }
                               else {
                                       System.out.println("Encoding " + selectedFileName + " frame " + i);
                                       logMe.aLogger.info("Encoding " + selectedFileName + " frame " + i);
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
                                           if(decodeddir.exists() == false) {
                                                    decodeddir.mkdirs();
                                            }

                                            try {

                                                    appendWrite(BitmapBytes, decodedDir + gifNameNoExt + ".rgb565"); //this writes one big file instead of individual ones


                                            } catch (IOException e1) {
                                                    // TODO Auto-generated catch block
                                                    //Log.e("PixelAnimate", "Had a problem writing the original unified animation rgb565 file");
                                                    e1.printStackTrace();
                                            }
                        }
                  }        
                   
                   else {  // we don't have a single frame gif
                  
                        for (int i = 0; i < numFrames; i++) { //loop through all the frames
                           BufferedImage rotatedFrame = d.getFrame(i);  
                          // rotatedFrame = Scalr.rotate(rotatedFrame, Scalr.Rotation.CW_90, null); //fixed bug, no longer need to rotate the image
                          // rotatedFrame = Scalr.rotate(rotatedFrame, Scalr.Rotation.FLIP_HORZ, null); //fixed bug, no longer need to flip the image

                           // These worked too but using the scalr library gives quicker results
                           //rotatedFrame = getFlippedImage(rotatedFrame); //quick hack, for some reason the code below i think is flipping the image so we have to flip it here as a hack
                           //rotatedFrame = rotate90ToLeft(rotatedFrame);  //quick hack, same as above, have to rotate

                               if (frameWidth != pixelMatrix_width || frameHeight != pixelMatrix_height) {
                                       System.out.println("Resizing and encoding " + selectedFileName + " frame " + i);
                                       logMe.aLogger.info("Resizing and encoding " + selectedFileName + " frame " + i);
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
                                       logMe.aLogger.info("Encoding " + selectedFileName + " frame " + i);
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
                                           if(decodeddir.exists() == false) {
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
                }
	       
	           //********** now let's write the meta-data text file
		   		
                            if (frameDelay == 0 || numFrames == 1) {  //we can't have a 0 frame delay so if so, let's add a 100ms delay by default
                                    frameDelay = 100; //to do fix there is some frame delay issue on some gifs
                            }
                            
                            if (numFrames == 1) {  //again because of the above hack where single frame gifs aren't writing on raspberry pi, we'll turn a single frame gif into two frames
		   			filetag = "2" + "," + String.valueOf(frameDelay) + "," + String.valueOf(currentResolution) + "," + String.valueOf(md5); //current resolution may need to change to led panel type, we're adding the md5 here so we can check if unique file
		   		}
		   		else {
		   			filetag = String.valueOf(numFrames) + "," + String.valueOf(frameDelay) + "," + String.valueOf(currentResolution) + "," + String.valueOf(md5); //current resolution may need to change to led panel type
		   	   }

                           // String filetag = String.valueOf(numFrames) + "," + String.valueOf(frameDelay) + "," + String.valueOf(currentResolution) + "," + String.valueOf(md5); //current resolution may need to change to led panel type
		   				
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
                                        logMe.aLogger.severe("Could not write " + selectedFileName);
					e.printStackTrace();
				}
		}
		else {
			System.out.println("ERROR  Could not find file " + gifFilePath);
                        logMe.aLogger.severe("Could not write " + selectedFileName);
		}
    }
    
    public void decodeArcadeGIFConstantFrameRate(final String decodedDir, String gifFilePath, String gifName, int currentResolution, final int pixelMatrix_width, final int pixelMatrix_height) throws NoSuchAlgorithmException, IOException 
    {  
		//we should add another flag here if we're decoding from the jar or user supplied gif
		
		//we're going to decode a native GIF into our RGB565 format
                //we'll need to know the resolution of the currently selected matrix type: 16x32, 32x32, 32x64, or 64x64
		//and then we will receive the gif accordingly as we decode
		//we also need to get the original width and vuHeight of the gif which is easily done from the gif decoder class
		//String gifName = FilenameUtils.removeExtension(gifName); //with no extension
		
                 
                //since this decoding may take awhile, let's add a pls wait message to pixelcade letting the user know what is happening
                
                
                String plsWaitFilePathPNG = pixelHome + "system" + "/" + "wait.png";
                File plsWaitFilePathPNG_ = new File(plsWaitFilePathPNG);
               
                //the please wait here screws up the Q so let's do a hack and only call if the Q is empty
                
                if (plsWaitFilePathPNG_.exists() && PixelQueue.isEmpty() && isLooping == false)
                    writeArcadeImage(plsWaitFilePathPNG_, false, 0, "system", "wait.png",true); //loop cannot be blank otherwise we kill the Q for the random feature
        
                //for some reason, on first time decoding, gif will not play after decoding, it's a problem
                
                int arcadeFrameDelayTarget = 100;
                int arcadeCurrentFrameDelay = 100;
                int numFramesCopy = 1;
                int totalFramesCounter = 0; //used to count the total frames since we're duplicating
       
                String selectedFileName = FilenameUtils.getName(gifFilePath); 
                fileType = FilenameUtils.getExtension(gifFilePath);
                //gifNameNoExt = FilenameUtils.removeExtension(selectedFileName); //with no extension
                gifNameNoExt = FilenameUtils.removeExtension(selectedFileName); //with no extension
                
                
                //if (arcadeNameExtension.length() > 3) {   //this means there just happened to be a period in the file  name 
                //    arcadeNameOnly = arcadeName;
                //}    
                //else  { //there was no extension or it's a valid extension so proceed as normal and remove the extension if it's there
                //    arcadeNameOnly = FilenameUtils.removeExtension(arcadeName);
                //} 
		
		System.out.println("Arcade file name: " + selectedFileName);
		System.out.println("Arcade file name path: " + gifFilePath);
                logMe.aLogger.info("Arcade file name: " + selectedFileName);
                logMe.aLogger.info("Arcade file name path: " + gifFilePath);
               
		File file = new File(gifFilePath);
		if (file.exists()) {
                    
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    String md5 = checksum(gifFilePath, md);
                      //since we are decoding, we need to first make sure the .rgb565 and .txt decoded file is not there and delete if so.
                    String gifName565Path =  decodedDir + gifNameNoExt + ".rgb565";  //   ex. c:\animation\decoded\tree.rgb565
                    String gifNameTXTPath = decodedDir + gifNameNoExt + ".txt";  //   ex. c:\animation\decoded\tree.txt


                    //since we are decoding, we need to first make sure the .rgb565 and .txt decoded file is not there and delete if so.

                    File file565 = new File(gifName565Path);
                    File fileTXT = new File(gifNameTXTPath);

                    if (file565.exists()) file565.delete();
                    if (fileTXT.exists()) file565.delete();
                    //*******************************************************************************************

                    GifDecoder d = new GifDecoder(); //our gif decoder
                    d.read(gifFilePath);             //open the gif
	        
                    int numFrames = d.getFrameCount();      //how many frames in the original gif
                    //arcadeFrameDelayTarget = d.getDelay(1); //even though gifs have a frame delay for each frmae, pixel doesn't support this so we'll take the frame rate of the second frame and use this for the whole animation. We take the second frame because often times the frame delay of the first frame in a gif is much longer than the rest of the frames
                    
                    // let's find out the smallest frame delay and then we can use that to make copies
                    int currentFrameDelay = 0;
                    int smallestFrameDelay = 100;
                    for (int i = 0; i < numFrames; i++) { 
                        currentFrameDelay = d.getDelay(i);
                        //System.out.println("Frame delay: " + currentFrameDelay);
                        if (currentFrameDelay < smallestFrameDelay)
                            smallestFrameDelay = currentFrameDelay;
                    }
                    
                    //let's loop through and get the smallest frame delay and use that!
                    if (smallestFrameDelay == 0)
                        arcadeFrameDelayTarget = 10; //tried 1 here but it makes the file too big
                    else 
                        arcadeFrameDelayTarget = smallestFrameDelay;
                    

                    Dimension frameSize = d.getFrameSize();
                    int frameWidth = frameSize.width;
                    int frameHeight = frameSize.height;
                    
	            //System.out.println("smallest frame delay: " + arcadeFrameDelayTarget);
                    System.out.println("frame count: " + numFrames);
                    System.out.println("frame delay: " + arcadeFrameDelayTarget);
                    System.out.println("frame height: " + frameHeight);
                    System.out.println("frame width: " + frameWidth);
                    logMe.aLogger.info("frame count: " + numFrames);
                    logMe.aLogger.info("frame delay: " + arcadeFrameDelayTarget);
                    logMe.aLogger.info("frame height: " + frameHeight);
                    logMe.aLogger.info("frame width: " + frameWidth);
	          	          
                    if (numFrames == 1) {  //ok this is a hack, for some reason only on raspberry pi, single frame gifs are not writing so the work around is to write 2 frames for a single frame GIF
                             for (int i = 0; i < 2; i++) { 
                             BufferedImage rotatedFrame = d.getFrame(0);  
                                 if (frameWidth != pixelMatrix_width || frameHeight != pixelMatrix_height) {
                                         System.out.println("Resizing and encoding " + selectedFileName + " frame " + i);
                                         logMe.aLogger.info("Resizing and encoding " + selectedFileName + " frame " + i);

                                         try {
                                                        rotatedFrame = getScaledImage(rotatedFrame, pixelMatrix_width,pixelMatrix_height);

                                                } catch (IOException e) {
                                                        // TODO Auto-generated catch block
                                                        e.printStackTrace();
                                                }
                                 }
                                 else {
                                         System.out.println("Encoding " + selectedFileName + " frame " + i);
                                         logMe.aLogger.info("Encoding " + selectedFileName + " frame " + i);
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
                                           if(decodeddir.exists() == false) {
                                                    decodeddir.mkdirs();
                                            }

                                            try {

                                                    appendWrite(BitmapBytes, decodedDir + gifNameNoExt + ".rgb565"); //this writes one big file instead of individual ones


                                            } catch (IOException e1) {
                                                    // TODO Auto-generated catch block
                                                    //Log.e("PixelAnimate", "Had a problem writing the original unified animation rgb565 file");
                                                    e1.printStackTrace();
                                            }
                        }
                  }        
                   
                   else {  // we don't have a single frame gif
                  
                        for (int i = 0; i < numFrames; i++) { //loop through all the frames
                            
                           arcadeCurrentFrameDelay = d.getDelay(i);
                           //System.out.println("Current Frame Delay " + i + ": " + arcadeCurrentFrameDelay);
                           
                           //now let's check how many frame copies to make
                           if (arcadeCurrentFrameDelay > arcadeFrameDelayTarget) 
                               numFramesCopy = Math.round(arcadeCurrentFrameDelay / arcadeFrameDelayTarget);
                           else 
                               numFramesCopy = 1;
                           
                            //System.out.println(numFramesCopy + "for frame " + i);
                            //let's nest another for loop to duplicate the frames if that specific frame is longer than the target frame rate
                           for (int fc = 0; fc < numFramesCopy; fc++ ) {  //we have to go here at least once , fc means frame copy
                               
                                totalFramesCounter++;                     //since we are making copies, we want to track the total number of frames including the copies
                                BufferedImage rotatedFrame = d.getFrame(i);  

                                if (frameWidth != pixelMatrix_width || frameHeight != pixelMatrix_height) {
                                        System.out.println("Resizing and encoding " + selectedFileName + " frame " + i);
                                        logMe.aLogger.info("Resizing and encoding " + selectedFileName + " frame " + i);

                                        try {
                                                       rotatedFrame = getScaledImage(rotatedFrame, pixelMatrix_width,pixelMatrix_height);
                                               } catch (IOException e) {
                                                       // TODO Auto-generated catch block
                                                       e.printStackTrace();
                                               }
                                }
                                else {
                                        System.out.println("Encoding " + selectedFileName + " frame " + i);
                                        logMe.aLogger.info("Encoding " + selectedFileName + " frame " + i);
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
                                         decodeddir.mkdirs();

                                try {
                                    appendWrite(BitmapBytes, decodedDir + gifNameNoExt + ".rgb565"); //this writes one big file instead of individual ones
                                 } catch (IOException e1) {
                                        e1.printStackTrace();
                                 }


                      } //end for, we are done with the loop so let's now write the meta-data text file
                   }       
                }
	       
	        //********** now let's write the meta-data text file
		   		
                    if (arcadeFrameDelayTarget == 0 || numFrames == 1)   //we can't have a 0 frame delay so if so, let's add a 100ms delay by default
                        arcadeFrameDelayTarget = 100; //to do fix there is some frame delay issue on some gifs

                    if (numFrames == 1)   //again because of the above hack where single frame gifs aren't writing on raspberry pi, we'll turn a single frame gif into two frames
                        filetag = "2" + "," + String.valueOf(arcadeFrameDelayTarget) + "," + String.valueOf(currentResolution) + "," + String.valueOf(md5); //current resolution may need to change to led panel type, we're adding the md5 here so we can check if unique file

                    else 
                        //filetag = String.valueOf(numFrames) + "," + String.valueOf(arcadeFrameDelayTarget) + "," + String.valueOf(currentResolution) + "," + String.valueOf(md5); //current resolution may need to change to led panel type
                        filetag = String.valueOf(totalFramesCounter) + "," + String.valueOf(arcadeFrameDelayTarget) + "," + String.valueOf(currentResolution) + "," + String.valueOf(md5); //current resolution may need to change to led panel type
                           

                   // String filetag = String.valueOf(numFrames) + "," + String.valueOf(arcadeFrameDelayTarget) + "," + String.valueOf(currentResolution) + "," + String.valueOf(md5); //current resolution may need to change to led panel type

                   File myFile = new File(decodedDir + gifNameNoExt + ".txt"); 
                   
                   logMe.aLogger.info("Smallest Frame Delay: " + arcadeFrameDelayTarget);
                   logMe.aLogger.info("Total Original Frames: " + numFrames);
                   logMe.aLogger.info("Total New Frames: " + totalFramesCounter);

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
                            logMe.aLogger.severe("Could not write " + selectedFileName);
                            e.printStackTrace();
                    }
                   
		} //did the file exist
		else {
			System.out.println("ERROR  Could not find file " + gifFilePath);
                        logMe.aLogger.severe("Could not write " + selectedFileName);
		}
    }
    
    /**
     * Currently only analog clock mode is supported
     * @param mode 
     */
    public void displayClock(ClockModes mode)
    {
        //long oneSecond = Duration.ofSeconds(1).toMillis();
        
        //TimerTask drawTask = new DrawAnalogClockTask();
                
        //runRepeatingTask(drawTask, oneSecond);
        
         //let's move this to new timer architecture and skip repeating task
        
        ScheduledExecutorService clockService = Executors.newScheduledThreadPool(1);
        futureclock = clockService.scheduleAtFixedRate(clockTask, 0, 1, TimeUnit.SECONDS);
        clockTimerRunningFlag.set(true);  //atomic boolean , better for threads
        
       
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
  
  public static String getSelectedFilePath(Component command) 
  {
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
  
   public String getPIXELHardwareID() {
       return pixelHardwareId;
   }
  
    public int getyScrollingTextOffset()  
    {
        return yScrollingTextOffset;
    }

    public static void setyScrollingTextOffset(int yScrollingTextOffset) 
    {
        Pixel.yScrollingTextOffset = yScrollingTextOffset; //used to be this.yScrolligTextOffset vs. Pixel.yScrollingTextOffset
    }
    
     public int getFontSize() 
    {
        return fontSize;
    }
    
    public static void setFontSize(int fontSize) 
    {
        Pixel.fontSize = fontSize;
    }
    
    public String getFont() 
    {
        return fontFamily;
    }
    
    public static void setFontFamily(String fontFamily_) 
    {
        Pixel.fontFamily = fontFamily_;
    }
    
    public int getYOffset() 
    {
        return yOffset;
    }
    
    public static void setYOffset(int yOffset_) 
    {
        Pixel.yOffset = yOffset_;
    }
    
    public boolean getDoubleLine() 
    {
        return doubleLine;
    }
    
    public static void setDoubleLine(boolean doubleLine_) 
    {
        Pixel.doubleLine = doubleLine_;
    }
    
    public boolean getFourLine() 
    {
        return fourLine;
    }
    
    public static void setFourLine(boolean fourLine_) 
    {
        Pixel.fourLine = fourLine_;
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
    
     public void setScrollSmooth(int scrollsmooth)
    {
        scrollingTextMultiplier = scrollsmooth;
    }
    
    public void setScrollingText(String text)
    {
        scrollingText = text;
    }
    
    public void setScrollTextColor(Color color)
    {
        scrollingTextColor = color;
    }
    
    public void addtoQueue(String mode, String consoleOrText, String FileNameOrSpeed, int loop, Boolean writeMode, Color color, int scrollSmooth) {
        System.out.println("Adding item to Queue..."); 
        
        /* System.out.println("mode: " + mode); 
        System.out.println("text: " + consoleOrText); 
        System.out.println("speed: " + FileNameOrSpeed); 
        System.out.println("loop: " + loop);
        System.out.println("writemode: " + writeMode); 
        System.out.println("color: " + color); 
        System.out.println("color hex: " + toHexString(color)); 
        System.out.println("scroll smooth: " + scrollSmooth);
        */
        
        try { 
            
            PixelQueue.add(mode + ";" + consoleOrText + ";" + FileNameOrSpeed + ";" + Integer.toString(loop) + ";" + writeMode.toString() + ";" + toHexString(color) + ";" + Integer.toString(scrollSmooth));
        } 
        catch (Exception e) { 
            System.out.println("Queue Exception: " + e); 
        } 
        
        System.out.println("Queue Contents : " + PixelQueue);
    }
   
    public void stopExistingTimer()
    {
         
            //IMPORTANT DO NOT ADD AN INTERACTIVEMODE() HERE, WAS CAUSING ISSUES 
            
            if (streamGIFTimerRunningFlag.get() == true) {
                streamGIFTimerRunningFlag.set(false);
                streamGIFservice.shutdownNow(); //Kai's memory optimization
                future.cancel(true); //dont' interrupt if busy but we're cancelling this timer
                z = 0; 
            }
            
            if (PinballAnimationRunningFlag.get() == true) {
                PinballAnimationRunningFlag.set(false);
                streamPinballservice.shutdownNow();
                futurepinball.cancel(true); //dont' interrupt if busy but we're cancelling this timer
                z = 0;
            }

            if (scrollingTextTimerRunningFlag.get() == true) {
                //drawktask.shutdown();    //looks like this is not needed
                scrollingTextTimerRunningFlag.set(false);
                futurescroll.cancel(true); //dont' interrupt if busy
            }

             if (clockTimerRunningFlag.get() == true) {
                clockTimerRunningFlag.set(false);
                futureclock.cancel(true); //dont' interrupt if busy
            }
             
            if (PNGTimerRunningFlag.get() == true) {
                PNGTimerRunningFlag.set(false);
                futureimage.cancel(true); 
            }
          

                /* //old code with timer that was causing ioio disconnect on windows
                 if(timer == null)
                {
                    System.out.println("No timer stop needed at Pixel mode change.");
                     z = 0; //to be safe
                }
                else
                {
                    System.out.println("Stopping timer PIXEL activity in " + getClass().getSimpleName() + "..");
                    //animateTimer.cancel();
                    timer.cancel();
                    timer = null;
                    z = 0;             //we use this counter in the streaming gif timer, important that it gets reset when a new gif is started
                }
                 */
    }    
    
    public void writeArcadeAnimation(String selectedPlatformName, String selectedFileName, boolean writeMode, int loop, boolean pixelConnected) throws NoSuchAlgorithmException
    {

        //we first need to check that pixel is connected and if not, let's write it to the queue
        //ledblinky needed this because ledblanky calls pixelweb.exe and then immediately sends some commands
        if (!pixelConnected) {
             
            if (loop == 0) loop = 1;
            addtoQueue("gif",selectedPlatformName,selectedFileName,loop,writeMode,Color.red,1);
            
        } else {
        
        
             if (isLooping && loop == 99999 && !writeMode) {  //we were already looping and new command has a loop and is not a write command so let's write to the Q

                 addtoQueue("gif",selectedPlatformName,selectedFileName,99999,writeMode,Color.red,1);
                 loop99999FlagGIF = true;
            } 
            
             else if (isLooping && loop != 0 && !writeMode) {  //we were already looping and new command has a loop and is not a write command so let's write to the Q

                 addtoQueue("gif",selectedPlatformName,selectedFileName,loop,writeMode,Color.red,1); //but don't add to the Q if it's empty?

            } 
            
            else 

            {

                if (loop!=0 && !writeMode && loop != 99999) {            //we just got a looping command but were not looping before so just start a new one 
                    isLooping = true;
                    loopGIFCounter = 0;
                    loopTimesGlobal = loop; 
                }
                else {   //loop = 0 so we are not looping or we have a write command so let's reset everything and clear the Q
                    isLooping = false;
                    loopGIFCounter = 0;
                    loopTimesGlobal = 0;
                    PixelQueue.clear();
                }

                if (isWindows()) {
                    decodedAnimationsPath =  pixelHome + selectedPlatformName + "\\decoded\\";  //don't have to do this technically, just for display purposes
                    gifFilePath = pixelHome + selectedPlatformName + "\\" + selectedFileName; //user home/pixelcade/mame/digdug.gif
                }

                else {
                    decodedAnimationsPath =  pixelHome + selectedPlatformName + "/decoded/";   //pixelcade/mame/decoded
                    gifFilePath = pixelHome + selectedPlatformName + "/" + selectedFileName; //user home/pixelcade/mame/digdug.gif
                }

                stopExistingTimer();

                //let's make sure the target gif exists before proceeding
                File file = new File(gifFilePath);

                if (file.exists() && !file.isDirectory()) { 

                       try 
                       {
                           System.out.println("Found GIF: " + gifFilePath);
                           logMe.aLogger.info("Found GIF: " + gifFilePath);
                           animationFilename = selectedFileName;
                           if(gifTxtExists(decodedAnimationsPath,selectedFileName) == true && GIFRGB565Exists(decodedAnimationsPath,selectedFileName) == true)
                           {
                               System.out.println("This GIF was already decoded");
                               logMe.aLogger.info("This GIF was already decoded");
                           }
                           else
                           {
                               System.out.println("Decoding " + selectedFileName);
                               System.out.println("Decoding " + gifFilePath);
                               logMe.aLogger.info("Decoding " + gifFilePath);
                               // the text file is not there so we cannot continue and we must decode, let's first copy the file to home dir
                               decodeArcadeGIFConstantFrameRate(decodedAnimationsPath, gifFilePath,selectedFileName, currentResolution, KIND.width, KIND.height);
                           }

                           if (GIFArcadeNeedsDecoding(decodedAnimationsPath, selectedFileName, currentResolution,gifFilePath) == true)
                           {
                               System.out.println("Selected LED panel is different than the encoded GIF, need to re-encode...");
                               logMe.aLogger.info("Selected LED panel is different than the encoded GIF, need to re-encode...");
                               decodeArcadeGIFConstantFrameRate(decodedAnimationsPath, gifFilePath, selectedFileName, currentResolution, KIND.width, KIND.height);
                           }

                           //****** Now let's setup the animation ******

                           // TODO: replace animation_name with selectedFileName
                           String animation_name = selectedFileName;

                           float GIFfps = getDecodedfps(decodedAnimationsPath, animation_name); //get the fps //to do fix this later becaause we are getting from internal path
                           GIFnumFrames = getDecodednumFrames(decodedAnimationsPath, animation_name);
                           int gifSelectedFileDelay = getDecodedframeDelay(decodedAnimationsPath, animation_name);

                           currentResolution = getDecodedresolution(decodedAnimationsPath, animation_name);
                           GIFresolution = currentResolution;

                           System.out.println("GIF Width: " + KIND.width + ", GIF Height: " + KIND.height);
                           logMe.aLogger.info("GIF Width: " + KIND.width + ", GIF Height: " + KIND.height);

                           String pixelHardwareId = "not found";
                           try
                           {
                               pixelHardwareId = ioiO.getImplVersion(v.HARDWARE_VER);
                           }
                           catch (ConnectionLostException ex)
                           {
                               Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
                               logMe.aLogger.log(Level.SEVERE, Pixel.class.getName(), ex);
                           }

                           if (pixelHardwareId.substring(0,4).equals("PIXL") && writeMode == true)
                           {
                               interactiveMode();         //have to put back into interactive mode, otherwise we were playing locally
                               // need to tell PIXEL the frames per second to use, how fast to play the animations
                               writeMode(GIFfps);
                               System.out.println("Now writing to PIXEL's SD card, the screen will go blank until writing has been completed...");
                               logMe.aLogger.info("Now writing to PIXEL's SD card, the screen will go blank until writing has been completed...");

                               WriteGIFAnimationTask();
                           }
                           else
                           {

                               interactiveMode();  //we are streaming here so need to put in interactive mode first , otherwise we're just playing locally

//                               streampinballTask = new StreamPinballTask();
//                               streamPinballservice = Executors.newScheduledThreadPool(1);
//                               futurepinball = streamPinballservice.scheduleAtFixedRate(streampinballTask, 0, gifSelectedFileDelay, TimeUnit.MILLISECONDS);
//                               PinballAnimationRunningFlag.set(true);
                               
                               streamgifTask = new StreamGIFTask();
                               streamGIFservice = Executors.newScheduledThreadPool(1);
                               future = streamGIFservice.scheduleAtFixedRate(streamgifTask, 0, gifSelectedFileDelay, TimeUnit.MILLISECONDS);
                               streamGIFTimerRunningFlag.set(true);  //atomic boolean , better for threads
                               
                               
//                               ScheduledExecutorService streamGIFservice = Executors.newScheduledThreadPool(1);
//                               future = streamGIFservice.scheduleAtFixedRate(streamgifTask, 0, gifSelectedFileDelay, TimeUnit.MILLISECONDS);
//                               streamGIFTimerRunningFlag.set(true);  //atomic boolean , better for threads

                           }
                       } 
                       catch (IOException ex) 
                       {
                           Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
                       }
                } else {
                     System.out.println("** ERROR ** GIF file not found: " + gifFilePath);
                     logMe.aLogger.severe("GIF file not found: " + gifFilePath);
                }
        }
      }
    }
    
     public void writePinballAnimation(String selectedPlatformName, String selectedFileName, boolean writeMode, int loop, boolean pixelConnected) throws NoSuchAlgorithmException
    {
       
        GIFPlayed.add(selectedFileName);
        
        if (GIFPlayed.size() > 1) {
            lastGIFName = GIFPlayed.get(GIFPlayed.size()-2); //get the second to last item, we want to know the gif that was playing before this current one
//            System.out.println("2nd to Last: " + GIFPlayed.get(GIFPlayed.size() -2));
//            System.out.println("GIF Queue: " + GIFPlayed);
            GIFPlayed.remove(0); //remove the top item of the Q so we don't fill up memory
        }
        
        GIFPlayedDecodedPath.add(selectedPlatformName);
        if (GIFPlayedDecodedPath.size() > 1) {
            lastGIFDecodedPath = GIFPlayedDecodedPath.get(GIFPlayedDecodedPath.size()-2);
//            System.out.println("2nd Last GIF Platform: " + lastGIFDecodedPath);
//            System.out.println("Decoded GIF Queue: " + GIFPlayedDecodedPath);
            GIFPlayedDecodedPath.remove(0); //remove top item of Q so we don't fill up memory
        }

        //we first need to check that pixel is connected and if not, let's write it to the queue
        //ledblinky needed this because ledblanky calls pixelweb.exe and then immediately sends some commands
        if (pixelConnected) {
           
            //loop = 0 so we are not looping or we have a write command so let's reset everything and clear the Q
            isLooping = false;
            loopGIFCounter = 0;
            loopTimesGlobal = 0;
            PixelQueue.clear();
                

                if (isWindows()) {
                    decodedAnimationsPath =  pixelHome + selectedPlatformName + "\\decoded\\";  //don't have to do this technically, just for display purposes
                    gifFilePath = pixelHome + selectedPlatformName + "\\" + selectedFileName; //user home/pixelcade/mame/digdug.gif
                }

                else {
                    decodedAnimationsPath =  pixelHome + selectedPlatformName + "/decoded/";   //pixelcade/mame/decoded
                    gifFilePath = pixelHome + selectedPlatformName + "/" + selectedFileName; //user home/pixelcade/mame/digdug.gif
                }

                stopExistingTimer();

                //let's make sure the target gif exists before proceeding
                File file = new File(gifFilePath);

                if (file.exists() && !file.isDirectory()) { 

                       try 
                       {
                           System.out.println("Found GIF: " + gifFilePath);
                           logMe.aLogger.info("Found GIF: " + gifFilePath);
                           animationFilename = selectedFileName;
                           if(gifTxtExists(decodedAnimationsPath,selectedFileName) == true && GIFRGB565Exists(decodedAnimationsPath,selectedFileName) == true)
                           {
                               System.out.println("This GIF was already decoded");
                               logMe.aLogger.info("This GIF was already decoded");
                           }
                           else
                           {
                               System.out.println("Decoding " + selectedFileName);
                               System.out.println("Decoding " + gifFilePath);
                               logMe.aLogger.info("Decoding " + gifFilePath);
                               // the text file is not there so we cannot continue and we must decode, let's first copy the file to home dir
                               decodeArcadeGIFConstantFrameRate(decodedAnimationsPath, gifFilePath,selectedFileName, currentResolution, KIND.width, KIND.height);
                           }

                           if (GIFArcadeNeedsDecoding(decodedAnimationsPath, selectedFileName, currentResolution,gifFilePath) == true)
                           {
                               System.out.println("Selected LED panel is different than the encoded GIF, need to re-encode...");
                               logMe.aLogger.info("Selected LED panel is different than the encoded GIF, need to re-encode...");
                               decodeArcadeGIFConstantFrameRate(decodedAnimationsPath, gifFilePath, selectedFileName, currentResolution, KIND.width, KIND.height);
                           }

                           //****** Now let's setup the animation ******

                           // TODO: replace animation_name with selectedFileName
                           String animation_name = selectedFileName;

                           float GIFfps = getDecodedfps(decodedAnimationsPath, animation_name); //get the fps //to do fix this later becaause we are getting from internal path
                           GIFnumFrames = getDecodednumFrames(decodedAnimationsPath, animation_name);
                           int gifSelectedFileDelay = getDecodedframeDelay(decodedAnimationsPath, animation_name);

                           currentResolution = getDecodedresolution(decodedAnimationsPath, animation_name);
                           GIFresolution = currentResolution;

                           System.out.println("GIF Width: " + KIND.width + ", GIF Height: " + KIND.height);
                           logMe.aLogger.info("GIF Width: " + KIND.width + ", GIF Height: " + KIND.height);

                           String pixelHardwareId = "not found";
                           try
                           {
                               pixelHardwareId = ioiO.getImplVersion(v.HARDWARE_VER);
                           }
                           catch (ConnectionLostException ex)
                           {
                               Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
                               logMe.aLogger.log(Level.SEVERE, Pixel.class.getName(), ex);
                           }

                            interactiveMode();  //we are streaming here so need to put in interactive mode first , otherwise we're just playing locally
                            
                            
//                             ScheduledExecutorService streamGIFservice = Executors.newScheduledThreadPool(1);
//                             future = streamGIFservice.scheduleAtFixedRate(streampinballTask, 0, gifSelectedFileDelay, TimeUnit.MILLISECONDS);
//                             streamGIFTimerRunningFlag.set(true);  //atomic boolean , better for threads
                            
                            if (PinballAnimationInterrupt.get() == true) {  //we want to know if the previous animation had finished or is still running so we can overlay or not
                                PinballAnimationWasInterrupted.set(true);

                            }
                            else {
                                PinballAnimationWasInterrupted.set(false);
                            }
                            streampinballTask = new StreamPinballTask();
                            streamPinballservice = Executors.newScheduledThreadPool(1);
                            futurepinball = streamPinballservice.scheduleAtFixedRate(streampinballTask, 0, gifSelectedFileDelay, TimeUnit.MILLISECONDS);
                            PinballAnimationRunningFlag.set(true);
                            
                            } 
                            catch (IOException ex) 
                            {
                                Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
                            }
                } else {
                     System.out.println("** ERROR ** GIF file not found: " + gifFilePath);
                     logMe.aLogger.severe("GIF file not found: " + gifFilePath);
                }
        }
        else {
            System.out.println("[ERROR] Pixelcade not Connected");
            logMe.aLogger.severe("[ERROR] Pixelcade not Connected");
        }
    }
    
    public void writeAnimationFilePath(String selectedPlatformName, String selectedFileName, boolean writeMode, int loop, boolean pixelConnected) throws NoSuchAlgorithmException
    {
        
         //we first need to check that pixel is connected and if not, let's write it to the queue
        //ledblinky needed this because ledblanky calls pixelweb.exe and then immediately sends some commands
        
        if (!pixelConnected) {
             
            if (loop == 0) loop = 1;
            addtoQueue("animations",selectedPlatformName,selectedFileName,loop,writeMode,Color.red,1);
            
        } else {

            if (isLooping && loop != 0 && !writeMode) {  //we were already looping and new command has a loop and is not a write command so let's write to the Q

                    addtoQueue("animations",selectedPlatformName,selectedFileName,loop,writeMode,Color.red,1);

               } else 

               {

                   if (loop!=0 && !writeMode) {            //we were not already looping but new command has a loop and is not a write so let's continue and loop the gif
                       isLooping = true;
                       loopGIFCounter = 0;
                       loopTimesGlobal = loop; 
                   }
                   else {   //we are not looping or we have a write command so let's reset everything and clear the Q
                       isLooping = false;
                       loopGIFCounter = 0;
                       loopTimesGlobal = 0;
                       PixelQueue.clear();
                   }


           Boolean gifFileExists = true;
           
           //let's check if the gif is in gifsource first and if not, we'll assume its in animations because a user may have uploaded their own gif which would not go into gifsource

           if (isWindows()) {
                   decodedAnimationsPath =  pixelHome + selectedPlatformName + "\\gifsource\\decoded\\";  //don't have to do this technically, just for display purposes
                   gifFilePath = pixelHome + selectedPlatformName + "\\gifsource\\" + selectedFileName; //user home/pixelcade/mame/digdug.gif
               }
               else {
                   decodedAnimationsPath =  pixelHome + selectedPlatformName + "/gifsource/decoded/";   //pixelcade/mame/decoded
                   gifFilePath = pixelHome + selectedPlatformName + "/gifsource/" + selectedFileName; //user home/pixelcade/mame/digdug.gif
           }
           File gifFile = new File(gifFilePath);

           if (!gifFile.exists()) {  //gif is not in gifsource so let's check root

               if (isWindows()) {
                       decodedAnimationsPath =  pixelHome + selectedPlatformName + "\\decoded\\";  //don't have to do this technically, just for display purposes
                       gifFilePath = pixelHome + selectedPlatformName + "\\" + selectedFileName; //user home/pixelcade/mame/digdug.gif
               }
               else {
                       decodedAnimationsPath =  pixelHome + selectedPlatformName + "/decoded/";   //pixelcade/mame/decoded
                       gifFilePath = pixelHome + selectedPlatformName + "/" + selectedFileName; //user home/pixelcade/mame/digdug.gif
               }
               //let's check if the file is in animations directory

               File gifFileRoot = new File(gifFilePath);
                if (!gifFileRoot.exists()) {
                   gifFileExists = false;
                   System.out.println("** ERROR ** GIF file not found: " + gifFilePath);
                   logMe.aLogger.severe("GIF file not found: " + gifFilePath);
               }
           } 

           if (gifFileExists) {

               stopExistingTimer();
               //stopExistingTimer("gif",selectedPlatformName,selectedFileName,"0",writeMode, null); //we need to pass all this stuff because if we're looping, we'll need these parameters to add to the Q

               //let's make sure the target gif exists before proceeding
               File file = new File(gifFilePath);

               if(file.exists() && !file.isDirectory()) { 

                      try 
                      {
                          System.out.println("Found GIF: " + gifFilePath);
                          logMe.aLogger.info("Found GIF: " + gifFilePath);
                          animationFilename = selectedFileName;
                          if(gifTxtExists(decodedAnimationsPath,selectedFileName) == true && GIFRGB565Exists(decodedAnimationsPath,selectedFileName) == true)
                          {
                              System.out.println("This GIF was already decoded");
                              logMe.aLogger.info("This GIF was already decoded");
                          }
                          else
                          {
                              System.out.println("Decoding " + selectedFileName);
                              System.out.println("Decoding " + gifFilePath);
                              logMe.aLogger.info("Decoding " + gifFilePath);
                              // the text file is not there so we cannot continue and we must decode, let's first copy the file to home dir
                              decodeArcadeGIFConstantFrameRate(decodedAnimationsPath, gifFilePath,selectedFileName, currentResolution, KIND.width, KIND.height);
                          }

                          if (GIFArcadeNeedsDecoding(decodedAnimationsPath, selectedFileName, currentResolution,gifFilePath) == true)
                          {
                              System.out.println("Selected LED panel is different than the encoded GIF, need to re-encode...");
                              logMe.aLogger.info("Selected LED panel is different than the encoded GIF, need to re-encode...");
                              decodeArcadeGIFConstantFrameRate(decodedAnimationsPath, gifFilePath, selectedFileName, currentResolution, KIND.width, KIND.height);
                          }

                          //****** Now let's setup the animation ******

                          // TODO: replace animation_name with selectedFileName
                          String animation_name = selectedFileName;

                          float GIFfps = getDecodedfps(decodedAnimationsPath, animation_name); //get the fps //to do fix this later becaause we are getting from internal path
                          GIFnumFrames = getDecodednumFrames(decodedAnimationsPath, animation_name);
                          int gifSelectedFileDelay = getDecodedframeDelay(decodedAnimationsPath, animation_name);

                          currentResolution = getDecodedresolution(decodedAnimationsPath, animation_name);
                          GIFresolution = currentResolution;

                          System.out.println("GIF Width: " + KIND.width + ", GIF Height: " + KIND.height);
                          logMe.aLogger.info("GIF Width: " + KIND.width + ", GIF Height: " + KIND.height);


                          String pixelHardwareId = "not found";
                          try
                          {
                              pixelHardwareId = ioiO.getImplVersion(v.HARDWARE_VER);
                          }
                          catch (ConnectionLostException ex)
                          {
                              Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
                              logMe.aLogger.log(Level.SEVERE, Pixel.class.getName(), ex);
                          }

                          //stopExistingTimer();
                          //System.out.println("The existing timer was stopped");

                          if (pixelHardwareId.substring(0,4).equals("PIXL") && writeMode == true)
                          {
                              interactiveMode();         //have to put back into interactive mode, otherwise we were playing locally
                              // need to tell PIXEL the frames per second to use, how fast to play the animations
                              writeMode(GIFfps);
                              System.out.println("Now writing to PIXEL's SD card, the screen will go blank until writing has been completed...");
                              logMe.aLogger.info("Now writing to PIXEL's SD card, the screen will go blank until writing has been completed...");

                              // we'll run this in the background and also update the UI with progress
                              //System.out.println("The Pixel animation writer is being created");

                              //Date now = new Date();
                              //SendGifAnimationTask wp = new SendGifAnimationTask();   //starts a timer which loops through frames doing a write and playlocal when time done
                              //timer = new Timer(); //note that this timer only runs through one loop and then stops
                              //timer.schedule(wp, now);

                               WriteGIFAnimationTask();

                              //System.out.println("The Pixel animation writer was created");
                          }
                          else
                          {

                              interactiveMode();  //we are streaming here so need to put in interactive mode first , otherwise we're just playing locally

                              //System.out.println("Future version of the timer is starting.");
                              //scheduledExecutorService.scheduleAtFixedRate(streamgifTask, 0, gifSelectedFileDelay, TimeUnit.MILLISECONDS);

                              //define and get reference
                              ScheduledExecutorService streamGIFservice = Executors.newScheduledThreadPool(1);
                              future = streamGIFservice.scheduleAtFixedRate(streamgifTask, 0, gifSelectedFileDelay, TimeUnit.MILLISECONDS);
                              streamGIFTimerRunningFlag.set(true);  //atomic boolean , better for threads

                              //timer = new Timer();
                              //TimerTask animateTimer = new AnimateTimer(); 
                              //Date firstTime = new Date();
                              //timer.schedule(animateTimer, firstTime, gifSelectedFileDelay);  //

                              //System.out.println("Streaming version of the timer has started.");
                          }
                      } 
                      catch (IOException ex) 
                      {
                          Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
                      }
               } else {
                    System.out.println("** ERROR ** GIF file not found: " + gifFilePath);
                    logMe.aLogger.severe("GIF file not found: " + gifFilePath);
               }
          }
        }
      }
    }
  
    /**
     * This method sends an animation to the PIXEL. <b>Be sure to call #stopExistingTimer()</b> 
     * before calling this method.
     * @param selectedFileName
     * @param writeMode 
     */
    public void writeAnimation(String selectedFileName, boolean writeMode)  //we don't need loop here since this is UI
    {
        
        
        isLooping = false;  //clear Q and reset
        loopGIFCounter = 0;
        loopTimesGlobal = 0;
        PixelQueue.clear();
        
        
        stopExistingTimer();
        //stopExistingTimer("gif",null,selectedFileName,"0",writeMode, null);
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
        
        //System.out.println("The existing timer was stopped");
		
        pixelHardwareId = "not found";
        try 
        {
            pixelHardwareId = ioiO.getImplVersion(v.HARDWARE_VER);
        } 
        catch (ConnectionLostException ex) 
        {
            Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (pixelHardwareId.substring(0,4).equals("PIXL") && writeMode == true) 
        {
            interactiveMode();
            
            // need to tell PIXEL the frames per second to use, how fast to play the animations
            writeMode(GIFfps); 
            System.out.println("Now writing to PIXEL's SD card, the screen will go blank until writing has been completed..."); 

            // we'll run this in the background and also update the UI with progress
            //System.out.println("The Pixel animation writer is being created");
            //Date now = new Date();
            //SendGifAnimationTask wp = new SendGifAnimationTask();
            //timer = new Timer();
            //timer.schedule(wp, now);
            WriteGIFAnimationTask();
            //System.out.println("The Pixel animation writer was created");
        }
        else 
        {
            //System.out.println("A non PIXL, version of the timer is starting.");
            //System.out.println("stopped the existing timer again.");
            
            //TimerTask animateTimer = new AnimateTimer();  //this was causing ioio disconnects on windows (worked on mac and Pi)
            //timer = new Timer();
            //Date firstTime = new Date();
            //timer.schedule(animateTimer, firstTime, gifSelectedFileDelay);
            
            ScheduledExecutorService streamGIFservice = Executors.newScheduledThreadPool(1);
            future = streamGIFservice.scheduleAtFixedRate(streamgifTask, 0, gifSelectedFileDelay, TimeUnit.MILLISECONDS);
            streamGIFTimerRunningFlag.set(true); 
        
            //System.out.println("A non PIXL, version of the timer has started.");
        } 
    }
    
    public void writeArcadeImage(File PNGFileFullPath, Boolean writeMode, int loop, String consoleNameMapped, String PNGNameWithExtension, boolean pixelConnected) throws IOException {
        
        //System.out.println("PNG loop status: " + isLooping);
        
        if (!pixelConnected) {
             
            if (loop == 0) loop = 3;
            addtoQueue("png",consoleNameMapped,PNGNameWithExtension,loop,writeMode,Color.red,1);
            
        } else {
        
        
            if (isLooping && loop == 99999 && !writeMode) {  //we were already looping and new command has a loop and is not a write command so let's write to the Q

                 addtoQueue("png",consoleNameMapped,PNGNameWithExtension,loop,writeMode,Color.red,1);
                 loop99999FlagPNG = true;
            } 
            
            else if (isLooping && loop != 0 && !writeMode) {  //we were already looping and new command has a loop and is not a write command so let's write to the Q
                 addtoQueue("png",consoleNameMapped,PNGNameWithExtension,loop,writeMode,Color.red,1);
            } 
            
            else 

           {

               stopExistingTimer(); 

               if (loop != 0 && !writeMode && loop != 99999) {         //we'll be looping but note we can't loop if in write mode

                    isLooping = true;
                    loopPNGCounter = 0;
                    loopTimesGlobal = loop; 

                } else {   //we are not looping or we have a write command so let's reset everything and clear the Q
                    isLooping = false;
                    loopPNGCounter = 0;
                    loopTimesGlobal = 0;
                    PixelQueue.clear();
                }

                URL url = null; 
                BufferedImage image;
                url = PNGFileFullPath.toURI().toURL();
                image = ImageIO.read(url);

                System.out.println("PNG image found: " + url.toString());
                logMe.aLogger.info("PNG image found: " + url.toString());

                //stopExistingTimer("png",consoleNameMapped,PNGNameWithExtension,"0",writeMode, null);
                //stopExistingTimer();

                //if (saveAnimation && pixel.getPIXELHardwareID().substring(0,4).equals("PIXL")) {
                if (writeMode) {

                    interactiveMode();
                    writeMode(10);

                     try {
                           writeImagetoMatrix(image, KIND.width, KIND.height);
                    } catch (ConnectionLostException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                    } 

                    try {
                        Thread.sleep(100); //this may not be needed but was causing a problem on the writes for the gif animations so adding here to be safe
                        //TO DO will a smaller delay still work too?
                    } catch (InterruptedException ex) {
                        //Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }

                     playLocalMode();

                } else {

                    interactiveMode();

                    /*
                    if (isWindows()) { //hack, windows has some issue so need to add this delay
                        try {
                            Thread.sleep(50); //this may not be needed but was causing a problem on the writes for the gif animations so adding here to be safe
                            //TO DO will a smaller delay still work too?
                        } catch (InterruptedException ex) {
                            //Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    */

                    try {
                        writeImagetoMatrix(image, KIND.width, KIND.height); //to do add save parameter here
                    } catch (ConnectionLostException ex) {
                        Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (isLooping && !writeMode) {

                        ScheduledExecutorService loopPNGservice = Executors.newScheduledThreadPool(1);
                        //futureimage = loopPNGservice.scheduleAtFixedRate(pngLoopTask, 0, loop, TimeUnit.SECONDS);
                        futureimage = loopPNGservice.scheduleAtFixedRate(pngLoopTask, 0, 1, TimeUnit.SECONDS); //start now and run every second
                        PNGTimerRunningFlag.set(true);  //atomic boolean , better for threads

                    }

                //TO DP
                //if loop !=0, then add a timer that waits for the number of seconds in the loop parameter
            }

          }
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


    
    public enum ClockModes
    {
        ANALOG,
        DIGITAL,
        TIX
    }
    
//TODO: can this class be moved outside of the Pixel.java class; just like the Edu clock task?     
    //private class DrawAnalogClockTask extends TimerTask
    private class DrawAnalogClockTask implements Runnable 
            
    {
        private EduAnalogClock clock;
//        private AnalogClock clock;

        final int OFFSCREEN_IMAGE_WIDTH = 401;
        final int OFFSCREEN_IMAGE_HEIGHT = 401;
        

                 
        public DrawAnalogClockTask()
        {
            clock = new EduAnalogClock(OFFSCREEN_IMAGE_WIDTH, OFFSCREEN_IMAGE_HEIGHT);

            clock.init();
        }
                
        @Override
        public void run()
        {	    
            int w = OFFSCREEN_IMAGE_WIDTH;
            int h = OFFSCREEN_IMAGE_HEIGHT;

            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = img.createGraphics();
            
            System.out.println("paintng clock");
            clock.paint(g2d);
            System.out.println("clock painted");
//TODO: keep this around, just in case            
            g2d.dispose();

// uncomment this to see how often the pixel is communicated with the host            
//            System.out.print(".");

            if(matrix == null)
            {
// uncomment this for debugging
//                logger.log(Level.INFO, "Analog clock has no matrix.");
            }
            else
            {
                try 
                {  
                    System.out.println("writing clock");
                    writeImagetoMatrix(img, KIND.width, KIND.height);
                    System.out.println("clock written");
                } 
                catch (ConnectionLostException ex) 
                {
                    logger.log(Level.SEVERE, null, ex);
                }                
            }
        }
    }
    
//TODO: Did this not exist before?
    public enum PixelModes
    {
        ANIMATED_GIF,
        SCROLLING_TEXT,
        ARCADE,          //TO DO how to use this later?
        STILL_IMAGE
    }
    
     private void WriteGIFAnimationTask()   //this timer ends after one loop through the GIF but doesn't need to be a timer
    
    {
            String message = "Pixel is writing a GIF to the hardware.";
            System.out.println(message);
            
            //let's loop through and send frame to PIXEL with no delay
            for(int y=0; y<GIFnumFrames; y++) 
            { 
                //Al removed the -1, make sure to test that!!!!!
                sendPixelDecodedFrame(decodedAnimationsPath, animationFilename, y, GIFnumFrames, GIFresolution, KIND.width,KIND.height);
                System.out.print("."); //indicator that shows the user writing is happening, one dot equals one frame written
            }

            message = "Pixel is done writing the GIF, setting PIXEL to local playback mode.";
            System.out.println(message);
            
            try {                                 //this delay seems to help as we were getting a ioio disconnect without this
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            playLocalMode();
            
            //message = "Pixel is in local playback mode.";
            //System.out.println(message);  
                        
            //TODO UPDATE THE BROWSER/CLIENTS SOMEHOW
        
    }
    
    /**
     * When this task is executed, it sends an animated GIF to the the Pixel.
     */
    /*
     // had this as a timer originally but as it only needs to loop through all the frames one time with no delay, we don't actually need it to be a timer
    class SendGifAnimationTask extends TimerTask   //this timer ends after one loop through the GIF but doesn't need to be a timer
    
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
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            playLocalMode();
            
            message = "Pixel is in local playback mode.";
            System.out.println(message);  
                        
            //TODO UPDATE THE BROWSER/CLIENTS SOMEHOW
        }
    }
    */
    
    /*  class StreamGifAnimationTask extends TimerTask  //**** NO LONGER USED ****
    {
        @Override
        public void run()
        {
            
           
            String message = "timer instance loop";
            //System.out.println(message);
            System.out.println(message);
            
            //let's loop through and send frame to PIXEL with no delay
            for(int y=0; y<GIFnumFrames-1; y++) 
            { 
                //Al removed the -1, make sure to test that!!!!!
                sendPixelDecodedFrame(decodedAnimationsPath, animationFilename, y, GIFnumFrames, GIFresolution, KIND.width,KIND.height);
                System.out.println("counter : " + y);
            }
            
            //!!!!! if the cancel is happening in the middle of the for loop, we're in trouble
            
            //try {
            //    Thread.sleep(100);
            //} catch (InterruptedException ex) {
            //    Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
            // }
        }
    } */
         
     
    
     
     public class StreamGIFTask implements Runnable {

        public void run() {
            
            
         //if (streamGIFTimerRunningFlag.get() == true) {
         if (streamGIFTimerRunningFlag.get() == true && !Thread.currentThread().isInterrupted()) {
            
            if (z >= GIFnumFrames) { //then we've completed one loop
            
                z = 0;
                //System.out.println("reset z to: " + z);
                loopGIFCounter++;
                //System.out.println("loop GIF counter: " + loopGIFCounter);
                //System.out.println("loop times global: " + loopTimesGlobal);
                //if loop and we've finished looping, now check the queue
            }
            
            if (isLooping == true && loop99999FlagGIF == true && loopGIFCounter == 0) { // and gif was not already looping  //first of all, we must be in loop mode and if so have we finished all of our loops
                //let's let it continue indefinitely and not stop and check Q
                loopGIFCounter = 0;
                loopTimesGlobal = 0;
                isLooping = false;
                loop99999FlagGIF = false; //reset the flag
                System.out.println("Clearing Queue and Playing Indefintately");
                PixelQueue.clear(); //let's clear the Q
            }
            
            else if (isLooping == true && loopGIFCounter >= loopTimesGlobal) {  //first of all, we must be in loop mode and if so have we finished all of our loops
                //ok we're done looping so now we need to stop this current animation or scrolling text and then check the queue
                loopGIFCounter = 0;
                loopTimesGlobal = 0;
                isLooping = false;
                loop99999FlagGIF = false; //reset the flag
                System.out.println("Done looping GIF, now checking Queue");
                doneLoopingCheckQueue();
                
            } else {
           
                if (streamGIFTimerRunningFlag.get() == true) sendPixelDecodedFrame(decodedAnimationsPath, animationFilename, z, GIFnumFrames, GIFresolution, KIND.width,KIND.height); //if z is not reset, then we could be sending a frame that doesn't exist and hence ioio disconnect
                //sendPixelDecodedFrame(decodedAnimationsPath, animationFilename, z, GIFnumFrames, GIFresolution, KIND.width,KIND.height); //if z is not reset, then we could be sending a frame that doesn't exist and hence ioio disconnect
                z++;
            }
            
         } 
        }

        void shutdown() { //not using
           
        }
    } 
     
      public class StreamPinballTask implements Runnable {  //this is a timer than runs every x ms

        public void run() {

         if (PinballAnimationRunningFlag.get() && !Thread.currentThread().isInterrupted()) {
            
            PinballAnimationInterrupt.set(true);  //this is how we know if we got interrupted and need to overlay
            
            if (z >= GIFnumFrames) { //then we've completed one loop
                z = 0;
                stopExistingTimer();
                interactiveMode(); //clear the display like this or send a blank frame
                PinballAnimationInterrupt.set(false);
            }
            
            else {
           
                if (PinballAnimationRunningFlag.get()) {
                    sendPixelDecodedFramePinball(decodedAnimationsPath, animationFilename, z, GIFnumFrames, GIFresolution, KIND.width,KIND.height);
                    z++;
                } 
            }
         } else if (!PinballAnimationRunningFlag.get()) shutdown();
        }

        void shutdown() { //not using
            Thread.currentThread().interrupt();
        }
    }  
     
     
     
     public class PNGLoopTask implements Runnable {  //this will fire once a second

        public void run() {
              
            loopPNGCounter++;
            //System.out.println("loop PNG counter: " + loopPNGCounter);
            //System.out.println("loop limit: " + loopTimesGlobal);
            
             if (isLooping == true && loop99999FlagPNG == true && loopPNGCounter == 0) {  //first of all, we must be in loop mode and if so have we finished all of our loops
                //let's let it continue indefinitely and not stop and check Q
                loopPNGCounter = 0;
                loopTimesGlobal = 0;
                isLooping = false;
                loop99999FlagPNG = false; //reset the flag
                System.out.println("Clearing Queue and Playing Indefintately");
                PixelQueue.clear(); //let's clear the Q
            }
            
            
            if (isLooping == true && loopPNGCounter > loopTimesGlobal) {  //first of all, we must be in loop mode and if so have we finished all of our loops
                //ok we're done looping so now we need to stop this current animation or scrolling text and then check the queue
                loopPNGCounter = 0;
                loopTimesGlobal = 0;
                isLooping = false;
                System.out.println("Done time looping PNG, now checking Queue");
                doneLoopingCheckQueue();
            }
        }

        void shutdown() { //not using
           
        }
    } 
     
     
     
    
    public void doneLoopingCheckQueue() {
        
        /* Queue structure
        gif or image
        [gif or png], console, filename with ext, loop, writemode, null
        
        text
        text, scrolling text, speed, loop, writeMode, color
        */
        
        String mode_ = null;
        String console_ = null;
        String filenameWithExt_ = null;
        int loop_ = 0;
        boolean writeMode_ = false;
        
        String text_ = null;
        Long speed_ = null;
        int scrollsmooth_ = 1;
        Color color_ = null;

        //must reset loop flag and loop times to 0 before stopping timer
        isLooping = false;
        loopTimesGlobal = 0;
        stopExistingTimer(); 
         
         //ok we've stopped things, now let's check if anything is in the queue
         //Object firstElement = PixelQueue.peek();
         System.out.println("Items in the Queue: " + PixelQueue);
         
         if (!PixelQueue.isEmpty()) {
             
              System.out.println("Processing items in the Queue");
              //we've got some stuff in the queue so let's process
              String QueueCommand = PixelQueue.element(); //get the item in the top of the queue
              String [] QueueCommandArray = QueueCommand.split(";"); 
              
              /*
               System.out.println("Q Length: " + QueueCommandArray.length);
               System.out.println("Q 1: " + QueueCommandArray[0]);
               System.out.println("Q 2: " + QueueCommandArray[1]);
               System.out.println("Q 3: " + QueueCommandArray[2]);
               System.out.println("Q 4: " + QueueCommandArray[3]);
               System.out.println("Q 5: " + QueueCommandArray[4]);
               System.out.println("Q 6: " + QueueCommandArray[5]);
              */
             
                    
              if (QueueCommandArray.length == 7) {
                  
                  mode_ = QueueCommandArray[0];
                  
                  if (mode_.equals("gif") || mode_.equals("png") || mode_.equals("animations")) {
                      console_ = QueueCommandArray[1];
                      filenameWithExt_ = QueueCommandArray[2];
                      loop_ = Integer.valueOf(QueueCommandArray[3]);
                      if (QueueCommandArray[4].equals("true")) {
                          writeMode_ = true;
                      } else {
                          writeMode_ = false;
                      }
                      
                      
                      if (mode_.equals("gif")) {
                          PixelQueue.remove();
                          System.out.println("Processing GIF Queue item...");
                          try {
                              writeArcadeAnimation(console_, filenameWithExt_, writeMode_, loop_, true); //sending true for pixelconnected as we would not have gotten here if pixel was not connected
                          } catch (NoSuchAlgorithmException ex) {
                              Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
                          }
                      }
                      
                      else if (mode_.equals("animations")) {
                          PixelQueue.remove();
                          System.out.println("Processing Animation Queue item...");
                          try {
                              writeAnimationFilePath(console_,filenameWithExt_,writeMode_,loop_,true); //sending true for pixelconnected as we would not have gotten here if pixel was not connected
                          } catch (NoSuchAlgorithmException ex) {
                              Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
                          }
                      } 
                      
                      else {  //it was a png
                           //public void writeArcadeImage(File PNGFileFullPath, Boolean writeMode, int loop, String consoleNameMapped, String PNGNameWithExtension) 
                           //first we need to construct the full PNG path
                            System.out.println("Processing PNG Queue item...");
                            String arcadeFilePathPNG = pixelHome + console_ + "/" + filenameWithExt_;
                            File arcadeFilePNG = new File(arcadeFilePathPNG);
                            PixelQueue.remove();
                            if (arcadeFilePNG.exists()) {
                                try {
                                    writeArcadeImage(arcadeFilePNG,writeMode_,loop_,console_,filenameWithExt_, true);
                                } catch (IOException ex) {
                                    Logger.getLogger(Pixel.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else {
                                System.out.println("Files does not exist: " + arcadeFilePathPNG);
                            }
                              
                      }
                  }
                  else if (mode_.equals("text")) {
                       //text, scrolling text, speed, loop, writeMode, color
                       //  addtoQueue("text","","",loop,false,Color.red);
                     
                      text_ = QueueCommandArray[1];
                      speed_ = Long.valueOf(QueueCommandArray[2]);
                      loop_ = Integer.valueOf(QueueCommandArray[3]); 
                     
                     
                      if (QueueCommandArray[4].equals("true")) {
                          writeMode_ = true;
                      } else {
                          writeMode_ = false;
                      }
                               // to do need error handling here in case bad value for the color
                      String colorHexString = QueueCommandArray[5]; //color in the array is stored as a hex string like this ff00ff
                      color_ = hex2Rgb(colorHexString);             // now let's convert that hex string to rgb color
                    
                      scrollsmooth_ = Integer.valueOf(QueueCommandArray[6]); //the default is 1
                      
                      if (text_ == null) text_ = "No Text Specified";  //we don't need to set text, speed, color here as that will happen in the scrolltext method
                      if (speed_ == null) speed_ = 10L;
                      if (color_ == null) color_ = Color.RED;  //let's default to red if color not specified
                      if (scrollsmooth_ == 0) scrollsmooth_ = 1; //not really needed but just to be safe
//
                      //ok, we have everything we need now so let's scroll text
                      
                        try { 
                                PixelQueue.remove(); 
                            } 
                        catch (Exception e) { 
                                System.out.println("Exception: " + e);
                        }
                     
                      scrollText(text_, loop_, speed_, color_, true,scrollsmooth_);
                      
                  }
                  
                   else if (mode_.equals("localplayback")) {
                                                                         // here is the command that was sent here addtoQueue("localplayback","","",loop, writeMode,Color.red,1);
                     
                        try { 
                                PixelQueue.remove(); 
                            } 
                        catch (Exception e) { 
                                System.out.println("Exception: " + e);
                        }
                     
                      playLocalMode();
                      System.out.println("Executing Queue command for localplayback and clearing the Queue...");
                      PixelQueue.clear();
                     
                  }
                  
                  else {
                      System.out.println("ERROR: Invalid Queue Command Format");
                  }
                  
                  
              }
              else {
                  System.out.println("ERROR: Queue parameters are not correct");
              }
             
         } else {
             System.out.println("Queue is empty, blanking display");
                //does not look like we need to add a check here to not call this if the Q command is localplayback
                interactiveMode(); //TO DO send a blank frame better way
         }
        
         
    }
    
     /* public String getConsoleNamefromMapping(String originalConsoleName)
    {
         String consoleNameMapped = null; //to do set this if null?
         
         originalConsoleName = originalConsoleName.toLowerCase();
         //add the popular ones first to save time
          
         switch (originalConsoleName) {
            
            
            case "atari-2600":
                 consoleNameMapped = "atari2600";
                 return consoleNameMapped;
             case "mame-libretro":
                 consoleNameMapped = "mame";
                 return consoleNameMapped;
            case "mame-mame4all":
                consoleNameMapped = "mame";
                 return consoleNameMapped;
            case "arcade":
                consoleNameMapped = "mame";
                 return consoleNameMapped;
            case "mame-advmame":
                consoleNameMapped = "neogeo";
                 return consoleNameMapped;
            case "atari 2600":
                consoleNameMapped = "atari2600";
                return consoleNameMapped;
            case "nintendo entertainment system":
                consoleNameMapped = "nes";
                return consoleNameMapped;
            case "nintendo 64":
                consoleNameMapped = "n64";
                return consoleNameMapped;
            case "sony playstation":
                 consoleNameMapped = "psx";
                 return consoleNameMapped;
            case "sony playstation 2":
                consoleNameMapped = "ps2";
                 return consoleNameMapped;
            case "sony pocketstation":
                consoleNameMapped = "psp";
                 return consoleNameMapped;
            case "sony psp":
                consoleNameMapped = "psp";
                 return consoleNameMapped;
            case "amstrad cpc":
                consoleNameMapped = "amstradcpc";
                 return consoleNameMapped;
            case "amstrad gx4000":
                consoleNameMapped = "amstradcpc";
                 return consoleNameMapped;
            case "apple II":
                consoleNameMapped = "apple2";
                 return consoleNameMapped;
            case "atari 5200":
                consoleNameMapped = "atari5200";
                 return consoleNameMapped;
            case "atari 7800":
                consoleNameMapped = "atari7800";
                 return consoleNameMapped;
            case "atari jaguar":
                consoleNameMapped = "atarijaguar";
                 return consoleNameMapped;
            case "atari jaguar cd":
                consoleNameMapped = "atarijaguar";
                 return consoleNameMapped;
            case "atari lynx":
                consoleNameMapped = "atarilynx";
                 return consoleNameMapped;
            case "bandai super vision 8000":
                consoleNameMapped = "wonderswan";
                 return consoleNameMapped;
            case "bandai wonderswan":
                consoleNameMapped = "wonderswan";
                 return consoleNameMapped;
            case "bandai wonderswan color":
                consoleNameMapped = "wonderswancolor";
                 return consoleNameMapped;
            case "capcom classics":
                consoleNameMapped = "capcom";
                 return consoleNameMapped;
            case "capcom play pystem":
                consoleNameMapped = "capcom";
                 return consoleNameMapped;
            case "capcom play system II":
                consoleNameMapped = "capcom";
                 return consoleNameMapped;
            case "capcom play system III":
                consoleNameMapped = "capcom";
                 return consoleNameMapped;
            case "colecovision":
                consoleNameMapped = "coleco";
                 return consoleNameMapped;
            case "commodore 128":
                consoleNameMapped = "c64";
                 return consoleNameMapped;
            case "commodore 16 & plus4":
                consoleNameMapped = "c64";
                 return consoleNameMapped;
            case "commodore 64":
                consoleNameMapped = "c64";
                 return consoleNameMapped;
            case "commodore amiga":
                consoleNameMapped = "amiga";
                 return consoleNameMapped;
            case "commodore amiga cd32":
                consoleNameMapped = "amiga";
                 return consoleNameMapped;
            case "commodore vic-20":
                consoleNameMapped = "c64";
                 return consoleNameMapped;
            case "final burn alpha":
                consoleNameMapped = "fba";
                 return consoleNameMapped;
            case "future pinball":
                consoleNameMapped = "futurepinball";
                 return consoleNameMapped;
            case "gce vectrex":
                consoleNameMapped = "vectrex";
                 return consoleNameMapped;
            case "magnavox odyssey":
                consoleNameMapped = "odyssey";
                 return consoleNameMapped;
            case "magnavox odyssey 2":
                consoleNameMapped = "odyssey";
                 return consoleNameMapped;
            case "mattel intellivision":
                consoleNameMapped = "intellivision";
                 return consoleNameMapped;
            case "microsoft msx":
                consoleNameMapped = "msx";
                 return consoleNameMapped;
            case "microsoft msx2":
                consoleNameMapped = "msx";
                 return consoleNameMapped;
            case "microsoft msx2+":
                consoleNameMapped = "msx";
                 return consoleNameMapped;
            case "microsoft windows 3.x":
                consoleNameMapped = "pc";
                 return consoleNameMapped;
            case "misfit mame":
                consoleNameMapped = "mame";
                 return consoleNameMapped;
            case "nec pc engine":
                consoleNameMapped = "pcengine";
                 return consoleNameMapped;
            case "nec pc engine-cd":
                consoleNameMapped = "pcengine";
                 return consoleNameMapped;
            case "nec pc-8801":
                consoleNameMapped = "pcengine";
                 return consoleNameMapped;
            case "nec pc-9801":
                consoleNameMapped = "pcengine";
                 return consoleNameMapped;
            case "nec pc-fx":
                consoleNameMapped = "pcengine";
                 return consoleNameMapped;
            case "nec supergrafx":
                consoleNameMapped = "pcengine";
                 return consoleNameMapped;
            case "nec turbografx-16":
                consoleNameMapped = "pcengine";
                 return consoleNameMapped;
            case "nec turbografx-cd":
                consoleNameMapped = "pcengine";
                 return consoleNameMapped;
            case "nintendo 64dd":
                consoleNameMapped = "n64";
                 return consoleNameMapped;
            case "nintendo famicom":
                consoleNameMapped = "nes";
                 return consoleNameMapped;
            case "nintendo famicom disk system":
                consoleNameMapped = "nes";
                 return consoleNameMapped;
            case "nintendo game boy":
                consoleNameMapped = "gb";
                 return consoleNameMapped;
            case "nintendo game boy advance":
                consoleNameMapped = "gba";
                 return consoleNameMapped;
            case "nintendo game boy color":
                consoleNameMapped = "gbc";
                 return consoleNameMapped;
            case "nintendo gamecube":
                consoleNameMapped = "nes";
                 return consoleNameMapped;
            case "nintendo pokemon mini":
                consoleNameMapped = "nes";
                 return consoleNameMapped;
            case "nintendo satellaview":
                consoleNameMapped = "nes";
                 return consoleNameMapped;
            case "nintendo super famicom":
                consoleNameMapped = "nes";
                 return consoleNameMapped;
            case "nintendo super game boy":
                consoleNameMapped = "gba";
                 return consoleNameMapped;
            case "nintendo virtual boy":
                consoleNameMapped = "nes";
                 return consoleNameMapped;
            case "nintendo wii":
                consoleNameMapped = "nes";
                 return consoleNameMapped;
            case "nintendo wii u":
                consoleNameMapped = "nes";
                 return consoleNameMapped;
            case "nintendo wiiware":
                consoleNameMapped = "nes";
                 return consoleNameMapped;
            case "panasonic 3do":
                consoleNameMapped = "3do";
                 return consoleNameMapped;
            case "pc games":
                consoleNameMapped = "pc";
                 return consoleNameMapped;
            case "pinball fx2":
                consoleNameMapped = "futurepinball";
                 return consoleNameMapped;
            case "sega 32x":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sega cd":
                consoleNameMapped = "segacd";
                 return consoleNameMapped;
            case "sega classics":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sega dreamcast":
                consoleNameMapped = "dreamcast";
                 return consoleNameMapped;
            case "sega game gear":
                consoleNameMapped = "gamegear";
                 return consoleNameMapped;
            case "sega genesis":
                consoleNameMapped = "genesis";
                 return consoleNameMapped;
            case "sega hikaru":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sega master system":
                consoleNameMapped = "mastersystem";
                 return consoleNameMapped;
            case "sega model 2":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sega model 3":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sega naomi":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sega pico":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sega saturn":
                consoleNameMapped = "saturn";
                 return consoleNameMapped;
            case "sega sc-3000":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sega sg-1000":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sega st-v":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sega triforce":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sega vmu":
                consoleNameMapped = "sega32x";
                 return consoleNameMapped;
            case "sinclair zx spectrum":
                consoleNameMapped = "zxspectrum";
                 return consoleNameMapped;
            case "sinclair zx81":
                consoleNameMapped = "zxspectrum";
                 return consoleNameMapped;
            case "snk classics":
                consoleNameMapped = "neogeo";
                 return consoleNameMapped;
            case "snk neo geo aes":
                consoleNameMapped = "neogeo";
                 return consoleNameMapped;
            case "snk neo geo cd":
                consoleNameMapped = "neogeo";
                 return consoleNameMapped;
            case "snk neo geo mvs":
                consoleNameMapped = "neogeo";
                 return consoleNameMapped;
            case "snk neo geo pocket":
                consoleNameMapped = "ngp";
                 return consoleNameMapped;
            case "snk neo geo pocket color":
                consoleNameMapped = "ngpc";
                 return consoleNameMapped;
            case "sony psp minis":
                consoleNameMapped = "psp";
                 return consoleNameMapped;
            case "super nintendo entertainment system":
                consoleNameMapped = "snes";
                 return consoleNameMapped;
            case "visual pinball":
                consoleNameMapped = "visualpinball";
                 return consoleNameMapped;
            default: 
                 consoleNameMapped = originalConsoleName;    //we didn't find a match so just return the name you got
                 return consoleNameMapped;
        }
    }*/
    
    private Color getColor (String colorText) {  //no longer used
           
        Boolean colorTextMatch = false;
        Color color;
        switch (colorText) {

                        case "red":
                            color = Color.RED;
                            colorTextMatch = true;
                            break;
                        case "blue":
                            color = Color.BLUE;
                            colorTextMatch = true;
                            break;
                         case "cyan":
                            color = Color.CYAN;
                            colorTextMatch = true;
                            break;
                         case "gray":
                            color = Color.GRAY;
                            colorTextMatch = true;
                            break;
                         case "darkgray":
                            color = Color.DARK_GRAY;
                            colorTextMatch = true;
                            break;
                        case "green":
                            color = Color.GREEN;
                            colorTextMatch = true;
                            break;
                         case "lightgray":
                            color = Color.LIGHT_GRAY;
                            colorTextMatch = true;
                            break;
                        case "magenta":
                            color = Color.MAGENTA;
                            colorTextMatch = true;
                            break;
                        case "orange":
                            color = Color.ORANGE;
                            colorTextMatch = true;
                            break;
                         case "pink":
                            color = Color.PINK;
                            colorTextMatch = true;
                            break;
                        case "yellow":
                            color = Color.YELLOW;
                            colorTextMatch = true;
                            break;
                        case "white":
                            color = Color.WHITE;
                            colorTextMatch = true;
                            break;
                        default: 
                            color = Color.RED;
                            colorTextMatch = false;
                    }

                    if (!colorTextMatch) {           //this means we have a hex code vs. color string text
                        color = hex2Rgb(colorText);
                    }
                    
                    return color;
                    
    }
    
     public static Color hex2Rgb(String colorStr) 
    {
        return new Color(
                Integer.valueOf( colorStr.substring( 0, 2 ), 16 ),
                Integer.valueOf( colorStr.substring( 2, 4 ), 16 ),
                Integer.valueOf( colorStr.substring( 4, 6 ), 16 ) );
    }  
     
     public final static String toHexString(Color colour) throws NullPointerException {
            String hexColour = Integer.toHexString(colour.getRGB() & 0xffffff);
            if (hexColour.length() < 6) {
              hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
            }
        return hexColour;
}
     
    //private class AnimateTimer extends TimerTask
    /* public class AnimateTimer extends TimerTask //***** NO LONGER USED ******
    {
        @Override
        public void run()
        {
            
            if (z >= GIFnumFrames) 
            {
                z = 0;
                System.out.println("reset z to: " + z);
            }
            
            String message = "frame:" + z; //should start at 0 in counter reset but is staritng at 1
            //System.out.println(message);
            System.out.println(message);
            sendPixelDecodedFrame(decodedAnimationsPath, animationFilename, z, GIFnumFrames, GIFresolution, KIND.width,KIND.height); //if z is not reset, then we could be sending a frame that doesn't exist and hence ioio disconnect
            
            z++;
            
        }
         
    } */
     
     
     public static boolean isWindows() {

		return (OS.indexOf("win") >= 0);

	}

	public static boolean isMac() {

		return (OS.indexOf("mac") >= 0);

	}

	public static boolean isUnix() {

		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
		
	}
        
        
 public class ScrollTextTask implements Runnable {
    public void run() {
      if (Pixel.this.scrollingTextTimerRunningFlag.get() == true && !Thread.currentThread().isInterrupted()) {
        int w = Pixel.this.KIND.width;
        int h = Pixel.this.KIND.height;
        BufferedImage img = new BufferedImage(w, h, 2);
        Graphics2D g2d = img.createGraphics();
        g2d.setPaint(Pixel.this.scrollingTextColor);
        g2d.setFont(Pixel.this.font);
        FontMetrics fm = g2d.getFontMetrics();
        int yDifference = Pixel.this.KIND.height - fm.getHeight() * 72 / 110;
        int y = yDifference / 2 + fm.getHeight() * 72 / 110 + Pixel.yOffset;
        try {
          Pixel.this.additionalBackgroundDrawing(g2d);
        } catch (Exception ex) {
          Pixel.this.logger.log(Level.SEVERE, (String)null, ex);
        } 
        if (Pixel.doubleLine == true) {
          int doublelineoffset = -7;
          String[] words = Pixel.this.scrollingText.split(" +");
          int count = (int)(words.length / 2.0D + 0.5D);
          String[] part = new String[2];
          Arrays.fill((Object[])part, "");
          for (int i = 0; i < words.length; i++) {
            if (i < count) {
              part[0] = part[0] + words[i] + " ";
            } else {
              part[1] = part[1] + words[i] + " ";
            } 
          } 
          part[1] = part[1].trim();
          g2d.drawString(part[0] + "\n", Pixel.this.x, y + doublelineoffset);
          g2d.drawString(part[1] + "\n", Pixel.this.x, y + 14 + doublelineoffset);
          if (part[0].length() > part[1].length()) {
            Pixel.this.maxMultiLine = part[0];
          } else {
            Pixel.this.maxMultiLine = part[1];
          } 
        } else if (Pixel.fourLine == true) {
          int FourLineOffset = -11;
          String[] words = Pixel.this.scrollingText.split(" +");
          int quarter = (int)(words.length / 4.0D + 0.5D);
          String[] part = new String[4];
          Arrays.fill((Object[])part, "");
          for (int i = 0; i < words.length; i++) {
            if (i < quarter) {
              part[0] = part[0] + words[i] + " ";
            } else if (i >= quarter && i < quarter * 2) {
              part[1] = part[1] + words[i] + " ";
            } else if (i >= quarter * 2 && i < quarter * 3) {
              part[2] = part[2] + words[i] + " ";
            } else {
              part[3] = part[3] + words[i] + " ";
            } 
          } 
          part[1] = part[1].trim();
          g2d.drawString(part[0] + "\n", Pixel.this.x, y + FourLineOffset);
          g2d.drawString(part[1] + "\n", Pixel.this.x, y + 6 + FourLineOffset);
          g2d.drawString(part[2] + "\n", Pixel.this.x, y + 12 + FourLineOffset);
          g2d.drawString(part[3] + "\n", Pixel.this.x, y + 18 + FourLineOffset);
          int[] arr = { part[0].length(), part[1].length(), part[2].length(), part[3].length() };
          int max = arr[0];
          int maxElement = 0;
          for (int j = 1; j < arr.length; j++) {
            if (arr[j] > max)
              maxElement = j; 
          } 
          Pixel.this.maxMultiLine = part[maxElement];
        } else {
          g2d.drawString(Pixel.this.scrollingText, Pixel.this.x, y);
        } 
        try {
          Pixel.this.additionalForegroundDrawing(g2d);
        } catch (Exception ex) {
          Pixel.this.logger.log(Level.SEVERE, (String)null, ex);
        } 
        g2d.dispose();
        if (Pixel.this.matrix == null) {
          Pixel.this.logger.log(Level.INFO, "There is no matrix for the text scrolller.");
        } else if (Pixel.this.scrollingTextTimerRunningFlag.get() == true) {
          try {
            Pixel.this.writeImagetoMatrix(img, Pixel.this.KIND.width, Pixel.this.KIND.height);
          } catch (ConnectionLostException ex) {
            Pixel.this.logger.log(Level.SEVERE, (String)null, (Throwable)ex);
          } 
        } 
        int messageWidth = fm.stringWidth(Pixel.this.scrollingText);
        if (Pixel.doubleLine == true || Pixel.fourLine == true)
          messageWidth = fm.stringWidth(Pixel.this.maxMultiLine); 
        int resetX = 0 - messageWidth;
        if (Pixel.this.x <= resetX) {
          Pixel.this.x = w;
          Pixel.this.loopScrollingTextCounter++;
          if (Pixel.this.isLooping.booleanValue() == true && Pixel.this.loop99999FlagText.booleanValue() == true && Pixel.this.loopScrollingTextCounter == 0) {
            Pixel.this.loopScrollingTextCounter = 0;
            Pixel.this.loopTimesGlobal = 0;
            Pixel.this.isLooping = Boolean.valueOf(false);
            Pixel.this.loop99999FlagText = Boolean.valueOf(false);
            System.out.println("Clearing Queue and Playing Indefintately");
            Pixel.this.PixelQueue.clear();
          } 
          if (Pixel.this.isLooping.booleanValue() == true && Pixel.this.loopScrollingTextCounter >= Pixel.this.loopTimesGlobal) {
            Pixel.this.loopScrollingTextCounter = 0;
            System.out.println("Done looping scrolling text, now checking Queue");
            Pixel.this.doneLoopingCheckQueue();
          } 
        } else {
          Pixel.this.x = Pixel.this.x - Pixel.this.scrollingTextMultiplier;
        } 
      } 
    }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    
    //private class TextScroller extends TimerTask
//     public class ScrollTextTask implements Runnable 
//    {
//        @Override
//        public void run()
//        {
//
//          if (scrollingTextTimerRunningFlag.get() == true && !Thread.currentThread().isInterrupted()) {
//           
//            int w = KIND.width;
//            int h = KIND.height;
//            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//            	    
//            Graphics2D g2d = img.createGraphics();
//            g2d.setPaint(scrollingTextColor);
//       
//            Font font = fonts.get(fontFamily);
//            font = new Font(fontFamily, Font.PLAIN, fontSize);
//            
//            if (!fontFamily.equals("Arial")) {
//                try {
//                        Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts//" + fontFamily + ".ttf"));
//                           GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//                           ge.registerFont(customFont);
//
//                       } catch (IOException e) {
//                           System.out.println(fontFamily + " not fount, defaulting to Arial");
//                           font = new Font("Arial", Font.PLAIN, KIND.height);
//                           fonts.put(fontFamily, font);
//
//                       } catch(FontFormatException e) {
//                           System.out.println(fontFamily + " not fount, defaulting to Arial");
//                           font = new Font("Arial", Font.PLAIN, KIND.height);
//                           fonts.put(fontFamily, font);
//                       }
//            }
//            
//            if(font == null)  //then we have a custom font most likely
//                { //default font size is 32 but we also set it from WebEnabledPixel
//                font = new Font("Arial", Font.PLAIN, KIND.height);
//                fonts.put(fontFamily, font);
//            } 
//            
//            g2d.setFont(font);
//            FontMetrics fm = g2d.getFontMetrics();
//            
//            //int descent = fm.getMaxDescent();
//            //int descent = fm.getDescent();
//            //System.out.println("descent size: " + descent);
//            
////            //int ascent = fm.getMaxAscent();
////            int ascent = fm.getAscent();
////            System.out.println("ascent size: " + ascent);
////            
////            System.out.println("font original height: " + fm.getHeight());
////            System.out.println("font original height pixels: " + fm.getHeight() * 72 / Toolkit.getDefaultToolkit().getScreenResolution());
//            //System.out.println("font width" + fm.stringWidth(scrollingText)); 
//            //int y = fm.getHeight() + yScrollingTextOffset;
//            
//            int yDifference = KIND.height - fm.getHeight() *  72 / Toolkit.getDefaultToolkit().getScreenResolution();
//            //System.out.println("yDifference Pixels: " + yDifference);
//            //int y = fm.getHeight() + yDifference + yOffset ;
//            //int y = KIND.height + yDifference + yOffset ;
//            int y = (fm.getHeight() *  72 / Toolkit.getDefaultToolkit().getScreenResolution()) + yOffset;
//            
//            //System.out.println("mod height" + y); 
//
//            try 
//            {
//                additionalBackgroundDrawing(g2d);
//            } 
//            catch (Exception ex) 
//            {
//                logger.log(Level.SEVERE, null, ex);
//            }
//            
//            if (doubleLine == true) {
//                String[] words = scrollingText.split(" +"); // Split words by spaces
//                int count = (int) ((words.length / 2.0) + 0.5); // Number of words in part[0]
//                String[] part = new String[2];
//                Arrays.fill(part, ""); // Initialize to empty strings
//                for (int i = 0; i < words.length; i++) {
//                    if (i < count) { // First half of the words go into part[0]
//                        part[0] += words[i] + " ";
//                    } else { // Next half go into part[1]
//                        part[1] += words[i] + " ";
//                    }
//                }
//                part[1] = part[1].trim(); // Since there will be extra space at end of part[1]
//                g2d.drawString(part[0] + "\n", x, y);
//                g2d.drawString(part[1] + "\n", x, y + 12);
//                if (part[0].length() > part[1].length())   //we need to get the longest length for the scrolling end
//                    maxMultiLine = part[0];
//                else 
//                    maxMultiLine = part[1];
//            }
//            else if (fourLine == true) { 
//                
//            fontFamily = "Minimal4";  //since we have 4 lines, we need to hard code the specific font here or it won't fit
//            fontSize = 14;
//            font = fonts.get(fontFamily);
//            font = new Font(fontFamily, Font.PLAIN, fontSize);
//           
//                try {
//                        Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts//" + fontFamily + ".ttf"));
//                           GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//                           ge.registerFont(customFont);
//                       } catch (IOException e) {
//                           System.out.println(fontFamily + " not fount, defaulting to Arial");
//                           font = new Font("Arial", Font.PLAIN, KIND.height);
//                           fonts.put(fontFamily, font);
//                       } catch(FontFormatException e) {
//                           System.out.println(fontFamily + " not fount, defaulting to Arial");
//                           font = new Font("Arial", Font.PLAIN, KIND.height);
//                           fonts.put(fontFamily, font);
//                       }
//            g2d.setFont(font);
//            fm = g2d.getFontMetrics();
//                
//                String[] words = scrollingText.split(" +"); // Split words by spaces
//                int quarter = (int) ((words.length / 4.0) + 0.5); // Number of words in part[0]
////                System.out.println("total words: " + words.length);
////                System.out.println("quarters: " + quarter);
//                String[] part = new String[4];
//                Arrays.fill(part, ""); // Initialize to empty strings
//                for (int i = 0; i < words.length; i++) {
//                    
//                    if (i < quarter)  // First quarter of the words go into part[0]
//                        part[0] += words[i] + " ";
//                    else if (i >= quarter && i < quarter*2)  // Next quarter go into part[1]
//                        part[1] += words[i] + " ";
//                    else if (i >= quarter*2 && i < quarter*3)  // third quarter go into part[2]
//                        part[2] += words[i] + " ";
//                    else 
//                        part[3] += words[i] + " ";
//                }
//                part[1] = part[1].trim(); // Since there will be extra space at end of part[1]
//                g2d.drawString(part[0] + "\n", x, y);
//                g2d.drawString(part[1] + "\n", x, y + 6);
//                g2d.drawString(part[2] + "\n", x, y + 12);
//                g2d.drawString(part[3] + "\n", x, y + 18);
//                
//                int arr[] = {part[0].length(), part[1].length(), part[2].length(), part[3].length()}; 
//                int i; 
//                // Initialize maximum element 
//                int max = arr[0]; 
//                int maxElement = 0;
//                // Traverse array elements from second and 
//                // compare every element with current max   
//                for (i = 1; i < arr.length; i++) 
//                    if (arr[i] > max) 
//                        maxElement = i;
//                maxMultiLine = part[maxElement];
//            }
//            else {
//                 g2d.drawString(scrollingText, x, y);
//            }
//             
//            try 
//            {
//                additionalForegroundDrawing(g2d);
//            } 
//            catch (Exception ex) 
//            {
//                logger.log(Level.SEVERE, null, ex);
//            }
//            
//            g2d.dispose();
//
//            // uncomment this to see how often the pixel is communicated with the host            
//            // System.out.print(".");
//
//            if(matrix == null)
//            {
//                // uncomment this for debugging
//                logger.log(Level.INFO, "There is no matrix for the text scrolller.");
//            }
//            else
//            {
//               
//                if (scrollingTextTimerRunningFlag.get() == true) {  //there was a timing issue on windows only so added this, if the timerflag has been set to false, then don't write this frame
//                    try 
//                    {  
//                        writeImagetoMatrix(img, KIND.width, KIND.height);
//                    } 
//                    catch (ConnectionLostException ex) 
//                    {
//                        logger.log(Level.SEVERE, null, ex);
//                    }   
//                }
//            }
//                        
//            int messageWidth = fm.stringWidth(scrollingText);    
//            // System.out.println("message width: " + messageWidth);
//            
//            if (doubleLine == true || fourLine == true) 
//                 messageWidth = fm.stringWidth(maxMultiLine);   
//            
//            int resetX = 0 - messageWidth;
//            //System.out.println("resetx: " + resetX);
//          
//            if(x <= resetX) {  //this means we finished one loop of scrolling text
//                x = w;
//                //System.out.println("x reset: " + x);
//                loopScrollingTextCounter++;
//                 
//                 if (isLooping == true && loop99999FlagText == true && loopScrollingTextCounter == 0) {  //first of all, we must be in loop mode and if so have we finished all of our loops
//                    //let's let it continue indefinitely and not stop and check Q
//                    loopScrollingTextCounter = 0;
//                    loopTimesGlobal = 0;
//                    isLooping = false;
//                    loop99999FlagText = false; //reset the flag
//                    System.out.println("Clearing Queue and Playing Indefintately");
//                    PixelQueue.clear(); //let's clear the Q
//                }
//                
//                if (isLooping == true && loopScrollingTextCounter >= loopTimesGlobal) {  //first of all, we must be in loop mode and if so have we finished all of our loops
//                    //ok we're done looping so now we need to stop this current animation or scrolling text and then check the queue
//                    loopScrollingTextCounter = 0; //reset this one, we'll reset the rest in doneLoopCheckingQueue
//                    System.out.println("Done looping scrolling text, now checking Queue");
//                    doneLoopingCheckQueue();
//                } 
//                 
//            }
//            else
//            {
//                //x--;
//                //System.out.println("x: " + x);
//                x = x-scrollingTextMultiplier; //TO DO tweak this for windows scrolling
//            }
//          }
//        }
        
        
        
         public void cancel() {  //not using this right now
             
            //System.out.println("we got a cancel call:" + p);
        
         }
         
        
         void shutdown() {
           
        }
    }
}
