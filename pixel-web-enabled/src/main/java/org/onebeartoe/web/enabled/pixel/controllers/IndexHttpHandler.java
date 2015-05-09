/*
 */
package org.onebeartoe.web.enabled.pixel.controllers;

import com.sun.net.httpserver.HttpExchange;

import org.onebeartoe.network.TextHttpHandler;

/**
 *
 * @author Roberto Marquez
 */
public class IndexHttpHandler extends TextHttpHandler
{
    @Override
    protected String getHttpText(HttpExchange t)
    {
        String response = "Hello, Pixel Worlds!\n";

        return response;
    }
}


