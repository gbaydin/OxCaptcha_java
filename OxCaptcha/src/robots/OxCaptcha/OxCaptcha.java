/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robots.OxCaptcha;


/**
 *
 * @author gunes
 */
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author gunes
 */
public class OxCaptcha {
    private static final Random RAND = new SecureRandom();
    private static final int DEFAULT_LENGTH = 5;
    private static final char[] DEFAULT_CHARS = new char[] { 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'k', 'm', 'n', 'p', 'r', 'w', 'x', 'y',
            '2', '3', '4', '5', '6', '7', '8', };
    private static final List<Color> DEFAULT_COLORS = new ArrayList<Color>();
    private static final List<Font> DEFAULT_FONTS = new ArrayList<Font>();
    // The text will be rendered 25%/5% of the image height/width from the X and Y axes
    private static final double YOFFSET = 0.25;
    private static final double XOFFSET = 0.05;

    static {
            DEFAULT_COLORS.add(Color.BLACK);
            DEFAULT_FONTS.add(new Font("Arial", Font.BOLD, 40));
            DEFAULT_FONTS.add(new Font("Courier", Font.BOLD, 40));
    }
	
    private final List<Color> _colors = new ArrayList<Color>();
    private final List<Font> _fonts = new ArrayList<Font>();    
    private BufferedImage _img;
    private Graphics2D _img_g;
    private int _width;
    private int _height;
    private BufferedImage _bg;
    private String _answer = "";
    private boolean _addBorder = false;
    
    public OxCaptcha(int width, int height) {
        _colors.addAll(DEFAULT_COLORS);
        _fonts.addAll(DEFAULT_FONTS);        
        _img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        _img_g = _img.createGraphics();
        _width = width;
        _height = height;
    }
    
    public OxCaptcha text() {
        return text(DEFAULT_LENGTH, DEFAULT_CHARS);
    }
    
    public OxCaptcha text(int length) {
        return text(length, DEFAULT_CHARS);
    }

    public OxCaptcha text(int length, char[] chars) {
        String t = "";
        for (int i = 0; i < length; i++) {
            t += chars[RAND.nextInt(chars.length)];
        }
        return text(t);
    }

    public OxCaptcha text(String t) {
        _answer = t;

        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));
        _img_g.setRenderingHints(hints);
        
        FontRenderContext frc = _img_g.getFontRenderContext();
        int xBaseline = (int) Math.round(_width * XOFFSET);
        int yBaseline =  _height - (int) Math.round(_height * YOFFSET);
        
        char[] chars = new char[1];
        for (char c : _answer.toCharArray()) {
            chars[0] = c;
            
            _img_g.setColor(_colors.get(RAND.nextInt(_colors.size())));

            int choiceFont = RAND.nextInt(_fonts.size());
            Font font = _fonts.get(choiceFont);
            _img_g.setFont(font);
            
            GlyphVector gv = font.createGlyphVector(frc, chars);
            _img_g.drawChars(chars, 0, chars.length, xBaseline, yBaseline);

            int width = (int) gv.getVisualBounds().getWidth();
            xBaseline = xBaseline + width;
        }
        return this;

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

        _img_g.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON));

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

    public OxCaptcha gimp() {
        return gimp(new RippleGimpyRenderer());
    }

    public OxCaptcha gimp(GimpyRenderer gimpy) {
        gimpy.gimp(_img);
        return this;
    }
        
    public BufferedImage build() {
        if (_bg == null) {
                _bg = new TransparentBackgroundProducer().getBackground(_img.getWidth(), _img.getHeight());
        }

        // Paint the main image over the background
        Graphics2D g = _bg.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.drawImage(_img, null, null);

        if (_addBorder) {
                int width = _img.getWidth();
                int height = _img.getHeight();

            g.setColor(Color.BLACK);
            g.drawLine(0, 0, 0, width);
            g.drawLine(0, 0, width, 0);
            g.drawLine(0, height - 1, width, height - 1);
            g.drawLine(width - 1, height - 1, width - 1, 0);
        }

        _img = _bg;
        return _img;
    }
    
    public BufferedImage getImage() {
        return _img;
    }
    
    public void writeImageToFile(String fileName) throws IOException {
        ImageIO.write(_img, "png", new File(fileName));
    }
}