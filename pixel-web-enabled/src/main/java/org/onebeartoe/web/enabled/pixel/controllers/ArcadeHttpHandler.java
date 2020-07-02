
package org.onebeartoe.web.enabled.pixel.controllers;

import ioio.lib.api.exception.ConnectionLostException;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;


public class ArcadeHttpHandler extends ImageResourceHttpHandler {
  protected LCDPixelcade lcdDisplay = null;

  public ArcadeHttpHandler(WebEnabledPixel application) {
    super(application);
    
    if(WebEnabledPixel.getLCDMarquee().equals("yes"))
       lcdDisplay = new LCDPixelcade();

    this.basePath = "";
    this.defaultImageClassPath = "btime.png";
    this.modeName = "arcade";
  }
  
  public void handlePNG(File arcadeFilePNGFullPath, Boolean saveAnimation, int loop, String consoleNameMapped, String PNGNameWithExtension) throws MalformedURLException, IOException, ConnectionLostException {
    LogMe logMe = LogMe.getInstance();
    Pixel pixel = this.application.getPixel();
    pixel.writeArcadeImage(arcadeFilePNGFullPath, saveAnimation, loop, consoleNameMapped, PNGNameWithExtension, WebEnabledPixel.pixelConnected);
    //lcdDisplay.displayImage(PNGNameWithExtension,consoleNameMapped);
  }
  
  public void handleGIF(String consoleName, String arcadeName, Boolean saveAnimation, int loop) {
    Pixel pixel = this.application.getPixel();
   /* try {
        lcdDisplay.displayImage(arcadeName, consoleName);
    } catch (IOException e){} */
    try {
      pixel.writeArcadeAnimation(consoleName, arcadeName, saveAnimation.booleanValue(), loop, WebEnabledPixel.pixelConnected);

    } catch (NoSuchAlgorithmException ex) {
      Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    }
  }
  
