package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;
import ioio.lib.api.exception.ConnectionLostException;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

/**
 * @author Roberto Marquez
 */
public abstract class ImageResourceHttpHandler extends TextHttpHandler
{
    protected String basePath;
    protected String defaultImageClassPath;
    protected String modeName;
        
    @Override
    protected String getHttpText(HttpExchange exchange)
    {        
        String imageClassPath;
        
        try
        {
            URI requestURI = exchange.getRequestURI();
            String path = requestURI.getPath();
            int i = path.lastIndexOf("/") + 1;
            String name = path.substring(i);
            
            if(name.equals(modeName))
            {
                // this is just a request change to still image mode
                imageClassPath = defaultImageClassPath;
            }
            else
            {
                imageClassPath = basePath + name;
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
            System.out.println("loading " + modeName + " image");

            try
            {
                System.out.println("writing image resource to the Pixel");
                
                writeImageResource(imageClassPath);
                
                System.out.println(modeName + " image resource was written to the Pixel");
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
            return "request received for " + imageClassPath;
        }
    }
    
    protected abstract void writeImageResource(String imageClassPath) throws IOException, ConnectionLostException;
            
}
