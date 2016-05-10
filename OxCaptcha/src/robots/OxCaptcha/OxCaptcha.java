
package robots.OxCaptcha;


import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import javax.imageio.ImageIO;
import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class OxCaptcha {
    private static final Random RAND = new SecureRandom();

    private List<Font> _fonts = new ArrayList<Font>();    
    private BufferedImage _img;
    private Graphics2D _img_g;
    private int _width;
    private int _height;
    private BufferedImage _bg;
    private char[] _text = new char[] {};
    private boolean _addBorder = false;
    private char[] _charSet = new char[] { 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'k', 'm', 'n', 'p', 'r', 'w', 'x', 'y',
            '2', '3', '4', '5', '6', '7', '8', };
    
    public OxCaptcha(int width, int height) {
        _img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        _img_g = _img.createGraphics();

        _img_g.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON));
        
        _width = width;
        _height = height;

        _fonts.add(new Font("Arial", Font.PLAIN, 40));
    }

    public void setCharSet(char[] charSet) {
        _charSet = charSet;
    }
    
    public OxCaptcha backgroundFlat() {
        return backgroundFlat(Color.BLACK);
    }
    public OxCaptcha backgroundFlat(Color color) {
        _img_g.setPaint(color);
        _img_g.fillRect(0,0, _width, _height);
        return this;
    }
    
    public OxCaptcha backgroundGradient() {
        return backgroundGradient(Color.DARK_GRAY, Color.BLACK);
    }
    public OxCaptcha backgroundGradient(Color color1, Color color2) {
        GradientPaint ytow = new GradientPaint(0, 0, color1, _width, _height, color2);
        _img_g.setPaint(ytow);
        _img_g.fillRect(0, 0, _width, _height);
        return this;     
    }
    
    public OxCaptcha backgroundSquiggles() {
        BasicStroke bs = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 2.0f, new float[] { 2.0f, 2.0f }, 0.0f);
        _img_g.setStroke(bs);
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                0.75f);
        _img_g.setComposite(ac);

        _img_g.translate(_width * -1.0, 0.0);
        double delta = 5.0;
        double xt;
        //double ts = 0.0;
        for (xt = 0.0; xt < (2.0 * _width); xt += delta) {
            Arc2D arc = new Arc2D.Double(0, 0, _width, _height, 0.0, 360.0,
                    Arc2D.OPEN);
            _img_g.draw(arc);
            _img_g.translate(delta, 0.0);
            //ts += delta;
        }

        return this;
    }
    
    public OxCaptcha text() {
        return text(5);
    }
    
    public OxCaptcha text(int length) {
        char[] t = new char[length];
        for (int i = 0; i < length; i++) {
            t[i] = _charSet[RAND.nextInt(_charSet.length)];
        }
        return text(t);
    }
    
    public OxCaptcha text(char[] text) {
        _text = text;
        renderText();
        return this;
    }
    
    private void renderText() {

        Color color = Color.WHITE;
        // The text will be rendered 25%/5% of the image height/width from the X and Y axes
        double yOffset = 0.25;
        double xOffset = 0.05;

        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));
        _img_g.setRenderingHints(hints);

        _img_g.setColor(color);
        
        FontRenderContext frc = _img_g.getFontRenderContext();
        int xBaseline = (int) Math.round(_width * xOffset);
        int yBaseline =  _height - (int) Math.round(_height * yOffset);
        
        char[] chars = new char[1];
        for (char c : _text) {
            chars[0] = c;
            
            int choiceFont = RAND.nextInt(_fonts.size());
            Font font = _fonts.get(choiceFont);
            _img_g.setFont(font);
            
            GlyphVector gv = font.createGlyphVector(frc, chars);
            _img_g.drawChars(chars, 0, chars.length, xBaseline, yBaseline);

            int width = (int) gv.getVisualBounds().getWidth();
            xBaseline = xBaseline + width + 5;
        }
    }
    
    public OxCaptcha noise() {
        return noiseCurvedLine();
    }
    
    public OxCaptcha noiseCurvedLine() {
        return noiseCurvedLine(Color.BLACK, 3.0f);
    }
    
    public OxCaptcha noiseCurvedLine(Color color, float thickness) {
        // the curve from where the points are taken
        CubicCurve2D cc = new CubicCurve2D.Float(_width * .1f, _height
                * RAND.nextFloat(), _width * .1f, _height
                * RAND.nextFloat(), _width * .25f, _height
                * RAND.nextFloat(), _width * .9f, _height
                * RAND.nextFloat());

        // creates an iterator to define the boundary of the flattened curve
        PathIterator pi = cc.getPathIterator(null, 2);
        Point2D tmp[] = new Point2D[200];
        int i = 0;

        // while pi is iterating the curve, adds points to tmp array
        while (!pi.isDone()) {
            float[] coords = new float[6];
            switch (pi.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
            case PathIterator.SEG_LINETO:
                tmp[i] = new Point2D.Float(coords[0], coords[1]);
            }
            i++;
            pi.next();
        }

        // the points where the line changes the stroke and direction
        Point2D[] pts = new Point2D[i];
        // copies points from tmp to pts
        System.arraycopy(tmp, 0, pts, 0, i);


        _img_g.setColor(color);

        // for the maximum 3 point change the stroke and direction
        for (i = 0; i < pts.length - 1; i++) {
            if (i < 3) {
            	_img_g.setStroke(new BasicStroke(thickness));
            }
            _img_g.drawLine((int) pts[i].getX(), (int) pts[i].getY(),
                    (int) pts[i + 1].getX(), (int) pts[i + 1].getY());
        }
        return this;
    }
    
    public OxCaptcha noiseStraightLine() {
        return noiseStraightLine(Color.BLACK, 3.0f);
    }
    
    public OxCaptcha noiseStraightLine(Color color, float thickness) {
        int y1 = RAND.nextInt(_height) + 1;
        int y2 = RAND.nextInt(_height) + 1;
        int x1 = 0;
        int x2 = _width;

        // The thick line is in fact a filled polygon
        _img_g.setColor(color);
        int dX = x2 - x1;
        int dY = y2 - y1;
        // line length
        double lineLength = Math.sqrt(dX * dX + dY * dY);

        double scale = thickness / (2 * lineLength);

        // The x and y increments from an endpoint needed to create a
        // rectangle...
        double ddx = -scale * dY;
        double ddy = scale * dX;
        ddx += (ddx > 0) ? 0.5 : -0.5;
        ddy += (ddy > 0) ? 0.5 : -0.5;
        int dx = (int) ddx;
        int dy = (int) ddy;

        // Now we can compute the corner points...
        int xPoints[] = new int[4];
        int yPoints[] = new int[4];

        xPoints[0] = x1 + dx;
        yPoints[0] = y1 + dy;
        xPoints[1] = x1 - dx;
        yPoints[1] = y1 - dy;
        xPoints[2] = x2 - dx;
        yPoints[2] = y2 - dy;
        xPoints[3] = x2 + dx;
        yPoints[3] = y2 + dy;

        _img_g.fillPolygon(xPoints, yPoints, 4);
        return this;
    }

    public OxCaptcha transform() {
        return transformFishEye();
    }
    
    public OxCaptcha transformFishEye() {
//        Color hColor = Color.BLACK;
//        Color vColor = Color.BLACK;
        float thickness = 1.0f;
        
        _img_g.setStroke(new BasicStroke(thickness));
        
//        int hstripes = _height / 7;
//        int vstripes = _width / 7;
//
//        // Calculate space between lines
//        int hspace = _height / (hstripes + 1);
//        int vspace = _width / (vstripes + 1);
//
//        // Draw the horizontal stripes
//        for (int i = hspace; i < _height; i = i + hspace) {
//            _img_g.setColor(hColor);
//            _img_g.drawLine(0, i, _width, i);
//        }
//
//        // Draw the vertical stripes
//        for (int i = vspace; i < _width; i = i + vspace) {
//            _img_g.setColor(vColor);
//            _img_g.drawLine(i, 0, i, _height);
//        }

        // Create a pixel array of the original image.
        // we need this later to do the operations on..
        int pix[] = new int[_height * _width];
        int j = 0;

        for (int j1 = 0; j1 < _width; j1++) {
            for (int k1 = 0; k1 < _height; k1++) {
                pix[j] = _img.getRGB(j1, k1);
                j++;
            }
        }

        double distance = ranInt(_width / 4, _width / 3);

        // put the distortion in the (dead) middle
        int wMid = _width / 2;
        int hMid = _height / 2;

        // again iterate over all pixels..
        for (int x = 0; x < _width; x++) {
            for (int y = 0; y < _height; y++) {

                int relX = x - wMid;
                int relY = y - hMid;

                double d1 = Math.sqrt(relX * relX + relY * relY);
                if (d1 < distance) {

                    int j2 = wMid
                            + (int) (((fishEyeFormula(d1 / distance) * distance) / d1) * (x - wMid));
                    int k2 = hMid
                            + (int) (((fishEyeFormula(d1 / distance) * distance) / d1) * (y - hMid));
                    _img.setRGB(x, y, pix[j2 * _height + k2]);
                }
            }
        }
        return this;
    }
    
    public OxCaptcha transformStretch() {
        double xScale = 3.0;
        double yScale = 1.0;
        Graphics2D g = _img.createGraphics();
        AffineTransform at = new AffineTransform();
        at.scale(xScale, yScale);
//		RenderingHints hints = new RenderingHints(RenderingHints.KEY_INTERPOLATION,
//                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawRenderedImage(_img, at);
        return this;
    }
    
    public OxCaptcha transformShear() {
        Color color = Color.BLACK;
        
        int xPeriod = RAND.nextInt(5) + 5;
        int xPhase = RAND.nextInt(5) + 2;
        int yPeriod = RAND.nextInt(3) + 10;
        int yPhase = 7;
        
        Graphics2D g = _img.createGraphics();
        shearX(g, color, xPeriod, xPhase, _width, _height);
        shearY(g, color, yPeriod, yPhase, _width, _height);
        g.dispose();
        return this;
    }
    
    public String getText() {
        return new String(_text);
    }

    public BufferedImage getImage() {
        return _img;
    }
    
