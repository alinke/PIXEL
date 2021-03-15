/*
This class is an object model for returning marquee paths, it is used by GetPNGandGIFMatch
 */
package org.onebeartoe.web.enabled.pixel.controllers;

import java.io.File;

/**
 *
 * @author al
 */
class MarqueePath { 
    String PNGPath; // path of the PNG
    String GIFPath; // path of the GIF
    File PNGFile;
    File GIFFile;
    String ConsoleNameMapped;
    String ConsolePNGPath;
    File ConsolePNGFile;
    String ConsoleGIFPath;
    File ConsoleGIFFile;
    String DefaultConsolePNGPath;
    File DefaultConsolePNGFile;
    MarqueePath(String p, String g, File pf, File gf, String cm, String cpp, File cpf, String cgp, File cgf, String dcpp, File dcpf) 
    { 
        PNGPath = p;
        GIFPath = g;
        PNGFile = pf;
        GIFFile = gf;
        ConsoleNameMapped = cm;
        ConsolePNGPath = cpp;
        ConsolePNGFile =  cpf;
        ConsoleGIFPath = cgp;
        ConsoleGIFFile = cgf;
        DefaultConsolePNGPath = dcpp;
        DefaultConsolePNGFile = dcpf;
    } 
} 