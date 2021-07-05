package org.onebeartoe.web.enabled.pixel;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
//import ioio.lib.api.AnalogInput;
import ioio.lib.api.RgbLedMatrix;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.pc.IOIOConsoleApp;

import ioio.lib.api.SpiMaster; //for the LED Strip

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.PixelEnvironment;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.controllers.AnimationsHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.AnimationsListHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.ArcadeHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.PinballHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.ArcadeListHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.ClockHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.ConsoleHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.IndexHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.LCDPixelcade;
import org.onebeartoe.web.enabled.pixel.controllers.LocalModeHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.PinDMDHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.QuitHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.RandomModeHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.ScrollingTextColorHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.ScrollingTextHttpHander;
import org.onebeartoe.web.enabled.pixel.controllers.ScrollingTextScrollSmoothHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.ScrollingTextSpeedHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.StaticFileHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.StillImageHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.StillImageListHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.UploadHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.UploadOriginHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.UploadPlatformHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.ShutdownHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.UpdateHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.RebootHttpHandler;
import org.onebeartoe.web.enabled.pixel.controllers.CurrentGameHttpHandler;

import xyz.gianlu.zeroconf.*;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;


public class WebEnabledPixel implements ServiceListener {
    
    @Override
    public void serviceAdded(ServiceEvent event) {
      //System.out.println("Service added: " + event.getInfo());
      //System.out.println("Service Port: " + event.getInfo().getPort());
      embeddedLoc = event.getInfo().getServer().replace("._pixelcade._tcp","");
      embeddedLoc = embeddedLoc.substring(0, embeddedLoc.length() - 1);
      embeddedLoc = embeddedLoc.replace("PixelcadeLCD-","");
      //System.out.println("Service URL: " + embeddedLoc);
       System.out.println("Pixelcade LCD mDNS Detected: " + embeddedLoc);
       LogMe.aLogger.info("Pixelcade LCD mDNS Detected: " + embeddedLoc);
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
      System.out.println("Service removed: " + event.getInfo());
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
      //System.out.println("Service resolved: " + event.getDNS().getHostName());
      //event.getDNS().getHostName();
    }
    
  public String embeddedLoc = "pixelcadedx.local"; 
    
  public static boolean dxEnvironment = true;
  
  public static String pixelwebVersion = "3.6.1";
  
  public static LogMe logMe = null;
  
  private HttpServer server;
  
  private int httpPort;
  
  private CliPixel cli;
  
  private Timer searchTimer;
  
  private static Pixel pixel;
  
  private String ledResolution_ = "";
  
  private String playLastSavedMarqueeOnStartup_ = "yes";
  
  private static int LED_MATRIX_ID = 15;
  
  private static PixelEnvironment pixelEnvironment;
  
  public static RgbLedMatrix.Matrix MATRIX_TYPE;
  
  public static boolean silentMode_ = false;
  
  public List<String> stillImageNames;
  
  public List<String> animationImageNames;
  
  public List<String> arcadeImageNames;
  
  public static String OS = System.getProperty("os.name").toLowerCase();
  
  public static String port_ = null;
  
  private static String alreadyRunningErrorMsg = "";
  
  private static int speed_ = 10;
  
  private static long speed = 10L;
  
  private static boolean backgroundMode_ = false;
  
  private static boolean stayConnected = true;
  
  public static boolean pixelConnected = false;
  
  public static boolean rom2GameMappingExists = false;
  
  public static boolean consoleMappingExists = false;
  
  public static boolean gameMetaDataMappingExists = false;
  
  public static boolean consoleMetaDataMappingExists = false;
  
  public static HashMap<String, String> rom2NameMap = new HashMap<>();
  
  public static HashMap<String, String> consoleMap = new HashMap<>();
  
  public static HashMap<String, String> gameMetaDataMap = new HashMap<>();
  
  public static HashMap<String, String> consoleMetaDataMap = new HashMap<>();
  
  private String SubDisplayAccessory_ = "no";
  
  private String SubDisplayAccessoryPort_ = "COM99";
  
  private static String textColor_ = "random";
  
  private static Color  color = Color.RED;
  
  private static String textSpeed_ = "normal";
  
  private static String lcdMarquee_ = "no";
  
  private static String lcdMarqueeHostName_ = "pixelcadedx.local";
  
  private static String lcdMarqueeMessage_ = "Welcome to Pixelcade and Game On!";
  
  private static String defaultFont = "Arial Narrow 7";
  
  private static int defaultFontSize = 28;
  
  private static int defaultyTextOffset = 0;
  
  private static int  scrollsmooth_ = 1;
  
  private static int MaxAnimationVersions = 3;
  
  private static boolean NumAnimationVersionsFirstRunFlag = false;
  
  private static int AnimationVersionNumber = 3;
  
  public static boolean arduino1MatrixConnected = false;
  
  public static SerialPort arduino1MatrixPort;
  
  public static SerialPort arduino2OLED1Port;
  
  public static SerialPort arduino3OLED2Port;
  
  public static PrintWriter Arduino1MatrixOutput;

  //public static String pixelHome = System.getProperty("user.dir") + File.separator;
  
  //public static String pixelHome = "/home/pi/pixelcade/";
  
  //public static String pixelHome = System.getProperty("user.dir") + File.separator + "pixelcade" + File.separator; //this means "location of pixelcade resources, art, etc"
  
  public static String pixelHome = System.getProperty("user.home") + File.separator + "pixelcade" + File.separator; //this means "location of pixelcade resources, art, etc"
  
  public static LCDPixelcade lcdDisplay = null;
  
  //private boolean isALU = System.getenv().containsValue("pixelcade/jre11/bin/java");
  //private static boolean isALU = System.getenv("PATH").contains("pixelcade/jre11/bin");
  private static boolean isALU = isPathValid("pixelcade/jre11/bin");
  
  //private static boolean isMister_ = System.getenv("PATH").contains("/media/fat/Scripts");
  
  private static boolean isMister_ = isPathValid("/media/fat/Scripts");  //had to change to this as when adding pixelcade to startup , the environment path is not yet set so the above call will fail
  
  //for LED Strip
  
  public RGB[] frame_ = null;
  
  private int height_;
  
  private int width_;
        
  private double frequency_;
        
  private double fadeRate_;
        
  public RGB tempRGB_ = new RGB();
        
  private byte[] buffer1_ = new byte[48];
        
  private byte[] buffer2_ = new byte[48];
  
  private static int red = 0;
  
  private static int green = 0;
  
  private static int blue = 0;
  
  public static SpiMaster spi_;
  
  public static SpiMaster spi2_;
  
  private static String LEDStrip1_ = "no";
  
  private static Integer LEDStrip1NumberLEDs_ = 13;
  
  private static Integer LEDStrip1DataPin_ = 1;
  
  private static Integer LEDStrip1CLKPin_ = 2;
  
  private static String LEDStrip2_ = "no";
  
  private static Integer LEDStrip2NumberLEDs_ = 13;
  
  private static Integer LEDStrip2DataPin_ = 4;
  
  private static Integer LEDStrip2CLKPin_ = 5;
  
  private static Boolean LCDOnly = false;
  
  private static boolean aluInitMode_ = false;
  
  private static boolean easterEggMode_ = false;
  
  private static String currentGame = "NotSelectedYet";
   
  private static String currentPlatform = "NotSelectedYet";
  
  private static String LCDLEDCompliment_ = "yes";
  
