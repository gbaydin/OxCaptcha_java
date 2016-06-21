/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CaptchaTest;

import java.awt.Color;
import robots.OxCaptcha.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

/**
 *
 * @author gunes
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        // Create Captcha container
        OxCaptcha c = new OxCaptcha(150, 50);

        long startTime = System.currentTimeMillis();

        // Create background
        c.background();
        
        // Add text
        c.text(5);
        //c.text(new char[] {'a', 'b', 'c'});
        //c.text("2a2ba");
        //c.text(new char[] {'a', 'b', 'c'}, new int[] {1,2,7}, new int[] {30,5,-10});

        
        // Add noise
        //c.noiseStraightLine();
        //c.noiseCurvedLine();
        //c.noiseSaltPepper();
        //c.noiseSaltPepper(0.05f, 0.05f);
        
        // Apply transformation
        //c.distortionFishEye();
        //c.distortionStretch(1.4, 1.1);
//        c.distortionShear();

        // Add blur
        //c.blur(10);
        c.blurGaussian(2, 1.75);
        //c.blurGaussian5x5s1();
        //c.blurGaussian5x5s2();

        
        // Get rendered image
        //BufferedImage img = c.getImage();
        
        // Get rendered image as a 2D array
        //int[] imgArray = c.getImageArray1D();
        
        //long stopTime = System.currentTimeMillis();
        //long elapsedTime = stopTime - startTime;

        //System.out.println(Arrays.toString(imgArray));
        
        // Write rendered image to a png file
        //System.out.println(Long.toString(elapsedTime) + " ms");
        try {
            c.writeImageToFile("test.png");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
