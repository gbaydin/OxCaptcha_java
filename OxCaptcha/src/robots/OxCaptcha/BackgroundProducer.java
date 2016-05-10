package robots.OxCaptcha;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.image.BufferedImage;

/**
 *
 * @author gunes
 */
public interface BackgroundProducer {

    /**
     * Add the background to the given image.
     * 
     * @param image The image onto which the background will be rendered.
     * @return The image with the background rendered.
     */
    public BufferedImage addBackground(BufferedImage image);
    
    public BufferedImage getBackground(int width, int height);
}