  public WebEnabledPixel(String[] args) throws FileNotFoundException, IOException {
      
    this.cli = new CliPixel(args);
    this.cli.parse();
    this.httpPort = this.cli.getWebPort();
    silentMode_ = CliPixel.getSilentMode();
    backgroundMode_ = CliPixel.getBackgroundMode();
    easterEggMode_ = CliPixel.getEasterEggCheck();
    aluInitMode_ = CliPixel.getALUInitMode();
    logMe = LogMe.getInstance();
    
    if (!silentMode_) {
      LogMe.aLogger.info("Pixelcade Listener (pixelweb) Version " + pixelwebVersion);
      System.out.println("Pixelcade Listener (pixelweb) Version " + pixelwebVersion);
    }      
    
//    Map<String, String> map = System.getenv();  //shows the env variables available to us
//    map.entrySet().forEach(System.out::println);
    
    defaultyTextOffset = this.cli.getyTextOffset();
    LED_MATRIX_ID = this.cli.getLEDMatrixType();
    
    if (isWindows()) {
      alreadyRunningErrorMsg = "*** ERROR *** \nPixel Listener (pixelweb.exe) is already running\nYou don't need to launch it again\nYou may also want to add the Pixel Listener to your Windows Startup Folder";
    } else {
      alreadyRunningErrorMsg = "*** ERROR *** \nPixel Listener (pixelweb.jar) is already running\nYou don't need to launch it again\nYou may also want to add the Pixel Listener to your system.d startup";
    } 
   
    if (isWindows()) {
          pixelHome = System.getProperty("user.dir") + File.separator;  //user dir is the folder where pixelweb.jar lives and would be placed there by the windows installer
    } else if (isALU){   
            System.out.println("ALU Detected");
            pixelHome = "/opt/pixelcade/";
    }
      else if (isMister_) {
            System.out.println("MiSTer Detected");
            pixelHome = "/media/fat/pixelcade/";
    }
    
    File file = new File("settings.ini");
    if (file.exists() && !file.isDirectory()) {
      Ini ini = null;
      try {
        ini = new Ini(new File("settings.ini"));
        Config config = ini.getConfig();
        config.setStrictOperator(true);
        ini.setConfig(config);
      } catch (IOException ex) {
        LogMe.aLogger.log(Level.SEVERE, "could not load settings.ini", ex);
        if (!silentMode_)
          LogMe.aLogger.severe("Could not open settings.ini" + ex); 
      } 
      Profile.Section sec = (Profile.Section)ini.get("PIXELCADE SETTINGS");
      this.ledResolution_ = (String)sec.get("ledResolution");
      if (sec.containsKey("playLastSavedMarqueeOnStartup")) {
        this.playLastSavedMarqueeOnStartup_ = (String)sec.get("playLastSavedMarqueeOnStartup");
      } else {
        System.out.println("Creating key in settings.ini : playLastSavedMarqueeOnStartup");
        sec.add("playLastSavedMarqueeOnStartup", "yes");
        sec.add("playLastSavedMarqueeOnStartup_OPTION", "yes");
        sec.add("playLastSavedMarqueeOnStartup_OPTION", "no");
        sec.add("ledResolution_OPTION", "128x32C2");
        sec.put("userMessageGenericPlatform", "Writing Generic LED Marquee for Emulator");
        sec.put("hostAddress", "localhost");
      } 
      if (sec.containsKey("SubDisplayAccessory")) {
        this.SubDisplayAccessory_ = (String)sec.get("SubDisplayAccessory");
        this.SubDisplayAccessoryPort_ = (String)sec.get("SubDisplayAccessoryPort");
      } else {
        sec.add("SubDisplayAccessory", "no");
        sec.add("SubDisplayAccessory_OPTION", "yes");
        sec.add("SubDisplayAccessory_OPTION", "no");
        sec.add("SubDisplayAccessoryPort", "COM99");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM1");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM2");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM3");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM4");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM5");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM6");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM7");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM8");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM9");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM10");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM12");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM13");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM14");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM15");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM16");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM17");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM18");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM19");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM20");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM21");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM22");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM23");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM24");
        sec.add("SubDisplayAccessoryPort_OPTION", "COM25");
        ini.store();
      } 
      if (sec.containsKey("textColor")) {
        textColor_ = (String)sec.get("textColor");
        textSpeed_ = (String)sec.get("textSpeed");
      } else {
        sec.add("textColor", "random");
        sec.add("textColor_OPTION", "random");
        sec.add("textColor_OPTION", "red");
        sec.add("textColory_OPTION", "blue");
        sec.add("textColor_OPTION", "cyan");
        sec.add("textColory_OPTION", "gray");
        sec.add("textColor_OPTION", "darkgray");
        sec.add("textColory_OPTION", "green");
        sec.add("textColor_OPTION", "lightgray");
        sec.add("textColory_OPTION", "magenta");
        sec.add("textColor_OPTION", "orange");
        sec.add("textColory_OPTION", "pink");
        sec.add("textColor_OPTION", "yellow");
        sec.add("textColory_OPTION", "white");
        sec.add("textSpeed", "slow");
        sec.add("textSpeed_OPTION", "slow");
        sec.add("textSpeed_OPTION", "normal");
        sec.add("textSpeed_OPTION", "fast");
        ini.store();
      } 
      if (sec.containsKey("font")) {
        defaultFont = (String)sec.get("font");
        defaultFontSize = Integer.parseInt((String)sec.get("fontSize"));
        defaultyTextOffset = Integer.parseInt((String)sec.get("yTextOffset"));
      } else {
        sec.add("font", "Arial Narrow 7");
        sec.add("font_OPTION", "Arial Narrow 7");
        sec.add("font_OPTION", "Benegraphic");
        sec.add("font_OPTION", "Candy Stripe (BRK)");
        sec.add("font_OPTION", "Casio FX-702P");
        sec.add("font_OPTION", "Chlorinar");
        sec.add("font_OPTION", "Daddy Longlegs NF");
        sec.add("font_OPTION", "Decoder");
        sec.add("font_OPTION", "DIG DUG");
        sec.add("font_OPTION", "dotty");
        sec.add("font_OPTION", "DPComic");
        sec.add("font_OPTION", "Early GameBoy");
        sec.add("font_OPTION", "Fiddums Family");
        sec.add("font_OPTION", "Ghastly Panic");
        sec.add("font_OPTION", "Gnuolane");
        sec.add("font_OPTION", "Grapevine");
        sec.add("font_OPTION", "Grinched");
        sec.add("font_OPTION", "Handwriting");
        sec.add("font_OPTION", "Harry P");
        sec.add("font_OPTION", "Haunting Attraction");
        sec.add("font_OPTION", "Minimal4");
        sec.add("font_OPTION", "Morris Roman");
        sec.add("font_OPTION", "MostlyMono");
        sec.add("font_OPTION", "Neon 80s");
        sec.add("font_OPTION", "Nintendo DS BIOS");
        sec.add("font_OPTION", "Not So Stout Deco");
        sec.add("font_OPTION", "Paulistana Deco");
        sec.add("font_OPTION", "Pixelated");
        sec.add("font_OPTION", "Pixeled");
        sec.add("font_OPTION", "RetroBoundmini");
        sec.add("font_OPTION", "RM Typerighter medium");
        sec.add("font_OPTION", "Samba Is Dead");
        sec.add("font_OPTION", "Shlop");
        sec.add("font_OPTION", "Space Patrol NF");
        sec.add("font_OPTION", "Star Jedi Hollow");
        sec.add("font_OPTION", "Star Jedi");
        sec.add("font_OPTION", "Still Time");
        sec.add("font_OPTION", "Stint Ultra Condensed");
        sec.add("font_OPTION", "Tall Films Fine");
        sec.add("font_OPTION", "taller");
        sec.add("font_OPTION", "techno overload (BRK)");
        sec.add("font_OPTION", "TR2N");
        sec.add("font_OPTION", "TRON");
        sec.add("font_OPTION", "Vectroid");
        sec.add("font_OPTION", "Videophreak");
        sec.add("fontSize", "28");
        sec.add("fontSize_OPTION", "28");
        sec.add("fontSize_OPTION", "32");
        sec.add("fontSize_OPTION", "30");
        sec.add("fontSize_OPTION", "24");
        sec.add("fontSize_OPTION", "20");
        sec.add("fontSize_OPTION", "18");
        sec.add("fontSize_OPTION", "14");
        sec.add("yTextOffset", "0");
        ini.store();
      } 
      
      if (sec.containsKey("LCDMarqueeHostName")) {
        lcdMarqueeHostName_ = (String)sec.get("LCDMarqueeHostName");
      } else {
        sec.add("LCDMarqueeHostName", "pixelcadedx.local");
        ini.store();
      } 
      
      if (sec.containsKey("LCDMarquee")) {
        lcdMarquee_ = (String)sec.get("LCDMarquee");
      } else {
        sec.add("LCDMarquee", "no");
        sec.add("LCDMarquee_OPTION", "no");
        sec.add("LCDMarquee_OPTION", "yes");
        ini.store();
      } 
      
      if (sec.containsKey("LCDLEDCompliment")) {
        LCDLEDCompliment_ = (String)sec.get("LCDLEDCompliment");
      } else {
        sec.add("LCDLEDCompliment", "yes");
        sec.add("LCDLEDCompliment_OPTION", "yes");
        sec.add("LCDLEDCompliment_OPTION", "no");
        ini.store();
      } 
      
     if (sec.containsKey("LCDMarquee_Message")) {
        lcdMarqueeMessage_ = (String)sec.get("LCDMarquee_Message");
      } else {
        sec.add("LCDMarquee_Message", "Welcome to Pixelcade and Game On!");
        ini.store();
      } 
     
      if (sec.containsKey("MaxAnimationVersions")) {
            MaxAnimationVersions = Integer.parseInt((String)sec.get("MaxAnimationVersions"));
      } else {
        sec.add("MaxAnimationVersions", "3");
        ini.store();
      } 
      
      if (sec.containsKey("LEDStrip1")) {
            LEDStrip1_ = (String)sec.get("LEDStrip1");
            LEDStrip1NumberLEDs_ = Integer.parseInt((String)sec.get("LEDStrip1NumberLEDs"));
            LEDStrip1DataPin_ = Integer.parseInt((String)sec.get("LEDStrip1DataPin"));
            LEDStrip1CLKPin_ = Integer.parseInt((String)sec.get("LEDStrip1CLKPin"));
            LEDStrip2_ = (String)sec.get("LEDStrip2");
            LEDStrip2NumberLEDs_ = Integer.parseInt((String)sec.get("LEDStrip2NumberLEDs"));
            LEDStrip2DataPin_ = Integer.parseInt((String)sec.get("LEDStrip2DataPin"));
            LEDStrip2CLKPin_ = Integer.parseInt((String)sec.get("LEDStrip2CLKPin"));
      } else {
        sec.add("LEDStrip1", "no");
        sec.add("LEDStrip1_OPTION", "no");
        sec.add("LEDStrip1_OPTION", "yes");
        sec.add("LEDStrip1NumberLEDs", "13");
        sec.add("LEDStrip1DataPin", "1");
        sec.add("LEDStrip1CLKPin", "2");
        sec.add("LEDStrip2", "no");
        sec.add("LEDStrip2_OPTION", "no");
        sec.add("LEDStrip2_OPTION", "yes");
        sec.add("LEDStrip2NumberLEDs", "13");
        sec.add("LEDStrip2DataPin", "4");
        sec.add("LEDStrip2CLKPin", "5");
        ini.store();
      } 
      
     //let's test if the LCD (pixelcadedx.local) is there and turn on if so
     //some users turned on LCD in settings but they only had LED making things slower, this call will take care of that
     
