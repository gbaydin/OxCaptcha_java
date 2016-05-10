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

// Add noise
c.noise();
//c.noiseStraightLine();
//c.noiseCurvedLine();

// Apply transformation
//c.transformFishEye();
//c.transformStretch();
c.transformShear();

// Get the rendered image
BufferedImage img = c.getImage();

// Write image to a png file
try {
    c.writeImageToFile("test.png");
}
catch (Exception e) {
    e.printStackTrace();
}
```

![alt text](https://raw.githubusercontent.com/gbaydin/OxCaptcha/master/test.png "")

### License

Released under the BSD license.

OxCaptcha is based on the [SimpleCaptcha](http://simplecaptcha.sourceforge.net/) library by James Childers (released under the BSD License).
