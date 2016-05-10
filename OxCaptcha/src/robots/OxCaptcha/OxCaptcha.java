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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;

/**
 *
 * @author gunes
 */
public class OxCaptcha {
    private BufferedImage _img;
    private BufferedImage _bg;
    private String _answer = "";
    private boolean _addBorder = false;
    
    public OxCaptcha(int width, int height) {
        _img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
    
    public OxCaptcha addText() {
        return addText(new DefaultTextProducer());
    }
    
    public OxCaptcha addText(TextProducer txtProd) {
        return addText(txtProd, new DefaultWordRenderer());
    }
    
    public OxCaptcha addText(WordRenderer wRenderer) {
        return addText(new DefaultTextProducer(), wRenderer);
    }
    
    public OxCaptcha addText(TextProducer txtProd, WordRenderer wRenderer) {
            _answer += txtProd.getText();
            wRenderer.render(_answer, _img);

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