//    public int[][] getImage() {
//        int pix[][] = new int[_height][_width];
//        int j = 0;
//
//        for (int x = 0; x < _width; x++) {
//            for (int y = 0; y < _height; y++) {
//                pix[j] = _img.getRGB(y, y, y, y, ints, y, y);
//                j++;
//            }
//        }
//        return pix;
//    }

    public void writeImageToFile(String fileName) throws IOException {
        ImageIO.write(_img, "png", new File(fileName));
    }
    
    
    
    private int ranInt(int i, int j) {
        double d = Math.random();
        return (int) (i + ((j - i) + 1) * d);
    }

    private double fishEyeFormula(double s) {
        // implementation of:
        // g(s) = - (3/4)s3 + (3/2)s2 + (1/4)s, with s from 0 to 1.
        if (s < 0.0D) {
            return 0.0D;
        }
        if (s > 1.0D) {
            return s;
        }

        return -0.75D * s * s * s + 1.5D * s * s + 0.25D * s;
    }

    private static final void applyFilter(BufferedImage img, ImageFilter filter) {
            FilteredImageSource src = new FilteredImageSource(img.getSource(), filter);
            Image fImg = Toolkit.getDefaultToolkit().createImage(src);
            Graphics2D g = img.createGraphics();
            g.drawImage(fImg, 0, 0, null, null);
            //g.dispose();
    }
    
    private void shearX(Graphics2D g, Color color, int period, int phase, int w1, int h1) {
        boolean borderGap = true;
        int frames = 15;

        for (int i = 0; i < h1; i++) {
            double d = (period >> 1)
                    * Math.sin((double) i / (double) period
                            + (6.2831853071795862D * phase) / frames);
            g.copyArea(0, i, w1, 1, (int) d, 0);
            if (borderGap) {
                g.setColor(color);
                g.drawLine((int) d, i, 0, i);
                g.drawLine((int) d + w1, i, w1, i);
            }
        }
    }

    private void shearY(Graphics2D g, Color color, int period, int phase, int w1, int h1) {
         
        boolean borderGap = true;
        int frames = 15;

        for (int i = 0; i < w1; i++) {
            double d = (period >> 1)
                    * Math.sin((float) i / period
                            + (6.2831853071795862D * phase) / frames);
            g.copyArea(i, 0, 1, h1, 0, (int) d);
            if (borderGap) {
                g.setColor(color);
                g.drawLine(i, (int) d, i, 0);
                g.drawLine(i, (int) d + h1, i, h1);
            }
        }
    }    
}