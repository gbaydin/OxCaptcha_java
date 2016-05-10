package robots.OxCaptcha;


import java.awt.image.BufferedImage;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gunes
 */
public interface WordRenderer {
    public void render(String word, BufferedImage image);
}
