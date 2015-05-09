
package org.onebeartoe.web.enabled.pixel.controllers;

import ioio.lib.api.exception.ConnectionLostException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.onebeartoe.pixel.hardware.Pixel;

/**
 * @author Roberto Marquez
 */
public class AnimationsApiHttpHandler extends ApiHttpHandler
{
    public AnimationsApiHttpHandler()
    {
        basePath = "animations/";
        defaultImageClassPath = basePath + "arrows.png";
        modeName = "animationtest";
    }
    @Override
    protected List<String> getList()
    {
        return getApp().animationImageNames;
    }
    
    @Override
    protected void writeImageResource(String imageClassPath) throws IOException, ConnectionLostException
    {
        logger.log(Level.INFO, "animation handler is writing " + imageClassPath);
        // the writeAnimation() method just take the name of the file
        int i = imageClassPath.lastIndexOf("/") + 1;
        String animationName = imageClassPath.substring(i);
            
        boolean saveAnimation = false;
        
        Pixel pixel = app.getPixel();
        pixel.writeAnimation(animationName, saveAnimation);
    }
}