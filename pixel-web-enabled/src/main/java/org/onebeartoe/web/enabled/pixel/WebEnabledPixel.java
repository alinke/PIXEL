
package org.onebeartoe.web.enabled.pixel;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import ioio.lib.api.RgbLedMatrix;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.pc.IOIOConsoleApp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Timer;
import org.apache.commons.io.IOUtils;
import org.onebeartoe.io.TextFileReader;
import org.onebeartoe.pixel.PixelEnvironment;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.controllers.AnimationsHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.AnimationsListHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.IndexHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.PixelHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.ScrollingTextColorHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.ScrollingTextHttpHander;
import org.onebeartoe.web.enabled.pixel.controllers.ScrollingTextSpeedHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.StaticFileHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.StillImageHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.StillImageListHttpHandler;

/**
 * @author Roberto Marquez
 */
public class WebEnabledPixel
{
    protected Logger logger;

    private HttpServer server;

    private int httpPort;

    private CliPixel cli;
    
    private Timer searchTimer;
    
    private Pixel pixel;

    //  1: 32x16 from Sparkfun - ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x16
    //  3: translates to RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32;
    // 10: translates to SEEEDSTUDIO_64x64
    private final static int LED_MATRIX_ID = 3;
//TODO: We shoudl invert this and have teh user specicy the matrix label 
//      (SEEEDSTUDIO_64x64, Matrix.SEEEDSTUDIO_32x32, etc...) instead of an
//      integer ID.
//      The lable makes sense if user's are copying and pasting the commands, if not then
//      integer IDs makes sense, but is harder to maintain.        
    private static final PixelEnvironment pixelEnvironment = new PixelEnvironment(LED_MATRIX_ID);
    
    public final static RgbLedMatrix.Matrix MATRIX_TYPE = pixelEnvironment.LED_MATRIX;
    
//TODO: MAKE THIS PRIVATE    
    public List<String> stillImageNames;

//TODO: MAKE THIS PRIVATE    
    public List<String> animationImageNames;
    
    public WebEnabledPixel(String[] args)
    {
        cli = new CliPixel(args);
        cli.parse();
        httpPort = cli.getPort();

        String name = getClass().getName();
        logger = Logger.getLogger(name);
        
        pixel = new Pixel(pixelEnvironment.LED_MATRIX, pixelEnvironment.currentResolution);
        
        extractDefaultContent();
        
        loadImageLists();
        
        createControllers();
    }
        
    private void createControllers()
    {
        try
        {
            InetSocketAddress anyhost = new InetSocketAddress(httpPort);
            server = HttpServer.create(anyhost, 0);
            
            List<PixelHttpHandler> handlers = new ArrayList();
            
            PixelHttpHandler indexHttpHandler = new IndexHttpHandler();
            handlers.add(indexHttpHandler);
            
            PixelHttpHandler scrollingTextHttpHander = new ScrollingTextHttpHander();
            handlers.add(scrollingTextHttpHander);
            
            PixelHttpHandler scrollingTextSpeedHttpHander = new ScrollingTextSpeedHttpHandler();
            handlers.add(scrollingTextSpeedHttpHander);
            
            PixelHttpHandler scrollingTextColorHttpHandler = new ScrollingTextColorHttpHandler();
            handlers.add(scrollingTextColorHttpHandler);
            
            PixelHttpHandler staticFileHttpHandler = new StaticFileHttpHandler();
            handlers.add(staticFileHttpHandler);
            
            PixelHttpHandler stillImageHttpHandler = new StillImageHttpHandler() ;
            handlers.add(stillImageHttpHandler);
            
            PixelHttpHandler stillImageListHttpHandler = new StillImageListHttpHandler();
            handlers.add(stillImageListHttpHandler);            
            
            PixelHttpHandler animationsHttpHandler = new AnimationsHttpHandler();
            handlers.add(animationsHttpHandler);
            
            PixelHttpHandler animationsListHttpHandler = new AnimationsListHttpHandler();
            handlers.add(animationsListHttpHandler);

            for(PixelHttpHandler phh : handlers)
            {
                phh.setApp(this);
            }
            
// ARE WE GONNA DO ANYTHING WITH THE HttpContext OBJECTS?            
            HttpContext createContext =     server.createContext("/",     indexHttpHandler);
            
            HttpContext animationsContext = server.createContext("/animation", animationsHttpHandler);
                                            server.createContext("/animation/list", animationsListHttpHandler);

            HttpContext staticContent =     server.createContext("/files", staticFileHttpHandler);
            
            HttpContext  stillContext =     server.createContext("/still", stillImageHttpHandler);
                                            server.createContext("/still/list", stillImageListHttpHandler);
                                            
            HttpContext   textContext =     server.createContext("/text", scrollingTextHttpHander);
                                            server.createContext("/text/speed", scrollingTextSpeedHttpHander);
                                            server.createContext("/text/color", scrollingTextColorHttpHandler);
                                            
        } 
        catch (IOException ex)
        {
            String message = "An error occurred while creating the controllers";
            logger.log(Level.SEVERE, message, ex);
        }
    }
    
