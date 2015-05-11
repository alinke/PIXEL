package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import ioio.lib.api.exception.ConnectionLostException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import com.google.gson.Gson;
import org.onebeartoe.web.enabled.pixel.ApiResponse;

/**
 * @author Roberto Marquez
 */
public abstract class ApiHttpHandler extends PixelHttpHandler
{
    protected String basePath;
    protected String defaultImageClassPath;
    protected String modeName;
    static String apiPrefix = "/api/";
    static int HTTP_OK=200;
    static int HTTP_CREATED=201;
    static int HTTP_NOT_MODIFIED=304;
    static int HTTP_BAD_REQUEST=400;
    static int HTTP_NOT_FOUND=404;
    static int HTTP_INTERNAL_SERVER_ERROR=500;
    
    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        ApiResponse response;
        switch (exchange.getRequestMethod()) {
            case "POST":
                response = postHttp(exchange);
                break;
            case "PUT":
                response = putHttp(exchange);
                break;
            case "DELETE":
                response = deleteHttp(exchange);
                break;
            case "GET": default:
                response = getHttp(exchange);
                break;
        }
        Headers responseHeaders=exchange.getResponseHeaders();
        responseHeaders.set("Content-Type","application/json");
        exchange.sendResponseHeaders(response.getHttpCode(), response.getHttpResponse().length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getHttpResponse().getBytes());
        os.close();
    }

    protected ApiResponse postHttp(HttpExchange exchange)
    {
        ApiResponse response = new ApiResponse();
        return response;
    }

    protected ApiResponse putHttp(HttpExchange exchange)
    {
        ApiResponse response = new ApiResponse();
        return response;
    }

    protected ApiResponse deleteHttp(HttpExchange exchange)
    {
        ApiResponse response = new ApiResponse();
        return response;        
    }

    protected ApiResponse getHttp(HttpExchange exchange)
    {        
        String imageClassPath;
        ApiResponse response = new ApiResponse();
        Gson gson = new Gson();

        try
        {
            URI requestURI = exchange.getRequestURI();
            String path[] = requestURI.getPath().split("/");
            String messageTest = "path=";

            for (String token : path)
            {
                messageTest = messageTest + "/" + token;
            }
            logger.log(Level.INFO, messageTest);

            if (path.length == 3)
            {
                response.setHttpResponse(gson.toJson(getList()));
                response.setHttpCode(HTTP_OK);
                return response;
            } else if (path.length == 4)
            {
                imageClassPath = basePath + path[2];
            } else
            {
                imageClassPath = defaultImageClassPath;
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
            response.setHttpResponse("request received for " + imageClassPath, "json");
            response.setHttpCode(HTTP_OK);
            return response;
        }
    }
    
    protected abstract List<String> getList();

    protected abstract void writeImageResource(String imageClassPath) throws IOException, ConnectionLostException;
            
}
