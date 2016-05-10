package robots.OxCaptcha;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import robots.OxCaptcha.BackgroundProducer;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author gunes
 */
public class TransparentBackgroundProducer implements BackgroundProducer {

	@Override
	public BufferedImage addBackground(BufferedImage image) {
		return getBackground(image.getWidth(), image.getHeight());
	}

	@Override
	public BufferedImage getBackground(int width, int height) {
		BufferedImage bg = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
		Graphics2D g = bg.createGraphics();

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
		g.fillRect(0, 0, width, height);
		
		return bg;
	}

}