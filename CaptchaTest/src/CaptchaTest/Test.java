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
        long startTime = System.currentTimeMillis();
        
        // Create Captcha container
        OxCaptcha c = new OxCaptcha(150, 50);
        
        // Create background
        c.backgroundFlat();
        //c.backgroundGradient();
        //c.backgroundSquiggles();
        
        // Add text
        c.text(5);
        //c.text("2a2ba");
        
        // Add noise
        c.noise();
        //c.noiseStraightLine();
        //c.noiseCurvedLine();
        
        // Apply transformation
        //c.transformFishEye();
        //c.transformStretch();
        c.transformShear();
        
        // Get rendered image
        BufferedImage img = c.getImage();
        
        // Get rendered image as a 2D array
        int[][] imgArray = c.getImageArray();
        System.out.println(Arrays.toString(imgArray[40]));
        
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        
        // Write rendered image to a png file
        System.out.println(Long.toString(elapsedTime) + " ms");
        try {
            c.writeImageToFile("test.png");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
