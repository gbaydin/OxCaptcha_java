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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
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
    
    public OxCaptcha addText() {
        return addText(DEFAULT_LENGTH, DEFAULT_CHARS);
    }
    
    public OxCaptcha addText(int length) {
        return addText(length, DEFAULT_CHARS);
    }

    public OxCaptcha addText(int length, char[] chars) {
        String text = "";
        for (int i = 0; i < length; i++) {
            text += chars[RAND.nextInt(chars.length)];
        }
        return addText(text);
    }

    public OxCaptcha addText(String text) {
        _answer = text;

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
    
    
    public OxCaptcha addNoise() {
        return this.addNoise(new CurvedLineNoiseProducer());
    }    

    public OxCaptcha gimp() {
        return gimp(new RippleGimpyRenderer());
    }

    public OxCaptcha gimp(GimpyRenderer gimpy) {
        gimpy.gimp(_img);
        return this;
    }
        
    public OxCaptcha addNoise(NoiseProducer nProd) {
        nProd.makeNoise(_img);
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