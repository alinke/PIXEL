
package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;
import java.util.List;
import org.onebeartoe.network.TextHttpHandler;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

/**
 * @deprecated use the version in the onebeartoe Java Libraries
 * 
 * @author Roberto Marquez
 */
@Deprecated
public abstract class ListHttpHandler extends TextHttpHandler
{
    protected WebEnabledPixel application;
    
    public ListHttpHandler(WebEnabledPixel application)
    {
        this.application = application;
    }
    
    @Override
    protected String getHttpText(HttpExchange t)
    {
        StringBuilder response = new StringBuilder();
        
        List<String> list = getList();
        for(String name : list)
        {
            response.append(name);
            response.append("\n");
            response.append("-+-");
            response.append("\n");
        }

        return response.toString();
    }
    
    protected abstract List<String> getList();
}
