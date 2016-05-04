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

import java.awt.geom.Point2D;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.devices.*;

// NOTE: Because a tile's content origin must be no greater than the viewToScroll.maxX - viewToScroll.width,
// If the tile to view's width is constrained to the max x, the content origin will always be 0.

// Fits to its parent's content dimensions.

public class ScrollView extends View {

	// A view of a tile.
	public static final Dec<View> viewToScroll = new Dec<View>((Element)null, true);

	public static final Expression<Real> maxXFunction = new Expression<Real>() { public Real evaluate(Element<?> env) {
		View tileInView = env.get(viewToScroll);
		return tileInView.paddedWidth();
//		return tileInView.rightmostChildsRight().plus(tileInView.get(hPad).times(new Real(2.0)));
	}};
	public static final Expression<Real> maxYFunction = new Expression<Real>() { public Real evaluate(Element<?> env) {
		View tileInView = env.get(viewToScroll);
		return tileInView.paddedHeight();
//		return tileInView.bottommostChildsBottom().plus(tileInView.get(vPad).times(new Real(2.0)));
	}};
	
	public static final Dec<Real> maxX = new Dec<Real>(true, maxXFunction);
	public static final Dec<Real> maxY = new Dec<Real>(true, maxYFunction);

	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(true));

	public static final Dec<Real> width = new Dec<Real>(true, "(this parentsWidth)");
	public static final Dec<Real> height = new Dec<Real>(true, "(this parentsRemainingHeight)");
	static {
		width.is(new PropertyRestriction("(width >= 64.0)", "64.0"));
		height.is(new PropertyRestriction("(width >= 64.0)", "64.0"));
	}

	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		new Behavior(App.mouse.leftButton.pressed, new Action() {
			public boolean evaluate(View t) {

				return t.get(viewToScroll).reactTo(App.eventJustProcessed).value;
				
			}
		}),
		new Behavior(App.mouse.wheel.moved, new Action() {
			public boolean evaluate(View t) {
				ScrollView sv = (ScrollView)t;
				if(App.keyboard.SHIFT.isDown())
					sv.getHorizontalScrollBar().setOffset(sv.getHorizontalScrollBar().getOffset() + App.mouse.wheel.get(MouseWheel.unitsMoved).value);
				else
					sv.getVerticalScrollBar().setOffset(sv.getVerticalScrollBar().getOffset() + App.mouse.wheel.get(MouseWheel.unitsMoved).value);
				return true;
			}
		}),
		new Behavior(App.keyboard.PAGEUP.pressed, new Action() {
			public boolean evaluate(View t) {
				ScrollView sv = (ScrollView)t;
				sv.getVerticalScrollBar().setOffset(sv.getVerticalScrollBar().getOffset() - sv.get(viewToScroll).get(height).value);
				return true;
			}
		}),
		new Behavior(App.keyboard.PAGEDOWN.pressed, new Action() {
			public boolean evaluate(View t) {
				ScrollView sv = (ScrollView)t;
				sv.getVerticalScrollBar().setOffset(sv.getVerticalScrollBar().getOffset() + sv.get(viewToScroll).get(height).value);
				return true;
			}
		})));

	// ScrollViewViewer, horizontal scroll bar, vertical scroll bar.
	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(
			"[" +
				"(a Viewer)" + 
			"]"));
	
	private static Element HBAR = CitrusParser.code(
			"(a HorizontalScrollBar " +
			"vertical=false " +
			"offset=((children first).children first).@xOrigin " +
			"partialLength=(children first).@width " +
			"totalLength=@maxX)");
	
	private static Element VBAR = CitrusParser.code(
			"(a VerticalScrollBar " +
			"vertical=true " +
			"offset=((children first).children first).@yOrigin " +
			"partialLength=(children first).@height " +
			"totalLength=@maxY)");

	public ScrollView(Namespace subType, ArgumentList arguments) { 
	
		super(subType, arguments); 

		get(children).append((View)HBAR.evaluate(this));
		get(children).append((View)VBAR.evaluate(this));

	}
    
    // Searches for focusable tiles do not escape a scroll view.
	private View getFocusableTile(boolean getNext, View from) { 
		
		return null;
		
	}
    	
    // Move the content origin to make this rectangle visible.
    protected void makeRectangleVisible(Point2D tl, Point2D br) {
    	
    		View viewerViewer = get(children).first().get(children).first();
    	
    		tl = globalToContent(tl);
    		br = globalToContent(br);
    		
    		View viewer = getFirstChild().getFirstChild();    		
    		Property<Real> ox = viewer.getPropertyByDeclaration(xOrigin);
    		Property<Real> oy = viewer.getPropertyByDeclaration(yOrigin);

    		// Note, that by the time the points get here, they are already relative to the
    		// content origin. Thus, we simply adjust the origin by the deviation of the coordinates.

    		// We only account for the bottom right if the rectangle is smaller than this scroll view
    		
    		// If the left edge is out of view, move the origin to the left.
    		if(tl.getX() < 0)
    			ox.set(new Real(ox.get().value + tl.getX()));

    		// If the right edge is out of view, move the origin to the right.
    		if(br.getX() - tl.getX() < viewer.get(width).value && br.getX() > viewer.get(width).value - 12)
    			ox.set(new Real(ox.get().value  + br.getX() - (viewer.get(width).value - 12)));

    		// If the top edge is out of view, move the origin up.
    		if(tl.getY() < 0)
    			oy.set(new Real(oy.get().value  + tl.getY() - 12));

    		// If the bottom edge is out of view, move the origin down.
    		if(br.getY() - tl.getY() < viewer.get(height).value && br.getY() > viewer.get(height).value)
    			oy.set(new Real(oy.get().value  + br.getY() - (viewer.get(height).value - 12)));

    }
    
    public ScrollBar getHorizontalScrollBar() { return (ScrollBar)get(children).nth(new Int(2)); }
    public ScrollBar getVerticalScrollBar() { return (ScrollBar)get(children).nth(new Int(3)); }
    
    public View getViewer() { return get(children).first(); }
    
    public static class HorizontalScrollBar extends ScrollBar {
    	
	    	public static final Dec<Real> width = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
				if(env.get(parent) != null)
					if(env.get(parent).get(children).first() != null)
						return env.get(parent).get(children).first().get(width); 
				return new Real(0.0);
			}});
	
	    	public static final Dec<Real> height = new Dec<Real>(View.<Real>parseExpression("(this getStyle).scrollBarWidth"));
	
	    	public static final Dec<Real> top = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
				if(env.get(parent) != null)
					if(env.get(parent).get(children).first() != null)
						return env.get(parent).get(children).first().get(bottom); 
				return new Real(0.0);
			}});
	
	    	public HorizontalScrollBar(Namespace subType, ArgumentList args) {
	    		
	    		super(subType, args);
	    		set(left, new Real(0.0));
	
	    	}
    	
    }
    
    public static class VerticalScrollBar extends ScrollBar {

	    	public static final Dec<Real> left = new Dec<Real>(true, new Expression<Real>() { public Real evaluate(Element<?> env) {
				if(env.get(parent) != null)
					if(env.get(parent).get(children).first() != null)
						return env.get(parent).getFirstChild().get(right); 
				return new Real(0.0);
			}});
	
	    	public static final Dec<Real> height = new Dec<Real>(true, new Expression<Real>() { public Real evaluate(Element<?> env) {
				if(env.get(parent) != null)
					if(env.get(parent).get(children).first() != null)
						return env.get(parent).get(children).first().get(height); 
				return new Real(0.0);
			}});
	
	    	public static final Dec<Real> width = new Dec<Real>("(this getStyle).scrollBarWidth");
	
	    	public VerticalScrollBar(Namespace subType, ArgumentList args) { 

	    		super(subType, args);	    		
	    		set(top, new Real(0.0));
	
	    	}	
    	
    }
    
    public static class Viewer extends View {

	    	public static final Dec<Real> width = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
				ScrollView sv = (ScrollView)(env.get(parent));
				if(sv == null || sv.getVerticalScrollBar() == null) return new Real(0.0);
				return new Real(sv.paddedWidth().value - sv.getVerticalScrollBar().paddedWidth().value - 1); }});
	
	    	public static final Dec<Real> height = new Dec<Real>(true, new BaseElement<Real>() { public Real evaluate(Element<?> env) {
				ScrollView sv = (ScrollView)(env.get(parent));
				if(sv == null || sv.getHorizontalScrollBar() == null) return new Real(0.0);
				return new Real(sv.paddedHeight().value - sv.getHorizontalScrollBar().paddedHeight().value - 1); }});
	    	
	    	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(true));
	
	    	public static final Dec<List<View>> children = new Dec<List<View>>("[(a ViewerViewer)]");
	
	    	public Viewer(ArgumentList arguments) { super(arguments); }
    	
    }
    
    //  The tile that contains the tile to view, and doesn't clip.
    public static class ViewerViewer extends View {

	    	public static final Dec<Real> width = new Dec<Real>(true, "(this parentsWidth)");
	    	public static final Dec<Real> height = new Dec<Real>(true, "(this parentsHeight)");
	
	    	public static final Dec<Real> xOrigin = new Dec<Real>(new Real(0.0));	
	    	public static final Dec<Real> yOrigin = new Dec<Real>(new Real(0.0));	
	
	    	static {
	
	    		xOrigin.is(new PropertyRestriction("(xOrigin <= (maxX - width))", "(0.0 max (maxX - width))"));	    		
	    		xOrigin.is(new PropertyRestriction("(xOrigin >= 0.0)", "0.0"));
//	    			return new Real(Math.max(0, env.get(maxX).value - env.get(width).value)); }}, true));

	    		yOrigin.is(new PropertyRestriction("(yOrigin <= (maxY - height))", "(0.0 max (maxY - height))"));
	    		yOrigin.is(new PropertyRestriction("(yOrigin >= 0.0)", "0.0"));

	    	}
	
	    	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));
	
	    	public static final Dec<List<View>> children = new Dec<List<View>>("[viewToScroll]");
	    	
	    	public ViewerViewer(ArgumentList arguments) { super(arguments); }
    	
    }

}