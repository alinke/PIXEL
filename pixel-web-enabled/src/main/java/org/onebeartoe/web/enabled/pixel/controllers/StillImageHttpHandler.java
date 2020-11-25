
package org.onebeartoe.web.enabled.pixel.controllers;

import ioio.lib.api.exception.ConnectionLostException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.getLCDMarqueeHostName;

/**
 * @author Roberto Marquez
 */
public class StillImageHttpHandler extends ImageResourceHttpHandler
{
    public StillImageHttpHandler(WebEnabledPixel application)
    {
        super(application);
        
        basePath = "images/";
        defaultImageClassPath = basePath + "btime.png";
        modeName = "still";
    }
    
    @Override
    protected void writeImageResource(String imageClassPath) throws IOException, ConnectionLostException
    {
        //ImageClassPath is for example /images/1941.png
        
        LogMe logMe = LogMe.getInstance();
        
         if (!CliPixel.getSilentMode()) {
            System.out.println("loading new classpath URL for still: " + imageClassPath);
            logMe.aLogger.info("loading new classpath URL for still: " + imageClassPath);
        }
         
        URL url = getClass().getClassLoader().getResource(imageClassPath);
        
        if (!CliPixel.getSilentMode()) {
            System.out.println("URL for " + modeName + " loaded");
            logMe.aLogger.info("URL for " + modeName + " loaded");
        }
        
        String path = "";
        boolean saveAnimation = false;  //TO DO: get the save working here
        BufferedImage image;
        
        //imageClassPath will be either images/1942.png or /still/save/1942.png
        
        String arcadeName = FilenameUtils.getName(imageClassPath); //get the name only WITH extension
        String ext = FilenameUtils.getExtension(imageClassPath); //get the extension, we want to know if PNG or GIF   
            
        //path = application.getPixel().getPixelHome() + imageClassPath; //home/pixelcade/images/1941.gif so full path
        //path = application.getPixel().getPixelHome() + "images/" + arcadeName; //to do need to regression test this
        path = WebEnabledPixel.getHome() + "images/" + arcadeName; //to do need to regression test this
        File targetFilePath = new File(path);
        url = targetFilePath.toURI().toURL();
        
        if( imageClassPath.contains("/save/"))  saveAnimation = true;
          
        //now let's find out if PNG or GIF
        
        System.out.println("arcadeNameOnly: " + arcadeName);
        System.out.println("ext: " + ext);
        System.out.println("path to file: " + path);
        
        
        if (WebEnabledPixel.getLCDMarquee().equals("yes")) {
            try {
                if (InetAddress.getByName(getLCDMarqueeHostName()).isReachable(5000)){
                    WebEnabledPixel.dxEnvironment = true;
                    System.out.println("Requested: " + "/arcade/stream/mame/" + arcadeName);
                    URL url2 = new URL("http://" + getLCDMarqueeHostName() + ":8080" + "/arcade/stream/mame/" + arcadeName);
                    HttpURLConnection con = (HttpURLConnection) url2.openConnection();
                    con.setRequestMethod("GET");
                    con.getResponseCode();
                    con.disconnect();
                }
            }catch (  Exception e){}
        }
        
        if (ext.equals("gif")) {
            
            try {
               
                if (!CliPixel.getSilentMode()) {
                    System.out.println("Arcade Marquee Handler sending GIF: " + path);
                    logMe.aLogger.info("Arcade Marquee Handler sending GIF: " + path);
                }

                Pixel pixel = application.getPixel();
                
                pixel.writeArcadeAnimation("images",arcadeName,saveAnimation,0, WebEnabledPixel.pixelConnected); //since this class handles pngs and gifs that are served up, we won't have a loop here so pass 0
                    } catch (NoSuchAlgorithmException ex) {
                        Logger.getLogger(StillImageHttpHandler.class.getName()).log(Level.SEVERE, null, ex);
                 }
        } 
        else if (ext.equals("png")) {
            
            Pixel pixel = application.getPixel();
            pixel.writeArcadeImage(targetFilePath, saveAnimation, 0,"","",WebEnabledPixel.pixelConnected); //since this class handles pngs and gifs that are served up, we won't have a loop and won't need the console and png names
            
            /*
             
             image = ImageIO.read(url);
        
            if (!CliPixel.getSilentMode()) {
                    System.out.println("buffered image created for: " + url.toString());
                    logMe.aLogger.info("buffered image created for: " + url.toString());
            }
            
            //to do : we need to move this to pixel.java 
            
            //Pixel pixel = application.getPixel();
            pixel.stopExistingTimer( null, null, null, null, null, null);  //a timer could be running from a gif so we need to kill it here
        
            //if (saveAnimation && pixel.getPIXELHardwareID().substring(0,4).equals("PIXL")) {
            if (saveAnimation) {
            
                pixel.interactiveMode();
                pixel.writeMode(10);

                 try {
                       pixel.writeImagetoMatrix(image, pixel.KIND.width, pixel.KIND.height);
                } catch (ConnectionLostException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                } 

                try {
                    Thread.sleep(100); //this may not be needed but was causing a problem on the writes for the gif animations so adding here to be safe
                    //TO DO will a smaller delay still work too?
                } catch (InterruptedException ex) {
                    Logger.getLogger(ArcadeHttpHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

                 pixel.playLocalMode();
            
            } else {

                pixel.interactiveMode();
                pixel.writeImagetoMatrix(image, pixel.KIND.width, pixel.KIND.height); //to do add save parameter here
            }   */ 
        } 
        else {
            
            System.out.println("**** ERROR **** Sorry only PNG and GIF are supported, cannot handle " + path);
            logMe.aLogger.severe("Sorry only PNG and GIF are supported, cannot handle " + path);
        }
    }        
}
