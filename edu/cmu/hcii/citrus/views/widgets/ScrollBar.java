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
import java.util.Hashtable;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;

////////////////////////////////////
//
// A view of a total length, visible length, and offset.
//
////////////////////////////////////
public class ScrollBar extends View {

	public static final Dec<Property<Real>> offset = new Dec<Property<Real>>();
	public static final Dec<Property<Real>> partialLength = new Dec<Property<Real>>();
	public static final Dec<Property<Real>> totalLength = new Dec<Property<Real>>();
	
	public static final Dec<Bool> vertical = new Dec<Bool>(new Bool(true));

	public static final Dec<Real> vPad = new Dec<Real>(new Real(1.0));
	public static final Dec<Real> hPad = new Dec<Real>(new Real(1.0));

	public static final Dec<Real> width = new Dec<Real>(true, new Expression<Real>() { public Real evaluate(Element<?> env) {
		ScrollBar sb = (ScrollBar)env;
		if(sb.bool(vertical)) return sb.getFirstChild().paddedWidth();
		else return sb.parentsWidth();
	}});

	public static final Dec<Real> height = new Dec<Real>(true, new Expression<Real>() { public Real evaluate(Element<?> env) {
		ScrollBar sb = (ScrollBar)env;
		if(sb.bool(vertical)) return sb.parentsHeight();
		else return sb.getFirstChild().paddedHeight();
	}});

	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(View.<List<Paint>>parseExpression("[(this getStyle).scrollBarTrackPaint]"));

	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		new Behavior(App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {
				ScrollBar sb = (ScrollBar)t;
				Point2D p = t.globalToLocal(App.mouse.pointer.getPosition());
				if(sb.bool(vertical))
					if(p.getY() < sb.getFirstChild().real(top))
						sb.setOffset(sb.getOffset() - sb.get(partialLength).get().value);
					else sb.setOffset(sb.getOffset() + sb.get(partialLength).get().value);
				else
					if(p.getX() < sb.getFirstChild().real(left))
						sb.setOffset(sb.getOffset() - sb.get(partialLength).get().value);
					else sb.setOffset(sb.getOffset() + sb.get(partialLength).get().value);
				return true;
			}})));

	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression("[(a Bar)]"));

	public ScrollBar(Namespace subType, ArgumentList args) { super(subType, args); }
	
	public void setOffset(double newOffset) { get(offset).set(new Real(newOffset)); }
	public double getOffset() { return get(offset).get().value; }

	
	
	public static class Bar extends View {
		
		// The x of the bar is a % of this tile's height, where % = offset / (Length - length)
		public static final Dec<Real> left = new Dec<Real>(true, new Expression<Real>() { public Real evaluate(Element env) {
			View bar = (View)env;
			ScrollBar barParent = (ScrollBar)bar.getParent();
			if(barParent == null) return new Real(0.0);
			if(barParent.bool(ScrollBar.vertical)) return new Real(0.0);
			Real barMaxWidth = barParent.get(width).minus(bar.paddedWidth());
			Real viewMaxWidth = barParent.get(totalLength).get().minus(barParent.get(partialLength).get());
			Real off = barParent.get(offset).get();
			if(viewMaxWidth.value > 0) return new Real(barMaxWidth.value * Math.min(1.0, (off.value / viewMaxWidth.value)));
			else return new Real(0.0);
		}});

		// The y of the bar is a % of this tile's height, where % = offset / (Length - length)
		public static final Dec<Real> top = new Dec<Real>(true, new Expression<Real>() { public Real evaluate(Element env) {
			View bar = (View)env;
			ScrollBar barParent = (ScrollBar)bar.getParent();
			if(barParent == null) return new Real(0.0);
			if(!barParent.bool(ScrollBar.vertical)) return new Real(0.0);
			Real barMaxHeight = barParent.get(height).minus(bar.paddedHeight());
			Real viewMaxHeight = barParent.get(totalLength).get().minus(barParent.get(partialLength).get());
			Real off = barParent.get(offset).get();
			if(viewMaxHeight.value > 0) return new Real(barMaxHeight.value * Math.min(1.0, (off.value / viewMaxHeight.value)));
			else return new Real(0.0);			
		}});

		// Width is a % of this tile's width, where % = min(1.0, partial / total length).
		public static final Dec<Real> width = new Dec<Real>(true, new Expression<Real>() { public Real evaluate(Element env) {
			if(((View)env).getParent() == null) return new Real(0.0);
			ScrollBar sb = (ScrollBar)(((View)env).getParent());
			if(sb.bool(ScrollBar.vertical)) return App.getGlobalStyle().get(Style.scrollBarWidth);
			Real total = sb.get(totalLength).get();
			Real partial = sb.get(partialLength).get();
			Real maxHeight = sb.get(width);
			if(total.value > 0) 
				if(total.value > partial.value) return new Real(Math.max(20, maxHeight.value * Math.min(1.0, (partial.value / total.value))));
				else return new Real(0.0);
			else return new Real(0.0); }});

		// Height is a % of this tile's height, where % = min(1.0, partial / total length).
		public static final Dec<Real> height = new Dec<Real>(true, new Expression<Real>() { public Real evaluate(Element env) {
			if(((View)env).getParent() == null) return new Real(0.0);				
			ScrollBar sb = (ScrollBar)(((View)env).getParent());
			if(sb.bool(ScrollBar.vertical)) {
				Real total = sb.get(totalLength).get();
				Real partial = sb.get(partialLength).get();
				Real maxHeight = sb.get(height);
				if(total.value > 0) 
					if(total.value > partial.value) return new Real(Math.max(20, maxHeight.value * Math.min(1.0, (partial.value / total.value))));
					else return new Real(0.0);
				else return new Real(0.0); 
			} else {
//					if(sb.getFirstChild().getReal(CONTENTWIDTH) <= 0.0) return 0.0;
				/*else */return App.getGlobalStyle().get(Style.scrollBarWidth);
			}				
		}});	
		
		public static final Dec<List<Paint>> background = new Dec<List<Paint>>(View.<List<Paint>>parseExpression("[(this getStyle).scrollBarPaint]"));

		public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
			new Behavior(App.mouse.leftButton.pressed, App.mouse.pointer.pick),
			new Behavior(App.mouse.leftButton.released, App.mouse.pointer.release),
			// Map mouse movements to offset changes.
			new Behavior(App.mouse.pointer.dragged, new Action() { 
				public boolean evaluate(View t) {
					ScrollBar sb = (ScrollBar)t.getParent();
					// Map the mouse position to a valid offset.
					if(sb.bool(ScrollBar.vertical)) 
						sb.setOffset(Math.max(0.0, Math.min(1.0, (App.mouse.pointer.positionRelativeToPointPicked().getY() / 
										   (sb.get(height).value - t.paddedHeight().value)))) * 
								   		   (sb.get(totalLength).get().value - sb.get(partialLength).get().value));
					else sb.setOffset(Math.max(0.0, Math.min(1.0, (App.mouse.pointer.positionRelativeToPointPicked().getX() / 
										   (sb.get(width).value - t.paddedWidth().value)))) * 
								   		   (sb.get(totalLength).get().value - sb.get(partialLength).get().value));
					return true;
				
				}})));

		public Bar() { super(); }
		public Bar(ArgumentList arguments) { super(arguments); }
		
	}
	
}