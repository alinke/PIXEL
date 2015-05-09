/*
 */
package org.onebeartoe.web.enabled.pixel.controllers;

import java.util.List;
import org.onebeartoe.web.enabled.pixel.WebEnabledPixel;

/**
 *
 * @author Roberto Marquez
 */
public class AnimationsListHttpHandler extends ListHttpHandler
{

    public AnimationsListHttpHandler(WebEnabledPixel application)
    {
        super(application);
    }
    @Override
    protected List<String> getList()
    {
        List<String> animations = application.loadAnimationList();
        
        return animations;
    }
}
