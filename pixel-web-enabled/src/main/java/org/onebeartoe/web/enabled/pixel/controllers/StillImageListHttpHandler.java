/*
 */
package org.onebeartoe.web.enabled.pixel.controllers;

import java.util.List;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

/**
 *
 * @author Roberto Marquez
 */
public class StillImageListHttpHandler extends ListHttpHandler
{
    public StillImageListHttpHandler(WebEnabledPixel application)
    {
        super(application);
    }

    @Override
    protected List<String> getList()
    {
        List<String> images = application.loadImageLists();

        return images;
    }
}
