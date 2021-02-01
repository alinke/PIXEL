
package org.onebeartoe.web.enabled.pixel.controllers;

import ioio.lib.api.exception.ConnectionLostException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.system.Sleeper;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.PixelLogFormatter;

/**
 * @author Roberto Marquez
 */
public class QuitHttpHandler extends ImageResourceHttpHandler
{
    public QuitHttpHandler(WebEnabledPixel application)
    {
        super(application);
      
    }
    
    @Override
    protected void writeImageResource(String urlParams) throws IOException, ConnectionLostException
    { 
        LogMe logMe = LogMe.getInstance();
        System.out.println("Received quit command, now exiting...");
        logMe.aLogger.info("Received quit command, now exiting...");
        System.exit(1);
   }
    
}
