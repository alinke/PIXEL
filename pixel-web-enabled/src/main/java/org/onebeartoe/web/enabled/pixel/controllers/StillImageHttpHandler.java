
package org.onebeartoe.web.enabled.pixel.controllers;

import ioio.lib.api.exception.ConnectionLostException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

/**
 * @author Roberto Marquez
 */
public class StillImageHttpHandler extends ImageResourceHttpHandler
{
    public StillImageHttpHandler(WebEnabledPixel application)
    {
        super(application);
        
        basePath = "images/";
        defaultImageClassPath = basePath + "Robot.png";
        modeName = "still";
    }
    
    @Override
    protected void writeImageResource(String imageClassPath) throws IOException, ConnectionLostException
    {
        System.out.println("loading new classpath URL for still: " + imageClassPath);
        URL url = getClass().getClassLoader().getResource(imageClassPath);
        System.out.println("URL for " + modeName + " loaded");
        
        BufferedImage originalImage = ImageIO.read(url);
        
        Pixel pixel = application.getPixel();
        pixel.stopExistingTimer();
        pixel.writeImagetoMatrix(originalImage, pixel.KIND.width, pixel.KIND.height);
    }        
}
