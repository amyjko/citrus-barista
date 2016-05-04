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
package edu.cmu.hcii.citrus.views.widgets;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.paints.ImagePaint;

////////////////////////////////////
//
// A Tile wrapped around its image paint.
//
////////////////////////////////////
public class Picture extends View {

	public static final Dec<Image> image = new Dec<Image>(Images.getImage("Slider.png"));

	public static final Dec<Real> width = new Dec<Real>(true, new Expression<Real>() { 
		public Real evaluate(Element<?> env) {
			return env.get(image).width(); }});

	public static final Dec<Real> height = new Dec<Real>(true, new Expression<Real>() { 
		public Real evaluate(Element<?> env) {
			return env.get(image).height(); }});

	public static final Dec<List<ImagePaint>> content = new Dec<List<ImagePaint>>("[(a ImagePaint image=image)]");
	
    public Picture(ArgumentList arguments) { super(arguments); }

}