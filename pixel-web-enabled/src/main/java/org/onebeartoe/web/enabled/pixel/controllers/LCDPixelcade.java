package org.onebeartoe.web.enabled.pixel.controllers;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

public class LCDPixelcade {

    public static String pixelHome = System.getProperty("user.home") + File.separator + "pixelcade" + File.separator; //this means "location of pixelcade resources, art, etc"
    //private static String pixelHome = WebEnabledPixel.getHome();
    private static String sep = File.separator;
    private static String fontPath = pixelHome + "fonts/";
    private static int loops = 0;
    private static String jarPath = new File(LCDPixelcade.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath();
    private static String wrapperHome = jarPath.substring(0, jarPath.lastIndexOf(File.separator)) + File.separator;
    private static String fontColor = "purple";
    private static String DEFAULT_COMMAND = "gsho -platform linuxfb " + pixelHome + "lcdmarquees/pixelcade.png";
    private static final String JPG_COMMAND = "gsho -platform linuxfb " + pixelHome + "lcdmarquees/${named}.jpg";
    private static String PNG_COMMAND = wrapperHome + "gsho -platform linuxfb  "+ pixelHome + "lcdmarquees/${named}.png ";
    private static String GIF_COMMAND = wrapperHome + "gsho  -platform linuxfb " + pixelHome + "${system}/${named}.gif";
    private static String TXT_COMMAND = wrapperHome + "skrola -platform linuxfb \"${txt}\" \"${fontpath}\" \"${color}\" ${speed}";
    private static final String SLIDESHOW = "sudo fbi " + pixelHome + "lcdmarquees/* -T 1 -d /dev/fb0 -t 2 --noverbose --nocomments --fixwidth -a";
    private static final String RESET_COMMAND = "sudo killall -9 fbi;killall -9 gsho; killall -9 skrola;";
    private static final String MARQUEE_PATH = pixelHome + "lcdmarquees/";
    private static final String ENGINE_PATH = wrapperHome + "/gsho";
    static String NOT_FOUND = pixelHome + "lcdmarquees/" + "pixelcade.png";
    public static String theCommand = DEFAULT_COMMAND;
    public static String currentMessage = "Welcome and Game On!";
    public static String gifSystem = "";
    public static  WindowsLCD windowsLCD = null;
    public static boolean dxEnvironment = WebEnabledPixel.dxEnvironment;
    private boolean dxChecked = false;
    public static boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    public static boolean  doGif = false;

    {
        if (!dxChecked){
            try {
                if (InetAddress.getByName("pixelcadedx.local").isReachable(20000)) {

                    dxChecked = true;
                    System.out.print("Setting DXEnvironment\n");
                } else {
                    System.out.print("LCD used in non-DXE...YMMV/Ill-Advised :)\n");
                    WebEnabledPixel.dxEnvironment = false;
                    dxChecked = true;
                }

            } catch (Exception e) {
            }
    }
    }
    public static void main(String[] args) {


        String shell = "bash";
        if(isWindows) {
            windowsLCD = new WindowsLCD();
            pixelHome =  WebEnabledPixel.getHome();
        }
   
        boolean haveFBI = new File(ENGINE_PATH).exists();
        //boolean haveExtraDisplay = new File("/dev/fb1").exists();

        if (!haveFBI && WebEnabledPixel.isUnix()) {
            System.out.print("Image engine failure.\n");
        }
        if (args.length > -1) {
            try {

                if(args.length == 1)
                displayImage(args[args.length - 1]);
                else
                    displayImage(args[0],args[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                displayImage(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (isWindows) {
            shell = "CMD.EXE";
            System.out.print("Shell: ${shell}");
        }
    }

public void setLCDFont(Font font, String fontFilename) {
        if (dxEnvironment) return;
        if(!isWindows) {
            this.fontPath = fontFilename; 
	   System.out.print("fontPath: " + fontFilename +"\n");
            return;
        }

        if(windowsLCD == null)
            windowsLCD = new WindowsLCD();

        windowsLCD.marqueePanel.setFont(font);
        windowsLCD.marqueePanel.setFontFileName(fontFilename);
        if(!windowsLCD.marqueePanel.didHi)
        windowsLCD.marqueePanel.setMessage("Welcome to Pixelcade and Game On!");
    }

    public void setAltText(String text){
        if (dxEnvironment) return;
        this.currentMessage = text;
        System.out.print("AltMessage set\n");
        if(isWindows && windowsLCD != null)
            windowsLCD.marqueePanel.setNumLoops(loops);
    }

    public void setNumLoops(int loops){
        if (dxEnvironment) return;
        this.loops = loops;
	System.out.print("Loops set\n");
        if(isWindows && windowsLCD != null)
            windowsLCD.marqueePanel.setNumLoops(loops);
    }

    static public void displayImage(String named, String system) throws IOException {
        if (dxEnvironment) return;
        System.out.println(String.format("[INTERNAL] Asked to display marquee for %s, %s",named,system));
        if(!WebEnabledPixel.getLCDMarquee().contains("yes"))
            return;

        if(isWindows) {
            if(windowsLCD == null)
            windowsLCD = new WindowsLCD();
            
            windowsLCD.displayImage(named, system);
            System.out.println(String.format("[INTERNAL] Switching to WindowsSubsytem because IsWindows:%d",isWindows));
            return;
        }
		
	String marqueePath = NOT_FOUND;

        if (new File(String.format("%slcdmarquees/%s.png",pixelHome, named)).exists()){
            //DEFAULT_COMMAND = "sudo fbi" + pixelHome + "lcdmarquees/" + named + ".png -T 1 -/d /dev/fb0  --noverbose --nocomments --fixwidth -a";
            DEFAULT_COMMAND = wrapperHome + "gsho  -platform linuxfb " + pixelHome + "lcdmarquees/" + named + ".png";
            marqueePath = String.format("%slcdmarquees/%s.png",pixelHome, named);
        }
        if (new File(String.format("/home/pi/pixelcade/lcdmarquees/console/default-%s.png", system)).exists()) {
            //DEFAULT_COMMAND = "sudo fbi /home/pi/pixelcade/lcdmarquees/console/default-" + system + ".png -T 1  --noverbose --nocomments --fixwidth -a";
            DEFAULT_COMMAND = wrapperHome + "gsho -platform linuxfb " + pixelHome + "lcdmarquees/console/default-" + system + ".png";
            marqueePath = String.format("/home/pi/pixelcade/lcdmarquees/console/default-%s.png", system);
        }

        doGif = new File(String.format("%s%s/%s.gif",pixelHome, system,named)).exists();
        gifSystem = system;
	theCommand = DEFAULT_COMMAND;
    	
	if(marqueePath.contains(NOT_FOUND)){
	     System.out.print(String.format("[INTERNAL] Could not locate %s.png in %slcdmarquees\nmp:%s\nnf:%s\n",named, pixelHome,marqueePath,NOT_FOUND));
	     //currentMessage = String.format("%s - %s",named,system);
	    named = "resetti";
	} 
      
	displayImage(named);

	}

    static public void  displayImage(String named) throws IOException {  //note this is Pi/linux only!
        if (named == null || dxEnvironment) return;


        if (named != null) if (named.contains("slideshow")) {
            theCommand = SLIDESHOW;
	} else if (new File(MARQUEE_PATH + named + ".png").exists()) {
            theCommand = PNG_COMMAND.replace("${named}", named);
        } else if (new File(MARQUEE_PATH + named + ".png").exists()) {
            theCommand = PNG_COMMAND.replace("${named}", named);
        } else if (new File(MARQUEE_PATH + named + ".jpg").exists()) {
            theCommand = JPG_COMMAND.replace("${named}", named);
	} else if (named.contains("resetti")) 
            theCommand = PNG_COMMAND.replace("${named}", "black");

        if(doGif){
          theCommand = GIF_COMMAND.replace("${named}", named).replace("${system}", gifSystem);
        }
        System.out.println(String.format("[INTERNAL] Running command: %s  For Marquee: %s",theCommand,named));
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", RESET_COMMAND + theCommand);
        System.out.println("[INTERNAL] Running cmd: " + "sh -c " +  RESET_COMMAND + theCommand);
        Process process = builder.start();
	    
        if (named.contains("resetti") && doGif == false)
        scrollText(currentMessage,new Font("Helvetica", Font.PLAIN, 18), Color.red,15);
        
        if(doGif) doGif = false;
    }

    static public void scrollText(String message, Font font, Color color, int speed) {
        if (dxEnvironment) return;
        if(isWindows){
		System.out.println("[INTERNAL]Scroller Switching to WindowsSubsystem");
            if(windowsLCD == null)
                windowsLCD = new WindowsLCD();

            windowsLCD.scrollText(message,font,color, speed);
            return;
        }
          String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
                fontColor = hex;
	 System.out.println("Gonna scroll: " + message + "\n");
	 System.out.println(String.format("Font:%s Color:%s Speed:%d\n",fontPath,fontColor,speed));
	 String theCommand = TXT_COMMAND.replace("${txt}",message).replace("${fontpath}",fontPath.replace(".ttf","")).replace("${color}",fontColor).replace("${speed}",String.format("%d",speed));
       try {    
	ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", RESET_COMMAND + theCommand);
        System.out.println("Running cmd: " + "sh -c " +  RESET_COMMAND + theCommand);
        Process process = builder.start();
	} catch (IOException ioe) {}
    }
}
