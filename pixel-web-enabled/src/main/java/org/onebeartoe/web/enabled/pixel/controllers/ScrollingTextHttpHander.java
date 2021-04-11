
package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;
import ioio.lib.api.exception.ConnectionLostException;
import java.awt.Color;
import java.awt.Font;
import java.io.File;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.io.ByteOrderMark.UTF_8;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.onebeartoe.network.TextHttpHandler;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.getLCDMarqueeHostName;

/**
 Logic for LCD and LED Text Scrolling
LED Only - Scroll text on LED as normal
LCD Only - Scroll text on LCD as normal including sub-displays if there
LED + LCD - Scroll text on LED, do not scroll text on LCD
But note that alt text and text send to LCD scrolls on the smaller displays
Therefore we need to send a new flag that tells LCD that LED is there and don't scroll on LCD but still scroll on the sub displays
 **/

public class ScrollingTextHttpHander extends TextHttpHandler  //TO DO have TextHttpHandler send a return
{
    protected LCDPixelcade lcdDisplay = null;
    protected WebEnabledPixel app;
    
    public ScrollingTextHttpHander(WebEnabledPixel application)
    {
        //super(application);
    
        if(WebEnabledPixel.getLCDMarquee().equals("yes"))
            lcdDisplay = new LCDPixelcade(); //bombing out on windows here
    
        String name = getClass().getName();
        
        this.app = application;
    }

