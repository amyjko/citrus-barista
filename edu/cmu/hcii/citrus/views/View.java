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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.util.Vector;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.devices.*;
import edu.cmu.hcii.citrus.views.layouts.*;
import edu.cmu.hcii.citrus.views.shapes.*;

// A Tile is the fundamental building block of this toolkit, much like a JComponent
// is the building block of Swing. A tile is responsible for drawing itself, 
// remembering its bounds, animating between changes in its state, and laying
// out its children. It also maintains a reference to its model: the list, hierarchies,
// etc. that are its children.

public class View extends BaseElement<View> implements Listener<Element> {

	// ///////////////////////////////////////////
	// Containment
	// ///////////////////////////////////////////

	public static final Dec<List<View>> children = new Dec<List<View>>(new NewList<View>());

	// ///////////////////////////////////////////
	// Behaviors
	// ///////////////////////////////////////////

	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>());

	// ///////////////////////////////////////////
	// Appearance
	// ///////////////////////////////////////////

	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(new NewList<Paint>());
	public static final Dec<List<Paint>> content = new Dec<List<Paint>>(new NewList<Paint>());
	public static final Dec<List<Paint>> foreground = new Dec<List<Paint>>(new NewList<Paint>());

	// The alpha blending value for the whole tile. This takes precedent over
	// the alpha settings in paints, allowing a whole tile
	// to be half visible, while the paints may have different transparencies.
	public static final Dec<Real> transparency = new Dec<Real>(new Real(1.0));

	// ///////////////////////////////////////////
	// Layout
	// ///////////////////////////////////////////

	public static final Dec<Layout> layout = new Dec<Layout>(new NoLayout(null));

	// ///////////////////////////////////////////
	// Font (inherited from parent, unless no parent)
	// ///////////////////////////////////////////

	// ///////////////////////////////////////////
	// Flag States
	// ///////////////////////////////////////////

	// Whether or not this tile's children are clipped when painting.
	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(true));

	// Whether or not this tile is hidden
	public static final Dec<Bool> hidden = new Dec<Bool>(new Bool(false));

	// Whether or not this tile is allowed to obtain keyboard focus.
	public static final Dec<Bool> focusable = new Dec<Bool>(new Bool(false));

	// Whether this tile and its children are "hoisted" (painted above its
	// ancestors and siblings).
	public static final Dec<Bool> hoisted = new Dec<Bool>(new Bool(false));

	// Whether this tile reacts to events. Put a constraint on it to make it
	// depend on other state.
	public static final Dec<Bool> enabled = new Dec<Bool>(new Bool(true));

	// ///////////////////////////////////////////
	// Tile geometry
	// ///////////////////////////////////////////

	public Real parentsLayoutsLeft() {
		View par = get(parent);
		if (par == null)
			return new Real(0.0);
		Layout lay = par.get(layout);
		if (lay == null)
			return new Real(0.0);
		return par.get(layout).getLeft(this);
	}

	public Real parentsLayoutsTop() {
		View par = get(parent);
		if (par == null)
			return new Real(0.0);
		Layout lay = par.get(layout);
		if (lay == null)
			return new Real(0.0);
		return par.get(layout).getTop(this);
	}

	public Real defaultRight() {
		return new Real(getContentRight() + real(hPad) * real(scale));
	}

	public Real defaultBottom() {
		return new Real(getContentBottom() + real(vPad) * real(scale));
	}

	// This is the general geometry for a tile: padded bounds, padding, content
	// bounds. Note that if
	// this tile has no layout, the function will fail, and the value will
	// remain unchanged.
	public static final Dec<Real> left = new Dec<Real>(true, "(this parentsLayoutsLeft)");
	public static final Dec<Real> right = new Dec<Real>(true, "(left + (((hPad * 2.0) + width) * scale))");
	public static final Dec<Real> top = new Dec<Real>(true,"(this parentsLayoutsTop)");
	public static final Dec<Real> bottom = new Dec<Real>(true, "(top + (((vPad * 2.0) + height) * scale))");

	// The padding for each of the edges around the content bounds
	public static final Dec<Real> hPad = new Dec<Real>(new Real(0.0));

	public static final Dec<Real> vPad = new Dec<Real>(new Real(0.0));

	// The physical shape of the tile, by default a rectangle shape defined by
	// this tile's boundaries.
	public static final Dec<Shape> shape = new Dec<Shape>(new RectangleShape());

	// The scale of the bounds. These accumulate when parents are nested.
	public static final Dec<Real> scale = new Dec<Real>(new Real(1.0));

	// The tile's content width and height, used for layout purposes.
	public static final Dec<Real> width = new Dec<Real>(new Real(50.0));

	public static final Dec<Real> height = new Dec<Real>(new Real(50.0));

	// The offset at which the tile's contents are painted (for scrolling).
	public static final Dec<Real> xOrigin = new Dec<Real>(new Real(0.0));

	public static final Dec<Real> yOrigin = new Dec<Real>(new Real(0.0));

	protected static Listener notifyApp = new ListenerAdapter() {
		public void outOfDate(Property p, Transition t, Element oldValue) {
			App.propertyIsOutOfDate(p);
		}

		public void changed(Property p, Transition t, Element oldValue,
				Element newValue) {
			App.propertyChanged(p);
		}
	};

	// Parent is constrained.
	public static final Dec<View> parent = new Dec<View>(true,
			new BaseElement<View>() {
				public View evaluate(Element<?> env) {
					return ((View) env).getOwnerOfType(View.class);
				}
			}, true);

	public Transform constructContentTransform() {
		return new Transform(new AffineTransform(real(scale), 0, 0,
				real(scale), getContentLeft() - real(xOrigin), getContentTop()
						- real(yOrigin)));
	}

	// The content transform is depdendent on the content bounds top-left,
	// the scale, and the content origin.
	// (1) Translate by the left and top of the content bounds.
	// (2) Scale by the scaling factor
	// (3) Translate by the content origin
	public static final Dec<Transform> contentTransform = new Dec<Transform>(true, "(this constructContentTransform)");

	public Transform constructCumulativeTransform() {

		AffineTransform rt = new AffineTransform(
				getParentsCumulativeTransform());
		rt.translate(getContentLeft() - real(xOrigin), getContentTop()
				- real(yOrigin));
		double sca = real(scale);
		rt.scale(sca, sca);
		return new Transform(rt);

	}

	// The cumulative transform is equal to the concatenation of the parent's
	// cumulative
	// transform with this tile's content transform.
	public static final Dec<Transform> cumulativeTransform = new Dec<Transform>(
			true, "(this constructCumulativeTransform)");

	// The clip that this tile applies to its children (but not its background
	// or foreground paint),
	// in terms of this tile's coordinate system. If this tile is hoisted, then
	// it inherits its
	// parent's coordinate system.
	public static final Dec<Rectangle> clip = new Dec<Rectangle>(true,
			new Expression<Rectangle>() {
				public Rectangle evaluate(Element<?> env) {
					View t = (View) env;
					View p = t.getParent();
					// If this has no parent, then its the full dimensions of
					// the tile.
					if (p == null)
						return new Rectangle(0, 0, t.real(width) + 1, t
								.real(height) + 1);

					Rectangle2D newClip = new Rectangle2D.Double();
					Rectangle2D parentsClip = p.getClip();
					if (t.bool(clipsChildren)) {

						double x1 = Math.max(parentsClip.getMinX(), t
								.getContentLeft());
						double y1 = Math.max(parentsClip.getMinY(), t
								.getContentTop());
						double x2 = Math.min(parentsClip.getMaxX(), t
								.getContentRight());
						double y2 = Math.min(parentsClip.getMaxY(), t
								.getContentBottom());
						newClip.setFrame(x1, y1, (x2 - x1) + 1, (y2 - y1) + 1);

					} else
						newClip.setFrame(parentsClip);

					Point2D tl = new Point2D.Double(newClip.getX(), newClip
							.getY());
					Point2D br = new Point2D.Double(newClip.getX()
							+ newClip.getWidth(), newClip.getY()
							+ newClip.getHeight());

					// Transform the coordinates to the local coordinate system
					try {
						t.getContentTransform().inverseTransform(tl, tl);
						t.getContentTransform().inverseTransform(br, br);
					} catch (NoninvertibleTransformException e) {
						throw new ViewError("Couldn't invert coordinate.");
					}

					return new Rectangle(tl.getX(), tl.getY(), (br.getX() - tl
							.getX()) + 2, (br.getY() - tl.getY()) + 2);
				}
			});

	static {

		// Parent, clip, and the two transforms aren't overridable
		parent.set(Dec.isOverridable, new Bool(false));
		clip.set(Dec.isOverridable, new Bool(false));
		contentTransform.set(Dec.isOverridable, new Bool(false));
		cumulativeTransform.set(Dec.isOverridable, new Bool(false));

		transparency.is(new PropertyRestriction("(transparency >= 0.0)", "0.0"));
		transparency.is(new PropertyRestriction("(transparency <= 1.0)", "1.0"));

		scale.is(new PropertyRestriction("(scale > 0.0)", "0.01"));

	}

	// ///////////////////////////////////////////
	// Caches
	// ///////////////////////////////////////////

	// These are the boundaries in which this tile was most recently painted.
	// This is used for
	// repairing damaged areas. Note that this is in GLOBAL terms, because it
	// has to be.
	// Otherwise, parents might move, but children's damage would not change,
	// despite being
	// painted in a different place.
	protected Rectangle2D paintedBoundaries = new Rectangle2D.Double();

	// The update time at which damage was posted. This is set when damage is
	// posted on
	// a tile. Damage is only posted if the update time is old, preventing
	// redundant damage posting.
	public long damaged = 0;

	public long paintIdentity = 0;

	// If hoisted changes, we asked the tile's window to hoist or unhoist this
	// tile.
	private ListenerAdapter<Bool> hoistedListener = new ListenerAdapter<Bool>() {
		public void changed(Property p, Transition t, Bool oldValue,
				Bool newValue) {
			Window w = getWindow();
			if (w != null) {
				if (newValue.value)
					getWindow().hoist(View.this);
				else
					getWindow().unhoist(View.this);
			}
		}
	};

	// If the parent changes to null, we post damage on the old parent's painted
	// boundaries
	// to erase this tile.
	private ListenerAdapter parentListener = new ListenerAdapter() {
		public void changed(Property p, Transition t, Object oldValue,
				Object newValue) {

			if (((Element) newValue).isNothing().value
					&& ((Element) oldValue).isSomething().value) {

				// Get the window and post the damage.
				View oldParent = ((View) oldValue).getParent();
				if (oldParent != null) {
					Window w = oldParent.getWindow();
					if (w != null)
						w.childIsDamaged(oldParent.paintedBoundaries);
				}

				// Convert this tile's top left to global coordinates, since it
				// has no parent.
				// java.awt.geom.Point2D newPoint =
				// ((Tile)oldValue).localToGlobal(getDouble(LEFT),
				// getDouble(TOP));
				// setDouble(LEFT, newPoint.getX());
				// setDouble(TOP, newPoint.getY());

			}
			// If the parent changed from nothing to something, transform the
			// global coordinates to the new parent.
			else if (((Element)oldValue).isNothing().value
					&& ((Element) newValue).isSomething().value) {

				// Get the window and post the damage.
				Window w = ((View) newValue).getWindow();
				if (w != null) {
					w.childIsDamaged(((View) newValue).paintedBoundaries);
				}

				// java.awt.geom.Point2D newPoint =
				// ((Tile)newValue).globalToLocal(new
				// Point2D.Double(getDouble(LEFT), getDouble(TOP)));
				// setDouble(LEFT, newPoint.getX());
				// setDouble(TOP, newPoint.getY());

			} else {

			}

		}
	};

	// ///////////////////////////////////////////////////////////////////
	//
	// ///////////////////////////////////////////////////////////////////
	public View(Namespace subtype, ArgumentList arguments) {
		super(subtype, arguments);
		init();
	}

	public View(ArgumentList arguments) {
		super(arguments);
		init();
	}

	public View() {
		super();
		init();
	}

	public void init() {

		// All of these properties affect this tile's appearance, and report
		// back to this tile when they change so that damage can be posted.

		getPropertyByDeclaration(parent).addListener(notifyApp);
		getPropertyByDeclaration(clipsChildren).addListener(notifyApp);
		getPropertyByDeclaration(transparency).addListener(notifyApp);
		getPropertyByDeclaration(hidden).addListener(notifyApp);
		getPropertyByDeclaration(background).addListener(notifyApp);
		getPropertyByDeclaration(content).addListener(notifyApp);
		getPropertyByDeclaration(foreground).addListener(notifyApp);
		getPropertyByDeclaration(left).addListener(notifyApp);
		getPropertyByDeclaration(right).addListener(notifyApp);
		getPropertyByDeclaration(top).addListener(notifyApp);
		getPropertyByDeclaration(bottom).addListener(notifyApp);
		getPropertyByDeclaration(scale).addListener(notifyApp);
		getPropertyByDeclaration(hoisted).addListener(notifyApp);
		getPropertyByDeclaration(enabled).addListener(notifyApp);
		getPropertyByDeclaration(width).addListener(notifyApp);
		getPropertyByDeclaration(height).addListener(notifyApp);
		getPropertyByDeclaration(xOrigin).addListener(notifyApp);
		getPropertyByDeclaration(yOrigin).addListener(notifyApp);
		// getPropertyByDeclaration(font).addListener(notifyApp);

		getPropertyByDeclaration(hoisted).addListener(hoistedListener);
		getPropertyByDeclaration(parent).addListener(parentListener);

	}

	/////////////////////////////////////////////////////////////////////
	//
	// Translation of properties to views
	//
	/////////////////////////////////////////////////////////////////////
	public View translate(Property property) {
		
		return Translator.toView(property);
		
	}
	
	/////////////////////////////////////////////////////////////////////
	//
	// The PropertyListener implementation which handles updates to this Tile's
	// visible properties' values
	//
	/////////////////////////////////////////////////////////////////////
	public void cycle(Property p, List<Property> cycle) {

		System.err.println("*****CYCLE DETECTED******");
		boolean first = true;
		for (Property prop : cycle) {
			System.err.print("" + prop.getElementOwner() + "'s "
					+ prop.getName());
			if (first)
				System.err.println(" depends on ");
			else
				System.err.println(", which depends on ");
			first = false;
		}
		System.err
				.println("" + p.getElementOwner() + "'s " + p.getName() + ".");

	}

	public void outOfDate(Property p, Transition t, Element oldValue) {}

	public void changed(Property p, Transition t, Element oldValue, Element newValue) {}

	public void validityChanged(Property p, Transition t, boolean isValid) {}

	public boolean isHoisted() {

		View t = this;
		while (t != null) {
			if (t.get(hoisted).value)
				return true;
			t = t.getParent();
		}
		return false;

	}

	public boolean isHidden() {

		View t = this;
		while (t != null) {
			if (t.bool(hidden))
				return true;
			t = t.getParent();
		}
		return false;

	}

	// ///////////////////////////////////////////////////////////////////
	//
	// Miscellanea
	//
	// ///////////////////////////////////////////////////////////////////
	public Image getImage(Text image) {
		return Images.getImage(image.value);
	}

	public Style getStyle() {

		return App.getApp().getStyle();

	}

	public List<Image> getImages(Text directory) {

		java.io.File file = new java.io.File(directory.value);
		List<Image> images = new List<Image>();
		if (file.exists()) {
			for (java.io.File f : file.listFiles()) {
				try {
					Image img = Images.loadImage(f.toURL());
					if (img != null) {
						images.append(img);
					}
				} catch (Exception e) {
				}
			}
		}
		return images;

	}

	// ///////////////////////////////////////////////////////////////////
	//
	// Containment
	//
	// ///////////////////////////////////////////////////////////////////

	// Search up the parent hierarchy for the window. This method may return
	// null,
	// indicating that this tile is not in a window (has no parent). Therefore,
	// all calls to this should handle a null.
	public Window getWindow() {

		View win = getParent();
		while (win != null) {
			if (win instanceof Window)
				return (Window) win;
			win = win.getParent();
		}
		return null;

	}

	public App app() {
		return App.getApp();
	}

	public Keyboard keyboard() {
		return App.keyboard;
	}

	public Mouse mouse() {
		return App.mouse;
	}

	// TODO This needs to be getting the visible value.
	public double getVisible(Dec<Real> pd) {
		return getPropertyByDeclaration(pd).getVisible().value;
	}

	public Rectangle2D getClip() {
		return get(clip).value;
	}

	// Continue searching up the ownership until we find a Tile; the
	// intermediate are Lists.
	public View getParent() {
		return get(parent);
	}

	public Bool isDescendantOf(View t) {

		View par = getParent();
		while (par != null) {
			if (par == t)
				return new Bool(true);
			par = par.getParent();
		}
		return new Bool(false);

	}

	public Bool isAncestorOf(View v) {
		return v.isDescendantOf(this);
	}

	// Returns the lowest common ancestor for this view and the given view v,
	// if one exists.
	public View lowestCommonAncestorWith(View v) {

		// Is this an ancestor of v? If so, return this.
		if (this.isAncestorOf(v).value)
			return this;

		// Is v an ancestor of this? If so, return v.
		if (v.isAncestorOf(this).value)
			return v;

		// Otherwise, find the common ancestor.
		View par = getParent();
		while (par != null) {
			// Is this ancestor of this view also an ancestor of v?
			if (par.isAncestorOf(v).value)
				return par;
			par = par.getParent();
		}
		return null;

	}

	public View getFirstChild() {
		return get(children).first();
	}

	public View getLastChild() {
		return get(children).last();
	}

	public View getNextSibling() {

		View par = getParent();
		if (par == null)
			return null;
		else
			return par.get(children).itemAfter(this);

	}

	// Get the List that owns this tile, if there is one, and get the list
	// that owns it. Return that list's element.
	public View getPreviousSibling() {

		View par = getParent();
		if (par == null)
			return null;
		else
			return par.get(children).itemBefore(this);

	}

	// ///////////////////////////////////////////////////////////////////
	// Inserting/Removing children.
	// ///////////////////////////////////////////////////////////////////

	// These rules must be satisified for a child to be added:
	// (1) Can't add a sibling to itself
	// (2) Can't add nothing
	// (3) Can't have a previous or next sibling
	// (4) Can't be owned by another parent.
	public void addChild(View newChild) {
		addChild(newChild, null);
	}

	public void addChild(View newChild, Transition t) {

		isTileInsertable(newChild);
		get(children).appendOverTime(newChild, t);

	}

	// Insert a list of children. Accepts the first child. Child cannot have a
	// previous sibling or a parent.
	public void setChildren(List<View> newChildren) {
		setChildrenOverTime(newChildren, null);
	}

	public void setChildrenOverTime(List<View> newChildren, Transition t) {

		getPropertyByDeclaration(children).set(newChildren, t);

	}

	// Get this tile's element owner, which is always a list, and have it remove
	// itself.
	public Bool remove() {
		return removeOverTime(null);
	}

	public Bool removeOverTime(Transition t) {

		View par = getParent();
		if (par == null)
			return new Bool(false);
		else {
			Window w = getWindow();
			if (w != null)
				w.childIsDamaged(paintedBoundaries);
			Bool result = par.get(children).removeOverTime(this, t);
			return result;
		}

	}

	// Removes this single tile and replaces it with the new one. The
	// replacement tile must
	// be an orphan, with no siblings or parent. Implemented by getting the
	// property that
	// points to this (always a List's element property), and replace it.
	public void replaceWith(View replacementTile) {
		replaceWith(replacementTile, null);
	}

	public void replaceWith(View replacementTile, Transition t) {

		isTileInsertable(replacementTile);
		postDamage();
		View par = getParent();
		if (par == null)
			System.err.println("Can't replace myself, I have no parent");
		else {
			Window w = par.getWindow();
			if (w != null)
				w.childIsDamaged(paintedBoundaries);
			par.get(children).replaceOverTime(this, replacementTile, t);
			replacementTile.postDamage();
		}

	}

	public void isTileInsertable(View newChild) {

		if (newChild == this)
			throw new ViewError("Can't insert " + this + " into itself");
		else if (newChild == null)
			throw new ViewError("Can't insert a null sibling");
		else if (newChild.getParent() != null)
			throw new ViewError("" + newChild + " is already a child of "
					+ newChild.getParent());
		else if (newChild.getNextSibling() != null)
			throw new ViewError("Can't insert " + newChild
					+ "; it's already inserted before "
					+ newChild.getNextSibling());
		else if (newChild.getPreviousSibling() != null)
			throw new ViewError("Can't insert " + newChild
					+ "it's already inserted after "
					+ newChild.getPreviousSibling());

	}

	// ///////////////////////////////////////////////////////////////////
	//
	// Tile geometry.
	//
	// ///////////////////////////////////////////////////////////////////

	public double getVisibleContentLeft() {
		return getVisible(left) + getVisible(hPad) * getVisible(scale);
	}

	public double getVisibleContentTop() {
		return getVisible(top) + getVisible(vPad) * getVisible(scale);
	}

	public double getVisibleContentRight() {
		return getVisibleContentLeft() + getVisible(width) * getVisible(scale);
	}

	public double getVisibleContentBottom() {
		return getVisibleContentTop() + getVisible(height) * getVisible(scale);
	}

	public double getContentLeft() {
		return real(left) + real(hPad) * real(scale);
	}

	public double getContentTop() {
		return real(top) + real(vPad) * real(scale);
	}

	public double getContentRight() {
		return getContentLeft() + real(width) * real(scale);
	}

	public double getContentBottom() {
		return getContentTop() + real(height) * real(scale);
	}

	public AffineTransform getContentTransform() {
		return get(contentTransform).value;
	}

	public AffineTransform getVisibleContentTransform() {
		return getPropertyByDeclaration(contentTransform).getVisible().value;
	}

	public AffineTransform getParentsCumulativeTransform() {

		if (getParent() == null)
			return new AffineTransform();
		else
			return getParent().get(cumulativeTransform).value;

	}

	public AffineTransform getParentsVisibleCumulativeTransform() {

		if (getParent() == null)
			return new AffineTransform();
		else
			return getParent().getPropertyByDeclaration(cumulativeTransform)
					.getVisible().value;

	}

	// ///////////////////////////////////////////////////////////////////
	//
	// Coordinate System Mappings
	//
	// ///////////////////////////////////////////////////////////////////

	// Converts the given global point to this tile's parent's coordinate
	// system. Generates a new point, leaving the given point unchanged.
	public Point globalToLocal(Point p) {

		Point2D p2D = globalToLocal(new Point2D.Double(p.getX(), p.getY()));
		return new Point(p2D.getX(), p2D.getY());
		
	}
	public Point2D globalToLocal(Point2D p) {

		AffineTransform t = getParentsCumulativeTransform();
		if (t == null)
			return p;
		try {
			return getParentsCumulativeTransform().inverseTransform(p, null);
		} catch (NoninvertibleTransformException e) {
			throw new ViewError("Couldn't invert coordinate: " + e + ", " + getParentsCumulativeTransform());
		}

	}

	// Converts the given global point to this tile's local coordinate system.
	// Generates a new point, rather than modifying the given point.
	public Point2D globalToContent(Point2D p) {

		try {
			return get(cumulativeTransform).value.inverseTransform(p, null);
		} catch (NoninvertibleTransformException e) {
			throw new ViewError("Couldn't invert coordinate.");
		}

	}

	// Converts the given local point to the global coordinate system.
	// Generates a new point, rather than modifying the given point.
	public Point2D visibleLocalToGlobal(double x, double y) {
		return visibleLocalToGlobal(new Point2D.Double(x, y));
	}

	public Point2D visibleLocalToGlobal(Point2D p) {
		return getParentsVisibleCumulativeTransform().transform(p, null);
	}

	public Point2D localToGlobal(double x, double y) {
		return localToGlobal(new Point2D.Double(x, y));
	}

	public Point2D localToGlobal(Point2D p) {

		AffineTransform t = getParentsCumulativeTransform();
		if (t == null)
			return p;
		return t.transform(p, null);

	}
	public Point localToGlobal(Point p) {

		Point2D p2D = localToGlobal(new Point2D.Double(p.getX(), p.getY()));
		return new Point(p2D.getX(), p2D.getY());
		
	}

	public Real localLeftOf(View t) {
		return new Real(getLocalTopLeft(t).getX());
	}

	public Real localRightOf(View t) {
		return new Real(getLocalBottomRight(t).getX());
	}

	public Real localTopOf(View t) {
		return new Real(getLocalTopLeft(t).getY());
	}

	public Real localBottomOf(View t) {
		return new Real(getLocalBottomRight(t).getY());
	}

	public Point2D getLocalTopLeft(View t) {

		Point2D p = t.localToGlobal(t.real(left), t.real(top));
		p = globalToLocal(p);
		return p;

	}

	public Point2D getLocalBottomRight(View t) {

		Point2D p = t.localToGlobal(t.real(right), t.real(bottom));
		p = globalToLocal(p);
		return p;

	}

	// ///////////////////////////////////////////////////////////////////
	//
	// Behavior Facade
	//
	// ///////////////////////////////////////////////////////////////////

	public List<Behavior> getBehaviors() {
		return get(behaviors);
	}

	// Given an event, iterate through the tile's behaviors. If one consumes the
	// event,
	// return true. Otherwise, return false.
	public Bool reactTo(Event event) {

		// Don't react if this tile isn't enabled.
		if (!bool(enabled))
			return Bool.FALSE;
		// Don't react if this tile isn't in a window.
		if (getWindow() == null)
			return Bool.FALSE;

		boolean reacted = false;
		for (Behavior b : get(behaviors)) {
			if (b.reactTo(event, this))
				return Bool.TRUE;
		}
		return new Bool(reacted);

	}

	public Bool reactsTo(Event event) {

		for (Behavior b : get(behaviors))
			if (b.reactsTo(event).value)
				return new Bool(true);
		return new Bool(false);

	}

	// ///////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	// ///////////////////////////////////////////////////////////////////

	public void addBackgroundPaint(Paint p) {

		List<Paint> b = get(background);
		if (!b.contains(p).value) {
			b.append(p);
			// p.addDamageListener(this);
		}

	}

	public void addContentPaint(Paint p) {

		List<Paint> c = get(content);
		if (!c.contains(p).value) {
			c.append(p);
			// p.addDamageListener(this);
		}

	}

	public void addForegroundPaint(Paint p) {

		List<Paint> f = get(foreground);
		if (!f.contains(p).value) {
			f.append(p);
			// p.addDamageListener(this);
		}

	}

	public void removeBackgroundPaint(Paint p) {
		get(background).remove(p);
	}

	public void removeContentPaint(Paint p) {
		get(content).remove(p);
	}

	public void removeForegroundPaint(Paint p) {
		get(foreground).remove(p);
	}

	// ///////////////////////////////////////////////////////////////////
	//
	// Tiles --> Pixels.
	//
	// ///////////////////////////////////////////////////////////////////

	// Only renders if the interim bounds interset the damaged rect
	public static int numCalls = 0;

	public void paint(Graphics2D g, boolean drawHoistedTiles) {

		numCalls++;

		// If this
		// /... has empty painted boundaries...
		// ... is hidden...
		// ... is hoisted (and were not drawing hoisted tiles)...
		//
		// ...don't paint this or its children.
		boolean clipChildren = bool(clipsChildren);

		// Don't paint if we're hidden
		if (bool(hidden))
			return;
		// Don't paint if we're hoisted and we're not painting hoisted tiles on
		// this pass
		else if (bool(hoisted) && !drawHoistedTiles)
			return;
		// Don't paint if we clip and we have no boundaries to paint within.
		else if (clipChildren && paintedBoundaries.isEmpty())
			return;

		// Get some state that we reuse throughout this method, avoiding the
		// get() overhead
		double ox = getVisible(xOrigin);
		double oy = getVisible(yOrigin);

		double l = getVisible(left);
		double t = getVisible(top);
		double r = getVisible(right);
		double b = getVisible(bottom);

		double vcl = getVisibleContentLeft();
		double vct = getVisibleContentTop();
		double vcr = getVisibleContentRight();
		double vcb = getVisibleContentBottom();

		AffineTransform parentsTransform = getParentsVisibleCumulativeTransform();

		// If this clips its children and the painted boundaries don't intersect
		// the clip,
		// we can stop painting. We have to untransform the context first, to
		// get the clip
		// into global coordinates.
		g.setTransform(new AffineTransform());
		if (clipChildren && !paintedBoundaries.intersects(g.getClipBounds()))
			return;
		g.setTransform(parentsTransform);

		// Translate by the offset.
		g.translate(-ox, -oy);

		// Composite this tile's transparency with the existing transprency.
		AlphaComposite oldComposite = (AlphaComposite) g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				(float) getVisible(transparency) * oldComposite.getAlpha()));

		// Paint the background layer.
		for (Paint paint : get(background))
			paint.paint(g, this, l, t, r, b);

		// Save the old clip before clipping the children.
		java.awt.Shape oldClip = g.getClip();
		if (clipChildren)
			g.clip(new Rectangle2D.Double(vcl, vct, vcr - vcl + 1, vcb - vct
					+ 1));

		// Draw the content paint layer
		double visibleScale = getVisible(scale);
		g.scale(visibleScale, visibleScale);
		for (Paint paint : get(content))
			paint.paint(g, this, vcl / visibleScale, vct / visibleScale, vcr
					/ visibleScale, vcb / visibleScale);

		// Apply this tile's transform onto the children.
		g.setTransform((getPropertyByDeclaration(cumulativeTransform))
				.getVisible().value);

		// Paint each child, first to last
		for (View child : get(children))
			child.paint(g, drawHoistedTiles);

		// Restore the old transform.
		g.setTransform(parentsTransform);

		// Now we translate the origin back to paint the foreground.
		g.translate(-ox, -oy);

		// Restore the clip
		g.setClip(oldClip);

		// Paint the foreground on top of the children's paint.
		for (Paint p : get(foreground))
			p.paint(g, this, l, t, r, b);

		// Paint some debugging paint
		if (Debug.boundaries()) {
			AffineTransform at = g.getTransform();
			g.setTransform(new AffineTransform());
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					.5f));
			g.setColor(java.awt.Color.green);
			g.drawRect((int) paintedBoundaries.getX(), (int) paintedBoundaries
					.getY(), (int) paintedBoundaries.getWidth() - 1,
					(int) paintedBoundaries.getHeight() - 1);
			g.setTransform(at);
		}

		// Restore the old transparency.
		g.setComposite(oldComposite);

		// Restore the clip
		g.setClip(oldClip);

		// Restore the original transform
		g.setTransform(getParentsVisibleCumulativeTransform());

	}

	// ///////////////////////////////////////////////////////////////////
	//
	// Shape
	//
	// ///////////////////////////////////////////////////////////////////

	// Given a global coordinate, determines if this tile's shape contains the
	// point.
	public Bool contains(Point2D point) {

		Point2D localPoint = globalToLocal(point);
		return new Bool(get(shape).contains(getVisible(left), getVisible(top),
				getVisible(right), getVisible(bottom), localPoint.getX(),
				localPoint.getY()));

	}
	
	public Bool containsLocalPoint(Point point) {
		
		return new Bool(get(shape).contains(getVisible(left), getVisible(top),
				getVisible(right), getVisible(bottom), point.getX(),
				point.getY()));
		
	}

	// ///////////////////////////////////////////////////////////////////
	//
	// Damage
	//
	// ///////////////////////////////////////////////////////////////////

	private Rectangle2D getPaintedBoundaries() {
		return paintedBoundaries;
	}

	protected Rectangle2D computePaintedBoundaries() {

		paintIdentity = 1;

		boolean isHidden = isHidden();
		paintIdentity = paintIdentity * 31 + (new Boolean(isHidden)).hashCode();
		paintIdentity = paintIdentity * 31
				+ (new Double(getVisible(transparency))).hashCode();

		if (isHidden || getParentsVisibleCumulativeTransform() == null) {

			paintedBoundaries.setFrame(0, 0, 0, 0);
			return paintedBoundaries;

		}

		double sca = getVisible(scale);
		double l = getVisible(left);
		double t = getVisible(top);
		double r = getVisible(right);
		double b = getVisible(bottom);
		double ox = getVisible(xOrigin);
		double oy = getVisible(yOrigin);

		double vl = getVisibleContentLeft();
		double vr = getVisibleContentRight();
		double vt = getVisibleContentTop();
		double vb = getVisibleContentBottom();

		paintIdentity = paintIdentity * 31 + (new Double(sca)).hashCode();
		paintIdentity = paintIdentity * 31 + (new Double(ox)).hashCode();
		paintIdentity = paintIdentity * 31 + (new Double(oy)).hashCode();

		// Add up the paints. Start with a null rectangle, so we can know if
		// there's no paint. If
		// at any time we get a non-null boundary from a paint, we set it to
		// that rectangle.
		// From then on, we add() the boundaries.
		paintedBoundaries = null;
		Rectangle2D temp = null;
		for (Paint p : get(background)) {
			temp = p.getPaintBounds(l, t, r, b);
			if (paintedBoundaries == null)
				paintedBoundaries = temp;
			else
				paintedBoundaries.add(temp);
			paintIdentity = paintIdentity * 31 + p.hashCode(this, l, t, r, b);
		}
		for (Paint p : get(content)) {
			temp = p.getPaintBounds(vl, vt, vr, vb);
			if (paintedBoundaries == null)
				paintedBoundaries = temp;
			else
				paintedBoundaries.add(temp);
			paintIdentity = paintIdentity * 31
					+ p.hashCode(this, vl, vr, vt, vb);
		}

		for (Paint p : get(foreground)) {
			temp = p.getPaintBounds(l, t, r, b);
			if (paintedBoundaries == null)
				paintedBoundaries = temp;
			else
				paintedBoundaries.add(temp);
			paintIdentity = paintIdentity * 31 + p.hashCode(this, l, t, r, b);
		}

		// Clip the boundaries, if necessary, and project them to the global
		// coordinate space.
		Point2D newTopLeft, newBottomRight;

		// If it doesn't paint anything, or its empty, set the painted
		// boundaries to the translated boundaries.
		if (paintedBoundaries == null || paintedBoundaries.isEmpty()) {
			paintedBoundaries = new Rectangle2D.Double((int) (vl - ox),
					(int) (vt - oy), (int) (vr - vl) + 1, (int) (vb - vt) + 1);
		} else
			paintedBoundaries
					.setRect(paintedBoundaries.getX() - ox, paintedBoundaries
							.getY()
							- oy, paintedBoundaries.getWidth(),
							paintedBoundaries.getHeight());

		// If this tile is hoisted, or it has no parent, the damage is equal to
		// the tile's paint boundaries.
		boolean isHoisted = isHoisted();
		paintIdentity = paintIdentity * 31
				+ (new Boolean(isHoisted)).hashCode();
		if (isHoisted()) {

			newTopLeft = new Point2D.Double(paintedBoundaries.getMinX(),
					paintedBoundaries.getMinY());
			newBottomRight = new Point2D.Double(paintedBoundaries.getMaxX(),
					paintedBoundaries.getMaxY());

		}
		// If this tile is not hoisted, calculate the intersection of this
		// tile's paint boundaries
		// and it's parent's clip.
		else {

			Rectangle2D parentsClip = getParent()
					.getPropertyByDeclaration(clip).getVisible().value;
			paintIdentity = paintIdentity * 31 + parentsClip.hashCode();

			// If the parent's clip is empty, make the painted boundaries empty.
			if (parentsClip.isEmpty()) {

				newTopLeft = new Point2D.Double(paintedBoundaries.getMinX(),
						paintedBoundaries.getMinY());
				newBottomRight = new Point2D.Double(newTopLeft.getX(),
						newTopLeft.getY());
			}
			// Otherwise, intersect the parent's clip and the paint boundaries.
			else {

				newTopLeft = new Point2D.Double(Math.max(parentsClip.getMinX(),
						paintedBoundaries.getMinX()), Math.max(parentsClip
						.getMinY(), paintedBoundaries.getMinY()));
				newBottomRight = new Point2D.Double(Math.min(parentsClip
						.getMaxX(), paintedBoundaries.getMaxX()), Math.min(
						parentsClip.getMaxY(), paintedBoundaries.getMaxY()));

			}

			// If this is a rectangle with negative bounds, equate the bottom
			// right
			// with the top left, so that it's zero bounds.
			if (newBottomRight.getX() - newTopLeft.getX() <= 0
					|| newBottomRight.getY() - newTopLeft.getY() <= 0)
				newBottomRight.setLocation(newTopLeft);

		}

		// Now that we've computed the painted boundaries for this coordinate
		// system,
		// we translate them to global coordinates to post damage. Note that
		// we should probably be using the visible transform for animation, but
		// this
		// causes problems when repainting within boundaries.
		newTopLeft = visibleLocalToGlobal(newTopLeft);
		newBottomRight = visibleLocalToGlobal(newBottomRight);

		paintIdentity = paintIdentity
				* 31
				+ getPropertyByDeclaration(cumulativeTransform).getVisible()
						.hashCode();

		Rectangle2D newDamage = new Rectangle2D.Double((int) newTopLeft.getX(),
				(int) newTopLeft.getY(),
				(int) (newBottomRight.getX() - newTopLeft.getX()),
				(int) (newBottomRight.getY() - newTopLeft.getY()));

		// Save the global damage so that it can be repaired when this tile
		// changes.
		paintedBoundaries.setFrame(newDamage);

		// Compute all of the children's painted boundaries.
		for (View c : get(children)) {
			if (c == null)
				throw new ElementError("Null child in " + this, null);
			else {
				c.postDamage();
				paintIdentity = paintIdentity * 31 + c.paintIdentity;
			}
		}

		return newDamage;

	}

	// Reports this tile's old and new painted boundaries if the tile
	// is inside of a window. If damage has already been reported,
	// we don't report it.
	public Bool postDamage() {

		if (damaged == App.getUpdateTime())
			return Bool.FALSE;
		damaged = App.getUpdateTime();

		Window win = getWindow();

		// If there's no parent, don't post damage
		if (win == null) {
			if (Debug.postDamage()) {
				if (getParent() == null)
					System.err.println("Can't post damage on " + this
							+ "; no parent.");
				else
					System.err.println("Can't post damage on " + this
							+ "; not in a window.");
			}
			return Bool.FALSE;
		}

		// Save the old paint boundaries in a new rectangle.
		Rectangle2D oldDamage = new Rectangle2D.Double();
		oldDamage.setRect(paintedBoundaries);

		// Save the old paint identity
		long oldPaintIdentity = paintIdentity;

		// Compute the new paint boundaries
		Rectangle2D newDamage = computePaintedBoundaries();

		// Did the identity change?
//		if (oldPaintIdentity == paintIdentity) {
//			return Bool.FALSE;
//		}

		// Send the new, the old, or both.
		if (newDamage.isEmpty()) {
			if (oldDamage.isEmpty()) {
				if (Debug.postDamage())
					System.err.println("NO DAMAGE on " + this);
				return Bool.FALSE;
			} else {
				if (Debug.postDamage())
					System.err.println("ONLY OLD DAMAGE on " + this + ":"
							+ "\n\tOLD = " + oldDamage);
				win.childIsDamaged(oldDamage);
			}
		} else {
			if (oldDamage.isEmpty()) {
				if (Debug.postDamage())
					System.err.println("ONLY NEW DAMAGE on " + this + ":"
							+ "\n\tNEW = " + newDamage);
				win.childIsDamaged(newDamage);
			} else {
				if (Debug.postDamage())
					System.err.println("DAMAGE on " + this + ":" + "\n\tOLD = "
							+ oldDamage + "\n\tNEW = " + newDamage
							+ "\n\tSUM = " + newDamage.createUnion(oldDamage));
				newDamage.add(oldDamage);
				win.childIsDamaged(newDamage);
			}
		}

		return Bool.TRUE;

	}

	// //////////////////////////////////////////////////////////
	//
	// Pixels --> Tiles.
	//
	// //////////////////////////////////////////////////////////

	public View getChildThatContains(Point point) {

		for(View view : get(children))
			if(view.contains(new Point2D.Double(point.getX(), point.getY())).value) 
				return view;
		return null;
		
	}
	
	// Given an empty list, appends tiles from the root down that contain
	// the given global coordinate. The resulting list's first
	// element will be the tile deepest in the tree that contains the point.
	public void whichViewsContain(Point2D testPoint, Vector<View> tiles) {

		// If this tile's paint bounds do not contain the point, it shouldn't
		// handle it, and nor
		// should any of its children. Note that even if the content origin on
		// this
		// tile is moved, the global to local will translate it to screen
		// coordinates.
		boolean containsPoint = contains(testPoint).value;

		// If this tile does not contain the point and it clips its children,
		// then don't bother checking the children, since they shouldn't be
		// interactive
		// if not visible.
		if (!containsPoint && bool(clipsChildren))
			return;

		// If this tile is hidden, don't bother checking it or its children,
		// since it and all
		// of its children aren't interactive.
		if (bool(hidden))
			return;

		// Do any of the children also contain the mouse position? If so, return
		// the deepest child that does.
		// Note that the tile paints in the order of the list, so we need to
		// search this list backwards.
		View c = getLastChild();
		View winner = null;
		while (c != null) {

			// Let the check check if it contains the test point.
			c.whichViewsContain(testPoint, tiles);
			c = c.getPreviousSibling();

		}

		// If this contains the test point, add this to the end of the list
		if (containsPoint)
			tiles.add(this);

	}

	public View whichViewContainsMousePointer() {

		Vector<View> views = new Vector<View>();
		whichViewsContain(App.mouse.pointer.getPosition(), views);
		if (views.isEmpty())
			return null;
		return views.firstElement();

	}

	// Return the global point in this tile's boundaries closest to the given
	// global point.
	public double getMinimumSquaredDistanceFrom(double x, double y) {

		Point2D tl = localToGlobal(real(left), real(top));
		Point2D br = localToGlobal(real(right), real(bottom));

		// Compute the distance from this tile's four edges to the given point.
		double left = Line2D.ptSegDistSq(tl.getX(), tl.getY(), tl.getX(), br
				.getY(), x, y);
		double right = Line2D.ptSegDistSq(br.getX(), tl.getY(), br.getX(), br
				.getY(), x, y);
		double top = Line2D.ptSegDistSq(tl.getX(), tl.getY(), br.getX(), tl
				.getY(), x, y);
		double bottom = Line2D.ptSegDistSq(tl.getX(), br.getY(), br.getX(), br
				.getY(), x, y);

		// Return the smallest squared distance.
		return Math.min(left, Math.min(right, Math.min(top, bottom)));

	}

	// Return the "center" of this tile, as defined by the tile. Text fields,
	// for example,
	// might define their center by their text caret's position.
	public Point2D getGlobalCenter() {

		// Get this tile's global center
		Point2D globalTopLeft = localToGlobal(real(left), real(top));
		Point2D globalBottomRight = localToGlobal(real(right), real(bottom));
		int x = (int) (globalTopLeft.getX() + globalBottomRight.getX()) / 2;
		int y = (int) (globalTopLeft.getY() + globalBottomRight.getY()) / 2;
		return new Point2D.Double(x, y);

	}

	public Bool requestKeyboardFocus(Text direction) {

		return requestKeyboardFocus(direction.value);

	}

	public Bool requestKeyboardFocus(String direction) {

		Window w = getWindow();
		if (w == null)
			w = App.getApp();
		if (w != null)
			return w.focusOn(this, direction);
		else return Bool.FALSE;

	}

	public Bool hasKeyboardFocus() {

		Window w = getWindow();
		if (w == null)
			return new Bool(false);
		else
			return new Bool(w.isFocusedOn(this));

	}

	public Bool isOrContainsFocusableView() {

		if (bool(focusable) && !isHidden())
			return Bool.TRUE;
		for (View child : get(children))
			if (child.isOrContainsFocusableView().value)
				return Bool.TRUE;
		return Bool.FALSE;

	}

	// v is the view we're searching inside of.
	public View focusableVisibleViewClosestTo(Real globalX, Real globalY) {

		double dist = Double.MAX_VALUE;
		View closest = null;

		boolean vertical = true;
		if (get(layout) instanceof HorizontalLayout)
			vertical = false;
		else if (get(layout) instanceof VerticalLayout)
			vertical = true;
		else
			vertical = true;

		for (View child : get(View.children)) {

			// We skip over views that don't contain and aren't themselves
			// focusable.
			if (!child.isOrContainsFocusableView().value)
				continue;

			java.awt.geom.Point2D topLeft = child.localToGlobal(child
					.real(View.left), child.real(View.top));
			java.awt.geom.Point2D bottomRight = child.localToGlobal(child
					.real(View.right), child.real(View.bottom));
			if (vertical) {
				double temp = Math.abs(globalY.value - topLeft.getY());
				if (topLeft.getY() < globalY.value && temp < dist) {
					dist = temp;
					closest = child;
				}
			} else {
				double temp = Math.abs(globalX.value - topLeft.getX());
				if (temp < dist) {
					dist = temp;
					closest = child;
				}
			}

		}

		// If we didn't find one (likely because this has no children)
		if (closest == null) {
			// If this is focusable and visible, return this
			if (bool(View.focusable) && !isHidden())
				return this;
			else
				return null;
		}
		// Otherwise, we return the closest visible view in the closest we found
		// in this view
		else {
			closest = closest.focusableVisibleViewClosestTo(globalX, globalY);
			if (closest == null) {
				// If v is focusable and visible, return v
				if (bool(View.focusable) && !isHidden())
					return this;
				else
					return null;
			} else
				return closest;
		}

	}

	public View firstFocusableFromThis(Bool direction) {

		View newFocus = thisOrFirstFocusableChild(direction);
		if (newFocus == null)
			return focusableSibling(direction);
		else
			return newFocus;

	}

	// Performs a depth first search for a focusable, non-hidden view
	// returning the highest view in the hierarchy (as opposed to the deepest).
	public View thisOrFirstFocusableChild(Bool direction) {

		// Is this focusable?
		if (bool(focusable) && !isHidden())
			return this;

		// Are any of the children focusable?
		View child = direction.value ? get(children).first() : get(children)
				.last();
		while (child != null) {
			View focusableView = child.thisOrFirstFocusableChild(direction);
			if (focusableView != null)
				return focusableView;
			child = direction.value ? child.getNextSibling() : child
					.getPreviousSibling();
		}

		return null;

	}

	public View firstFocusableChild(Bool direction) {

		// Are any of the children focusable?
		View child = direction.value ? get(children).first() : get(children)
				.last();
		while (child != null) {
			View focusableView = child.thisOrFirstFocusableChild(direction);
			if (focusableView != null)
				return focusableView;
			child = direction.value ? child.getNextSibling() : child
					.getPreviousSibling();
		}

		return null;

	}

	public View focusableSibling(Bool direction) {

		View sibling = direction.value ? getNextSibling()
				: getPreviousSibling();
		while (sibling != null) {
			View focusableView = sibling.thisOrFirstFocusableChild(direction);
			if (focusableView != null)
				return focusableView;
			sibling = direction.value ? sibling.getNextSibling() : sibling
					.getPreviousSibling();
		}

		return null;

	}

	// A view's next focusable view is its next sibling's first focusale, or if
	// it has none its
	// parent's next focusable view.
	public View nextFocusableView() {

		View v = focusableSibling(Bool.TRUE);
		if (v == null)
			if (getParent() == null)
				return null;
			else
				return getParent().nextFocusableView();
		else
			return v;

	}

	public View previousFocusableView() {

		View v = focusableSibling(Bool.FALSE);
		if (v == null)
			if (getParent() == null)
				return null;
			else
				return getParent().previousFocusableView();
		else
			return v;

	}

	public View childWithFocus() {

		View focus = getWindow().getFocus();

		View childWithFocus = null;
		for (View child : get(View.children))
			if (focus == child || focus.isDescendantOf(child).value)
				childWithFocus = child;

		return childWithFocus;

	}

	// Do a depth first search inside this tile for the first focusable tile.
	// Returns
	// null if this tile contains no focusable children.
	public View getDeepestFocusableAncestor(boolean getNext) {

		for (View t = getNext ? getFirstChild() : getLastChild(); t != null; t = getNext ? t
				.getNextSibling()
				: t.getPreviousSibling()) {

			View focusable = t.getDeepestFocusableAncestor(getNext);
			if (focusable != null)
				return focusable;
		}
		return bool(focusable) && !isHidden() ? this : null;

	}

	// Search up the tile hierarchy for the first ancestor with a parent with a
	// vertical layout.
	// This essentially searches for what row this tile is in.
	private View getFirstAncestorWithParentWithVerticalLayout() {

		View tileWithParentWithVerticalLayout = this;
		while (tileWithParentWithVerticalLayout.getParent() != null
				&& !(tileWithParentWithVerticalLayout.getParent().get(layout) instanceof VerticalLayout))
			tileWithParentWithVerticalLayout = tileWithParentWithVerticalLayout
					.getParent();

		return tileWithParentWithVerticalLayout;

	}

	// This essentially searches for the deepest set of rows in this tile.
	private View getLastChildWithParentWithVerticalLayout() {

		View lastChild = null;
		for (View c = getFirstChild(); c != null; c = c.getNextSibling())
			lastChild = c.getLastChildWithParentWithVerticalLayout();

		if (lastChild != null)
			return lastChild;
		else if (get(layout) instanceof VerticalLayout)
			return getLastChild();
		else
			return null;

	}

	// This essentially searches for the deepest set of rows in this tile.
	private View getFirstChildWithParentWithVerticalLayout() {

		View lastChild = null;
		for (View c = getLastChild(); c != null; c = c.getPreviousSibling())
			lastChild = c.getFirstChildWithParentWithVerticalLayout();

		if (lastChild != null)
			return lastChild;
		else if (get(layout) instanceof VerticalLayout)
			return getFirstChild();
		else
			return null;

	}

	// Get the current row and get its previous sibling. If it has none
	public View getRowAbove() {

		View currentRow = getFirstAncestorWithParentWithVerticalLayout();
		// If this row has no previous sibling, then we get the previous sibling
		// of the current row's parent.
		while (currentRow != null && currentRow.getPreviousSibling() == null)
			currentRow = currentRow.getParent();

		// If we didn't find a previous sibling, this must be the very top.
		if (currentRow == null)
			return null;
		else {
			View t = currentRow.getPreviousSibling()
					.getLastChildWithParentWithVerticalLayout();
			if (t != null)
				return t;
			else
				return currentRow.getPreviousSibling();
		}

	}

	// Get the current row and get its previous sibling. If it has none
	public View getRowBelow() {

		View currentRow = getFirstAncestorWithParentWithVerticalLayout();
		// If this row has no previous sibling, then we get the previous sibling
		// of the current row's parent.
		while (currentRow != null && currentRow.getNextSibling() == null)
			currentRow = currentRow.getParent();

		// If we didn't find a next sibling, this must be the very bottom.
		if (currentRow == null)
			return null;
		else {
			View t = currentRow.getNextSibling()
					.getFirstChildWithParentWithVerticalLayout();
			if (t != null)
				return t;
			return currentRow.getNextSibling();
		}

	}

	// (1) get the row above this, (2) find the closest focusable tile in that
	// row to this.
	// (3) if there wasn't one, get the row above the row above this, and search
	// in them.
	public View getLastFocusableViewInRowAbove() {

		Point2D center = getGlobalCenter();
		View rowAbove = getRowAbove();
		while (rowAbove != null) {

			View candidate = rowAbove.getDeepestFocusableAncestor(false);
			if (candidate != null)
				return candidate;
			else
				rowAbove = rowAbove.getRowAbove();

		}
		return null;

	}

	public View getFirstFocusableViewInRowBelow() {

		Point2D center = getGlobalCenter();
		View rowBelow = getRowBelow();
		while (rowBelow != null) {

			View candidate = rowBelow.getDeepestFocusableAncestor(true);
			if (candidate != null)
				return candidate;
			else
				rowBelow = rowBelow.getRowBelow();

		}
		return null;

	}

	// ///////////////////////////////////////////////////////
	//
	// Moving the top left. These methods are only useful if the tile's
	// top left corner is unconstrained by a layout.
	//
	// ///////////////////////////////////////////////////////

	public void translate(double tx, double ty) {
		translate(tx, ty, null);
	}

	public void translate(double tx, double ty, Transition t) {
		moveTo(real(left) + tx, real(top) + ty, t);
	}

	public void moveTo(Point2D p) {
		moveTo(p.getX(), p.getY(), null);
	}

	public void moveTo(Point2D p, Transition t) {
		moveTo(p.getX(), p.getY(), t);
	}

	public void moveTo(double x, double y) {
		moveTo(x, y, null);
	}

	public void moveTo(double x, double y, Transition t) {
		set(left, new Real(x), t);
		set(top, new Real(y), t);
	}

	public void makeVisible() {

		if (getWindow() == null)
			return;
		Point2D tl = localToGlobal(real(left), real(top));
		Point2D br = localToGlobal(real(right), real(bottom));
		makeRectangleVisible(tl, br);

	}

	// Defer this to the parent by default. The rectangle is in global
	// coordinates.
	protected void makeRectangleVisible(Point2D tl, Point2D br) {

		if (getParent() != null)
			getParent().makeRectangleVisible(tl, br);

	}

	public Point2D adjustPointWithinWindow(Point2D desiredTopLeft) {

		// ...but this might be offscreen, so we adjust if necessary.
		java.awt.geom.Point2D topLeft = localToGlobal(desiredTopLeft.getX(),
				desiredTopLeft.getY());
		java.awt.geom.Point2D bottomRight = localToGlobal(desiredTopLeft.getX()
				+ paddedWidth().value, desiredTopLeft.getY()
				+ paddedHeight().value);

		// Get the window boundaries.
		Window w = getWindow();
		double width = w.getCanvasWidth();
		double height = w.getCanvasHeight();

		// First bound by the bottom right.
		topLeft.setLocation(Math.min(topLeft.getX(), width
				- (bottomRight.getX() - topLeft.getX())), Math.min(topLeft
				.getY(), height - (bottomRight.getY() - topLeft.getY())));

		// Then bound by the top left.
		topLeft.setLocation(Math.max(0, topLeft.getX()), Math.max(0, topLeft
				.getY()));

		// Convert the coordinates back.
		topLeft = globalToLocal(topLeft);

		return topLeft;

	}

	// ///////////////////////////////////////////////////////////////
	//
	// Common Geometric Constraints.
	//
	// ///////////////////////////////////////////////////////////////

	// Computes the total width of tile, accounting for the content width,
	// padding, and scale.
	public Real paddedWidth() {
		return new Real(((2 * real(hPad)) + real(width)) * real(scale));
	}

	// Computes the total height of tile, accounting for the content height,
	// padding, and scale.
	public Real paddedHeight() {
		return new Real(((2 * real(vPad)) + real(height)) * real(scale));
	}

	// Computes the right-most point of all its children. This constraint
	// function
	// depends on a lot of things, and is fairly slow. Use the last child's
	// right edge
	// if you can guarantee that it's always the right most child (as in a
	// horizontal layout).
	public Real rightmostChildsRight() {

		double maxX = 0.0;
		for (View c : get(children)) {
			double cr = c.real(right);
			if (cr > maxX)
				maxX = cr;
		}
		return new Real(maxX);

	}

	// Computes the lowest point of all its children. This constraint function
	// depends on a lot of things, and is fairly slow. Use the last child's
	// bottom edge
	// if you can guarantee that it's always the bottom-most child (as in a
	// vertical layout).
	public Real bottommostChildsBottom() {

		double maxY = 0.0;
		for (View c : get(children)) {
			double cb = c.real(bottom);
			if (cb > maxY)
				maxY = cb;
		}
		return new Real(maxY);

	}

	public Real widestChildsWidth() {

		double maxWidth = 0.0;
		for (View c : get(children)) {
			double cp = c.paddedWidth().value;
			if (cp > maxWidth)
				maxWidth = cp;
		}
		return new Real(maxWidth);

	}

	public Real tallestChildsHeight() {

		double maxHeight = 0.0;
		for (View c : get(children)) {
			double cp = c.paddedHeight().value;
			if (cp > maxHeight)
				maxHeight = cp;
		}
		return new Real(maxHeight);

	}

	public Real totalHeightOfChildren() {

		double total = 0.0;
		for (View c : get(children))
			total += c.paddedHeight().value;
		return new Real(total);

	}

	public Real parentsWidth() {
		return new Real(getParent() == null ? 0.0 : getParent().real(width) - 2
				* real(hPad));
	}

	public Real parentsHeight() {
		return new Real(getParent() == null ? 0.0 : getParent().real(height)
				- 2 * real(vPad));
	}

	// Returns a function returning the last child's right or bottom coordinate.
	public Real lastChildsRight() {
		View lastChild = getLastChild();
		if (lastChild == null)
			return new Real(0.0);
		else
			return lastChild.get(right);
	}

	public Real lastChildsBottom() {
		View lastChild = getLastChild();
		if (lastChild == null)
			return new Real(0.0);
		else
			return lastChild.get(bottom);
	}

	// Returns a function returning the first child's right or bottom
	// coordinate.
	public Real firstChildsRight() {
		View first = getFirstChild();
		if (first == null)
			return new Real(0.0);
		else
			return first.get(right);
	}

	public Real firstChildsBottom() {
		View first = getFirstChild();
		if (first == null)
			return new Real(0.0);
		else
			return first.get(bottom);
	}

	public Real previousSiblingsRight() {
		
		View prev = getPreviousSibling();
		if(prev == null) return new Real(0.0);
		else return prev.get(right);
		
	}

	public Real previousSiblingsBottom() {
		
		View prev = getPreviousSibling();
		if(prev == null) return new Real(0.0);
		else return prev.get(bottom);
		
	}

	public Real previousSiblingsTop() {
		
		View prev = getPreviousSibling();
		if(prev == null) return new Real(0.0);
		else return prev.get(top);
		
	}

	public Real parentsRemainingWidth() {

		// Does the parent depend on any children?
		View p = getParent();
		if (p == null)
			return new Real(0);
		double total = 0;
		double thisTop = real(top);
		double thisBottom = real(bottom);
		for (View child : p.get(children)) {
			if (child != this) {
				double t = child.real(top);
				double b = child.real(bottom);
				if ((t >= thisTop && t <= thisBottom)
						|| (b >= thisTop && b <= thisBottom))
					total += child.paddedWidth().value;
			}
		}
		return p.get(width).minus(new Real(total));

	}

	// TODO: This doesn't account for a layout's spacing; in other words,
	// we can't just total the children's widths.
	public Real parentsRemainingHeight() {

		View p = getParent();
		if (p == null)
			return new Real(0);
		double total = 0;
		double thisLeft = real(left);
		double thisRight = real(right);
		for (View child : p.get(children)) {
			if (child != this) {
				double l = child.real(left);
				double r = child.real(right);
				if ((l >= thisLeft && l <= thisRight)
						|| (r >= thisLeft && r <= thisRight))
					total += child.paddedHeight().value;
			}
		}
		return p.get(height).minus(new Real(total));

	}

	public Real sumOfChildrensWidths() {

		double sum = 0.0;
		for (View c : get(children))
			sum += c.real(width);
		return new Real(sum);

	}

	public Real greatestSumOfChildrensWidths() {

		double greatest = 0.0;
		for (View c : get(children)) {
			double sum = c.sumOfChildrensWidths().value;
			if (sum > greatest)
				greatest = sum;
		}
		return new Real(greatest);

	}

	// Max
	public Real getMaxChildAscent() {

		double max = 0.0;
		View topChoice = null;
		for (View c : get(children)) {

			if (!c.bool(hidden)) {
				double tempMax = c.getMaxChildAscent().value;
				if (topChoice == null || tempMax > max) {
					topChoice = c;
					max = tempMax;
				}
			}
		}
		if (topChoice == null)
			return get(height);
		else
			return new Real(max);

	}

	// //////////////////////////////////////////////////////////
	//
	// Element interface.
	//
	// Override the user and view methods to avoid accumulating
	// unecessary information about the tiles.
	//
	// //////////////////////////////////////////////////////////

	public void addUser(Property newUser) {
	}

	public void removeUser(Property newUser) {
	}

	public Nothing foreachAncestor(Function f) {

		for (View child : get(children)) {
			Evaluate.eval(this, this, f, new List<Arg>(
					new Arg("", false, child)));
			child.foreachAncestor(f);
		}
		return null;

	}

	// //////////////////////////////////////////////////////////
	//
	// View to String mappings
	//
	// //////////////////////////////////////////////////////////

	public String childrenToString() {

		String result = "Children of " + this + " [parent = " + getParent()
				+ ", first = " + getFirstChild() + ", last = " + getLastChild()
				+ ", previous = " + getPreviousSibling() + ", next = "
				+ getNextSibling() + "]\n";

		for (View c = getFirstChild(); c != null; c = c.getNextSibling())
			result = result + "\t" + c.toString() + " [previous = "
					+ c.getPreviousSibling() + ", next = " + c.getNextSibling()
					+ "]\n";
		return result;

	}

	public String descendantsToString() {
		return descendantsToString(0);
	}

	private String descendantsToString(int index) {

		String str = "";
		for (int i = 0; i < index; i++)
			str = str + "  ";
		str = str + getType() + "\n";
		for (View child : get(children))
			str = str + child.descendantsToString(index + 1);
		return str;

	}

	public Bool undo() {
		return new Bool(Behavior.undo());
	}

}