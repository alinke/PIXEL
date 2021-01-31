package org.onebeartoe.lcdfinder.cade.pixel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author curtis-bull
 */
public class CliPixel 
{

 private static final Logger log = Logger.getLogger(CliPixel.class.getName());
 private String[] args = null;
 private Options options = new Options();
 private int WebPortOption = 8080;
 //private String PortOption = "";
 //private int ledMatrixType = 3;
 //private int ledMatrixTypeDefault = 3;
 private int ledMatrixType = 11;
 private int ledMatrixTypeDefault = 11;
 private int ledMatrixTypeMax = 25;
 
 private int yTextOffset = 0;

 public CliPixel(String[] args) 
 {

  this.args = args;

  options.addOption("h", "help", false, "show help.");
  options.addOption("w", "webport", true, "Listening port. Default Port = 8080");
  //options.addOption("p", "port", true, "PIXEL Port. No Default");
  options.addOption("l", "ledmatrix", true, "Sets the LED matrix type. Default = 11\n " +
                    "Example -l 15 or --ledmatrix 15\n" +
                    "0=32x16 Old 1=32x16, 2=32x32 Old\n" +
                    "3=PIXEL V2 32x32 Old, 4=64x32 Old, 5=32x64 Old, 6=Old 2 Mirrored\n" +
                    "7=Old 4 Mirrored (does not work), 8=128x32 Old, 9=32x128 Old\n" +
                    "10=SUPER PIXEL 64x64, 11=32x32, 12=32x32 Color Swap\n" +
                    "13=64x32, 14=64x64, 15=128x32\n" +
                    "16=32x128, 17=64x16, \n" +
                    "18=64x32 Mirrored, 19=256x16, \n" +
                    "20=32x32 Mirrored, 21=32x32 4x Mirrored, \n" +
                    "22=128x16, 23=ALIEXPRESS RANDOM1 32x32, \n" +
                    "24=64x32 COLOR SWAP, 25=64x32 COLOR SWAP\n");
  
  
  options.addOption("m", "matrix", true, "Sets the LED matrix type, same as l option");
  options.addOption("y", "y text offset", true, "This is the y offset for scrolling text.");
 }

 public void parse() 
 {
  CommandLineParser parser = new BasicParser();

  CommandLine cmd = null;
  try {
   cmd = parser.parse(options, args);

   if (cmd.hasOption("h"))
    help();

   if (cmd.hasOption("w")) {
    log.log(Level.INFO, "Using cli argument -w=" + cmd.getOptionValue("w"));
    WebPortOption = Integer.parseInt(cmd.getOptionValue("p"));
   }
   
  /* if (cmd.hasOption("p")) {
    log.log(Level.INFO, "Using cli argument -p=" + cmd.getOptionValue("p"));
    PortOption = cmd.getOptionValue("p");
   } */
   
   if( cmd.hasOption("y") )
   {
       String o = cmd.getOptionValue("y");
       log.log(Level.INFO, "Using cli argument -y: " + o);
       yTextOffset = Integer.parseInt(o);
   }
   
    if (cmd.hasOption("l")) {
    log.log(Level.INFO, "Using cli argument -l=" + cmd.getOptionValue("l"));
    ledMatrixType = Integer.parseInt(cmd.getOptionValue("l"));
   }
    
    if (cmd.hasOption("m")) {
    log.log(Level.INFO, "Using cli argument -m=" + cmd.getOptionValue("m"));
    ledMatrixType = Integer.parseInt(cmd.getOptionValue("m"));
    //let's handle the case if the user entered a number larger than the max
    if (ledMatrixType > ledMatrixTypeMax) {
        ledMatrixType = ledMatrixTypeDefault;
    } 
   }

  } catch (ParseException e) {
   log.log(Level.SEVERE, "Failed to parse command line properties", e);
   help();
  } catch (NumberFormatException e) {
   log.log(Level.SEVERE, "Failed to parse command line properties.  Number expected",e);        
   help();
  }
 }
 public int getWebPort()
 {
    return WebPortOption;
 }
 
 /* public String getPort()
 {
    return PortOption;
 }*/
 
    public int getyTextOffset() {
        return yTextOffset;
    }

    public void setyTextOffset(int yTextOffset) {
        this.yTextOffset = yTextOffset;
    } 
 
 public int getLEDMatrixType()
 {
    return ledMatrixType;
 }
 private void help() {
  // This prints out some help
  HelpFormatter formater = new HelpFormatter();

  formater.printHelp("Main", options);
 }
}
