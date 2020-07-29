
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


public class PinballHttpHandler extends ImageResourceHttpHandler {
  protected LCDPixelcade lcdDisplay = null;

  public PinballHttpHandler(WebEnabledPixel application) {
    super(application);
    
    if(WebEnabledPixel.getLCDMarquee().equals("yes"))
       lcdDisplay = new LCDPixelcade();

    this.basePath = "";
    this.defaultImageClassPath = "btime.png";
    this.modeName = "pinball";
  }
  
  public void handleGIF(String pinTable, String PinAnimationName, Boolean saveAnimation, int loop) {
    Pixel pixel = this.application.getPixel();
   
    try {
      //pixel.writeArcadeAnimation(pinTable, PinAnimationName, saveAnimation.booleanValue(), loop, WebEnabledPixel.pixelConnected);
      pixel.writePinballAnimation(pinTable, PinAnimationName, saveAnimation.booleanValue(), loop, WebEnabledPixel.pixelConnected);

    } catch (NoSuchAlgorithmException ex) {
      Logger.getLogger(PinballHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    }
  }
  
  public void writeImageResource(String urlParams) throws IOException, ConnectionLostException {
    Pixel pixel = this.application.getPixel();
    String streamOrWrite = null;
    String pinTable = null;
    String PinAnimationName = null;
    String pinAnimationNameExtension = null;
    String pinAnimationNameOnly = null;
    String arcadeFilePathGIF = null;
    String pixelHome = System.getProperty("user.home") + File.separator + "pixelcade" + File.separator; //this means "location of pixelcade resources, art, etc"
    LogMe logMe = null;
    
    boolean saveAnimation = false;
    boolean overlay = true;
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
    
    pixelHome = WebEnabledPixel.getHome();
    
    if (WebEnabledPixel.isWindows())
      scrollsmooth_ = 3; 
    
    List<NameValuePair> params = null;
    try {
      params = URLEncodedUtils.parse(new URI(urlParams), "UTF-8");
    } catch (URISyntaxException ex) {
      Logger.getLogger(PinballHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    URI tempURI = null;
    
    try {
      tempURI = new URI("http://localhost:8080" + urlParams);
    } catch (URISyntaxException ex) {
      Logger.getLogger(PinballHttpHandler.class.getName()).log(Level.SEVERE, (String)null, ex);
    } 
    
    String URLPath = tempURI.getPath();
    String[] arcadeURLarray = URLPath.split("/");
    logMe = LogMe.getInstance();
    
    if (!CliPixel.getSilentMode()) {
      System.out.println("pinball handler received: " + urlParams);
      LogMe.aLogger.info("pinball handler received: " + urlParams);
    } 
    
//    System.out.println(URLPath.toString());
//    System.out.println(arcadeURLarray);
//    System.out.println("length " + arcadeURLarray.length);
//    System.out.println("length " + arcadeURLarray[3]);
    
    if (arcadeURLarray.length == 5) {
      streamOrWrite = arcadeURLarray[2];
      pinTable = arcadeURLarray[3];
      PinAnimationName = arcadeURLarray[4];
      PinAnimationName = PinAnimationName.trim();
      PinAnimationName = PinAnimationName.replace("\n", "").replace("\r", "");
      pinAnimationNameExtension = FilenameUtils.getExtension(PinAnimationName);
      
      if (pinAnimationNameExtension.length() > 3) {
        pinAnimationNameOnly = PinAnimationName;
      } else {
        pinAnimationNameOnly = FilenameUtils.removeExtension(PinAnimationName);
      } 
      
      i = 0;
      for (NameValuePair param : params) {
        i++;
        switch (param.getName()) {
          case "l":
            loop_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "loop":
            loop_ = Integer.valueOf(param.getValue()).intValue();
             break;
          case "no":  //no overlay , did not implement this yet
            overlay = false;
            break;
        } 
      } 
      
      pinTable = pinTable.toLowerCase();
      
      if (!CliPixel.getSilentMode()) {
        System.out.println(streamOrWrite.toUpperCase() + " MODE");
        System.out.println("Pinball Table or ROM: " + pinTable);
        System.out.println("Pinball Animation: " + pinAnimationNameOnly);
        LogMe.aLogger.info("Pin Table Name: " + pinTable);
        LogMe.aLogger.info("Pinball Animation: " + pinAnimationNameOnly);
      } 
      
      arcadeFilePathGIF = pixelHome + "pinball/" + pinTable + "/" + pinAnimationNameOnly + ".gif";  //pixelcade/pinball/table/animation
      File arcadeFileGIF = new File(arcadeFilePathGIF);
      
      if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
        pinAnimationNameOnly = FilenameUtils.removeExtension(PinAnimationName);
        pinTable = "pinball/" + pinTable;
      } 
      else {  //if not in the specific table folder, let's get it from the pinball folder           //pixelcade/pinball/animation
       
          arcadeFilePathGIF = pixelHome + "pinball/" + pinAnimationNameOnly + ".gif";  
          arcadeFileGIF = new File(arcadeFilePathGIF);
          
          if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
               pinAnimationNameOnly = FilenameUtils.removeExtension(PinAnimationName);
               pinTable = "pinball";
        } 
      } 
      
      //String requestedPath = pixelHome + pinTable + "\\" + pinAnimationNameOnly;
      String requestedPath = arcadeFilePathGIF;
      
      if (!CliPixel.getSilentMode()) {
            System.out.println("Looking for: " + requestedPath);
            LogMe.aLogger.info("Looking for: " + requestedPath);
      }  
     
        saveAnimation = false; //we're streaming which would be the most common case
        
      if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
            handleGIF(pinTable, pinAnimationNameOnly + ".gif", Boolean.valueOf(saveAnimation), loop_);  //either pinball/gif or pinball/table/gif
      } 
      
    } else {
            System.out.println("[ERROR] URL format incorect, use http://localhost:8080/pinball/stream/<Pinball Table/ROM Name>/<Pinball GIF Name>");
            System.out.println("Example: http://localhost:8080/pinball/stream/tron/s02");
            LogMe.aLogger.severe("[ERROR] URL format incorect, use http://localhost:8080/pinball/stream/<Pinball Table/ROM Name>/<Pinball GIF Name>");
            LogMe.aLogger.severe("Example: http://localhost:8080/pinball/stream/tron/s02");
    } 
  }
}
   

 