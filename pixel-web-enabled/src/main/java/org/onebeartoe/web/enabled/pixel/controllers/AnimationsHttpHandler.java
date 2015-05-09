
package org.onebeartoe.web.enabled.pixel.controllers;

import ioio.lib.api.exception.ConnectionLostException;
import java.io.IOException;
import java.util.logging.Level;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

/**
 * @author Roberto Marquez
 */
public class AnimationsHttpHandler extends ImageResourceHttpHandler
{
    
    
    public AnimationsHttpHandler(WebEnabledPixel application)
    {
        super(application);
        
        basePath = "animations/";
        defaultImageClassPath = basePath + "arrows.png";
        modeName = "animation";
    }
    
    @Override
    protected void writeImageResource(String imageClassPath) throws IOException, ConnectionLostException
    {
        logger.log(Level.INFO, "animation handler is writing " + imageClassPath);
        // the writeAnimation() method just take the name of the file
        int i = imageClassPath.lastIndexOf("/") + 1;
        String animationName = imageClassPath.substring(i);
            
        boolean saveAnimation = false;
        
        Pixel pixel = application.getPixel();
        pixel.writeAnimation(animationName, saveAnimation);
    }
}