  public void writeImageResource(String urlParams) throws IOException, ConnectionLostException {
    Pixel pixel = this.application.getPixel();
    String streamOrWrite = null;
    String consoleName = null;
    String arcadeName = null;
    String arcadeNameExtension = null;
    String arcadeNameOnly = null;
    String arcadeFilePathPNG = null;
    String arcadeFilePathGIF = null;
    String consoleFilePathPNG = null;
    String consoleFilePathGIF = null;
    String defaultConsoleFilePathPNG = null;
    String consoleNameMapped = null;
    LogMe logMe = null;
    
    String[] consoleArray = { 
        "mame", "atari2600", "daphne", "nes", "neogeo", "atarilynx", "snes", "atari5200", "atari7800", "atarijaguar", 
        "c64", "genesis", "capcom", "n64", "psp", "psx", "coleco", "dreamcast", "fba", "gb", 
        "gba", "ngp", "ngpc", "odyssey", "saturn", "megadrive", "gbc", "gamegear", "mastersystem", "sega32x", 
        "3do", "msx", "atari800", "pc", "nds", "amiga", "fds", "futurepinball", "amstradcpc", "apple2", 
        "intellivision", "macintosh", "ps2", "pcengine", "segacd", "sg-1000", "ti99", "vectrex", "virtualboy", "visualpinball", 
        "wonderswan", "wonderswancolor", "zinc", "sss", "zmachine", "zxspectrum" };
    
    boolean saveAnimation = false;
    int loop_ = 0;
    String text_ = "";
    int scrollsmooth_ = 1;
    Long speeddelay_ = Long.valueOf(10L);
    String speed_ = null;
    Long speed = null;
    String color_ = null;
    Color color = null;
    int i = 0;
    boolean textSelected = false;
    int fontSize_ = 0;
    int yOffset_ = 0;
    int lines_ = 1;
    String font_ = null;
    if (WebEnabledPixel.isWindows())
      scrollsmooth_ = 3; 
    List<NameValuePair> params = null;
    try {
      params = URLEncodedUtils.parse(new URI(urlParams), "UTF-8");
    } catch (URISyntaxException ex) {
      Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    URI tempURI = null;
    
    try {
      tempURI = new URI("http://localhost:8080" + urlParams);
    } catch (URISyntaxException ex) {
      Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    
    String URLPath = tempURI.getPath();
    String[] arcadeURLarray = URLPath.split("/");
    logMe = LogMe.getInstance();
    
    if (!CliPixel.getSilentMode()) {
      System.out.println("arcade handler received: " + urlParams);
      LogMe.aLogger.info("arcade handler received: " + urlParams);
    } 
    
    if (arcadeURLarray.length == 5) {
      streamOrWrite = arcadeURLarray[2];
      consoleName = arcadeURLarray[3];
      arcadeName = arcadeURLarray[4];
      arcadeName = arcadeName.trim();
      arcadeName = arcadeName.replace("\n", "").replace("\r", "");
      arcadeNameExtension = FilenameUtils.getExtension(arcadeName);
      
      if (arcadeNameExtension.length() > 3) {
        arcadeNameOnly = arcadeName;
      } else {
        arcadeNameOnly = FilenameUtils.removeExtension(arcadeName);
      } 
      
      i = 0;
      for (NameValuePair param : params) {
        i++;
        switch (param.getName()) {
          case "t":
            text_ = param.getValue();
            textSelected = true;
            break;
          case "text":
            text_ = param.getValue();
            textSelected = true;
             break;
          case "l":
            loop_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "loop":
            loop_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "gt":
            text_ = WebEnabledPixel.getGameName(arcadeNameOnly);
             break;
          case "gametitle":
            text_ = WebEnabledPixel.getGameName(arcadeNameOnly);
             break;
          case "ss":
            scrollsmooth_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "font":
            font_ = param.getValue();
             break;
          case "size":
            fontSize_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "yoffset":
            yOffset_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "lines":
            lines_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "scrollsmooth":
            scrollsmooth_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "speed":
            speed_ = param.getValue();
             break;
          case "c":
            color_ = param.getValue();
             break;
          case "color":
            color_ = param.getValue();
            break;
        } 
      } 
      consoleName = consoleName.toLowerCase();
      if (!consoleMatch(consoleArray, consoleName)) {
        consoleNameMapped = WebEnabledPixel.getConsoleMapping(consoleName);
      } else {
        consoleNameMapped = consoleName;
      } 
      if (consoleNameMapped.equals("mame-libretro"))
        consoleNameMapped = "mame"; 
      
      if (!CliPixel.getSilentMode()) {
        System.out.println(streamOrWrite.toUpperCase() + " MODE");
        System.out.println("Console Before Mapping: " + consoleName);
        System.out.println("Console Mapped: " + consoleNameMapped);
        System.out.println("Game Name Only: " + arcadeNameOnly);
        
        if (loop_ == 0) {
          System.out.println("# of Times to Loop: null");
        } else {
          System.out.println("# of Times to Loop: " + loop_);
        } 
        
        if (text_ != "")
          System.out.println("alt text if game file not found: " + text_);
	 
 
        LogMe.aLogger.info(streamOrWrite.toUpperCase() + " MODE");
        LogMe.aLogger.info("Console Before Mapping: " + consoleName);
        LogMe.aLogger.info("Console Mapped: " + consoleNameMapped);
        LogMe.aLogger.info("Game Name Only: " + arcadeNameOnly);
        if (loop_ == 0) {
          LogMe.aLogger.info("# of Times to Loop: null");
        } else {
          LogMe.aLogger.info("# of Times to Loop: " + loop_);
        } 
        if (text_ != "")
          LogMe.aLogger.info("alt text if marquee file not found: " + text_); 
      } 
      
      arcadeFilePathPNG = application.getPixel().getPixelHome() + consoleNameMapped + "/" + arcadeNameOnly + ".png";
      File arcadeFilePNG = new File(arcadeFilePathPNG);
      arcadeFilePathGIF = this.application.getPixel().getPixelHome() + consoleNameMapped + "/" + arcadeNameOnly + ".gif";
      File arcadeFileGIF = new File(arcadeFilePathGIF);
      
      if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory()) {
        arcadeNameOnly = FilenameUtils.removeExtension(arcadeName);
      } else {
        String arcadeNameOnlyUnderscore = arcadeNameOnly.replaceAll("_", " ");
        String arcadeFilePathPNGUnderscore = this.application.getPixel().getPixelHome() + consoleNameMapped + "/" + arcadeNameOnlyUnderscore + ".png";
        arcadeFilePNG = new File(arcadeFilePathPNGUnderscore);
        if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory()) {
          arcadeNameOnly = arcadeNameOnlyUnderscore;
        } else {
          String arcadeNamelowerCase = arcadeNameOnly.toLowerCase();
          String arcadeFilePathPNGlowerCase = this.application.getPixel().getPixelHome() + consoleNameMapped + "/" + arcadeNamelowerCase + ".png";
          arcadeFilePNG = new File(arcadeFilePathPNGlowerCase);
          if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory())
            arcadeNameOnly = arcadeNamelowerCase; 
        } 
      } 
      if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
        arcadeNameOnly = FilenameUtils.removeExtension(arcadeName);
      } 
      else {
        String arcadeNameOnlyUnderscore = arcadeNameOnly.replaceAll("_", " ");
        String arcadeFilePathGIFUnderscore = this.application.getPixel().getPixelHome() + consoleNameMapped + "/" + arcadeNameOnlyUnderscore + ".gif";
        arcadeFileGIF = new File(arcadeFilePathGIFUnderscore);
        if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
          arcadeNameOnly = arcadeNameOnlyUnderscore;
        } else {
          String arcadeNamelowerCase = arcadeNameOnly.toLowerCase();
          String arcadeFilePathGIFlowerCase = this.application.getPixel().getPixelHome() + consoleNameMapped + "/" + arcadeNamelowerCase + ".gif";
          arcadeFileGIF = new File(arcadeFilePathGIFlowerCase);
          if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory())
            arcadeNameOnly = arcadeNamelowerCase; 
        } 
      } 
      String requestedPath = this.application.getPixel().getPixelHome() + consoleNameMapped + "\\" + arcadeNameOnly;
      if (!CliPixel.getSilentMode()) {
            System.out.println("Looking for: " + requestedPath + ".png or .gif");
            LogMe.aLogger.info("Looking for: " + requestedPath + ".png or .gif");
      }  
      
       if (color_ == null) {
            if (WebEnabledPixel.getTextColor().equals("random")) {
              color = WebEnabledPixel.getRandomColor();
            } else {
              color = WebEnabledPixel.getColorFromHexOrName(WebEnabledPixel.getTextColor());
            } 
          } else {
            color = WebEnabledPixel.getColorFromHexOrName(color_);
        } 
      
      
      //if (WebEnabledPixel.getLCDMarquee().equals("yes") && !consoleNameMapped.equals("retropie") && !consoleNameMapped.equals("power")) { 
      if (WebEnabledPixel.getLCDMarquee().equals("yes")) { 
            String arcadeLCDFilePathPNG = this.application.getPixel().getPixelHome() + "lcdmarquees" + "/" + arcadeNameOnly + ".png"; 
            System.out.println("Looking for lcd marquee @: " + arcadeLCDFilePathPNG);
            LogMe.aLogger.info("Looking for lcd marquee @: " + arcadeLCDFilePathPNG);
            File arcadeLCDFilePNG = new File(arcadeLCDFilePathPNG);  
            
            String consoleLCDFilePathPNG = this.application.getPixel().getPixelHome() + "lcdmarquees/console" + "/" + "default-" + consoleNameMapped + ".png";
            System.out.println("Looking for lcd console marquee @: " + consoleLCDFilePathPNG);
            LogMe.aLogger.info("Looking for lcd console marquee @: " + consoleLCDFilePathPNG);
            File consoleLCDFilePNG = new File(consoleLCDFilePathPNG);  
            
            if (arcadeLCDFilePNG.exists()) {
                System.out.println("FOUND: " + arcadeLCDFilePathPNG);
                LogMe.aLogger.info("FOUND: " + arcadeLCDFilePathPNG);
                if (this.lcdDisplay == null) {
                   this.lcdDisplay = new LCDPixelcade();
                }  
                 lcdDisplay.displayImage(arcadeNameOnly, consoleNameMapped);
            } else if (consoleLCDFilePNG.exists()) {
                System.out.println("FOUND: " + consoleLCDFilePathPNG);
                LogMe.aLogger.info("FOUND: " + consoleLCDFilePathPNG);
                if (this.lcdDisplay == null) {
                   this.lcdDisplay = new LCDPixelcade();
                }  
                 lcdDisplay.displayImage(arcadeNameOnly, consoleNameMapped);
            } 
            else if (text_ != "") {  //if not matching png, do we have some scrolling text we can show?
                lcdDisplay.setAltText(text_);	
                lcdDisplay.setNumLoops(loop_);   
                lcdDisplay.scrollText(text_, new Font(font_, Font.PLAIN, 288), color, 15);
            }
             else {         //we don't have a matching lcd marquee png or alt text so just show the default marquee
                lcdDisplay.displayImage("pixelcade", consoleNameMapped);
                System.out.println("went to generic image");
            }
      } else {
           System.out.println("SKIPPED LCD Update");
      }
      
      if (streamOrWrite.equals("write")) {
                saveAnimation = true;
                if (WebEnabledPixel.arduino1MatrixConnected) {
                  WebEnabledPixel.writeArduino1Matrix(WebEnabledPixel.getGameMetaData(arcadeNameOnly));
                  LogMe.aLogger.info("Accessory Call: " + WebEnabledPixel.getGameMetaData(arcadeNameOnly));
                } 
                if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
                  handleGIF(consoleNameMapped, arcadeNameOnly + ".gif", Boolean.valueOf(saveAnimation), loop_);

                } else if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory()) {
                  handlePNG(arcadeFilePNG, Boolean.valueOf(saveAnimation), loop_, consoleNameMapped, FilenameUtils.getName(arcadeFilePathPNG));

                } else if (text_ != "" && !text_.equals("nomatch")) {
                int LED_MATRIX_ID = WebEnabledPixel.getMatrixID();
                speed = Long.valueOf(10L);
                speed = Long.valueOf(WebEnabledPixel.getScrollingTextSpeed(LED_MATRIX_ID));

                if (speeddelay_.longValue() != 10L)
                    speed = speeddelay_; 

//                if (color_ != null)
//                    color = WebEnabledPixel.getColorFromHexOrName(color_);

                pixel.scrollText(text_, loop_, speed.longValue(), color, WebEnabledPixel.pixelConnected, scrollsmooth_);

                } else {
                  consoleFilePathGIF = this.application.getPixel().getPixelHome() + "console/default-" + consoleNameMapped + ".gif";
                  File consoleFileGIF = new File(consoleFilePathGIF);
                  consoleFilePathPNG = this.application.getPixel().getPixelHome() + "console/default-" + consoleNameMapped + ".png";

                  File consoleFilePNG = new File(consoleFilePathPNG);
                  if (consoleFileGIF.exists() && !consoleFileGIF.isDirectory()) {
                    if (!CliPixel.getSilentMode()) {
                      System.out.println("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
                      LogMe.aLogger.info("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
                    } 
                    handleGIF("console", "default-" + consoleNameMapped + ".gif", Boolean.valueOf(saveAnimation), loop_);

                  } else if (consoleFilePNG.exists() && !consoleFilePNG.isDirectory()) {
                    handlePNG(consoleFilePNG, Boolean.valueOf(saveAnimation), loop_, "console", FilenameUtils.getName(consoleFilePathPNG));

                  } else {

                      if (!CliPixel.getSilentMode()) {
                      System.out.println("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
                      LogMe.aLogger.info("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
                    } 
                    defaultConsoleFilePathPNG = this.application.getPixel().getPixelHome() + "console/default-marquee.png";
                    File defaultConsoleFilePNG = new File(defaultConsoleFilePathPNG);
                    if (defaultConsoleFilePNG.exists() && !defaultConsoleFilePNG.isDirectory()) {
                      handlePNG(defaultConsoleFilePNG, Boolean.valueOf(saveAnimation), loop_, "console", FilenameUtils.getName(defaultConsoleFilePathPNG));
                    } else if (!CliPixel.getSilentMode()) {
                      System.out.println("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
                      System.out.println("Skipping LED marquee " + streamOrWrite + ", please check the files");
                      LogMe.aLogger.info("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
                      LogMe.aLogger.info("Skipping LED marquee " + streamOrWrite + ", please check the files");
                    } 
                  } 
                } 
      } else {
        saveAnimation = false;
        
        if (WebEnabledPixel.arduino1MatrixConnected) {
          WebEnabledPixel.writeArduino1Matrix(WebEnabledPixel.getGameMetaData(arcadeNameOnly));
          LogMe.aLogger.info("Accessory Call: " + WebEnabledPixel.getGameMetaData(arcadeNameOnly));
        } 
        
        if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory() && arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
          handlePNG(arcadeFilePNG, Boolean.valueOf(false), 0, "black", "nodata");
          handleGIF(consoleNameMapped, arcadeNameOnly + ".gif", Boolean.valueOf(saveAnimation), 1);
          handlePNG(arcadeFilePNG, Boolean.valueOf(saveAnimation), 99999, consoleNameMapped, arcadeNameOnly + ".png");
          //to part does not work with the Q and looping, address later
        
        } else if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory()) {
          handlePNG(arcadeFilePNG, Boolean.valueOf(saveAnimation), loop_, consoleNameMapped, arcadeNameOnly + ".png");

	} else if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
          handleGIF(consoleNameMapped, arcadeNameOnly + ".gif", Boolean.valueOf(saveAnimation), loop_);
        
        } else if (text_ != "" && !text_.equals("nomatch")) {

          int LED_MATRIX_ID = WebEnabledPixel.getMatrixID();
          speed = Long.valueOf(WebEnabledPixel.getScrollingTextSpeed(LED_MATRIX_ID));
          if (speed_ != null) {
            speed = Long.valueOf(speed_);
            if (speed.longValue() == 0L)
              speed = Long.valueOf(10L); 
          } 
          if (scrollsmooth_ == 0) {
            String scrollSpeedSettings = WebEnabledPixel.getTextScrollSpeed();
            scrollsmooth_ = WebEnabledPixel.getScrollingSmoothSpeed(scrollSpeedSettings);
          } 
          if (font_ == null)
            font_ = WebEnabledPixel.getDefaultFont(); 
          this.application.getPixel();
          Pixel.setFontFamily(font_);
          if (yOffset_ == 0)
            yOffset_ = WebEnabledPixel.getDefaultyTextOffset(); 
          this.application.getPixel();
          Pixel.setYOffset(yOffset_);
          if (fontSize_ == 0)
            fontSize_ = WebEnabledPixel.getDefaultFontSize(); 
          this.application.getPixel();
          Pixel.setFontSize(fontSize_);
          
          
        if (lines_ == 2) 
            Pixel.setDoubleLine(true);
        else if (lines_ == 4)
            Pixel.setFourLine(true);
        else
            Pixel.setDoubleLine(false); //don't forget to set it back
          
            pixel.scrollText(text_, loop_, speed.longValue(), color, WebEnabledPixel.pixelConnected, scrollsmooth_);
          
         
        } else {
                consoleFilePathPNG = this.application.getPixel().getPixelHome() + "console/default-" + consoleNameMapped + ".png";
                File consoleFilePNG = new File(consoleFilePathPNG);
                consoleFilePathGIF = this.application.getPixel().getPixelHome() + "console/default-" + consoleNameMapped + ".gif";
                File consoleFileGIF = new File(consoleFilePathGIF);
                if (consoleFilePNG.exists() && !consoleFilePNG.isDirectory()) {
                  handlePNG(consoleFilePNG, Boolean.valueOf(saveAnimation), loop_, "console", FilenameUtils.getName(consoleFilePathPNG));
                } else if (consoleFileGIF.exists() && !consoleFileGIF.isDirectory()) {
                  if (!CliPixel.getSilentMode()) {
                    System.out.println("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
                    LogMe.aLogger.info("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
                  } 
                  handleGIF("console", "default-" + consoleNameMapped + ".gif", Boolean.valueOf(saveAnimation), loop_);
                } else {
                  if (!CliPixel.getSilentMode()) {
                    System.out.println("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
                    LogMe.aLogger.info("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
                  } 
                  defaultConsoleFilePathPNG = this.application.getPixel().getPixelHome() + "console/default-marquee.png";
                  File defaultConsoleFilePNG = new File(defaultConsoleFilePathPNG);
                  if (defaultConsoleFilePNG.exists() && !defaultConsoleFilePNG.isDirectory()) {
                    handlePNG(defaultConsoleFilePNG, Boolean.valueOf(saveAnimation), loop_, "console", FilenameUtils.getName(defaultConsoleFilePathPNG));
                  } else if (!CliPixel.getSilentMode()) {
                    System.out.println("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
                    System.out.println("Skipping LED marquee " + streamOrWrite + ", please check the files");
                    LogMe.aLogger.info("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
                    LogMe.aLogger.info("Skipping LED marquee " + streamOrWrite + ", please check the files");
                  } 
                } 
              } 
      } 
    } else {
      System.out.println("** ERROR ** URL format incorect, use http://localhost:8080/arcade/<stream or write>/<platform name>/<game name .gif or .png>");
      System.out.println("Example: http://localhost:8080/arcade/write/mame/pacman.png or http://localhost:8080/arcade/stream/atari2600/digdug.gif");
      LogMe.aLogger.severe("** ERROR ** URL format incorect, use http://localhost:8080/arcade/<stream or write>/<platform name>/<game name .gif or .png>");
      LogMe.aLogger.severe("Example: http://localhost:8080/arcade/write/mame/pacman.png or http://localhost:8080/arcade/stream/atari2600/digdug.gif");
    } 
  }
  
  public static boolean consoleMatch(String[] arr, String targetValue) {
    for (String s : arr) {
      if (s.equals(targetValue))
        return true; 
    } 
    return false;
  }
  
  public int getMaxAnimationsPerGame(String baseName) { //this will tell us how many GIFs there are per rom, ex. pacman_01, pacman_02, pacman_03, etc.
      
      int max = 0;
      Boolean match = true;     
      int i = 0;
      while (match) {
          i++;
          File targetFile = new File(String.format("%s/%s_%d.gif",WebEnabledPixel.getHome(),baseName,i));
          if (targetFile.exists() && !targetFile.isDirectory())
              match = true;
          else
              match = false;
      } 
      // ok we didn't have a match so are done with the while loop, let's set max to i
      max = i;
      return max;
  }
}
   

 