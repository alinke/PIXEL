    
package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.eh.core.model.FileInfo;
import org.eh.core.util.FileUploadContentAnalysis;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.logMe;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.pixelwebVersion;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.net.URLCodec;

/**
 * The classes from this Github repository are used:
 * https://github.com/NotBadPad/easy-httpserver
 * @author Roberto Marquez
 */
public class UploadPlatformHttpHandler implements HttpHandler//extends TextHttpHandler
{
    protected WebEnabledPixel application;
    
    private List<String> uploadOrigins;
    
    public UploadPlatformHttpHandler(WebEnabledPixel application)
    {
        this.application = application;
        
        uploadOrigins = new ArrayList();
    }

    protected String handleUpload(HttpExchange httpExchange)
    {
        
       
        Headers headers = httpExchange.getRequestHeaders();
        // get ContentType
        String contentType = headers.get("Content-type").toString().replace("[", "")
                        .replace("]", "");

        // get content length
        int length = Integer.parseInt(headers.get("Content-length").toString().replace("[", "")
                        .replace("]", ""));

        StringBuilder issues = new StringBuilder();
        
        try
        {
            InputStream requestBody = httpExchange.getRequestBody();
            Map<String, Object> map = FileUploadContentAnalysis.parse(requestBody, contentType, length);
            
             //************* to do remove this logging later
             
            /*
            String uploadTypeKey2 = "upload-type";
            String s2 = (String) map.get(uploadTypeKey2);
            logMe.aLogger.info("upload-type: " + s2);
            System.out.println("upload-type: " + s2);
            
            String consoleKey2 = "console";
            String PixelcadeConsole2 = (String) map.get(consoleKey2);
            //to do remove this logging later
            logMe.aLogger.info("console: " + PixelcadeConsole2);
            System.out.println("console: " + PixelcadeConsole2);
             
            String displayNowKey2 = "displaynow";
            String displayNow2 = (String) map.get(displayNowKey2);
            //to do remove this logging later
            logMe.aLogger.info("displaynow: " + displayNow2);
            System.out.println("displaynow: " + displayNow2);
            */
            
            //**** keep ***
            String uploadKey = "upload";
            FileInfo fileInfo = (FileInfo) map.get(uploadKey);
            //**************
            
            String outpath2 = fileInfo.getFilename();
           
                
            logMe.aLogger.info("upload file path: " + outpath2);
            System.out.println("upload file path: " + outpath2);
            
            //********************************
            
            if(fileInfo == null)
            {
                issues.append("No file was posted with request, for param named: " + uploadKey);
                logMe.aLogger.info("No file was posted with request, for param named: " + uploadKey);
                System.out.println("No file was posted with request, for param named: " + uploadKey);
            }
            else
            {
                String uploadTypeKey = "upload-type";
                String s = (String) map.get(uploadTypeKey);
                 if(s == null) {
                     s = "STILL_IMAGE";
                }   
                
                String path = null;
                        
                UploadType type = UploadType.valueOf(s);
                
                String consoleKey = "console";
                String PixelcadeConsole = (String) map.get(consoleKey);
                
                if(PixelcadeConsole == null) {
                     logMe.aLogger.info("console was not passed, defaulting to mame");
                     System.out.println("console was not passed, defaulting to mame");
                     PixelcadeConsole = "mame";
                }   
                
                String displayNowKey = "displaynow";
                String displayNow = (String) map.get(displayNowKey);
                
                if(displayNow == null) {
                     logMe.aLogger.info("displaynow was not passed, should be 1 to display now and 0 to not display now, defaulting to 0");
                     System.out.println("displaynow was not passed, should be 1 to display now and 0 to not display now, defaulting to 0");
                     displayNow = "0";
                } 
                
                uploadOrigins.add(type.name());
                
                switch(type)
                {
                    case ANIMATED_GIF:
                    {
                        path = WebEnabledPixel.getHome() + "/" + PixelcadeConsole + "/"; 
                        
                        break;
                    }
                    case STILL_IMAGE:
                    {
                        
                        path = WebEnabledPixel.getHome() + "/" + PixelcadeConsole + "/"; 
                        
                        break;
                    }
                    default:
                    {
                        logMe.aLogger.info("upload-type was not passed");
                        System.out.println("upload-type was not passed");
                        throw new Exception("upload-type was not passed");
                    }
                }

               
                String outpath = path + fileInfo.getFilename();
                //String outpath = path + fileInfo.getFilename().toLowerCase();  //to do upper case is not working for some reason so converting everything here to lower case
                
                FileOutputStream fos = new FileOutputStream(outpath);
                fos.write(fileInfo.getBytes());
                fos.close();
                
                logMe.aLogger.info("New file upload complete: " + outpath);
                System.out.println("New file upload complete: " + outpath);
                
                //now that file has been uploaded, let's display it if the flag was set
                if (displayNow.equals("1")) {
                    ArcadeHttpHandler streamImage = new ArcadeHttpHandler(application);
                    //String url = "/arcade/stream/" + PixelcadeConsole + "/" + encodeValue(fileInfo.getFilename()) + "?l=0";
                    String encodedFilePath = new URLCodec().encode(fileInfo.getFilename()).replace("+","%20"); //these encoders all add a + for a space so quick hack to change that to %20
                    //String encodedFilePath = new URLCodec().encode(fileInfo.getFilename().toLowerCase()).replace("+","%20");
                    
                    String url = "/arcade/stream/" + PixelcadeConsole + "/" + encodedFilePath + "?l=0";
                    streamImage.writeImageResource(url);
                }
                
            }
        }
        catch (Exception ex)
        {
            String message = ex.getMessage();
            issues.append(message);
            
            Logger.getLogger(UploadHttpHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        String response = "made it back";
        
        return contentType + "\n" + 
               response + "\n" + 
               issues.toString();
    }
    
    private static String encodeValue(String value) throws UnsupportedEncodingException {
      
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
       
    }

    public String getLastUploadOrigin()
    {
        String lastUploadOrigin;
        
        if(uploadOrigins.size() < 1)
        {
            lastUploadOrigin = "ALL-CONSUMED";
        }
        else
        {
            lastUploadOrigin = uploadOrigins.remove(0);
        }
            
        return lastUploadOrigin;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        handleUpload(exchange);

//TODO: Pull the HTML document from a static file in the distribution JAR        
        String response = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
"\n" +
"<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
"\n" +
"    <head>\n" +
"<META HTTP-EQUIV=REFRESH CONTENT=\"1; URL=files/index.html\">" +
"	            \n" +
"        <title>Upload Redirect</title>\n" +
"    </head>\n" +
"\n" +
"    <body>\n" +
"        redirecting...\n" +
"    </body>\n" +
"</html>";
        
        exchange.sendResponseHeaders(302, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    
    enum UploadType
    {
        ANIMATED_GIF,
        STILL_IMAGE
    }
}
