
package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import org.onebeartoe.network.TextHttpHandler;
import org.onebeartoe.pixel.LogMe;
import org.onebeartoe.pixel.hardware.Pixel;
import org.onebeartoe.web.enabled.pixel.CliPixel;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

/**
 * @author Roberto Marquez
 */
public class ShutdownHttpHandler extends TextHttpHandler
{
    protected WebEnabledPixel application;
    
    public ShutdownHttpHandler(WebEnabledPixel application)
    {
        this.application = application;
    }

    @Override
    protected String getHttpText(HttpExchange exchange)
    {
        LogMe logMe = LogMe.getInstance();
        
         String returnMessage = null;
        
        if (WebEnabledPixel.isUnix()) {  
            
            System.out.println("Received shutdown command...");
            logMe.aLogger.info("Received shutdown command...");
            
            ProcessBuilder processBuilder = new ProcessBuilder();

            // -- Linux --

            // Run a shell command
            processBuilder.command("bash", "-c", "sudo shutdown -h now > shutdowncommandresult");

             // Run a shell script
            //processBuilder.command("path/to/hello.sh");

            // -- Windows --

            // Run a command
            //processBuilder.command("cmd.exe", "/c", "dir C:\\Users\\mkyong");

            // Run a bat file
            //processBuilder.command("C:\\Users\\mkyong\\hello.bat");

            try {

                    Process process = processBuilder.start();

                    StringBuilder output = new StringBuilder();

                    BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                            output.append(line + "\n");
                    }

                    int exitVal = process.waitFor();
                    if (exitVal == 0) {
                            returnMessage = "Shutdown command sent with result:\n";
                            returnMessage = returnMessage + output;
                            System.out.println(returnMessage);
                            System.exit(1);
                            
                    } else {
                            //abnormal...
                            returnMessage = "Shutdown command sent:\n";
                            returnMessage = returnMessage + output;
                            System.out.println(returnMessage);
                    }

                    } catch (IOException e) {
                            e.printStackTrace();
                    } catch (InterruptedException e) {
                            e.printStackTrace();
                    }
           
        } 
        else {
            returnMessage = "Sorry, shutdown command only available on Raspberry Pi and Linux";
            System.exit(1);
            System.out.println(returnMessage);
            logMe.aLogger.info(returnMessage);
        }
        
        return returnMessage;
    }
}