    public void extractAnimationImages() throws IOException
    {
        String animationsListFilesystemPath = pixel.getPixelHome() + "animations.text";
        File animationsListFile = new File(animationsListFilesystemPath);
        
        String pathPrefix = "animations/";
        String animationsListClasspath = "/animations.text";
        
        extractClasspathResourcesList(animationsListFile, animationsListClasspath, pathPrefix);        
    }
    
    private void extractDefaultContent()
    {
        try
        {
            extractHtmlAndJavascript();
                        
            extractStillImages();
            
            extractAnimationImages();
        } 
        catch (IOException ex)
        {
            logger.log(Level.SEVERE, "could not extract all default content", ex);
        }
    }
    
// AND CSS OR RENAME!    
    private void extractHtmlAndJavascript() throws IOException
    {
        String contentClasspath = "/web-content/";
        String inpath = contentClasspath + "index.html";

        String pixelHomePath = pixel.getPixelHome();
        File pixelHomeDirectory = new File(pixelHomePath);
            
        extractClasspathResource(inpath, pixelHomeDirectory);
        
        inpath = contentClasspath + "pixel.js";
        extractClasspathResource(inpath, pixelHomeDirectory);
        
        inpath = contentClasspath + "images.css";
        extractClasspathResource(inpath, pixelHomeDirectory);
    }
    
    private void extractStillImages() throws IOException
    {
        String imagesListFilesystemPath = pixel.getPixelHome() + "images.text";
        File imagesListFile = new File(imagesListFilesystemPath);
        
        String pathPrefix = "images/";
        String imagesListClasspath = "/images.text";
        
        extractClasspathResourcesList(imagesListFile, imagesListClasspath, pathPrefix);
    }

    private void extractClasspathResource(String classpath, File parentDirectory) throws IOException
    {
        InputStream instream = getClass().getResourceAsStream(classpath);

        if( !parentDirectory.exists() )
        {
            parentDirectory.mkdirs();
        }

        int i = classpath.lastIndexOf("/") + 1;
        String outname = classpath.substring(i);
        String outpath = parentDirectory.getAbsolutePath() + File.separator + outname;
        File outfile = new File(outpath);
        
//TODO: UNCOMMENT THIS IF/ELSE WHEN YOU ARE DONE TESTING, as is, the code extracts 
//      all files every run to make Web development faster.  That is, the existing 
//      files don't need not be removed to get the latest changes extracted.
//        if( outfile.exists() )
//        {
//            logger.log(Level.INFO, "Pixel app will not extract " + classpath + ".  It already exists.");
//        }
//        else
        {
            logger.log(Level.INFO, "Pixel app is extracting " + classpath);

            FileOutputStream fos = new FileOutputStream(outfile);
            IOUtils.copy(instream, fos);
        }
    }
    
    /**
     * This method only extracts the resources to the file system if the list file
     * does not exist on the filesystem.  This is to keep from extracting the default 
     * content every time the application runs.
     */
    private void extractClasspathResourcesList(File resourceListFile, 
                                                 String resourceListClasspath,
                                                 String pathPrefix) throws IOException
    {
        if(resourceListFile.exists() )
        {
            String message = "Pixel app will not extract the contents of " + resourceListClasspath
                        + ".  The list already exists at " + resourceListFile.getAbsolutePath();
            logger.log(Level.INFO, message);
        }
        else
        {
            // extract the list so on next run the app knows not to extract the default content
            extractClasspathResource(resourceListClasspath, resourceListFile);
            
            String outputDrectoryPath = pixel.getPixelHome() + pathPrefix;
            File outputDirectory = new File(outputDrectoryPath);
            
            TextFileReader tfr = new TextFileReader();
            List<String> imageNames = tfr.readTextLinesFromClasspath(resourceListClasspath);
            
            for(String name : imageNames)
            {
                String classpath = "/" + pathPrefix + name;
                
                System.out.println("Extracting " + classpath);
                
                extractClasspathResource(classpath, outputDirectory);
            }
        }
    }

    public Pixel getPixel()
    {
        return pixel;
    }
    
    private List<String> loadImageList(String directoryName) throws Exception
    {
        String dirPath = pixel.getPixelHome() + directoryName;
        File parent = new File(dirPath);
        
        List<String> namesList = new ArrayList();
        
        if( !parent.exists() || !parent.isDirectory() )
        {
            String message = "The directory is not valid:" +
                             dirPath + "\n" + 
                             "exists: " + parent.exists() + "\n" + 
                             "directory: " + parent.isDirectory();
            throw new Exception(message);
        }
        else
        {
            String [] names = parent.list( new FilenameFilter()
            {

                @Override
                public boolean accept(File dir, String name)
                {
                    return name.toLowerCase().endsWith(".png") || 
                            name.toLowerCase().endsWith(".gif");
                }
            });
            
            List<String> list = Arrays.asList(names);
            namesList.addAll(list);
        }
        
        return namesList;
    }
    