//       try {
//            if (InetAddress.getByName(lcdMarqueeHostName_).isReachable(5000)){
//                lcdMarquee_ = "yes";
//                System.out.println("PixelcadeLCD detected on startup: " + lcdMarqueeHostName_);
//                LogMe.aLogger.info("PixelcadeLCD detected on startup: " + lcdMarqueeHostName_);
//             } else
//             {
//                lcdMarquee_ = "no"; 
//                System.out.println("PixelcadeLCD not detected on startup: " + lcdMarqueeHostName_);
//                LogMe.aLogger.info("PixelcadeLCD not detected on startup: " + lcdMarqueeHostName_);
//             } 
//       } catch (  Exception e){}
      

if (lcdMarquee_.equals("yes") && lcdDisplay != null) {
	if(lcdDisplay == null) lcdDisplay = new LCDPixelcade();
        try {
          Font temp = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(pixelHome + "fonts/" + defaultFont + ".ttf"));
          lcdDisplay.setLCDFont(temp.deriveFont(244f),defaultFont + ".ttf");
          //lcdDisplay.windowsLCD.marqueeFrame.setFont(temp.deriveFont(244f));
        }catch (FontFormatException|IOException| NullPointerException e){
          System.out.println("Could not set lcd font from: " + pixelHome + "fonts/" + defaultFont + ".ttf" +"   (...\n");
        }
    }      
      if (this.ledResolution_.equals("128x32")) {
        if (!silentMode_) {
          System.out.println("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
          LogMe.aLogger.info("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
        } 
        LED_MATRIX_ID = 15;
      } 
      if (this.ledResolution_.equals("64x32")) {
        if (!silentMode_) {
          System.out.println("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
          LogMe.aLogger.info("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
        } 
        LED_MATRIX_ID = 13;
      } 
      if (this.ledResolution_.equals("32x32")) {
        if (!silentMode_) {
          System.out.println("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
          LogMe.aLogger.info("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
        } 
        LED_MATRIX_ID = 11;
      } 
      if (this.ledResolution_.equals("64x64")) {
        if (!silentMode_) {
          System.out.println("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
          LogMe.aLogger.info("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
        } 
        LED_MATRIX_ID = 14;
      } 
      if (this.ledResolution_.equals("64x32C")) {
        if (!silentMode_) {
          System.out.println("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
          LogMe.aLogger.info("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
        } 
        LED_MATRIX_ID = 24;
      } 
      if (this.ledResolution_.equals("64x64C")) {
        if (!silentMode_) {
          System.out.println("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
          LogMe.aLogger.info("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
        } 
        LED_MATRIX_ID = 25;
      } 
      if (this.ledResolution_.equals("64x32C2")) {
        if (!silentMode_) {
          System.out.println("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
          LogMe.aLogger.info("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
        } 
        LED_MATRIX_ID = 26;
      } 
      if (this.ledResolution_.equals("128x32C2")) {
        if (!silentMode_) {
          System.out.println("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
          LogMe.aLogger.info("PIXEL resolution found in settings.ini: resolution=" + this.ledResolution_);
        } 
        LED_MATRIX_ID = 27;
      } 
    } 
    pixelEnvironment = new PixelEnvironment(LED_MATRIX_ID);
    MATRIX_TYPE = pixelEnvironment.LED_MATRIX;
    pixel = new Pixel(pixelEnvironment.LED_MATRIX, pixelEnvironment.currentResolution);
    switch (LED_MATRIX_ID) {
      case 11:
        speed_ = 38;
        break;
      case 13:
        speed_ = 18;
        break;
      case 14:
        speed = 10L;
        break;
      case 15:
        speed_ = 10;
        break;
      case 24:
        speed_ = 18;
        break;
      case 25:
        speed = 10L;
        break;
      case 26:
        speed_ = 18;
        break;
      case 27:
        speed_ = 10;
        break;
      default:
        speed_ = 38;
        break;
    } 
    Pixel.setYOffset(defaultyTextOffset);
    Pixel.setFontSize(defaultFontSize);
    pixel.setScrollDelay(speed_);
    pixel.setScrollTextColor(Color.red);
    
    if (!silentMode_) {
      LogMe.aLogger.info("Pixelcade HOME DIRECTORY: " + pixelHome); 
      System.out.println("Pixelcade HOME DIRECTORY: " + pixelHome);
    }  
    //extractDefaultContent();  //saving space by removing this as the retropie installer now includes all these files so no need to include here and make the .jar bigger
    
 
     // Create a JmDNS instance, we'll use this to auto-detect Pixelcade LCD on the network using Bonjour mDNS
//      Thread thread = new Thread(() -> {
//        JmDNS jmdns = null;
//        try {
//          jmdns = JmDNS.create(InetAddress.getLocalHost());
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//
//        // Add a service listener
//        jmdns.addServiceListener("_pixelcade._tcp.local.", this);
//
//        // Wait a bit
//        try {
//          Thread.sleep(10000);
//        } catch (InterruptedException e) {
//        }
//
//        Thread.currentThread().interrupt();
//      });
//      thread.start();
    
    
    createControllers();
    
    File mamefile = new File("mame.csv");
    if (mamefile.exists() && !mamefile.isDirectory()) {
      rom2GameMappingExists = true;
      String filePath = "mame.csv";
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",", 2);
        if (parts.length >= 2) {
          String key = parts[0];
          String value = parts[1];
          rom2NameMap.put(key, value);
          continue;
        } 
        System.out.println("ignoring line in mame.csv: " + line);
      } 
      reader.close();
    } else {
      System.out.println("mame.csv not found");
    } 
    File consolefile = new File("console.csv");
    if (consolefile.exists() && !consolefile.isDirectory()) {
      consoleMappingExists = true;
      String filePath = "console.csv";
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",", 2);
        if (parts.length >= 2) {
          String key = parts[0];
          String value = parts[1];
          consoleMap.put(key, value);
          continue;
        } 
        System.out.println("ignoring line in console.csv: " + line);
      } 
      reader.close();
    } else {
      System.out.println("console.csv not found");
    } 
    File gameMetaData_ = new File("gamemetadata.csv");
    if (gameMetaData_.exists() && !gameMetaData_.isDirectory()) {
      gameMetaDataMappingExists = true;
      String filePath = "gamemetadata.csv";
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",", 2);
        if (parts.length >= 2) {
          String key = parts[0];
          String value = parts[1];
          gameMetaDataMap.put(key, value);
          continue;
        } 
        System.out.println("ignoring line in gamemetadata.csv: " + line);
      } 
      reader.close();
    } else {
      System.out.println("gamemetadata.csv not found");
    } 
    File consoleMetaData_ = new File("consolemetadata.csv");
    if (consoleMetaData_.exists() && !consoleMetaData_.isDirectory()) {
      consoleMetaDataMappingExists = true;
      String filePath = "consolemetadata.csv";
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split("@", 2); //note switched over to this separate for LCD so changing here too
        if (parts.length >= 2) {
          String key = parts[0];
          String value = parts[1];
          consoleMetaDataMap.put(key, value);
          continue;
        } 
        System.out.println("ignoring line in consolemetadata.csv: " + line);
      } 
      reader.close();
    } else {
      System.out.println("consolemetadata.csv not found");
    }

    if (lcdMarquee_.equals("yes")) {
      LCDPixelcade lcdDisplay = new LCDPixelcade();
	try {
          Font temp = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(pixelHome + "fonts/" + defaultFont + ".ttf"));
          lcdDisplay.setLCDFont(temp.deriveFont(244f),defaultFont + ".ttf");
          //lcdDisplay.windowsLCD.marqueeFrame.setFont(temp.deriveFont(244f));
        }catch (FontFormatException|IOException| NullPointerException e){
          System.out.println("Could not set lcd font from: " + pixelHome + "fonts/" + defaultFont + ".ttf" +" :(...\n");
        }
    }
    
    if (this.SubDisplayAccessory_.equals("yes")) {
      System.out.println("Attempting to connect to Pixelcade Sub Display Accessory...");
      arduino1MatrixPort = SerialPort.getCommPort(this.SubDisplayAccessoryPort_);
      arduino1MatrixPort.setComPortTimeouts(4096, 0, 0);
      arduino1MatrixPort.setBaudRate(57600);
      if (!arduino1MatrixPort.openPort()) {
        try {
          throw new Exception("Serial port \"" + this.SubDisplayAccessoryPort_ + "\" could not be opened.");
        } catch (Exception e) {
          e.printStackTrace();
        } 
      } else {
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException e1) {
          e1.printStackTrace();
        } 
        MessageListener listener = new MessageListener();
        arduino1MatrixPort.addDataListener((SerialPortDataListener)listener);
        try {
          Thread.sleep(1000L);
        } catch (Exception e) {
          e.printStackTrace();
        } 
        Arduino1MatrixOutput = new PrintWriter(arduino1MatrixPort.getOutputStream());
        Arduino1MatrixOutput.print("pixelcadeh\n");
        Arduino1MatrixOutput.flush();
      } 
    } 
    
    //this is a shutdown listener that we'll want to use if we have LCD to do a graceful shutdown when the JVM is terminated
    
//    if (lcdMarquee_.equals("yes")) {
//    
//        Runtime.getRuntime().addShutdownHook(
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("Graceful shutdown command sent to PixelcadeLCD");
//                LogMe.aLogger.info("Graceful shutdown command sent to PixelcadeLCD");
//                // this is executed on JVM shut-down
//                URL url = null;
//                try {
//                    url = new URL("http://" + lcdMarqueeHostName_ + ":8080/shutdown");
//                } catch (MalformedURLException ex) {
//                    Logger.getLogger(WebEnabledPixel.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                 HttpURLConnection con = null;
//                try {
//                    con = (HttpURLConnection) url.openConnection();
//                } catch (IOException ex) {
//                    Logger.getLogger(WebEnabledPixel.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                try {
//                    con.setRequestMethod("GET");
//                } catch (ProtocolException ex) {
//                    Logger.getLogger(WebEnabledPixel.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                try {
//                    con.getResponseCode();
//                } catch (IOException ex) {
//                    Logger.getLogger(WebEnabledPixel.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                 con.disconnect();
//            }
//        }));
//    }
    
    
  }
  
  private void createControllers()
    {
        try
        {
            InetSocketAddress anyhost = new InetSocketAddress(httpPort);
            server = HttpServer.create(anyhost, 0);
            //server.setExecutor(Executors.newFixedThreadPool(50));
            server.setExecutor(Executors.newSingleThreadExecutor());
            
            HttpHandler indexHttpHandler = new IndexHttpHandler();
            
            HttpHandler scrollingTextHttpHander = new ScrollingTextHttpHander(this);
                
            HttpHandler scrollingTextSpeedHttpHander = new ScrollingTextSpeedHttpHandler(this);
            
            HttpHandler scrollingTextScrollSmoothHttpHandler = new ScrollingTextScrollSmoothHttpHandler(this);
            
            HttpHandler scrollingTextColorHttpHandler = new ScrollingTextColorHttpHandler(this);
            
            HttpHandler staticFileHttpHandler = new StaticFileHttpHandler(this);
            
            HttpHandler stillImageHttpHandler = new StillImageHttpHandler(this) ;
            
            HttpHandler stillImageListHttpHandler = new StillImageListHttpHandler(this);
            
            HttpHandler animationsHttpHandler = new AnimationsHttpHandler(this);
            
            HttpHandler randomModeHttpHandler = new RandomModeHttpHandler(this);
            
            HttpHandler animationsListHttpHandler = new AnimationsListHttpHandler(this);
            
            HttpHandler arcadeListHttpHandler = new ArcadeListHttpHandler(this);
            
            HttpHandler consoleListHttpHandler = new ConsoleHttpHandler(this);

            HttpHandler uploadHttpHandler = new UploadHttpHandler(this);
            
            HttpHandler uploadPlatformHttpHandler = new UploadPlatformHttpHandler(this);
            
            HttpHandler uploadOriginHttpHandler = new UploadOriginHttpHandler( (UploadHttpHandler) uploadHttpHandler);
            
            HttpHandler clockHttpHandler = new ClockHttpHandler(this);
            
            HttpHandler arcadeHttpHandler = new ArcadeHttpHandler(this);
            
            HttpHandler pinballHttpHandler = new PinballHttpHandler(this);
            
            HttpHandler pindmdHttpHandler = new PinDMDHttpHandler(this);
            
            HttpHandler quitHttpHandler = new QuitHttpHandler(this);
            
            HttpHandler localModeHttpHandler = new LocalModeHttpHandler(this);
            
            HttpHandler updateHttpHandler = new UpdateHttpHandler(this);
            
            HttpHandler shutdownHttpHandler = new ShutdownHttpHandler(this);
            
            HttpHandler rebootHttpHandler = new RebootHttpHandler(this);
            
            HttpHandler currentGameHttpHandler = new CurrentGameHttpHandler();
          
            
            // ARE WE GONNA DO ANYTHING WITH THE HttpContext OBJECTS?   
            
            HttpContext createContext =     server.createContext("/", indexHttpHandler);
                                            server.createContext("/currentgame", currentGameHttpHandler);
            
            
            HttpContext animationsContext = server.createContext("/animations", animationsHttpHandler);
                                            server.createContext("/animations/list", animationsListHttpHandler);
                                            server.createContext("/animations/save", animationsListHttpHandler);
                                            
            HttpContext arcadeContext =     server.createContext("/arcade", arcadeHttpHandler);
                                            server.createContext("/pinball", pinballHttpHandler);
                                            server.createContext("/quit", quitHttpHandler);
                                            server.createContext("/shutdown", shutdownHttpHandler);
                                            server.createContext("/reboot", rebootHttpHandler);
                                            server.createContext("/update", updateHttpHandler);
                                            server.createContext("/arcade/list", arcadeListHttpHandler);
                                            server.createContext("/console", consoleListHttpHandler);
                                            server.createContext("/localplayback",localModeHttpHandler);
            
            HttpContext pindmdContext =     server.createContext("/dmd", pindmdHttpHandler);

            HttpContext staticContent =     server.createContext("/files", staticFileHttpHandler);
            
            HttpContext  stillContext =     server.createContext("/still", stillImageHttpHandler);
                                            server.createContext("/still/list", stillImageListHttpHandler);
                                            
                                            
            HttpContext   textContext =     server.createContext("/text", scrollingTextHttpHander);
                                            server.createContext("/text/speed", scrollingTextSpeedHttpHander);
                                            server.createContext("/text/scrollsmooth", scrollingTextScrollSmoothHttpHandler);
                                            server.createContext("/text/color", scrollingTextColorHttpHandler);
                                            
            HttpContext uploadContext =     server.createContext("/upload", uploadHttpHandler);
                                            server.createContext("/uploadplatform", uploadPlatformHttpHandler);
                                            server.createContext("/upload/origin", uploadOriginHttpHandler);
            
            HttpContext clockContext =      server.createContext("/clock", clockHttpHandler);
            
            HttpContext randomContext =      server.createContext("/random", randomModeHttpHandler);
                                            
        } 
        catch (IOException ex)
        {
            //if we got here, most likely the pixel listener was already running so let's give a message and then exit gracefully
            
             System.out.println(alreadyRunningErrorMsg);
             //System.out.println("Exiting...");
             
             // took this out as the pop up is no good when pinball dmdext also running as this interrupts
             /* if (isWindows() || isMac()) {  //we won't have xwindows on the Pi so skip this for the Pi
            
                JFrame frame = new JFrame("JOptionPane showMessageDialog example");  //let's show a pop up too so the user doesn't miss it
                JOptionPane.showMessageDialog(frame,
                   alreadyRunningErrorMsg,
                   "Pixelcade Listener Already Running",
                   JOptionPane.ERROR_MESSAGE);
             } */
        
             System.exit(1);    //we can't continue because the pixel listener is already running
        }
    }
  
  public static String getGameName(String romName) {
    String GameName = "";
    romName = romName.trim();
    romName = romName.toLowerCase();
    if (rom2GameMappingExists) {
      if (rom2NameMap.containsKey(romName)) {
        GameName = rom2NameMap.get(romName);
      } else {
        GameName = "nomatch";
      } 
    } else {
      GameName = "nomatch";
    } 
    return GameName;
  }
  
  public static String getGameMetaData(String romName) {
    String GameMetaData = "";
    romName = romName.trim();
    romName = romName.toLowerCase();
    System.out.println("ROM Name: " + romName);
    LogMe.aLogger.info("ROM Name: " + romName);
    if (gameMetaDataMappingExists) {
      if (gameMetaDataMap.containsKey(romName)) {
        GameMetaData = gameMetaDataMap.get(romName);
      } else {
        GameMetaData = romName + "%0000%Manufacturer Unknown%Genre Unknown%Rating Unknown";
      } 
    } else {
      GameMetaData = romName + "%0000%Manufacturer Unknown%Genre Unknown%Rating Unknown";
    } 
    if (GameMetaData.length() > 90)
      GameMetaData = GameMetaData.substring(0, Math.min(GameMetaData.length(), 90)); 
    System.out.println("Game Metadata: " + GameMetaData);
    LogMe.aLogger.info("Game Metadata: " + GameMetaData);
    return GameMetaData;
  }
  
  public static String getConsoleMetaData(String console) {
    String ConsoleMetaData = "";
    console = console.trim();
    console = console.toLowerCase();
    System.out.println("Console: " + console);
    LogMe.aLogger.info("Console: " + console);
    if (consoleMetaDataMappingExists) {
      if (consoleMetaDataMap.containsKey(console)) {
        ConsoleMetaData = consoleMetaDataMap.get(console);
      } else {
        ConsoleMetaData = console + "%0000%Manufacturer Unknown%Units Sold Unknown%CPU Unknown";
      } 
    } else {
      ConsoleMetaData = console + "%0000%Manufacturer Unknown%Units Sold Unknown%CPU Unknown";
    } 
    if (ConsoleMetaData.length() > 90)
      ConsoleMetaData = ConsoleMetaData.substring(0, Math.min(ConsoleMetaData.length(), 90)); 
    System.out.println("Game Metadata: " + ConsoleMetaData);
    LogMe.aLogger.info("Game Metadata: " + ConsoleMetaData);
    return ConsoleMetaData;
  }
  
  public static String getConsoleMapping(String originalConsole) {
    String ConsoleMapped = "";
    if (consoleMappingExists) {
      if (consoleMap.containsKey(originalConsole)) {
        ConsoleMapped = consoleMap.get(originalConsole);
      } else {
        ConsoleMapped = originalConsole;
      } 
    } else {
      ConsoleMapped = getConsoleNamefromMapping(originalConsole);
      System.out.println("console.csv file NOT FOUND");
      LogMe.aLogger.info("console.csv file NOT FOUND");
    } 
    return ConsoleMapped;
  }
  
  public static int getMatrixID() {
    return LED_MATRIX_ID;
  }
  
  public static String getTextColor() {
    return textColor_;
  }
  
  public static String getTextScrollSpeed() {
    return textSpeed_;
  }
  
  public static String getDefaultFont() {
    return defaultFont;
  }
  
  public static String getLCDMarquee() {
    return lcdMarquee_;
  }
  
  public static String getLCDMarqueeHostName() {
    return lcdMarqueeHostName_;
  }
  
  public static int getDefaultFontSize() {
    return defaultFontSize;
  }
  
  public static int getDefaultyTextOffset() {
    return defaultyTextOffset;
  }
  
  public static Boolean getLCDLEDCompliment() {
     Boolean LCDLEDComplimentBool_;
     if (LCDLEDCompliment_.equals("yes")) {
         LCDLEDComplimentBool_ = true;
     }
     else {
         LCDLEDComplimentBool_ = false;
     }
      return LCDLEDComplimentBool_;
  }
  
  public static Boolean LEDStripExists() {
      Boolean LEDStripExists_ = false;
      if (LEDStrip1_.equals("yes") || LEDStrip2_.equals("yes")) {
          LEDStripExists_ = true;
      }
      else {
           LEDStripExists_ = false;
      }
      return LEDStripExists_;
  }
  
//  public static Integer GetLEDStrip1NumberLEDs() {
//      return LEDStrip1NumberLEDs_;
//  }
  
  
  public static void setLEDStripColor(int r, int g, int b) {  //red and blue are switched
  //public static void setLEDStripColor(int b, int g, int r) {  //red and blue are switched
				//if (r > 127) r = 127;
                                //if (g > 127) g = 127;
                                //if (b > 127) b = 127;
                                red = r;
                                green = g;
                                blue = b;
   }
  
   public static void setCurrentPlatformGame(String Platform, String Game) {  //used by API to get current platform and game
       currentPlatform = Platform;                       
       currentGame = Game;
   }
   
    public static String getCurrentPlatformGame() {  //used by API to get current platform and game
       String PlatformGame = currentPlatform + "," + currentGame;
       return PlatformGame;
   }
  
  public static long getScrollingTextSpeed(int LED_MATRIX_ID) {
    switch (LED_MATRIX_ID) {
      case 11:
        speed = 38L;
        return speed;
      case 13:
        speed = 18L;
        return speed;
      case 14:
        speed = 10L;
        return speed;
      case 15:
        speed = 10L;
        return speed;
      case 24:
        speed_ = 18;
        return speed;
      case 25:
        speed = 10L;
        return speed;
      case 26:
        speed = 18L;
        return speed;
      case 27:
        speed = 10L;
        return speed;
    } 
    speed = 38L;
    return speed;
  }
  
  public static int getScrollingSmoothSpeed(String speedfromSettings) {
    switch (speedfromSettings) {
      case "slow":
        scrollsmooth_ = 1;
        return scrollsmooth_;
      case "normal":
        scrollsmooth_ = 3;
        return scrollsmooth_;
      case "fast":
        scrollsmooth_ = 5;
        return scrollsmooth_;
    } 
    int scrollsmooth_ = 3;
    return scrollsmooth_;
  }
  
  public static Color getRandomColor() {
    Random randomGenerator = new Random();
    int randomInt = randomGenerator.nextInt(11) + 1;
    switch (randomInt) {
      case 1:
        color = Color.RED;
        return color;
      case 2:
        color = Color.BLUE;
        return color;
      case 3:
        color = Color.CYAN;
        return color;
      case 4:
        color = Color.GRAY;
        return color;
      case 5:
        color = Color.DARK_GRAY;
        return color;
      case 6:
        color = Color.GREEN;
        return color;
      case 7:
        color = Color.LIGHT_GRAY;
        return color;
      case 8:
        color = Color.MAGENTA;
        return color;
      case 9:
        color = Color.ORANGE;
        return color;
      case 10:
        color = Color.PINK;
        return color;
      case 11:
        color = Color.YELLOW;
        return color;
      case 12:
        color = Color.WHITE;
        return color;
    } 
    Color color = Color.MAGENTA;
    return color;
  }
  
  public static Color hex2Rgb(String colorStr) {
    return new Color(
        Integer.valueOf(colorStr.substring(0, 2), 16).intValue(), 
        Integer.valueOf(colorStr.substring(2, 4), 16).intValue(), 
        Integer.valueOf(colorStr.substring(4, 6), 16).intValue());
  }
  
  public static boolean isHexadecimal(String input) {
    Pattern HEXADECIMAL_PATTERN = Pattern.compile("\\p{XDigit}+");
    Matcher matcher = HEXADECIMAL_PATTERN.matcher(input);
    return matcher.matches();
  }
  
  public static Color getColorFromHexOrName(String ColorStr) {
    Color color;
    if (isHexadecimal(ColorStr) && ColorStr.length() == 6) {
      color = hex2Rgb(ColorStr);
      if (!CliPixel.getSilentMode())
        System.out.println("Hex color value detected"); 
    } else {
      switch (ColorStr) {
        case "red":
          color = Color.RED;
          return color;
        case "blue":
          color = Color.BLUE;
          return color;
        case "cyan":
          color = Color.CYAN;
          return color;
        case "gray":
          color = Color.GRAY;
          return color;
        case "darkgray":
          color = Color.DARK_GRAY;
          return color;
        case "green":
          color = Color.GREEN;
          return color;
        case "lightgray":
          color = Color.LIGHT_GRAY;
          return color;
        case "magenta":
          color = Color.MAGENTA;
          return color;
        case "orange":
          color = Color.ORANGE;
          return color;
        case "pink":
          color = Color.PINK;
          return color;
        case "yellow":
          color = Color.YELLOW;
          return color;
        case "white":
          color = Color.WHITE;
          return color;
      } 
      color = Color.RED;
      if (!CliPixel.getSilentMode())
        System.out.println("Invalid color, defaulting to red"); 
    } 
    return color;
  }
  
  public static String getHome() {
      return pixelHome;
  }
  
  public static boolean isPathValid(String path) {
      
        File f = new File(path);
        if (f.exists() && f.isDirectory()) {
           return true;
        }
        else {
            return false;
        } 
            

//    try {
//
//        Paths.get(path);
//
//    } catch (InvalidPathException ex) {
//        return false;
//    }
//
//    return true;
  }
  
  public static Integer getAnimationNumber() {
     
      if (NumAnimationVersionsFirstRunFlag == false) { //then this was the first run so set to the max
          AnimationVersionNumber = MaxAnimationVersions;
      } else {   
            AnimationVersionNumber--;
            if (AnimationVersionNumber == 0) { //then the last name , so now reset to the max
                AnimationVersionNumber = MaxAnimationVersions;
            } 
      }
      NumAnimationVersionsFirstRunFlag = true; //set the first run flag
      return AnimationVersionNumber;
  }
  
  public static boolean isMister() {
      return isMister_;
  }
  
  public static boolean isWindows() {
    return (OS.indexOf("win") >= 0);
  }
  
  public static boolean isMac() {
    return (OS.indexOf("mac") >= 0);
  }
  
  public static boolean isUnix() {
    return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
  }
  
  public static void setLocalMode() {
    pixel.playLocalMode();
  }
  
  public Pixel getPixel() {
    return pixel;
  }
  
  public List<String> loadAnimationList() {
    try {
      this.animationImageNames = loadImageList("animations");
    } catch (Exception ex) {
      LogMe.aLogger.log(Level.SEVERE, "could not load image resources on the filesystem", ex);
    } 
    return this.animationImageNames;
  }
  
  public List<String> loadArcadeList() {
    try {
      this.arcadeImageNames = loadImageList("mame");
    } catch (Exception ex) {
      LogMe.aLogger.log(Level.SEVERE, "could not load image resources on the filesystem", ex);
    } 
    return this.arcadeImageNames;
  }
  
  private List<String> loadImageList(String directoryName) throws Exception {
    String dirPath = pixelHome + directoryName;
    File parent = new File(dirPath);
    List<String> namesList = new ArrayList<>();
    if (!parent.exists() || !parent.isDirectory()) {
      String message = "The directory is not valid:" + dirPath + "\nexists: " + parent.exists() + "\ndirectory: " + parent.isDirectory();
      throw new Exception(message);
    } 
    String[] names = parent.list(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return (name.toLowerCase().endsWith(".png") || name
              .toLowerCase().endsWith(".gif"));
          }
        });
    List<String> list = Arrays.asList(names);
    namesList.addAll(list);
    Collections.sort(namesList);
    return namesList;
  }
  
  public List<String> loadImageLists() {
    try {
      this.stillImageNames = loadImageList("images");
    } catch (Exception ex) {
      LogMe.aLogger.log(Level.SEVERE, "could not load image resources on the filesystem", ex);
    } 
    return this.stillImageNames;
  }
  
  public static void main(String[] args) throws IOException {
    WebEnabledPixel app = new WebEnabledPixel(args);
    app.startServer();
  }
  
 public void setPixel(Pixel pixel)
    {
        this.pixel = pixel;
    }  
  
  private void startSearchTimer() {
    int refreshDelay = 12000;
    this.searchTimer = new Timer();
    TimerTask task = new SearchTimerTask();
    this.searchTimer.schedule(task, refreshDelay);
  }
  
  private void startServer() {
    startSearchTimer();
    this.server.start();
    PixelIntegration pi = new PixelIntegration();
  }
  
  public static void writeArduino1Matrix(String Arduino1MatrixText) {
    Arduino1MatrixOutput.print(Arduino1MatrixText + "\n");
    Arduino1MatrixOutput.flush();
  }
   
  public static String getConsoleNamefromMapping(String originalConsoleName) {
    String consoleNameMapped = null;
    originalConsoleName = originalConsoleName.toLowerCase();
    switch (originalConsoleName) {
      case "atari-2600":
        consoleNameMapped = "atari2600";
        return consoleNameMapped;
      case "atari_2600":
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
      case "nintendo_entertainment_system":
        consoleNameMapped = "nes";
        return consoleNameMapped;
      case "nintendo 64":
        consoleNameMapped = "n64";
        return consoleNameMapped;
      case "nintendo_64":
        consoleNameMapped = "n64";
        return consoleNameMapped;
      case "sony playstation":
        consoleNameMapped = "psx";
        return consoleNameMapped;
      case "sony_playstation":
        consoleNameMapped = "psx";
        return consoleNameMapped;
      case "sony playstation 2":
        consoleNameMapped = "ps2";
        return consoleNameMapped;
      case "sony_playstation_2":
        consoleNameMapped = "ps2";
        return consoleNameMapped;
      case "sony pocketstation":
        consoleNameMapped = "psp";
        return consoleNameMapped;
      case "sony psp":
        consoleNameMapped = "psp";
        return consoleNameMapped;
      case "sony_psp":
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
      case "atari_5200":
        consoleNameMapped = "atari5200";
        return consoleNameMapped;
      case "atari 7800":
        consoleNameMapped = "atari7800";
        return consoleNameMapped;
      case "atari_7800":
        consoleNameMapped = "atari7800";
        return consoleNameMapped;
      case "atari jaguar":
        consoleNameMapped = "atarijaguar";
        return consoleNameMapped;
      case "atari_jaguar":
        consoleNameMapped = "atarijaguar";
        return consoleNameMapped;
      case "atari jaguar cd":
        consoleNameMapped = "atarijaguar";
        return consoleNameMapped;
      case "atari lynx":
        consoleNameMapped = "atarilynx";
        return consoleNameMapped;
      case "atari_lynx":
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
    } 
    consoleNameMapped = originalConsoleName;
    return consoleNameMapped;
  }
  
  @Deprecated
  private class PixelIntegration extends IOIOConsoleApp {
    public PixelIntegration() {
      try {
        if (!WebEnabledPixel.silentMode_)
          System.out.println("PixelIntegration is calling go()"); 
        go(null);
      } catch (Exception ex) {
        String message = "Could not initialize Pixel: " + ex.getMessage();
        LogMe.aLogger.info(message);
      } 
    }
    
    protected void run(String[] args) throws IOException {
      if (WebEnabledPixel.backgroundMode_) {
        while (WebEnabledPixel.stayConnected) {
          long duration = 60000L;
          try {
            Thread.sleep(duration);
          } catch (InterruptedException ex) {
            String str = "Error sleeping for Pixel initialization: " + ex.getMessage();
          } 
        } 
      } else {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(isr);
        boolean abort = false;
        String line;
        while (!abort && (line = reader.readLine()) != null) {
          if (line.equals("t"))
            continue; 
          if (line.equals("q")) {
            abort = true;
            System.exit(1);
            continue;
          } 
          System.out.println("Unknown input. q=quit.");
        } 
      } 
    }
    
    

    public IOIOLooper createIOIOLooper(String connectionType, Object extra) { //we only go here if the Pixelcade board is detected via correct COM port
      return (IOIOLooper)new BaseIOIOLooper() {
      //return new BaseIOIOLooper() {  ///???
          
//            private SpiMaster spi_;
//  
//            private SpiMaster spi2_;
          
          //private  AnalogInput audioIn;
          
          public void disconnected() {
            String message = "PIXEL was Disconnected";
            System.out.println(message);
            LogMe.aLogger.severe(message);
          }
          
          public void incompatible() {
            String message = "Incompatible Firmware Detected";
            System.out.println(message);
            LogMe.aLogger.severe(message);
          }
          
          @Override
          protected void setup() throws ConnectionLostException, InterruptedException {
              
                pixel = new Pixel(pixelEnvironment.LED_MATRIX, pixelEnvironment.currentResolution);
                pixel.matrix = ioio_.openRgbLedMatrix(pixel.KIND);
                pixel.ioiO = ioio_;  //TO DO is this really needed?
                
                // ******** analog input testing ***********
                
                //audioIn = ioio_.openAnalogInput(33);
               
                // *****************************************
                //TO DO need to re-do the LED strip, do not use the IOIO Loop as PixelIntegation is arleady there
//                if (LEDStripExists()) { //https://groups.google.com/g/ioio-users/c/tKVuNFZRreQ/m/1JaMwn9ScEYJ
//                    
//                    if (LEDStrip1_.equals("yes")) {
//                        spi_ = ioio_.openSpiMaster(6, LEDStrip1DataPin_, LEDStrip1CLKPin_, 8, SpiMaster.Rate.RATE_50K); // 1 is clock I think 
//                    }
//                    
//                    if (LEDStrip2_.equals("yes")) {
//                        spi2_ = ioio_.openSpiMaster(9, LEDStrip2DataPin_, LEDStrip2CLKPin_, 29  , SpiMaster.Rate.RATE_50K); // 1 is clock I think 
//                    }
//                }
                
            StringBuilder message = new StringBuilder();
            
            if (WebEnabledPixel.pixel.matrix == null) {
              message.append("wtffff\n");
            } else {
              message.append("Found PIXEL: " + WebEnabledPixel.pixel.matrix + "\n");
            } 
            
            message.append("You may now interact with PIXEL!\n");
            message.append("LED matrix type is: " + WebEnabledPixel.LED_MATRIX_ID + "\n");
            WebEnabledPixel.this.searchTimer.cancel();
            message.append("PIXEL Status: Connected");
            WebEnabledPixel.pixelConnected = true;
            
            if (!WebEnabledPixel.this.playLastSavedMarqueeOnStartup_.equals("no"))
              WebEnabledPixel.pixel.playLocalMode(); 
            
            if (!WebEnabledPixel.pixel.PixelQueue.isEmpty()) {
              WebEnabledPixel.pixel.doneLoopingCheckQueue();
              if (!WebEnabledPixel.silentMode_) {
                System.out.println("Processing Startup Queue Items...");
                LogMe.aLogger.info("Processing Startup Queue Items...");
              } 
            } else if (!WebEnabledPixel.silentMode_) {
              System.out.println("No Items in the Queue at Startup...");
              LogMe.aLogger.info("No Items in the Queue at Startup...");
            } 
            
            if (!WebEnabledPixel.silentMode_) {
              System.out.println(message);
              LogMe.aLogger.info(message.toString());
            } 
            
            if (isALU) {
            
                if (aluInitMode_) {
                     System.out.println("[Status]: ALU First Connect Successful");
                     System.out.println("Stopped");
                     System.exit(1);
                }

                Date date = new Date();
                LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int month = localDate.getMonthValue();
                int day   = localDate.getDayOfMonth();

                if (!aluInitMode_ && easterEggMode_)  {  //if this is the second ALU run and we are in easter egg mode
                    if (month == 7 && (day == 3 || day == 4)) {
                            System.out.println("Fourth of July Easter Egg Match");
                            pixel.scrollText("Happy Fourth of July!", 1, 10L, Color.cyan,WebEnabledPixel.pixelConnected,1);


                        try {
                            pixel.writeArcadeAnimation("alu", "fireworks.gif", false, 10, WebEnabledPixel.pixelConnected);
                        } catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(WebEnabledPixel.class.getName()).log(Level.SEVERE, null, ex);
                        }



                    } else {

                        pixel.scrollText(lcdMarqueeMessage_, 1, 10L, Color.cyan,WebEnabledPixel.pixelConnected,1);

                        try {
                            pixel.writeArcadeAnimation("alu", "legends.gif", false, 99999, WebEnabledPixel.pixelConnected);
                        } catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(WebEnabledPixel.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                } else {
                    if (!aluInitMode_) {  //this is the second run and we're not in easter egg mode
                        try {
                            System.out.println("Welcome to Pixelcade");
                            String logoConsoleFilePathPNG = WebEnabledPixel.pixelHome + "alu/default-alu.png";
                            File logoConsoleFilePNG = new File(logoConsoleFilePathPNG);
                            pixel.writeArcadeImage(logoConsoleFilePNG, false, 99999, "alu", "default-alu.png", WebEnabledPixel.pixelConnected);
                        } catch (IOException ex) {
                            Logger.getLogger(WebEnabledPixel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            
            }
            
            else {  //if not an ALU
            
                //may need this for the Raspberry Pi issue where first connect is not successful
                if (aluInitMode_) {
                         System.out.println("[Status]: First Connect Attempt");
                         System.out.println("Exiting...");
                         LogMe.aLogger.info("[Status]: First Connect Attempt");
                         LogMe.aLogger.info("Exiting...");
                         System.exit(1);
                }
            }
            
            //to do later possibly, easter egg holiday animations could go here 
            
//            Date date = new Date();
//            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//            int year  = localDate.getYear();
//            int month = localDate.getMonthValue();
//            int day   = localDate.getDayOfMonth();
//            System.out.println("year:" + year + "month: " + month + "day " + day);
//            
//            if (month == 7 && day == 4) {
//                
//            }
          }
          
            // had to take out the loop as it was consuming CPU even when idle, this is only needed for the LED strips which is not a production feature, use IOIOFactory instead I guess here
          
            //it is possible that the WS281x waveforms can be generated using SPI. I would try running at 5.33MHz sending a 0b11000000 = 0xC0 byte for a "0" bit and a 0b11110000 = 0xF0 byte for a "1" bit. Not very efficient, but may not matter. In either case, adding support for this protocol in firmware using either bit-banging or the SPI peripheral is probably not too hard.
		
//          @Override  
//          //public void loop() throws ConnectionLostException, InterruptedException {  //for LED Strip
//          public void loop() throws ConnectionLostException {  //for LED Strip
//              
//                    //float audioValue = audioIn.read();
//                    //System.out.println("Audio Value: " + audioValue);
//                    
//              
//              
//                    if (LEDStripExists()) {
//                            
//                            if (LEDStrip1_.equals("yes")) {
//                        
//                                for (int i = 0; i < LEDStrip1NumberLEDs_; i++) {
//                                         tempRGB_.clear();
//                                         //red = 0; //120
//                                         //green = 0;
//                                         //blue = 0;
//                                         setColor(tempRGB_,(byte) red,(byte) green,(byte) blue);  //red, green, blue are set from a public method
//                                         
//                                         setLed(i, tempRGB_);
//                                }  
//
//                                 try {
//                                         ioio_.beginBatch();
//                                         spi_.writeReadAsync(0, buffer1_, buffer1_.length,
//                                                         buffer1_.length, null, 0);
//                                         spi_.writeRead(buffer2_, buffer2_.length, buffer2_.length,
//                                                         null, 0);
//                                         ioio_.endBatch();
//                                         Thread.sleep(50);
//                                 } catch (InterruptedException e1) {
//                                 }
//                            }
//                            
//                             if (LEDStrip2_.equals("yes")) {
//                        
//                                for (int i = 0; i < LEDStrip2NumberLEDs_; i++) {
//                                         tempRGB_.clear();
//                                         setColor(tempRGB_,(byte) red,(byte) green,(byte) blue);  //red, green, blue are set from a public method
//                                         setLed(i, tempRGB_);
//                                }  
//
//                                 try {
//                                         ioio_.beginBatch();
//                                         spi2_.writeReadAsync(0, buffer1_, buffer1_.length,
//                                                         buffer1_.length, null, 0);
//                                         spi2_.writeRead(buffer2_, buffer2_.length, buffer2_.length,
//                                                         null, 0);
//                                         ioio_.endBatch();
//                                         Thread.sleep(50);
//                                 } catch (InterruptedException e1) {
//                                 }
//                            }
//                             
//                    }
//		}
          
            
                
                public void setColor(RGB rgb, byte r, byte g, byte b) {  //blue and green are switched, not sure but that may be strip dependent, may need to make this configurable
				rgb.r = r;  
				rgb.g = b; 
				rgb.b = g;  
		}
                
                private void setLed(int num, RGB rgb) {
			// Find the right buffer to write to (first or second half).
			byte[] buffer;
			if (num >= 16) {
				buffer = buffer2_;
				num -= 16;
			} else {
				buffer = buffer1_;
			}
			num *= 3;
			if (rgb.r == 0 && rgb.g == 0 && rgb.b == 0) {
				fadeOut(buffer, num++);
				fadeOut(buffer, num++);
				fadeOut(buffer, num++);
			} else {
				// Poor-man's white balanace :)
				buffer[num++] = fixColor(rgb.r, 0.9);
				buffer[num++] = rgb.g;
				buffer[num++] = fixColor(rgb.b, 0.5);
			}
		}
                
                /** Attenuates a brightness level. */
		private byte fixColor(byte color, double attenuation) {
			double d = (double) ((int) color & 0xFF) / 256;
			d *= attenuation;
			return (byte) (d * 256);
		}

		private void fadeOut(byte[] buffer, int num) {
			final int value = (int) buffer[num] & 0xFF;
			buffer[num] = (byte) (value * fadeRate_);
		}
        };
    }
  }
  
	
  
  private class SearchTimerTask extends TimerTask {
    final long searchPeriodLength = 45000L;
    
    final long periodStart;
    
    final long periodEnd;
    
    private int dotCount = 0;
    
    String message = "Searching for PIXEL";
    
    StringBuilder label = new StringBuilder(this.message);
    
    public SearchTimerTask() {
      this.label.insert(0, "<html><body><h2>");
      Date d = new Date();
      this.periodStart = d.getTime();
      this.periodEnd = this.periodStart + 45000L;
    }
    
    public void run() {
      if (this.dotCount > 10) {
        this.label = new StringBuilder(this.message);
        this.label.insert(0, "<html><body><h2>");
        this.dotCount = 0;
      } else {
        this.label.append('.');
      } 
      this.dotCount++;
      Date d = new Date();
      long now = d.getTime();
      if (now > this.periodEnd) {
        WebEnabledPixel.this.searchTimer.cancel();
        if (WebEnabledPixel.pixel == null || WebEnabledPixel.pixel.matrix == null) {
          this.message = "A connection to PIXEL could not be established.";
          String title = "PIXEL Connection Unsuccessful: ";
          this.message = title + this.message;
          LogMe.aLogger.severe(this.message);
        } else if (!WebEnabledPixel.silentMode_) {
          LogMe.aLogger.info("Looks like we have a PIXEL connection!");
        } 
      } 
    }
  }
  
  private static class MessageListener implements SerialPortMessageListener {
    private MessageListener() {}
    
    public int getListeningEvents() {
      return 16;
    }
    
    public byte[] getMessageDelimiter() {
      return new byte[] { 45, 45 };
    }
    
    public boolean delimiterIndicatesEndOfMessage() {
      return true;
    }
 
    
    public void serialEvent(SerialPortEvent event) {
      byte[] delimitedMessage = event.getReceivedData();
      String firmwareString = new String(delimitedMessage);
      firmwareString = firmwareString.trim();
      firmwareString = WebEnabledPixel.right(firmwareString, 21);
      String FW_Hardware = "";
      String HW_Version = "";
      if (firmwareString.length() > 8) {
        FW_Hardware = firmwareString.substring(0, 4);
        HW_Version = firmwareString.substring(4, 8);
      } else {
        System.out.println("Invalid firmware: " + firmwareString);
      } 
      System.out.println("Sub Display Accessory Found with Plaform Firmware: " + FW_Hardware);
      System.out.println("Sub Display Accessory Found with Version: " + HW_Version);
      WebEnabledPixel.arduino1MatrixConnected = true;
    }
  }
  
  public static String right(String value, int length) {
    return value.substring(value.length() - length);
  }
  
  /** An RGB triplet. */
    private static class RGB {
		byte r;
		byte g;
		byte b;

		RGB() {
			clear();
		}

		public void clear() {
			r = g = b = 0;
		}
    }
}
