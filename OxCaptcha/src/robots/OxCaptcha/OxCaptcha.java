
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
import java.awt.image.BufferedImageOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.Kernel;
import java.awt.image.ConvolveOp;
import javax.imageio.ImageIO;
import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class OxCaptcha {
    private static final Random RAND = new SecureRandom();
    public static final int RELATIVE = 0;
    public static final int ABSOLUTE = 1;

    private BufferedImage _img;
    private Graphics2D _img_g;
    private int _width;
    private int _height;
    private BufferedImage _bg;
    private Color _bg_color;
    private Color _fg_color;
    private char[] _chars = new char[] {};
    private int _length = 0;
    private int[] _xOffsets = new int[] {};
    private int[] _yOffsets = new int[] {};
    private int[] _xs = new int[] {};
    private int[] _ys = new int[] {};
    private Font _font = new Font("Arial", Font.PLAIN, 40);
    private FontRenderContext _fontRenderContext;
    private char[] _charSet = new char[] { 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'k', 'm', 'n', 'p', 'r', 'w', 'x', 'y',
            '2', '3', '4', '5', '6', '7', '8', 'A', 'J', 'K','z','q','S'};

    public OxCaptcha(int width, int height) {
        _img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        _img_g = _img.createGraphics();
        _img_g.setFont(_font);
        _fontRenderContext = _img_g.getFontRenderContext();
        _bg_color = Color.WHITE;
        _fg_color = Color.BLACK;

        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));
        _img_g.setRenderingHints(hints);

        _width = width;
        _height = height;

    }

    public void setCharSet(char[] charSet) {
        _charSet = charSet;
    }

    public OxCaptcha background() {
        return background(_bg_color);
    }
    public OxCaptcha background(Color color) {
        _bg_color = color;
        _img_g.setPaint(color);
        _img_g.fillRect(0,0, _width, _height);
        return this;
    }

    public OxCaptcha foreground(Color color) {
        _fg_color = color;
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

    public OxCaptcha text(String chars) {
        return text(chars, (int)(0.05 * _width), (int)(0.75 * _height), 0);
    }

    public OxCaptcha text(String chars, int kerning) {
        return text(chars, (int)(0.05 * _width), (int)(0.75 * _height), kerning);
    }

    public OxCaptcha text(char[] chars) {
        return text(chars, (int)(0.05 * _width), (int)(0.75 * _height), 0);
    }

    public OxCaptcha text(String chars, int xOffset, int yOffset, int kerning) {
        int l = chars.length();
        char[] t = new char[l];
        chars.getChars(0, l, t, 0);
        return text(t, xOffset, yOffset, kerning);
    }

    public OxCaptcha text(char[] chars, int xOffset, int yOffset, int kerning) {
        int xn[] = new int[chars.length];
        for (int i = 0; i < chars.length; i++)
        {
            xn[i] = kerning;
        }
        int yn[] = new int[chars.length];
        xn[0] = xOffset;
        yn[0] = yOffset;
        return text(chars, xn, yn);
    }

    // Add letters with per letter positioning
    // Offsets give the position of each letter relative to the top right of the previous letter
    // The offsets of the first letter are relative to the top left of the image
    // (For an image 50 pixels high, it's a good idea to start the first y offset around 30, so that the text is inside the image)
    // x increases from left to right
    // y increases from top to bottom
    public OxCaptcha text(char[] chars, int[] xOffsets, int[] yOffsets) {
        return text(chars, xOffsets, yOffsets, OxCaptcha.RELATIVE);
    }

    // Add letters with either RELATIVE (per letter) or ABSOLUTE positioning.
    // if mode is OxCaptcha.ABSOLUTE, xs and ys are absolute positions of letters in chars
    // if mode is OxCaptcha.RELATIVE, xs and ys are relative positions of letters in chars:
    // Offsets give the position of each letter relative to the top right of the previous letter
    // The offsets of the first letter are relative to the top left of the image
    // (For an image 50 pixels high, it's a good idea to start the first y offset around 30, so that the text is inside the image)
    // x increases from left to right
    // y increases from top to bottom
    public OxCaptcha text(char[] chars, int[] xs, int[] ys, int mode) {
        if (mode == OxCaptcha.RELATIVE) {
            _xOffsets = xs;
            _yOffsets = ys;
        } else {
            _xs = xs;
            _ys = ys;
        }
        _chars = chars;
        _length = _chars.length;

        renderText(mode);
        return this;
    }

    private void renderText() {
        renderText(OxCaptcha.RELATIVE);
    }

    private void renderText(int mode) {
        _img_g.setColor(_fg_color);
        if (mode == OxCaptcha.RELATIVE) {
            int x = 0;
            int y = 0;
            char[] cc = new char[1];
            for (int i = 0; i < _length; i++) {
                x = x + _xOffsets[i];
                y = y + _yOffsets[i];
                cc[0] = _chars[i];

                _img_g.drawChars(cc, 0, 1, x, y);

                GlyphVector gv = _font.createGlyphVector(_fontRenderContext, cc);
                int width = (int) gv.getVisualBounds().getWidth();
                x = x + width + 1;
            }
        } else {
            char[] cc = new char[1];
            for (int i = 0; i < _length; i++) {
                cc[0] = _chars[i];

                _img_g.drawChars(cc, 0, 1, _xs[i], _ys[i]);
            }
        }
    }

    public OxCaptcha blur() {
        return blur(3);
    }

    public OxCaptcha blur(int kernelSize) {

        float[] k = new float[kernelSize * kernelSize];
        for (int i = 0; i < kernelSize; i++) {
            k[i] = 1f / ((float) (kernelSize));
        }
        Kernel kernel = new Kernel(kernelSize, kernelSize, k);

        BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        _img = op.filter(_img, null);
        _img_g = _img.createGraphics();
        _img_g.setFont(_font);

        return this;
    }

    private ConvolveOp gbConvolve(int radius, float sigma, boolean horizontal) {
        int size = radius * 2 + 1;
        float[] vals = new float[size];
        float twoSigmaSq = 2.0f * sigma * sigma;
        float sqrtPiTwoSigmaSq = (float) Math.sqrt(twoSigmaSq * Math.PI);
        float sum = 0.0f;

        for (int i = -radius; i <= radius; i++) {
            float distance = i * i;
            int index = i + radius;
            vals[index] = (float) Math.exp(-distance / twoSigmaSq) / sqrtPiTwoSigmaSq;
            sum += vals[index];
        }
        for (int i = 0; i < size; i++) {
            vals[i] /= sum;
        }

        Kernel kernel = null;
        if (horizontal) {
            kernel = new Kernel(size, 1, vals);
        }
        else {
            kernel = new Kernel(1, size, vals);
        }
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }

    public OxCaptcha blurGaussian(int radius, double sigma) {
        if (radius < 1) {
            throw new IllegalArgumentException("radius must be greater than 1");
        }
        BufferedImageOp op = gbConvolve(radius, (float)sigma, true);
        _img = op.filter(_img, null);

        op = gbConvolve(radius, (float)sigma, false);
        _img = op.filter(_img, null);
        _img_g = _img.createGraphics();
        _img_g.setFont(_font);
        return this;
    }

    public OxCaptcha blurGaussian3x3() {

        float[] k = new float[] {
            1f/16f, 1f/8f, 1f/16f,
            1f/8f, 1f/4f, 1f/8f,
            1f/16f, 1f/8f, 1f/16f};

        Kernel kernel = new Kernel(3, 3, k);

        BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        _img = op.filter(_img, null);
        _img_g = _img.createGraphics();
        _img_g.setFont(_font);

        return this;
    }

    public OxCaptcha blurGaussian5x5s1() {

        float[] k = new float[] {
            1f/273f,  4f/273f,  7f/273f,  4f/273f, 1f/273f,
            4f/273f, 16f/273f, 26f/273f, 16f/273f, 4f/273f,
            7f/273f, 26f/273f, 41f/273f, 26f/273f, 7f/273f,
            4f/273f, 16f/273f, 26f/273f, 16f/273f, 4f/273f,
            1f/273f,  4f/273f,  7f/273f,  4f/273f, 1f/273f};

        Kernel kernel = new Kernel(5, 5, k);

        BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        _img = op.filter(_img, null);
        _img_g = _img.createGraphics();
        _img_g.setFont(_font);

        return this;
    }

    public OxCaptcha blurGaussian5x5s2() {

        float[] k = new float[] {
            0.023528f, 0.033969f, 0.038393f, 0.033969f, 0.023528f,
            0.033969f, 0.049045f, 0.055432f, 0.049045f, 0.033969f,
            0.038393f, 0.055432f, 0.062651f, 0.055432f, 0.038393f,
            0.033969f, 0.049045f, 0.055432f, 0.049045f, 0.033969f,
            0.023528f, 0.033969f, 0.038393f, 0.033969f, 0.023528f};

        Kernel kernel = new Kernel(5, 5, k);

        BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        _img = op.filter(_img, null);
        _img_g = _img.createGraphics();
        _img_g.setFont(_font);
        return this;
    }


    public OxCaptcha noiseCurvedLine() {
        return noiseCurvedLine(_fg_color, 3.0f);
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
        return noiseStraightLine(_fg_color, 3.0f);
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

    public OxCaptcha noiseSaltPepper() {
        return noiseSaltPepper(0.01f, 0.01f);
    }

    public OxCaptcha noiseSaltPepper(float salt, float pepper) {
        int s = (int) (_height * _width * salt);
        int p = (int) (_height * _width * pepper);

        _img_g.setStroke(new BasicStroke(1));

        _img_g.setColor(Color.WHITE);

        for (int i = 0; i < s; i++)
        {
            int x = (int) (RAND.nextFloat() * _width);
            int y = (int) (RAND.nextFloat() * _height);

            _img_g.drawLine(x, y, x, y);
        }
        _img_g.setColor(Color.BLACK);
        for (int i = 0; i < p; i++)
        {
            int x = (int) (RAND.nextFloat() * _width);
            int y = (int) (RAND.nextFloat() * _height);
            _img_g.drawLine(x, y, x, y);
        }
        return this;
    }

    public OxCaptcha distortion() {
        return distortionShear();
    }

    public OxCaptcha distortionFishEye() {
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

    public OxCaptcha distortionStretch() {
        double xScale = RAND.nextDouble() * 2;
        double yScale = RAND.nextDouble() * 2;
        return distortionStretch(xScale, yScale);
    }

    public OxCaptcha distortionStretch(double xScale, double yScale) {
        AffineTransform at = new AffineTransform();
        at.scale(xScale, yScale);
        _img_g.drawRenderedImage(_img, at);
        return this;
    }

    public OxCaptcha distortionShear() {
        int xPeriod = RAND.nextInt(10) + 8;
        int xPhase = RAND.nextInt(8) + 8;
        int yPeriod = RAND.nextInt(10) + 8;
        int yPhase = RAND.nextInt(8) + 8;

        return distortionShear(xPeriod, xPhase, yPeriod, yPhase);
    }

    public OxCaptcha distortionShear(int xPeriod, int xPhase, int yPeriod, int yPhase) {
        shearX(_img_g, xPeriod, xPhase, _width, _height);
        shearY(_img_g, yPeriod, yPhase, _width, _height);
        return this;
    }

    public OxCaptcha normalize() {
        int p[] = getImageArray1D();
        int pmin = p[0];
        int pmax = p[0];
        for (int i = 0 ; i < p.length; i++)
        {
            if (p[i] < pmin) {
                pmin = p[i];
            }
            else if (p[i] > pmax) {
                pmax = p[i];
            }
        }
        int prange = pmax - pmin;

        if (prange > 2) {
            int i = 0;
            for (int y = 0; y < _height; y++)
            {
                for (int x = 0; x < _width; x++)
                {
                    int pp = 255 * (p[i++] - pmin) / prange;
                    _img.setRGB(x, y, new Color(pp, pp, pp).getRGB());
                }
            }
        }
        return this;
    }

    public void save(int[] pixels, int width, int height, String fileName) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        int i = 0;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                img.setRGB(x, y, new Color(pixels[i], pixels[i], pixels[i]).getRGB());
                i++;
            }
        }
        ImageIO.write(img, "png", new File(fileName));
    }

    public void save(int[][] pixels, String fileName) throws IOException {
        int height = pixels.length;
        int width = pixels[0].length;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                img.setRGB(x, y, new Color(pixels[x][y], pixels[x][y], pixels[x][y]).getRGB());
            }
        }
        ImageIO.write(img, "png", new File(fileName));
    }

    public String getText() {
        return new String(_chars);
    }

    public BufferedImage getImage() {
        return _img;
    }

    public int[][] getImageArray2D() {
        int ret[][] = new int[_height][_width];
        for (int x = 0; x < _width; x++){
            for (int y = 0; y < _height; y++)
            {
                int p = _img.getRGB(x, y);
                int red = (p >> 16) & 0xff;
                ret[y][x] = red;

            }
        }
        return ret;
    }

    public int[] getImageArray1D() {
        int ret[] = new int[_height * _width];
        int i = 0;
        for (int y = 0; y < _height; y++)
            for (int x = 0; x < _width; x++){
            {
                int p = _img.getRGB(x, y);
                int red = (p >> 16) & 0xff;
                ret[i++] = red;
            }
        }
        return ret;
    }

    public void save(String fileName) throws IOException {
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

    private void shearX(Graphics2D g, int period, int phase, int width, int height) {
        int frames = 15;

        for (int i = 0; i < height; i++) {
            double d = (period >> 1)
                    * Math.sin((double) i / (double) period
                            + (6.2831853071795862D * phase) / frames);
            g.copyArea(0, i, width, 1, (int) d, 0);
        }
    }

    private void shearY(Graphics2D g, int period, int phase, int width, int height) {
        int frames = 15;

        for (int i = 0; i < width; i++) {
            double d = (period >> 1)
                    * Math.sin((float) i / period
                            + (6.2831853071795862D * phase) / frames);
            g.copyArea(i, 0, 1, height, 0, (int) d);
        }
    }
}
