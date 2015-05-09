
package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;
import java.awt.Color;
import java.net.URI;
import org.onebeartoe.network.TextHttpHandler;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

/**
 * @author Roberto Marquez
 */
public class ScrollingTextColorHttpHandler extends TextHttpHandler
{
    protected WebEnabledPixel application;
    
    public ScrollingTextColorHttpHandler(WebEnabledPixel application)
    {
        this.application = application;
    }
    
    @Override
    protected String getHttpText(HttpExchange exchange)
    {
        URI requestURI = exchange.getRequestURI();
        String path = requestURI.getPath();
        int i = path.lastIndexOf("/") + 1;
        String hex = path.substring(i);

        Color color = hex2Rgb(hex);
// I think in head less environment decode() did not work        
//        Color color = Color.decode(hex);
        
        Pixel pixel = application.getPixel();
        pixel.stopExistingTimer();
        pixel.setScrollTextColor(color);
        pixel.scrollText();
        
        return "scrolling text color update received:" + hex;
    }
    
    /**
     * 
     * @param colorStr e.g. "FFFFFF"
     * @return 
     */
    public static Color hex2Rgb(String colorStr) 
    {
        return new Color(
                Integer.valueOf( colorStr.substring( 0, 2 ), 16 ),
                Integer.valueOf( colorStr.substring( 2, 4 ), 16 ),
                Integer.valueOf( colorStr.substring( 4, 6 ), 16 ) );
    }    
}
