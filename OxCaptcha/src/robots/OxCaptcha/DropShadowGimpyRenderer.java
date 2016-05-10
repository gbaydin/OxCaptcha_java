/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package robots.OxCaptcha;


import java.awt.image.BufferedImage;
import com.jhlabs.image.ShadowFilter;

/**
 * Adds a dark drop-shadow.
 * 
 * @author <a href="mailto:james.childers@gmail.com">James Childers</a>
 * 
 */
public class DropShadowGimpyRenderer implements GimpyRenderer {
	private static final int DEFAULT_RADIUS = 3;
	private static final int DEFAULT_OPACITY = 75;
	
	private final int _radius;
	private final int _opacity;
	
	public DropShadowGimpyRenderer() {
		this(DEFAULT_RADIUS, DEFAULT_OPACITY);
	}
	
	public DropShadowGimpyRenderer(int radius, int opacity) {
		_radius = radius;
		_opacity = opacity;
	}

	@Override
    public void gimp(BufferedImage image) {
        ShadowFilter sFilter = new ShadowFilter();
        sFilter.setRadius(_radius);
        sFilter.setOpacity(_opacity);
        ImageUtil.applyFilter(image, sFilter);
    }
}