    private void loadImageLists()
    {
        try
        {        
            stillImageNames = loadImageList("images");
            animationImageNames = loadImageList("animations");
        } 
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "could not load image resources on the filesystem", ex);
        }
    }
    
    public static void main(String[] args)
    {
        WebEnabledPixel app = new WebEnabledPixel(args);
        app.startServer();
    }

    public void setPixel(Pixel pixel)
    {
        this.pixel = pixel;
    }  

    private void startSearchTimer()
    {
	int refreshDelay = 1000 * 12;  // in twelve seconds
        searchTimer = new Timer();
        
        TimerTask task = new SearchTimerTask();
       
	searchTimer.schedule(task, refreshDelay);
    }
        
    private void startServer()
    {
        startSearchTimer();
        
        server.start();
        
        PixelIntegration pi = new PixelIntegration();
//TODO: ONCE USING THE PIXelINTEGRATRION FROM PIXEL-COMMONS
//      CALL ITS addXxxxxListeners() methods
//      AND then its initialize() method
    }
    
    
    /**
     * @deprecated Use the version in pixel-commons from Alinke's github.com repository.
     */
    @Deprecated
    private class PixelIntegration extends IOIOConsoleApp
    {
        public PixelIntegration()
        {
            try
            {
                System.out.println("PixelIntegration is calling go()");
                
                go(null);
            } 
            catch (Exception ex)
            {
                String message = "Could not initialize Pixel: " + ex.getMessage();
                logger.log(Level.INFO, message);
            }
        }
        
        /**
         * can you belive this was what was not letting the app connect to the PIXEL?
         * @param args
         * @throws IOException 
         */
        @Override
        protected void run(String[] args) throws IOException 
        {
            System.out.println("now it begins!");
            
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(isr);
            boolean abort = false;
            String line;
            while (!abort && (line = reader.readLine()) != null) 
            {
                if (line.equals("t")) 
                {
                    //ledOn_ = !ledOn_;
                } 
                else if (line.equals("q")) {
                    abort = true;
                    System.exit(1);
                } 
                else 
                {
                    System.out.println("Unknown input. q=quit.");
                }
            }
        }

        @Override
        public IOIOLooper createIOIOLooper(String connectionType, Object extra)
        {
            IOIOLooper looper = new BaseIOIOLooper() 
            {
            
                @Override
                public void disconnected() 
                {
                    String message = "PIXEL was Disconnected";
                    System.out.println(message);
                }

                @Override
                public void incompatible() 
                {
                    String message = "Incompatible Firmware Detected";
                    System.out.println(message);
                }

                @Override
                protected void setup() throws ConnectionLostException, InterruptedException
                {
//                    pixel = new Pixel(pixelEnvironment.LED_MATRIX, pixelEnvironment.currentResolution);
                    pixel.matrix = ioio_.openRgbLedMatrix(pixel.KIND);
                    pixel.ioiO = ioio_;

                    StringBuilder message = new StringBuilder();
                    
                    if(pixel.matrix == null)
                    {
                        message.append("wtffff" + "\n");
                    }
                    else
                    {
                        message.append("Found PIXEL: " + pixel.matrix + "\n");
                    }
                    
                    
                    
                    message.append("You may now interact with the PIXEL!\n");

//TODO: Load something on startup

                    searchTimer.cancel(); //need to stop the timer so we don't still display the pixel searching message
                    
                    message.append("PIXEL Status: Connected");

                    logger.log(Level.INFO, message.toString());
                }
            };
                    
            return looper;
        }        
    }
        
    private class SearchTimerTask extends TimerTask
    {
	final long searchPeriodLength = 45 * 1000;
	
	final long periodStart;
	
	final long periodEnd;
	
	private int dotCount = 0;
	
	String message = "Searching for PIXEL";
	
	StringBuilder label = new StringBuilder(message);
	
	public SearchTimerTask()
	{
	    label.insert(0, "<html><body><h2>");
	    
	    Date d = new Date();
	    periodStart = d.getTime();
	    periodEnd = periodStart + searchPeriodLength;
	}
	
	public void run()
	{	    	    	    
	    if(dotCount > 10)
	    {
		label = new StringBuilder(message);
		label.insert(0, "<html><body><h2>");
		
		dotCount = 0;
	    }
	    else
	    {
		label.append('.');
	    }
	    dotCount++;
	    
	    Date d = new Date();
	    long now = d.getTime();
	    if(now > periodEnd)
	    {
		searchTimer.cancel();//stop();
                
		if(pixel == null || pixel.matrix == null)
		{
		    message = "A connection to PIXEL could not be established.";
		    String title = "PIXEL Connection Unsuccessful: ";
                    message = title + message;
                    logger.log(Level.SEVERE, message);
		}
                else
                {
                    logger.log(Level.INFO, "Looks like we have a PIXEL connection!");
                }
	    }
	}        
    }    
}