    @Override
    protected String getHttpText(HttpExchange exchange)
    {
        
        String text_ = null;
        String system_ = null;
        String game_ = null;
        String color_ = null;
        Color color = null;
        String speed_ = null;
        Long speed = null;
        String loop_ = null;
        int loop = 0;
        int scrollsmooth_ = 0;
        Long speeddelay_ = 10L;
        int fontSize_ = 0;
        int yOffset_ = 0;
        int lines_ = 1;
        String font_ = null;
        LogMe logMe = LogMe.getInstance();
        URI requestURI = exchange.getRequestURI();
        Font font = null;
        
         if (!CliPixel.getSilentMode()) {
             logMe.aLogger.info("Scrolling text handler received a request: " + requestURI);
             System.out.println("Scrolling text handler received a request: " + requestURI);
         }

         if (WebEnabledPixel.getLCDMarquee().equals("yes")) {  //this is where we relay the call to LCD
            try {
               if (InetAddress.getByName(getLCDMarqueeHostName()).isReachable(5000)){
                   WebEnabledPixel.dxEnvironment = true;
                   System.out.println("Requested: " + requestURI.getPath());
                
//                   //but we first need to check if there is a game param and if so add an extension if not there as LCD needs that extension
//                   String gameName = null;
//                   String textURL = requestURI.toString();
//                   
//                   try {
//                            Map<String, String> values = getUrlValues(requestURI.toString());
//                            gameName = values.get("game");
//                            //System.out.println("GAME: " + gameName);
//                       } catch (UnsupportedEncodingException e) {
//                       } 
//                   
//                   
//                   if (gameName != null) {       //if game is there, let's check if it has an extension with LCD eeds
//                       String gameNameExtension = null;
//                       gameNameExtension = FilenameUtils.getExtension(gameName);
//                             if (gameNameExtension.isEmpty()) {  //if its empty, then we need to add the extension
//                                    textURL = textURL.replace(gameName, gameName + ".jpg");
//                                    //System.out.println("temp URL: " + textURL);
//                             }
//                   }
//                   URL url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + textURL);

                    URL url = null;
                    if (WebEnabledPixel.getLCDLEDCompliment() == true && WebEnabledPixel.pixelConnected == true) { //then we need to add &led to the end of the URL params
                       String textURL = requestURI.toString();
                       url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + textURL + "&led"); //this flag tells LCD not to scroll as we already have LED scrolling
                    }
                    else {
                       url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + requestURI);
                    }

                   HttpURLConnection con = (HttpURLConnection) url.openConnection();
                   con.setRequestMethod("GET");
                   con.getResponseCode();
                   con.disconnect();
                   
               }
           }catch (  Exception e){}
        }
         
        String encodedQuery = requestURI.getQuery();
        
        if(encodedQuery == null)
        {
            text_ = "scrolling text";
            
             if (!CliPixel.getSilentMode()) {
                logMe.aLogger.info("scrolling default text");
                System.out.println("scrolling default text");
            }
        }
        else  {
            
            //we'll have something /text?t=hello world&c=red&s=100&l=5
            List<NameValuePair> params = null;
            try {
                    params = URLEncodedUtils.parse(new URI(requestURI.toString()), "UTF-8");
            } catch (URISyntaxException ex) {
            }

            for (NameValuePair param : params) {

                switch (param.getName()) {

                    case "t": //scrolling text value
                        text_ = param.getValue();
                        break;
                    case "c": //text color
                        color_ = param.getValue();
                        break;
                    case "l": //loop
                        loop_ = param.getValue();
                        break;
                    case "text": //scrolling text value
                        text_ = param.getValue();
                        break;
                    case "color": //text color
                        color_ = param.getValue();
                        break;
                    case "speed": //scrolling speed
                        speed_ = param.getValue();
                        break;
                    case "loop": //loop
                        loop_ = param.getValue();
                        break;
                    case "ss": //scroll smooth
                        scrollsmooth_ = Integer.valueOf(param.getValue());
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
                    case "scrollsmooth": //scroll smooth
                        scrollsmooth_ = Integer.valueOf(param.getValue());
                        break;
                    case "system": //scroll smooth
                        system_ = param.getValue();
                        break;
                    case "game": //scroll smooth
                        game_ = param.getValue();
                        break;
                }
            }

            /* TO DO catch this wrong URL format as I made this mistake of ? instead of & after the first one!!!!
            Scrolling text handler received a request: /text/?t=hello%20world?c=red?s=10?l=2
            t : hello world?c=red?s=10?l=2
            */
           
            }

        if (!CliPixel.getSilentMode()) {

            System.out.println("scrolling text: " + text_);
            if (color_ != null) System.out.println("text color: " + color_);
            if (speed_ != null) System.out.println("scrolling speed: " + speed_);
            //if (scrollsmooth_ != 1) System.out.println("scrolling smooth factor: " + scrollsmooth_);
            System.out.println("# times to loop: " + loop_);
            logMe.aLogger.info("scrolling text: " + text_);
            if (color_ != null) logMe.aLogger.info("text color: " + color_);
            if (speed_ != null) logMe.aLogger.info("scrolling speed: " + speed_);
            //if (scrollsmooth_ != 1) logMe.aLogger.info("scrolling smooth factor: " + scrollsmooth_);
            logMe.aLogger.info("# times to loop: " + loop_);
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
    if (loop_ != null)
      loop = Integer.valueOf(loop_).intValue(); 
    
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
    
    Pixel.setFontFamily(font_);
    
    if (yOffset_ == 0)
      yOffset_ = WebEnabledPixel.getDefaultyTextOffset(); 
    
    Pixel.setYOffset(yOffset_);
    
    if (fontSize_ == 0)
      fontSize_ = WebEnabledPixel.getDefaultFontSize(); 
    
    Pixel.setFontSize(fontSize_);
    
    if (lines_ == 2) 
        Pixel.setDoubleLine(true);
    else if (lines_ == 4)
         Pixel.setFourLine(true);
    else {
        Pixel.setDoubleLine(false); //don't forget to set it back
        Pixel.setFourLine(false); //don't forget to set it back
    }
    
    app.getPixel().scrollText(text_, loop, speed, color,WebEnabledPixel.pixelConnected,scrollsmooth_);
        
    /* if (game_ != null) {   //then this means we have a call to display text and then a marquee in one shot (we needed to do this for LCD)                  
            
            //scroll the text and start the Q
            app.getPixel().scrollText(text_, loop, speed, color,WebEnabledPixel.pixelConnected,scrollsmooth_); // we do so scroll text and then play the game marquee

            MarqueePath paths = null;
            paths = getPNGandGIFMatch.getPaths(game_,system_);

//            System.out.println("PNGPath = " + paths.PNGPath);
//            System.out.println("GIFPath = " + paths.GIFPath);
//            System.out.println("PNGFile = " + paths.PNGFile);
//            System.out.println("GIFFile = " + paths.GIFFile);
//            System.out.println("Console Name Mapped = " + paths.ConsoleNameMapped);
//            System.out.println("Console PNG Path = " + paths.ConsolePNGPath);
//            System.out.println("Console PNG File = " + paths.ConsolePNGFile);
//            System.out.println("Console GIF Path = " + paths.ConsoleGIFPath);
//            System.out.println("Console GIF File = " + paths.ConsoleGIFFile);
//            System.out.println("Console Default PNG Path = " + paths.DefaultConsolePNGPath);
//            System.out.println("Console Default PNG File = " + paths.DefaultConsolePNGFile);
            

            if (paths.PNGFile.exists() && !paths.PNGFile.isDirectory() && paths.GIFFile.exists() && !paths.GIFFile.isDirectory()) {  //if there is both a png and a gif, we'll play gif and then png
                   
                    if (app.getPixel().getLoopStatus() == false) {  //we're not looping so we can interrupt with black frame
                        try {
                           app.getPixel().writeArcadeImage(paths.PNGFile, false, loop, "black", "nodata",WebEnabledPixel.pixelConnected);
                            // handlePNG(arcadeFilePNG, Boolean.valueOf(false), 0, "black", "nodata");
                        } catch (IOException ex) {
                            Logger.getLogger(ScrollingTextHttpHander.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    if (loop == 0 || loop == 99999) {  //we'll need to loop the GIF in the Q before the PNG plays, had to add 99999 because the gif will loop on 99999 forever and not get to the PNG
                       loop = 1; 
                    }
                    System.out.println("LOOP: " + loop_);
                    
                    //so now let's play the GIF and then the PNG
                    
                    try {
                         app.getPixel().writeArcadeAnimation(paths.ConsoleNameMapped, FilenameUtils.getName(paths.GIFPath) + ".gif", false, loop, WebEnabledPixel.pixelConnected);
                      } catch (NoSuchAlgorithmException ex) {
                          Logger.getLogger(ScrollingTextHttpHander.class.getName()).log(Level.SEVERE, null, ex);
                      }
                      try {
                       
                          app.getPixel().writeArcadeImage(paths.PNGFile, false, 99999, paths.ConsoleNameMapped, FilenameUtils.getName(paths.PNGPath) + ".png", WebEnabledPixel.pixelConnected);

                          //to do known issue here in that if scrolling through front end and one with gif and png are selected back to back, the second one won't interrupt and must complete before the next
                      } catch (IOException ex) {
                          Logger.getLogger(ScrollingTextHttpHander.class.getName()).log(Level.SEVERE, null, ex);
                      }

            } else if (paths.PNGFile.exists() && !paths.PNGFile.isDirectory()) {
                    try {
                        app.getPixel().writeArcadeImage(paths.PNGFile, false, 99999, paths.ConsoleNameMapped, FilenameUtils.getName(paths.PNGPath) + ".png", WebEnabledPixel.pixelConnected);
                    } catch (IOException ex) {
                        Logger.getLogger(ScrollingTextHttpHander.class.getName()).log(Level.SEVERE, null, ex);
                    }
            } 

            else if (paths.GIFFile.exists() && !paths.GIFFile.isDirectory()) {
                    try {
                        app.getPixel().writeArcadeAnimation(paths.ConsoleNameMapped, FilenameUtils.getName(paths.GIFPath) + ".gif", false, 99999, WebEnabledPixel.pixelConnected);
                    } catch (NoSuchAlgorithmException ex) {
                        Logger.getLogger(ScrollingTextHttpHander.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }

            else if (paths.ConsoleGIFFile.exists() && !paths.ConsoleGIFFile.isDirectory()) {
                try {
                        app.getPixel().writeArcadeAnimation("console", FilenameUtils.getName(paths.ConsoleGIFPath) + ".gif", false, 99999, WebEnabledPixel.pixelConnected);
                    } catch (NoSuchAlgorithmException ex) {
                        Logger.getLogger(ScrollingTextHttpHander.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
            
            else if (paths.ConsolePNGFile.exists() && !paths.ConsoleGIFFile.isDirectory()) {
                try {
                      app.getPixel().writeArcadeImage(paths.ConsolePNGFile, false, 99999, "console", FilenameUtils.getName(paths.ConsolePNGPath) + ".png", WebEnabledPixel.pixelConnected);
                    } catch (IOException ex) {
                    Logger.getLogger(ScrollingTextHttpHander.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if (paths.DefaultConsolePNGFile.exists() && !paths.DefaultConsolePNGFile.isDirectory()) {
                try {
                      app.getPixel().writeArcadeImage(paths.DefaultConsolePNGFile, false, 99999, "console", FilenameUtils.getName(paths.DefaultConsolePNGPath) + ".png", WebEnabledPixel.pixelConnected);
                    } catch (IOException ex) {
                    Logger.getLogger(ScrollingTextHttpHander.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else {
                System.out.println("[MISSING MARQUEE] " + paths.DefaultConsolePNGPath + " does not exist");
            }
            
        
     } else {  //we're just scrolling text
         app.getPixel().scrollText(text_, loop, speed, color,WebEnabledPixel.pixelConnected,scrollsmooth_);
     }
    
    */
    
    
    
      //TO DO is this needed?
      
//    if (WebEnabledPixel.getLCDMarquee().equals("yes")) {
//                if(lcdDisplay == null)
//                   lcdDisplay = new LCDPixelcade();
//                
//            lcdDisplay.setNumLoops(loop);    
//            lcdDisplay.scrollText(text_, new Font(font_, Font.PLAIN, 288), color, 5); //int speed
//            //lcdDisplay.scrollText(text_, new Font(font_, Font.PLAIN, 288), color, 40); //int speed
//    }
        
    return "scrolling text request received: " + text_ ;
    
    }
    
public static Map<String, String> getQueryMap(String query) {  
    String[] params = query.split("&");  
    Map<String, String> map = new HashMap<String, String>();

    for (String param : params) {  
        String name = param.split("=")[0];  
        String value = param.split("=")[1];  
        map.put(name, value);  
    }  
    return map;  
}

public Map<String, String> getUrlValues(String url) throws UnsupportedEncodingException {
    int i = url.indexOf("?");
    Map<String, String> paramsMap = new HashMap<>();
    if (i > -1) {
        String searchURL = url.substring(url.indexOf("?") + 1);
        String params[] = searchURL.split("&");

        for (String param : params) {
            String temp[] = param.split("=");
            paramsMap.put(temp[0], java.net.URLDecoder.decode(temp[1], "UTF-8"));
        }
    }

    return paramsMap;
}


 
    
}