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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

public class ConsoleHttpHandler extends ImageResourceHttpHandler {
    protected LCDPixelcade lcdDisplay = null;
    
  public ConsoleHttpHandler(WebEnabledPixel application) {
    super(application);
    
     if(WebEnabledPixel.getLCDMarquee().equals("yes"))
       lcdDisplay = new LCDPixelcade();
     
    this.basePath = "";
    this.defaultImageClassPath = "mame";
    this.modeName = "arcade";
  }
  
  private void handlePNG(File arcadeFilePNGFullPath, Boolean saveAnimation, int loop, String console, String PNGNameWithExtension, String consoleNameMapped) throws MalformedURLException, IOException, ConnectionLostException {
    LogMe logMe = LogMe.getInstance();
    Pixel pixel = this.application.getPixel();
    pixel.writeArcadeImage(arcadeFilePNGFullPath, saveAnimation, loop, console, PNGNameWithExtension, WebEnabledPixel.pixelConnected);
   
  }
  
  private void handleGIF(String consoleName, String arcadeName, Boolean saveAnimation, int loop, String consoleNameMapped) {
    Pixel pixel = this.application.getPixel();
    try {
      pixel.writeArcadeAnimation(consoleName, arcadeName, saveAnimation.booleanValue(), loop, WebEnabledPixel.pixelConnected);
    } catch (NoSuchAlgorithmException ex) {
      Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    
  }
  
  protected void writeImageResource(String urlParams) throws IOException, ConnectionLostException {
    Pixel pixel = this.application.getPixel();
    String streamOrWrite = null;
    String consoleName = null;
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
    String color_ = null;
    Color color = null;
    int scrollsmooth_ = 0;
    String speed_ = null;
    Long speed = null;
    Long speeddelay_ = Long.valueOf(10L);
    int fontSize_ = 0;
    int yOffset_ = 0;
    int lines_ = 1;
    String font_ = null;
    List<NameValuePair> params = null;
    Font font = null;
    String pixelHome = System.getProperty("user.home") + File.separator + "pixelcade" + File.separator; //this means "location of pixelcade resources, art, etc"
    
    pixelHome = WebEnabledPixel.getHome();
    
    try {
      params = URLEncodedUtils.parse(new URI(urlParams), "UTF-8");
    } catch (URISyntaxException ex) {
      Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    for (NameValuePair param : params) {
      switch (param.getName()) {
        case "t":
          text_ = param.getValue();
          break;
        case "text":
          text_ = param.getValue();
           break;
        case "l":
          loop_ = Integer.valueOf(param.getValue()).intValue();
           break;
        case "loop":
          loop_ = Integer.valueOf(param.getValue()).intValue();
           break;
        case "ss":
          scrollsmooth_ = Integer.valueOf(param.getValue()).intValue();
           break;
        case "scrollsmooth":
          scrollsmooth_ = Integer.valueOf(param.getValue()).intValue();
        case "speed":
          speeddelay_ = Long.valueOf(param.getValue());
           break;
        case "c":
          color_ = param.getValue();
           break;
        case "color":
          color_ = param.getValue();
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
      } 
    } 
    URI tempURI = null;
    try {
      tempURI = new URI("http://localhost:8080" + urlParams);
    } catch (URISyntaxException ex) {
      Logger.getLogger(ConsoleHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    String URLPath = tempURI.getPath();
    String[] arcadeURLarray = URLPath.split("/");
    logMe = LogMe.getInstance();
    if (!CliPixel.getSilentMode()) {
      System.out.println("console handler received: " + urlParams);
      LogMe.aLogger.info("console handler received: " + urlParams);
    } 
    if (arcadeURLarray.length == 4) {
      streamOrWrite = arcadeURLarray[2];
      consoleName = arcadeURLarray[3];
      consoleName = consoleName.trim();
      consoleName = consoleName.replace("\n", "").replace("\r", "");
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
        
        if (loop_ == 0) {
          LogMe.aLogger.info("# of Times to Loop: null");
        } else {
          LogMe.aLogger.info("# of Times to Loop: " + loop_);
        } 
        if (text_ != "")
          LogMe.aLogger.info("alt text if marquee file not found: " + text_); 
      } 
      
      String requestedPath = pixelHome + "console\\" + consoleNameMapped;
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
       
//      if (WebEnabledPixel.getLCDMarquee().equals("yes")) {      //no longer needed now that we have pixelcade dx
//            String consoleLCDFilePathPNG = pixelHome + "lcdmarquees/console" + "/" + "default-" + consoleNameMapped + ".png"; 
//            System.out.println("Looking for console LCD marquee @: " + consoleLCDFilePathPNG);
//            LogMe.aLogger.info("Looking for console LCD marquee @: " + consoleLCDFilePathPNG);
//            File consoleLCDFilePNG = new File(consoleLCDFilePathPNG);  
//            
//            if (consoleLCDFilePNG.exists()) {
//                 System.out.println("FOUND LCD: " + consoleLCDFilePNG);
//                LogMe.aLogger.info("FOUND LCD: " + consoleLCDFilePNG);
//                if (this.lcdDisplay == null) {
//                   this.lcdDisplay = new LCDPixelcade();
//                }  
//                lcdDisplay.displayImage("no-game", consoleNameMapped);
//            } 
//             else {  //we don't have a matching lcd marquee so just display generic pixelcade image
//                System.out.println("Console LCD marquee not found, displaying generic Pixelcade marquee: " + consoleLCDFilePNG);
//                //lcdDisplay.displayImage("pixelcade", consoleNameMapped);
//               lcdDisplay.displayImage("console/pixelcade");
//            }
//      }
      
      if (streamOrWrite.equals("write")) {
        saveAnimation = true;
        consoleFilePathGIF = pixelHome + "console/default-" + consoleNameMapped + ".gif";
        File consoleFileGIF = new File(consoleFilePathGIF);
        consoleFilePathPNG = pixelHome + "console/default-" + consoleNameMapped + ".png";
        File consoleFilePNG = new File(consoleFilePathPNG);
        if (consoleFileGIF.exists() && !consoleFileGIF.isDirectory()) {
            
          if (WebEnabledPixel.arduino1MatrixConnected) {
            WebEnabledPixel.writeArduino1Matrix(WebEnabledPixel.getConsoleMetaData(consoleName));
            LogMe.aLogger.info("Accessory Call Console: " + WebEnabledPixel.getConsoleMetaData(consoleName));
          } 
          
          if (!CliPixel.getSilentMode()) {
            System.out.println("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
            LogMe.aLogger.info("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
          } 
          handleGIF("console", "default-" + consoleNameMapped + ".gif", Boolean.valueOf(saveAnimation), loop_,consoleNameMapped);
          
          
        } else if (consoleFilePNG.exists() && !consoleFilePNG.isDirectory()) {
          if (WebEnabledPixel.arduino1MatrixConnected) {
            WebEnabledPixel.writeArduino1Matrix(WebEnabledPixel.getConsoleMetaData(consoleName));
            LogMe.aLogger.info("Accessory Call Console: " + WebEnabledPixel.getConsoleMetaData(consoleName));
          } 
        handlePNG(consoleFilePNG, Boolean.valueOf(saveAnimation), loop_, "console", FilenameUtils.getName(consoleFilePathPNG),consoleNameMapped);
          
          
        } else if (text_ != "") {
          int LED_MATRIX_ID = WebEnabledPixel.getMatrixID();
          speed = Long.valueOf(10L);
          speed = Long.valueOf(WebEnabledPixel.getScrollingTextSpeed(LED_MATRIX_ID));
          
          if (speeddelay_.longValue() != 10L)
            speed = speeddelay_; 
          
//          if (color_ != null)
//            color = getColorFromHexOrName(color_); 
          
          pixel.scrollText(text_, loop_, speed.longValue(), color, WebEnabledPixel.pixelConnected, scrollsmooth_);
          
          
        } else {
          if (!CliPixel.getSilentMode()) {
            System.out.println("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
            LogMe.aLogger.info("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
          } 
          defaultConsoleFilePathPNG = pixelHome + "console/default-marquee.png";
          File defaultConsoleFilePNG = new File(defaultConsoleFilePathPNG);
          if (defaultConsoleFilePNG.exists() && !defaultConsoleFilePNG.isDirectory()) 
          {
            handlePNG(defaultConsoleFilePNG, Boolean.valueOf(saveAnimation), loop_, "console", FilenameUtils.getName(defaultConsoleFilePathPNG),consoleNameMapped);
            
          } else if (!CliPixel.getSilentMode()) {
            System.out.println("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
            System.out.println("Skipping LED marquee " + streamOrWrite + ", please check the files");
            LogMe.aLogger.info("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
            LogMe.aLogger.info("Skipping LED marquee " + streamOrWrite + ", please check the files");
          } 
        } 
      } else {
        saveAnimation = false;
        consoleFilePathGIF = pixelHome + "console/default-" + consoleNameMapped + ".gif";
        File consoleFileGIF = new File(consoleFilePathGIF);
        consoleFilePathPNG = pixelHome + "console/default-" + consoleNameMapped + ".png";
        File consoleFilePNG = new File(consoleFilePathPNG);
        if (consoleFileGIF.exists() && !consoleFileGIF.isDirectory()) {
          if (WebEnabledPixel.arduino1MatrixConnected) {
            WebEnabledPixel.writeArduino1Matrix(WebEnabledPixel.getConsoleMetaData(consoleName));
            LogMe.aLogger.info("Accessory Call Console: " + WebEnabledPixel.getConsoleMetaData(consoleName));
          } 
          if (!CliPixel.getSilentMode()) {
            System.out.println("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
            LogMe.aLogger.info("PNG default console LED Marquee file not found, looking for GIF version: " + consoleFilePathPNG);
          } 
          handleGIF("console", "default-" + consoleNameMapped + ".gif", Boolean.valueOf(saveAnimation), loop_, consoleNameMapped);
          
        } else if (consoleFilePNG.exists() && !consoleFilePNG.isDirectory()) {
          if (WebEnabledPixel.arduino1MatrixConnected) {
            WebEnabledPixel.writeArduino1Matrix(WebEnabledPixel.getConsoleMetaData(consoleName));
            LogMe.aLogger.info("Accessory Call Console: " + WebEnabledPixel.getConsoleMetaData(consoleName));
          } 
          handlePNG(consoleFilePNG, Boolean.valueOf(saveAnimation), loop_, "console", FilenameUtils.getName(consoleFilePathPNG),consoleNameMapped);
         
        } else if (text_ != "") {

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
        
      } 
        
        else {
          if (!CliPixel.getSilentMode()) {
            System.out.println("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
            LogMe.aLogger.info("GIF default console LED Marquee file not found, looking for default marquee: " + consoleFilePathGIF);
          } 
          defaultConsoleFilePathPNG = pixelHome + "console/default-marquee.png";
          File defaultConsoleFilePNG = new File(defaultConsoleFilePathPNG);
          if (defaultConsoleFilePNG.exists() && !defaultConsoleFilePNG.isDirectory()) {
            handlePNG(defaultConsoleFilePNG, Boolean.valueOf(saveAnimation), loop_, "console", FilenameUtils.getName(defaultConsoleFilePathPNG),consoleNameMapped);
          } else if (!CliPixel.getSilentMode()) {
            System.out.println("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
            System.out.println("Skipping LED marquee " + streamOrWrite + ", please check the files");
            LogMe.aLogger.info("Default console LED Marquee file not found: " + defaultConsoleFilePathPNG);
            LogMe.aLogger.info("Skipping LED marquee " + streamOrWrite + ", please check the files");
          } 
        } 
      } 
    } else {
      System.out.println("** ERROR ** URL format incorect, use http://localhost:8080/console/<stream or write>/<platform name>");
      System.out.println("Example: http://localhost:8080/conosle/write/mame");
      LogMe.aLogger.severe("** ERROR ** URL format incorect, use http://localhost:8080/console/<stream or write>/<platform name>");
      LogMe.aLogger.severe("Example: http://localhost:8080/conosle/write/mame");
    } 
  }
  
  public static boolean consoleMatch(String[] arr, String targetValue) {
    for (String s : arr) {
      if (s.equals(targetValue))
        return true; 
    } 
    return false;
  }
  
  public static Color hex2Rgb(String colorStr) {
    return new Color(
        Integer.valueOf(colorStr.substring(0, 2), 16).intValue(), 
        Integer.valueOf(colorStr.substring(2, 4), 16).intValue(), 
        Integer.valueOf(colorStr.substring(4, 6), 16).intValue());
  }
  
  private static boolean isHexadecimal(String input) {
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
}

