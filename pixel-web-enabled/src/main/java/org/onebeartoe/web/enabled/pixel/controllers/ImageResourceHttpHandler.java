package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;
import ioio.lib.api.exception.ConnectionLostException;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.WordUtils;
import org.onebeartoe.network.TextHttpHandler;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.getLCDMarqueeHostName;

/**
 * @author Roberto Marquez
 */
public abstract class ImageResourceHttpHandler extends TextHttpHandler
{
    protected String basePath;
    protected String defaultImageClassPath;
    protected String modeName;
    protected WebEnabledPixel application;
    protected Logger logger;
    LogMe logMe = null;
        
    public ImageResourceHttpHandler(WebEnabledPixel application)
    {
        String name = getClass().getName();
        logger = Logger.getLogger(name);
        
        this.application = application;
        
        //logMe = LogMe.getInstance();
        LogMe logMe = LogMe.getInstance();
    }
    
    @Override
    protected String getHttpText(HttpExchange exchange)
    {        
        String imageClassPath;
        
        
         //thought i might need to do this but turns out not
        //String encoding = "UTF-8";
        //exchange.getResponseHeaders().set("Content-Type", "text/html"); 
        //exchange.getRequestURI().set("Content-Type", "text/html; charset=" + encoding);
        //url is http://localhost:8080/arcade/stream/nes/marios brows.&l=0
        
        //String encoding = "UTF-8";
        //exchange.getResponseHeaders().set("Content-Type", "text/html; charset=" + encoding);
        
        //String encodedValue = "Hell%C3%B6%20W%C3%B6rld%40Java";

        // Decoding the URL encoded string
      
        
        //logMe.aLogger.info("RAW PATHA: " + exchange.getResponseBody()); 
        //System.out.println("RAW PATHA: " + exchange.getResponseBody());
        //logMe.aLogger.info("RAW PATHB: " + exchange.getRequestURI()); 
        //System.out.println("RAW PATHB: " + exchange.getRequestURI());
        //System.out.println("RAW PATHC: " + exchange.getLocalAddress());

        try {
            URI requestURI = exchange.getRequestURI();
            String path = requestURI.getPath();

            int i = path.lastIndexOf("/") + 1;
            String name = path.substring(i);
            
            if (WebEnabledPixel.getLCDMarquee().equals("yes")) {
                try {
                    if (InetAddress.getByName(getLCDMarqueeHostName()).isReachable(5000)){ //to do should we be checking everytime if reachable?
                        WebEnabledPixel.dxEnvironment = true;
                        
                        //if it's a console call, let's re-direct to arcade because pixelcade embedded doesn't know about the console calls
                        if (requestURI.getPath().contains("console")) {
                             System.out.println("Request Console Redirected: " + requestURI.getPath());
                             String consoleName = (requestURI.getPath().substring(requestURI.getPath().lastIndexOf("/") + 1)).toLowerCase();
                             String redirect = "/arcade/stream/mame/" + consoleName;
                             URL url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + redirect);
                             HttpURLConnection con = (HttpURLConnection) url.openConnection();
                             con.setRequestMethod("GET");
                             con.getResponseCode();
                             con.disconnect();
                        }
                        else {
                            //System.out.println("Requested: " + requestURI.getPath());  
                            
                            String string = requestURI.getPath();
                            String[] bits = string.split("/");
                            String gameName = bits[bits.length - 1];
                            
                            //String gameName = (requestURI.getPath().substring(requestURI.getPath().lastIndexOf("/") + 1));
                          
                            //System.out.println("Game name original: " + gameName);
                            
                            if(gameName.contains("_")) {
                            //if(gameName.contains("_") && WebEnabledPixel.isWindows()) { //only do this check if there is an _ and we are on Windows
                                
                                //we want to capital the first char after the delimineters of: space, left paranth, and comma
                                //for example on Windows with LEDBlinky, we will get this 007_-_NIGHTFIRE_(USA,_EUROPE)_(EN,FR,DE) which we will convert to 007 - Nightfire (Usa, Europe) (En,Fr,De)
                                
                                gameName = WordUtils.capitalizeFully(gameName.replace("_"," "),new char[]{' ', '(', ','});
                                gameName = gameName.replace("Usa","USA").replace("(Xbox)","(XBOX)".replace("Nfl ","NFL ").replace("Lego ","LEGO ").replace("Nba ","NBA ").replace("Nhl ","NHL ").replace("Mlb ","MLB ").replace("Bmx ","BMX ").replace("Gt ","GT ").replace("Ncaa ","NCAA ").replace("Ii ","II ").replace("Iii ","III ").replace("Nascar ","NASCAR ").replace("Espn ","ESPN ").replace("Sd ","SD "));
                                //System.out.println("Game name2 : " + gameName);
                                
                                String console = bits[bits.length - 2];
                                
                                String redirect = "/arcade/stream/" + console + "/" + gameName + ".jpg";
                                System.out.println("_ Detected and Redirected:" + redirect);
                                redirect = redirect.replace(" ","%20"); //if we don't add this, the URL call wont' make it and will be truncated at the spaces
                               
                                URL url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + redirect);
                                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                con.setRequestMethod("GET");
                                con.getResponseCode();
                                con.disconnect();
                            }
                            
                            else { //we're good and no need to rename the call, send as is
                                URL url = new URL("http://" + getLCDMarqueeHostName() + ":8080" + requestURI);
                                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                                con.setRequestMethod("GET");
                                con.getResponseCode();
                                con.disconnect();
                            }
                        }
                    }
                }catch (  Exception e){}
            }

            if(name.equals(modeName))
            {
                // this is just a request change to still image mode
                imageClassPath = defaultImageClassPath;
            }
             else if( path.contains("/animations/"))
            {
                imageClassPath = requestURI.toString(); //this returns /arcade/stream/mame/pacman?t=1?c=2?r=5
            }
            else if( path.contains("/save/"))
            {
                imageClassPath = path;
            }
            else if( path.contains("/console/"))
            { 
                imageClassPath = requestURI.toString(); //this returns /arcade/stream/mame/pacman?t=1?c=2?r=5
            }
            else if( path.contains("/arcade/"))
            {
                imageClassPath = requestURI.toString(); //this returns /arcade/stream/mame/pacman?t=1?c=2?r=5
            }
            else if( path.contains("/pinball/"))
            {
                imageClassPath = requestURI.toString(); //this returns /arcade/stream/mame/pacman?t=1?c=2?r=5
            }
             else if( path.contains("/localplayback"))
            {
                imageClassPath = requestURI.toString(); 
            }
            else
            {
                imageClassPath = basePath + name;
                
                //to do need to add text here too
            }
        }
        catch(Exception e)
        {
            imageClassPath = defaultImageClassPath;
            
            String message = "An error occurred while determining the image from the request.  " +
                             "The default is used now.";
            
            logger.log(Level.SEVERE, message, e);
        }

        try
        {
            //System.out.println("loading " + modeName + " image");

            try
            {
                //System.out.println("writing image resource to the Pixel");
                writeImageResource(imageClassPath);
                
                //System.out.println(modeName + " image resource was written to the Pixel");
            } 
            catch (ConnectionLostException ex)
            {
                String message = "connection lost";
                logger.log(Level.SEVERE, message, ex);
            }
        }
        catch (IOException ex)
        {
            String message = "error with image resource";
            logger.log(Level.SEVERE, message, ex);
        }
        finally
        {
            return "REST call received for " + imageClassPath;
        }
    }
    
  
    
    protected abstract void writeImageResource(String imageClassPath) throws IOException, ConnectionLostException;
            
    }
