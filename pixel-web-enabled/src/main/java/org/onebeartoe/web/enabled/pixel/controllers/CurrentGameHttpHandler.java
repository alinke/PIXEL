package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.onebeartoe.network.TextHttpHandler;
import static org.onebeartoe.web.enabled.pixel.WebEnabledPixel.getCurrentPlatformGame;

public class CurrentGameHttpHandler extends TextHttpHandler
{
    @Override
    protected String getHttpText(HttpExchange t)
    { 
        String response = getCurrentPlatformGame();
        return response;
    }
}