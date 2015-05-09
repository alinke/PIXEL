
package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.onebeartoe.network.TextHttpHandler;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

/**
 * @author Roberto Marquez
 */
public class ScrollingTextHttpHander extends TextHttpHandler
{
    private Logger logger;
    
    protected WebEnabledPixel app;
    
    public ScrollingTextHttpHander(WebEnabledPixel application)
    {
        String name = getClass().getName();
        logger = Logger.getLogger(name);
        
        this.app = application;
    }

    @Override
    protected String getHttpText(HttpExchange exchange)
    {
        URI requestURI = exchange.getRequestURI();
        
        logger.log(Level.INFO, "Scrolling text handler received a request: " + requestURI);

        String encodedQuery = requestURI.getQuery();

        String text = null;
        
        if(encodedQuery == null)
        {
            text = "scolling text is not set";

            logger.log(Level.INFO, "scrolling default text");            
        }
        else
        {
            try
            {
                String query = URLDecoder.decode(encodedQuery, "UTF-8");
                String[] parameters = query.split("&");
                logger.log(Level.INFO, "parameters: " + parameters);
                if(parameters != null && parameters.length > 0)
                {
                    String command = parameters[0];
                    String [] strs = command.split("=");
                    String t = strs[0];
                    text = strs[1];

                    logger.log(Level.INFO, "scrolling custom message:" + text);
                }
                else
                {
                    text = "error processing request";
                }
            } 
            catch (UnsupportedEncodingException ex)
            {
                logger.log(Level.SEVERE, "The scrolling text parameters could not be decoded.", ex);
            }
        }
        
        app.getPixel().setScrollingText(text);
        app.getPixel().scrollText();
        
        return "scrolling text request received";
    }
}
