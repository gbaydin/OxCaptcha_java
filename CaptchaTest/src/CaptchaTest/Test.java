/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CaptchaTest;

import robots.OxCaptcha.*;

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
        c.text(5, new char[] {'a', 'b', 'c', '1', '2', '3'});
        //c.text("2a2ba");
        
        // Add noise
        c.noise();
        //c.noiseStraightLine();
        //c.noiseCurvedLine();
        
        // Apply transformation
        //c.transformFishEye();
        //c.transformStretch();
        c.transformShear();
        
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(Long.toString(elapsedTime) + " ms");
        try {
            c.writeImageToFile("test.png");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
