
package org.onebeartoe.pixel;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.pc.IOIOConsoleApp;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.onebeartoe.pixel.hardware.Pixel;

/**
 * @author Roberto Marquez
 */
public class PixelIntegration extends IOIOConsoleApp    
{
    private Logger logger;
 
    private Pixel pixel;
    
    List<LedMatrixListener> matrixListeners;
    
    List<IoioListener> ioioListeners;
    private int retries = 0;
    
    /**
     * @param envronment 
     */
    public PixelIntegration(PixelEnvironment envronment)
    {
        String className = getClass().getName();
        logger = Logger.getLogger(className);
        
        matrixListeners = new ArrayList();        
        ioioListeners = new ArrayList();

// THE PIXEL CLASS SHOULD TAKE A PixelEnvironment as teh contructor's paramter
// INSTEAD TWO SEAPARATE ARUGEMSNT OF THE SAME CLASS        
        pixel = new Pixel(envronment.LED_MATRIX, envronment.currentResolution);
    }
    
    public void addIoioListener(IoioListener listener)
    {
        ioioListeners.add(listener);
    }
    
    public void addLedMatrixListener(LedMatrixListener listener)
    {
        matrixListeners.add(listener);
    }
        
    /**
     * can you belive this was what was not letting the app connect to the PIXEL?
     * @param args
     * @throws IOException 
     */
    @Override
    protected void run(String[] args) throws IOException 
    {
        System.out.println("now it begins!");

        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(isr);
        boolean abort = false;
        String line;
        while (!abort && (line = reader.readLine()) != null) 
        {
            if (line.equals("t")) 
            {
                //ledOn_ = !ledOn_;
            } 
            else if (line.equals("q")) {
                abort = true;
                System.exit(1);
            } 
            else 
            {
                System.out.println("Unknown input. q=quit.");
            }
        }
    }

    @Override
    public IOIOLooper createIOIOLooper(String connectionType, Object extra)
    {
        IOIOLooper looper = new BaseIOIOLooper() 
        {

            @Override
            public void disconnected() 
            {
                String message = "PIXEL was Disconnected";
                System.out.println(message);
            }

            @Override
            public void incompatible() 
            {
                String message = "Incompatible Firmware Detected";
                System.out.println(message);
            }

            @Override
            protected void setup() throws ConnectionLostException, InterruptedException
            {
                pixel.matrix = ioio_.openRgbLedMatrix(pixel.KIND);
                pixel.ioiO = ioio_;

//TODO: pass teh matrix object to all listeners                 
                for(LedMatrixListener listener : matrixListeners)
                {
                    listener.ledMatrixReady(pixel.matrix);
                }
                
//TODO: pass the IOIO IOIO to all listeners               
                for(IoioListener ioioListener : ioioListeners)
                {
                    ioioListener.ioioReady(pixel.ioiO);
                }

                StringBuilder message = new StringBuilder();

                if(pixel.matrix == null)
                {
                    message.append("wtffff" + "\n");
                }
                else
                {
                    message.append("Found PIXEL: " + pixel.matrix + "\n");
                }



                message.append("You may now interact with the PIXEL!\n");

//TODO: Load something on startup

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! WE NEED THIS FOR API CLIENTS
//TODO: MOVE THIS TO A MTHOS THAT CANCELAS ALL SEARCH TIMERS THAT WERE ADDED TO 
//      THE SEARCH LISTENERS LSIT                
//                searchTimer.cancel(); //need to stop the timer so we don't still display the pixel searching message

                message.append("PIXEL Status: Connected");

                logger.log(Level.INFO, message.toString());
            }
        };

        return looper;
    }
    
    public void initialize()
    {
        try
        {
            System.out.println("PixelIntegration is calling go()");

            if (retries < 3){
                go(null);
            } else {
                System.out.println("NOJOY");
            }

        } 
        catch (Exception ex)
        {
            String message = "Could not initialize Pixel: " + ex.getMessage() + "Attempt: " + retries;
            logger.log(Level.INFO, message);
            retries++;
            initialize();
        }        
    }
}