/*
 * 
 * Citrus - A object-oriented, interpreted language that is designed to simplify 
 * the creation of dynamic, immediate feedback graphical desktop applications.
 * 
 * Copyright (c) 2005 Andrew Jensen Ko
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package edu.cmu.hcii.citrus.views.paints;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

public class ImagePaint extends Paint {

	public static final Dec<Image> image = new Dec<Image>(Images.getImage("ellen.jpg"));
	public static final Dec<Real> width = new Dec<Real>("(image width)");

	public ImagePaint() { super(); }
	public ImagePaint(ArgumentList args) { super(args); }
	public ImagePaint(Image newImage) { super(); set(image, newImage); }	

	public Rectangle getPaintBounds(double l, double t, double r, double b) {

		return new Rectangle((int)Math.floor(l) - 1, (int)Math.floor(t) - 1, (int)(r - l + 2), (int)(b - t + 2));

	}

	public void paint(Graphics2D g, View v, double l, double t, double r, double b) {
		
		java.awt.Image img = get(image).value;
		double tileWidth = r - l;
		double tileHeight = b - t;
		double imageWidth = img.getWidth(null);
		double imageHeight = img.getHeight(null);

		double desiredWidth = real(width);
		double scaleFactor = desiredWidth / imageWidth;
		imageHeight *= scaleFactor;
		
		g.drawImage(img, 
			(int)(l + (tileWidth - desiredWidth) / 2), 
			(int)(t + (tileHeight - imageHeight) / 2),
			(int)desiredWidth,
			(int)imageHeight,
			null);

	}

}
