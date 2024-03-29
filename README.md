# OxCaptcha
Java CAPTCHA generator without dependencies.

### Usage

```java
import robots.OxCaptcha.*;

// Create Captcha container
OxCaptcha c = new OxCaptcha(150, 50);

// Create background
c.backgroundFlat();
//c.backgroundGradient();
//c.backgroundSquiggles();

// Add text
c.text(5);
//c.text("2a2ba");
//c.text(new char[] {'a', 'b', 'c'}, new int[] {1, 2, 7}, new int[] {30, 5, -10});


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

// Write image to a png file
try {
    c.writeImageToFile("test.png");
}
catch (IOException e) {
    e.printStackTrace();
}
```

![alt text](https://raw.githubusercontent.com/gbaydin/OxCaptcha_java/master/test.png "")

### License

Released under the BSD license.

OxCaptcha is based on the [SimpleCaptcha](http://simplecaptcha.sourceforge.net/) library by James Childers (released under the BSD License).
