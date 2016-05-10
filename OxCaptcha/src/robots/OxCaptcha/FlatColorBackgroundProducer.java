/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robots.OxCaptcha;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public final class FlatColorBackgroundProducer implements BackgroundProducer {

    private Color _color = Color.GRAY;

    public FlatColorBackgroundProducer() {
        this(Color.GRAY);
    }

    public FlatColorBackgroundProducer(Color color) {
        _color = color;
    }

    @Override
    public BufferedImage addBackground(BufferedImage bi) {
        int width = bi.getWidth();
        int height = bi.getHeight();
        return this.getBackground(width, height);
    }

    @Override
    public BufferedImage getBackground(int width, int height) {
        BufferedImage img = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = img.createGraphics();
        graphics.setPaint(_color);
        graphics.fill(new Rectangle2D.Double(0, 0, width, height));
        graphics.drawImage(img, 0, 0, null);
        graphics.dispose();

        return img;
    }
}
