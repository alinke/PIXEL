package org.onebeartoe.web.enabled.pixel.controllers;

import ioio.lib.api.RgbLedMatrix;
import ioio.lib.api.exception.ConnectionLostException;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.setCurrentPlatformGame;

public class getPNGandGIFMatch  {

//  public getPNGandGIFMatch() {
//   
//  }
  
  public static MarqueePath getPaths (String gameName, String consoleName)  {
  
    
    String arcadeName = null;
    String arcadeNameExtension = null;
    String arcadeNameOnly = null;
    String arcadeNameOnlyPNG = null;
    String arcadeFilePathPNG = null;
    String arcadeFilePathGIF = null;
    String consoleFilePathPNG = null;
    String consoleFilePathGIF = null;
    String defaultConsoleFilePathPNG = null;
    String consoleNameMapped = null;
    Integer animationVersionCounter = 3;
    String pixelHome = System.getProperty("user.home") + File.separator + "pixelcade" + File.separator; //this means "location of pixelcade resources, art, etc"
    LogMe logMe = null;
    File arcadeFileGIF = new File(pixelHome);
    
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
    pixelHome = WebEnabledPixel.getHome();
    
      arcadeName = gameName;
      arcadeName = arcadeName.trim();
      arcadeName = arcadeName.replace("\n", "").replace("\r", "");
      arcadeNameExtension = FilenameUtils.getExtension(arcadeName);
      
      if (arcadeNameExtension.length() > 3) {
        arcadeNameOnly = arcadeName;
        arcadeNameOnlyPNG = arcadeName;
      } else {
        arcadeNameOnly = FilenameUtils.removeExtension(arcadeName);
        arcadeNameOnlyPNG = FilenameUtils.removeExtension(arcadeName);
      } 
       
      consoleName = consoleName.replace(" ", "_"); //had to add this as Dennis made the change to send the native console name with spaces as prior code and mapping tables assumed an _ instead of space
      consoleName = consoleName.toLowerCase();
      if (!consoleMatch(consoleArray, consoleName)) {
        consoleNameMapped = WebEnabledPixel.getConsoleMapping(consoleName);
      } else {
        consoleNameMapped = consoleName;
      }
      
      if (consoleNameMapped.equals("mame-libretro"))
        consoleNameMapped = "mame"; 
      
      //set the vars for the API of the current / last game
      setCurrentPlatformGame(consoleNameMapped,arcadeNameOnly);
      
      
      //let's find the matching PNG
      
      arcadeFilePathPNG = pixelHome + consoleNameMapped + "/" + arcadeNameOnlyPNG + ".png";
      File arcadeFilePNG = new File(arcadeFilePathPNG);
      
      if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory()) {
        arcadeNameOnlyPNG = FilenameUtils.removeExtension(arcadeName);
        
      } else {
        String arcadeNameOnlyUnderscore = arcadeNameOnlyPNG.replaceAll("_", " ");
        String arcadeFilePathPNGUnderscore = pixelHome + consoleNameMapped + "/" + arcadeNameOnlyUnderscore + ".png";
        arcadeFilePNG = new File(arcadeFilePathPNGUnderscore);
        
        if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory()) {
          arcadeNameOnlyPNG = arcadeNameOnlyUnderscore;
           
        } else {
          String arcadeNamelowerCase = arcadeNameOnlyPNG.toLowerCase();
          String arcadeFilePathPNGlowerCase = pixelHome + consoleNameMapped + "/" + arcadeNamelowerCase + ".png";
          arcadeFilePNG = new File(arcadeFilePathPNGlowerCase);
          if (arcadeFilePNG.exists() && !arcadeFilePNG.isDirectory())
            arcadeNameOnlyPNG = arcadeNamelowerCase; 
        } 
      } 
      
       // Now let's find the matching GIF
       // let's first check if there is a ( and if so, take only text to the left
       // then let's check if there is basename_01, basename_02, or basename_03
       // let's first check if there is a ( and if so , we'll take what is to the left
       // So here's our logic for the gifs, let's first check if arcadenameonly_03 exists and if so we'll take that and then increment down to arcadenameonly_02 for the next one
       
      
        int iend = arcadeNameOnly.indexOf("("); //this finds the first occurrence of "(" 
        //in string thus giving you the index of where it is in the string

        //String subString;
        if (iend != -1)  { //then there was a ( there
            arcadeNameOnly = (arcadeNameOnly.substring(0 , iend)).trim(); //this will the name to the left of (
            //System.out.println("parathesis is here: " + arcadeNameOnly);
            
            if (WebEnabledPixel.isWindows()) {
               arcadeNameOnly = arcadeNameOnly.replaceAll("_", " ").trim(); //windows will add an _ for a space so taking care of that here
               //System.out.println("windows call parathesis is here: " + arcadeNameOnly);
            }
        
            //ok now since we have a ( match for the GIF, let's also see if we have a matching PNG without the (
             String arcadeFilePathPNGTest = pixelHome + consoleNameMapped + "/" + arcadeNameOnly + ".png";
             File arcadeFilePNGTest = new File(arcadeFilePathPNGTest);
             if (arcadeFilePNGTest.exists() && !arcadeFilePNGTest.isDirectory()) {
                    arcadeNameOnlyPNG = arcadeNameOnly;
                    arcadeFilePNG = new File(arcadeFilePathPNGTest); //we have to set the new arcadeFilePNG here as we use it in the handlePNG call
             }     
        }  
        
       arcadeFilePathGIF = pixelHome + consoleNameMapped + "/" + arcadeNameOnly + "_0" + WebEnabledPixel.getAnimationNumber().toString() + ".gif";
       arcadeFileGIF = new File(arcadeFilePathGIF);
       
       if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
            arcadeNameOnly = FilenameUtils.getBaseName(arcadeFilePathGIF);
       } else {  //this means we did not find the multiple version so proceed how we were searching before
           
            arcadeFilePathGIF = pixelHome + consoleNameMapped + "/" + arcadeNameOnly + ".gif";
            arcadeFileGIF = new File(arcadeFilePathGIF);
      
            if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
              //arcadeNameOnly = FilenameUtils.removeExtension(arcadeName);  //not sure why this was here?
            } 
            else {
              String arcadeNameOnlyUnderscore = arcadeNameOnly.replaceAll("_", " ");
              String arcadeFilePathGIFUnderscore = pixelHome + consoleNameMapped + "/" + arcadeNameOnlyUnderscore + ".gif";
              arcadeFileGIF = new File(arcadeFilePathGIFUnderscore);

              if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory()) {
                arcadeNameOnly = arcadeNameOnlyUnderscore;
              } else {
                String arcadeNamelowerCase = arcadeNameOnly.toLowerCase();
                String arcadeFilePathGIFlowerCase = pixelHome + consoleNameMapped + "/" + arcadeNamelowerCase + ".gif";
                arcadeFileGIF = new File(arcadeFilePathGIFlowerCase);
                if (arcadeFileGIF.exists() && !arcadeFileGIF.isDirectory())
                  arcadeNameOnly = arcadeNamelowerCase; 
              } 
            } 
       } 
      
      //now that we have arcadeNameOnly, let's proceed
      
      String requestedPathPNG = pixelHome + consoleNameMapped + "\\" + arcadeNameOnlyPNG;
      String requestedPath = pixelHome + consoleNameMapped + "\\" + arcadeNameOnly;
      
      if (!CliPixel.getSilentMode()) {
            System.out.println("Looking for PNG: " + requestedPathPNG + ".png");
            LogMe.aLogger.info("Looking for PNG: " + requestedPathPNG + ".png");
            System.out.println("Looking for GIF: " + requestedPath + ".gif");
            LogMe.aLogger.info("Looking for GIF: " + requestedPath + ".gif");
      } 
      
     //lastlly let's get the console defaults
     
        consoleFilePathPNG = pixelHome + "console/default-" + consoleNameMapped + ".png";
        File consoleFilePNG = new File(consoleFilePathPNG);
        consoleFilePathGIF = pixelHome + "console/default-" + consoleNameMapped + ".gif";
        File consoleFileGIF = new File(consoleFilePathGIF);
        defaultConsoleFilePathPNG = pixelHome + "console/default-marquee.png";
        File defaultConsoleFilePNG = new File(defaultConsoleFilePathPNG);
   
    
    return new MarqueePath(requestedPathPNG,requestedPath,arcadeFilePNG,arcadeFileGIF,consoleNameMapped,consoleFilePathPNG,consoleFilePNG,consoleFilePathGIF,consoleFileGIF,defaultConsoleFilePathPNG,defaultConsoleFilePNG);
  
}

   

  public static boolean consoleMatch(String[] arr, String targetValue) {
    for (String s : arr) {
      if (s.equals(targetValue))
        return true; 
    } 
    return false;
  }
  
}