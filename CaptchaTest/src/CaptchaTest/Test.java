/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CaptchaTest;

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
        c.backgroundFlat();
        //c.backgroundGradient();
        //c.backgroundSquiggles();
        
        // Add text
        //c.text(5);
        //c.text(new char[] {'a', 'b', 'c'});
        //c.text("2a2ba");
        c.text(new char[] {'a', 'b', 'c'}, new int[] {1,2,7}, new int[] {30,5,-10});

        // Add blur, optional parameter for 
        //c.blur();
        c.gaussianBlur5x5s2();
        //c.blur(5);
        
        // Add noise
        //c.noise();
        //c.noiseStraightLine();
        //c.noiseCurvedLine();
        c.noiseSaltPepper();
        //c.noiseSaltPepper(0.05f, 0.05f);
        
        // Apply transformation
        //c.transformFishEye();
        //c.transformStretch();
        //c.transformShear();
        
        // Get rendered image
        //BufferedImage img = c.getImage();
        
        // Get rendered image as a 2D array
        int[][] imgArray = c.getImageArray();
        
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        //System.out.println(Arrays.toString(imgArray[40]));
        
        // Write rendered image to a png file
        System.out.println(Long.toString(elapsedTime) + " ms");
        try {
            c.writeImageToFile("testt.png");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
