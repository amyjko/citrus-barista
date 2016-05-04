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
package edu.cmu.hcii.citrus.views;

import java.awt.Component;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JPanel;

import edu.cmu.hcii.citrus.*;

public class Images {

	private static final boolean debug = false;

	private static File imageDirectory = new File(Universe.getImagesPathname());
	private static Hashtable<String,Image> images = new Hashtable<String,Image>(100);
	private static Component c = new JPanel();

	static { setComponent(c); }
	
	public static void setComponent(Component newC) { 

		c = newC;
		if(!imageDirectory.isDirectory()) System.err.println("Warning: Couldn't find an image directory " + imageDirectory);

	}
	
	public static Iterator<Image> getImages() { return images.values().iterator(); }
	public static Iterator<String> getImageNames() { return images.keySet().iterator(); }
	
	private static Image putImage(URL path, String name) {
		
		// Get an image from the file                            
		Image newImage = loadImage(path);

		if(newImage != null)
			images.put(name, newImage);
		else
			throw new ViewError("Couldn't load " + path);

		if(Images.debug)
			System.err.println("Putting " + name + " of size [" + 
								newImage.width() + ", " + 
								newImage.height() + "]");
		
		return newImage;
		
	}
	
	public static edu.cmu.hcii.citrus.views.Image loadImage(URL path) {
		
		if(path.toString().endsWith("jpg") || path.toString().endsWith("png") || path.toString().endsWith("gif")) {
			java.awt.Image newImage = Toolkit.getDefaultToolkit().createImage(path);
			MediaTracker tracker = new MediaTracker(c);
			tracker.addImage(newImage, 0);
			try { tracker.waitForAll(); } catch(InterruptedException ex) {}
			if(newImage != null || !tracker.isErrorAny()) return new Image(newImage);
			else System.err.println("Couldn't load " + path);
			return null;
		} else return null;
		
	}
	
	public static edu.cmu.hcii.citrus.views.Image getImage(String name) {

		Image i = images.get(name);
		if(i != null) return 	i;
		else try { 
			return putImage((new File(imageDirectory, name)).toURL(), name);
		} catch(MalformedURLException ex) { return null; }
			
	}

}