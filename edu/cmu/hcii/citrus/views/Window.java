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

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;

import javax.swing.*;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.devices.*;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.awt.event.KeyEvent;

// A Tile that's painted inside a Swing JFrame.
public class Window extends View {

	// The minimum and default window size
    private static final Dimension minimumCanvasSize = new Dimension(100, 100);
    
    // The graphics context of the image buffer.
    private Graphics2D graphics;

    // The canvas to paint on that we will paint the buffered image on.
    private JComponent canvas;

    // The buffer that we'll paint to and then paint to screen.
    private BufferedImage buffer;
    
	// The tiles currently hoisted above their containers. These are paint last,
	// get events first, and aren't clipped.
	private HashSet<View> hoistedTiles = new HashSet<View>(5);

	// This is the rectangle that contains the pixels that need to be redrawn
	private Rectangle2D damagedArea;

	// Clusters of damage
	private LinkedList<Rectangle2D> damagedAreas;
    
	// The JFrame
	protected JFrame frame;

	// The keyboard focus in this window.
	public static final String FOCUS = "focus";
	public static final String LASTFOCUS = "lastFocus";
	
	public static final Dec<Int> blinkRate = new Dec(new Int(600));
	public static final Dec<Bool> paintCaret = new Dec<Bool>(new Bool(true));
	private long lastFocusChange = 0;

	// The keyboard focus is the tile that is currently receiving keyboard events
	// This causes all keyboard events to go to the keyboard focus.
	public static final Dec<View> focus = new Dec<View>((Element)null, true);
	public static final Dec<View> lastFocus = new Dec<View>((Element)null, true);
	static {
		focus.set(Dec.isUndoable, Bool.TRUE);
		lastFocus.set(Dec.isUndoable, Bool.TRUE);
	}
	
	public Listener focusChangeListener = new ListenerAdapter() {
		public void changed(Property p, Transition t, Element o, Element n) {

			View oldFocus = o.isNothing().value ? null : (View)o;
			View newFocus = n.isNothing().value ? null : (View)n;
			
			if(newFocus == null) debug("Focus is null. That's not allowed!");
			else {
				if(!newFocus.bool(focusable)) debug("" + newFocus + " is not focusable");
				if(newFocus.getWindow() == null) debug("" + newFocus + " is not in a window");
			}

			// Set the last keyboard focus.
			set(lastFocus, oldFocus);
			
			// Have the old focus react to focus lost.
			if(oldFocus != null) {
				
				oldFocus.reactTo(new App.FocusLost());
				View par = oldFocus.getParent();
				while(par != null) {
					par.reactTo(new App.ChildLostFocus());
					par = par.getParent();
				}
				
			}

			// If the new focus is not null, set the blinker
			if(newFocus != null && caretBlinkTimer == null) {
				caretBlinkTimer = new Timer(true);
				caretBlinkTimer.scheduleAtFixedRate(new TimerTask() { public void run() {
					if(System.currentTimeMillis() - lastFocusChange > 500)
						App.enqueue(App.blinkCaret);
				}}, 0, integer(blinkRate));
			}
			
			// Remove the old focus as a listener
			if(oldFocus != null) {
				oldFocus.postDamage();
				getPropertyByDeclaration(paintCaret).removeListener(damageCaret);
			}

			// Add the new focus as a listener.
			if(newFocus != null) {
				getPropertyByDeclaration(paintCaret).addListener(damageCaret);

				// Have the new focus react to the focus being received
				newFocus.reactTo(new App.FocusReceived());
			
				// Set the paint caret flag to true and have the focus react to the blink
				lastFocusChange = System.currentTimeMillis();
				set(paintCaret, new Bool(true));
				newFocus.reactTo(App.blinkCaret);

				View par = newFocus.getParent();
				while(par != null) {
					par.reactTo(new App.ChildReceivedFocus());
					par = par.getParent();
				}
			
				// Make the focus visible, if possible
				newFocus.makeVisible();
				
			}
			
		}
	};


	public static final Dec<Bool> resizable = new Dec<Bool>(new Bool(true));

	public static final Dec<Real> width = new Dec<Real>(new Real(200.0));
	public static final Dec<Real> height = new Dec<Real>(new Real(200.0));
	
	// Direction of the last focus movement. 0 indicates no particular direction,
	// -1 indicates previous, 1 indicates next.
	private String directionOfLastFocusMovement;
	private static HashSet<String> validFocusMovementDirections = new HashSet<String>(10);
	static {
		validFocusMovementDirections.add("above");
		validFocusMovementDirections.add("below");
		validFocusMovementDirections.add("next");
		validFocusMovementDirections.add("previous");
		validFocusMovementDirections.add("left");
		validFocusMovementDirections.add("right");
		validFocusMovementDirections.add("mouse");
	}
	
	private Timer caretBlinkTimer = null;
	
    // Creates the window, the canvas, the image buffer, and the graphics context to draw with
	public Window() { super(null, null); init("", true, 640, 480); }
	public Window(Namespace type, ArgumentList args) { super(type, args); init("", true, (int)real(width), (int)real(height)); }
	public Window(String title, boolean resizable, int width, int height) {

    		super(null, null);
    		init(title, resizable, width, height);

	}

	// The window closing event.
	public static class ElementDropped extends Event {
		
		public static final Dec<Element> element = new Dec<Element>(false, null, true);

		public ElementDropped(Window window, Element newElement) { 
		
			super(window, 0); 
			set(element, newElement);
			
		}
		public ElementDropped(ArgumentList args) { super(args); }

		public void handle() { 
			
			View view = window.whichViewContainsMousePointer();
			while(view != null && !view.reactTo(this).value) view = view.getParent();
			if(view == null)
				System.err.println("Couldn't find a view to handle element dropped");
			
		}
		public boolean isNegligible() { return false; }
	
	}

	protected void init(String title, boolean resizable, int width, int height) {
    		
        	// TODO: Dynamic layout doesn't seem to work on Windows.
		// Turn on dynamic layout during resize.
        	Toolkit.getDefaultToolkit().setDynamicLayout(true);
		
		// Whenever Swing requests a repaint, the canvas paints itself by painting the complete buffered image.
        // This should only happen when resizing the canvas, since all other times the canvas can be painted
        // onto via it's graphics context without calling repaint.
		canvas = new JComponent() { 
			public void paint(Graphics g) {
				repaintCanvas(new Rectangle2D.Double(0, 0, getCanvasWidth(), getCanvasHeight()), null); }};
		RepaintManager.currentManager(canvas).setDoubleBufferingEnabled(false);		
		canvas.setMinimumSize(minimumCanvasSize);
		canvas.setPreferredSize(new Dimension(width, height));
		canvas.setSize(new Dimension(width, height));
		canvas.setOpaque(false);
		canvas.setDoubleBuffered(true);
		
		// Set the content pane to the canvas
		frame = new JFrame(title);
		frame.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
		frame.setTitle(title);
    		frame.setResizable(resizable);
    		frame.setSize(width, height);
		frame.setContentPane(canvas);
		frame.pack();
		
        	// Created an image buffer to draw in. Note that we create a compatible image from the graphics
        	// environment so that we gain the advantage of hardware acceleration. We don't need the buffer
        	// to be transparent, so we ask the canvas to create it.
        	java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        	if(frame.isResizable()) buffer = (BufferedImage)canvas.createImage(screenSize.width, screenSize.height);
        	else buffer = (BufferedImage)canvas.createImage(width, height);
        	
        	// Create a graphics context from the image, and set the anti-aliasing flags for quality.
        	graphics = buffer.createGraphics();
        	graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        	graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        	graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        	graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        	graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);        

        	// Clear the canvas
        	graphics.setBackground(java.awt.Color.black);
        	graphics.clearRect(0, 0, screenSize.width, screenSize.height);        

		// No damage yet
		damagedArea = null;

		// Note that we add the component listener to this window, and NOT
		// any other components. This is because we want to be notified of window
		// resizes immediately, so that we can immediately repaint. If we we're to
		// listen to resizes to the canvas component, we'd already be too late for painting.
		frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) { 
				quicklyResize(); 
			}
		});

		frame.addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent e) { 
				quicklyResize(); 			
			}
		});

		frame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) { if(Debug.window()) System.err.println("WINDOW Opened"); }
			public void windowClosing(WindowEvent e) { 
				
				if(Debug.window()) System.err.println("WINDOW Closing");
				App.enqueue(new App.Closing(Window.this, System.currentTimeMillis()));
				App.remove(Window.this);
				
			}
			public void windowIconified(WindowEvent e) { if(Debug.window()) System.err.println("WINDOW Iconified"); }
			public void windowDeiconified(WindowEvent e) { if(Debug.window()) System.err.println("WINDOW Deiconified"); }
			public void windowActivated(WindowEvent e) {
				
				App.windowInFocus = Window.this;
				if(Debug.window()) System.err.println("WINDOW Activated");
				
			}
			public void windowDeactivated(WindowEvent e) { if(Debug.window()) System.err.println("WINDOW Deactivated"); }
		});

		frame.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) { 

				//shift, control, alt, meta
				App.enqueue(new Keyboard.Typed(Window.this, e.getWhen(), App.keyboard, 
						new Char(e.getKeyChar()),
						App.keyboard.SHIFT.get(KeyboardKey.down),
						App.keyboard.CONTROL.get(KeyboardKey.down),
						App.keyboard.ALT.get(KeyboardKey.down),
						App.keyboard.COMMAND.get(KeyboardKey.down))); 
				
			}
			public void keyPressed(KeyEvent e) { 
				KeyboardKey key = App.keyboard.getKeyboardKeyByKeycode(e.getKeyCode());
				if(key == null) return;
				App.enqueue(new Keyboard.Pressed(Window.this, e.getWhen(), 
						key.get(KeyboardKey.name),
						App.keyboard.SHIFT.get(KeyboardKey.down),
						App.keyboard.CONTROL.get(KeyboardKey.down),
						App.keyboard.ALT.get(KeyboardKey.down),
						App.keyboard.COMMAND.get(KeyboardKey.down))); 	
			}
			public void keyReleased(KeyEvent e) { 
				KeyboardKey key = App.keyboard.getKeyboardKeyByKeycode(e.getKeyCode());
				if(key == null) return;
				App.enqueue(new Keyboard.Released(Window.this, e.getWhen(), 
						key.get(KeyboardKey.name),
						App.keyboard.SHIFT.get(KeyboardKey.down),
						App.keyboard.CONTROL.get(KeyboardKey.down),
						App.keyboard.ALT.get(KeyboardKey.down),
						App.keyboard.COMMAND.get(KeyboardKey.down))); 
			}
		});

		// Add the mouse listeners
		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) { 
				MouseButton mb = App.mouse.getMouseButtonByCode(e.getButton());
				if(mb == null) return;
				App.enqueue(new MouseButton.Pressed(Window.this, e.getWhen(), mb, new Point(e.getX(), e.getY()))); 
			}
			public void mouseReleased(MouseEvent e) { 	
				MouseButton mb = App.mouse.getMouseButtonByCode(e.getButton());
				if(mb == null) return;
				App.enqueue(new MouseButton.Released(Window.this, e.getWhen(), mb, new Point(e.getX(), e.getY()))); 
			}
			public void mouseExited(MouseEvent e) { setCursorTo(Cursor.DEFAULT_CURSOR); }
		});

		canvas.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) { 
				App.enqueue(new MousePointer.Moved(Window.this, e.getWhen(), App.mouse.pointer, new Real(e.getX()), new Real(e.getY())));
			}
			public void mouseDragged(MouseEvent e) { 
				App.enqueue(new MousePointer.Moved(Window.this, e.getWhen(), App.mouse.pointer, new Real(e.getX()), new Real(e.getY())));
			}
		});
		
		canvas.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) { 
				App.enqueue(new MouseWheel.Moved(Window.this, e.getWhen(), App.mouse.wheel, new Int(e.getUnitsToScroll())));
			}
		});
		
		DropTarget fileTarget = new DropTarget(frame.getContentPane(), new DropTargetListener() {
            
            public void dragEnter(DropTargetDragEvent e) {}
            public void dragExit(DropTargetEvent e) {}
            public void dragOver(DropTargetDragEvent e) {}
            public void dropActionChanged(DropTargetDragEvent e) {}
            public void drop(DropTargetDropEvent e) {
            	
                if(e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    
                    java.util.List<java.io.File> fileList = null;
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    try {
                        
                        fileList = (java.util.List<java.io.File>)e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        Element newElement = XMLParser.readXMLFrom(new Text(fileList.get(0).getAbsolutePath()));
                        if(newElement != null) {
                        	App.enqueue(new ElementDropped(Window.this, newElement));  
                        }
                        else System.err.println("Didn't drop an xml file with an element");
                        
                    } catch(Exception ex) {
                        
                    		System.err.println("I couldn't get the data because " + ex);
                        
                    }
                    
                }
                else System.err.println("Not a file list; won't accept it.");
   
                e.dropComplete(true);
                
            }
        
        });
        frame.getContentPane().setDropTarget(fileTarget);

		// Update the dimensions. 
		set(Window.width, new Real(width));
		set(Window.height, new Real(height));
		
		get(behaviors).append(new Behavior(App.keyboard.typed, new Action() { public boolean evaluate(View t) {
				
			if(App.keyboard.COMMAND.isDown()) {
				char lastCharacter = App.keyboard.character(Keyboard.lastChar);
				if(lastCharacter == 'c') {
					debug("Copying focus");
					View focus = getFocus();
					if(focus instanceof ElementView) {
						
						Element e = focus.get(ElementView.model);
						if(e != null) {
							
							clipboard = e.duplicate();
							return true;
							
						} debug("Focus has no element");
						
					} else debug("Focus is not an element view: " + focus);

				}
				else if(lastCharacter == 'v') {

					View focus = getFocus();
					if(focus != null && !(focus instanceof ElementView)) focus = focus.getParent();
					if(focus instanceof ElementView) {
						
						Element duplicate = clipboard.duplicate();
						debug("Replacing " + ((ElementView)focus).getModel() + " with " + duplicate);
						((ElementView)focus).getModel().replaceWith(null, duplicate, getStyle().getQuickTransition());
						return true;
						
					} else debug("Focus is not an element view: " + focus);
					
				}
			}
			return false;
				
		}}));
		
		getPropertyByDeclaration(focus).addListener(focusChangeListener);
		
    }
    
    private Element clipboard;

	public View getFocus() { return get(focus); }
	public View getLastFocus() { return get(lastFocus); }
	
	public String getDirectionOfLastFocusMovement() { return directionOfLastFocusMovement; }

	public Bool shouldPaintCaret() { return peek(paintCaret); }

	public boolean isFocused() { return get(focus).isSomething().value; }
	public boolean isFocusedOn(View tile) { return getFocus() == tile; }
    
	// Get the view of the given element, if there is one, and focus on it's next focusable child.
	public void focusOn(Element<?> e, String direction) {

		View t = null;
		for(View view : e.getViews())
			if(view != null && view.getWindow() != null) { t = view; break; }
		
		if(t == null) return;
		t = t.getDeepestFocusableAncestor(true);
		if(t == null) return;
		
		focusOn(t, direction);
		
	}
	
	public Listener damageCaret = new ListenerAdapter() {
		public void changed(Property p, Transition t, Element o, Element n) {
			View focus = getFocus();
			if(focus != null) focus.postDamage();
		}
	};
	
	// We force damage to be posted
	public Bool postDamage() {
		
		computePaintedBoundaries();
		childIsDamaged(paintedBoundaries);
		return Bool.TRUE;
		
	}

	// Focus on a particular focusable tile.
	public Bool focusOn(View newFocus, String direction) {

		if(newFocus != null && newFocus.bool(focusable)) {
			directionOfLastFocusMovement = direction;
			set(focus, newFocus);
			return Bool.TRUE;
		} else return Bool.FALSE;

	}

    public void centerOnScreen() {
    	
        	java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        	frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
    	
    }
    
    public void setSize(int w, int h) {
    	
    		frame.setSize(w, h);
    		frame.validate();
    		
    }
    
    public int getWindowWidth() { return frame.getWidth(); }
    public int getWindowHeight() { return frame.getHeight(); }
    
        
    /////////////////////////////////////////////////////////////////////
    //
    // Frame Facade
    //
    /////////////////////////////////////////////////////////////////////
    
    public Nothing show() { 

		if(Debug.threads()) System.err.println("" + Thread.currentThread().getName() + ": SHOWING " + this);
		
		Real w = get(Window.width);
		set(width, new Real(0.0));
		quicklyResize();
		damagedArea = new Rectangle2D.Double(0, 0, getCanvasWidth(), getCanvasHeight());
		damagedAreas = new LinkedList<Rectangle2D>();
		repairDamagedArea();
		frame.setVisible(true);

		if(Debug.threads()) System.err.println("" + Thread.currentThread().getName() + ": DONE SHOWING " + this);
		
		return null;
    
    }
    public void hide() { frame.setVisible(false); }
    public void setLocation(int x, int y) { frame.setLocation(x, y); }
    public int getCanvasWidth() { return frame.getContentPane().getWidth(); }
    public int getCanvasHeight() { return frame.getContentPane().getHeight(); }
    public Graphics2D getGraphicsContext() { return graphics; }
	public void setCursorTo(int cursor) { canvas.setCursor(Cursor.getPredefinedCursor(cursor)); }	
	public void setCursorTo(Cursor cursor) { canvas.setCursor(cursor); }	
	public java.awt.Cursor getCursor() { return canvas.getCursor(); }	

	//////////////////////////////////////////////
	//
	// Hoisted tiles.
	//
	//////////////////////////////////////////////

	protected void hoist(View t) { if(hoistedTiles != null) hoistedTiles.add(t); }
	protected void unhoist(View t) { 

		if(hoistedTiles != null) {
			hoistedTiles.remove(t); 
		}
		
	}

	protected Rectangle2D computePaintedBoundaries() {

		paintedBoundaries.setFrame(0, 0, getCanvasWidth() + 1, getCanvasHeight() + 1);
		return paintedBoundaries;
		
	}
	
	// This is for live resizing. We don't have time to send an event to the User
	// and wait for the user to update, so instead we have the application update immediately.
	public void quicklyResize() {

		if(Debug.threads()) System.err.println("" + Thread.currentThread().getName() + ": QUICKLY RESIZING...");
	
		// Update the dimensions. 
		set(width, new Real(getCanvasWidth()));
		set(height, new Real(getCanvasHeight()));

		// Update the screen
		App.work();

		if(Debug.threads()) 
			System.err.println("" + Thread.currentThread().getName() + ": DONE QUICKLY RESIZING.");

	}

	////////////////////////////////////////////////////////
	//
	// Override the default picking behavior to check the hoisted tiles
	// first. If none contain the point, then use the default picking behavior.
	//
	// The order of the resulting list will be each of the hoisted tiles,
	// then all tiles below the hoisted tiles.
	//
	////////////////////////////////////////////////////////
	public void whichViewsContain(Point2D testPoint, Vector<View> tiles) {

		// First we add the hoisted tiles that contain the point.
		for(View t : hoistedTiles) t.whichViewsContain(testPoint, tiles);

		// Now check all of the tiles below the hoisted tiles.
		super.whichViewsContain(testPoint, tiles);	
		
	}

	// Overrides to return itself.
	public Window getWindow() { return this; }	
	
	public AffineTransform getParentsVisibleCumulativeTransform() { return getPropertyByDeclaration(contentTransform).getVisible().value; }

	// Views report damage to this window by sending their damage here.
	public void childIsDamaged(Rectangle2D newDamage) {
		
		if(damagedAreas == null) damagedAreas = new LinkedList<Rectangle2D>();

		synchronized(this) {
    
			// If there is no damage, set it to this.
			if(damagedArea == null) damagedArea = newDamage;
			// Otherwise, union the damage.
			else damagedArea.add(newDamage);
			
			ListIterator<Rectangle2D> listIterator = damagedAreas.listIterator();
			while(listIterator.hasNext()) {

				Rectangle2D area = listIterator.next();

				// Compute A's area, B's area, the union's area, and the intersection's area.
				double areaOfA = area.getWidth() * area.getHeight();
				double areaOfB = newDamage.getWidth() * newDamage.getHeight();
				Rectangle2D union = area.createUnion(newDamage);
				Rectangle2D intersection = area.createIntersection(newDamage);
				double areaOfUnion = union.getWidth() * union.getHeight();
				double areaOfIntersection = intersection.isEmpty() ? 0 : intersection.getWidth() * intersection.getHeight();

				// Compute the number of unnecessarily painted pixels
				double unnecessaryArea = areaOfUnion - ((areaOfA + areaOfB) - areaOfIntersection);
				
				// If there are no unecessarily painted pixels, or the ratio of the number of redundant
				// pixels to the unecessarily painted pixels is greater than 1, cluster the damage.
				if(unnecessaryArea == 0.0 || (areaOfIntersection / unnecessaryArea) > 1.0) {
					
					newDamage.add(area);
					listIterator.remove();
					
				}

			}

			// Now add the accumulated new cluster to the beginning of the list.
			// TODO For some reason, if we don't clone this, the damage that's added
			// gets subsequently modified, which messes with the damage.
			damagedAreas.addFirst((Rectangle2D)newDamage.clone());
			
		}

	}

	// If there's any damage, repaint inside of the damaged area.	
	public void repairDamagedArea() {
		
		synchronized(this) {
		
		if(damagedArea != null) {	

			if(Debug.threads()) System.err.println("" + Thread.currentThread().getName() + ": Repairing " + damagedArea + " in " + this);
			
			// A version of repainting that just repaints the single rectangle
			// Set the affine transform to an identify transform
			graphics.setTransform(new AffineTransform());

			// Set the clip to the damaged rectangle.
			graphics.setClip(damagedArea);

			// Set the composite to 1.
			graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

			graphics.setColor(java.awt.Color.white);
			graphics.fill(damagedArea);
			
			// Paint the root, allowing it to paint or not paint its children, depending on their damaged state
			paint(graphics, false);

/*			
			// Paint each damaged cluster
			ListIterator<Rectangle2D> areaIterator = damagedAreas.listIterator();
			Rectangle2D area = null;
			while(areaIterator.hasNext()) {
				
				area = areaIterator.next();
				area = area.createIntersection(new Rectangle2D.Double(0, 0, getCanvasWidth(), getCanvasHeight()));
				
				// Set the affine transform to an identify transform
				graphics.setTransform(new AffineTransform());

				// Set the clip to the damaged rectangle.
				graphics.setClip(area);

				// Set the composite to 1.
				graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

				graphics.setColor(java.awt.Color.white);
				graphics.fill(area);
				
				// Paint the root, allowing it to paint or not paint its children, depending on their damaged state
				paint(graphics, false);
				
			}
*/
			// Paint all of the hoisted tiles last
			Iterator<View> i = hoistedTiles.iterator();
			while(i.hasNext()) {
        
				// Get the next child
				View hoistedTile = i.next();

				// If this isn't in a window, unset its hoisted and remove it.
				if(hoistedTile.getWindow() == null) {
					debug("Warning: " + hoistedTile + " was hoisted but not in a window");
					i.remove();
				}
				// Apply the local transform and paint the hoisted tile
				else if(hoistedTile.getParentsCumulativeTransform() != null) {
				
					// Reset the composite
					graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

					// Reset the clip to the damaged rectangle.
					graphics.setClip(damagedArea);

					// Set the transform
					graphics.setTransform(hoistedTile.getParentsCumulativeTransform());

					// Paint the hoisted tile.
					hoistedTile.paint(graphics, true);
					
				}
        
			}
			
			// Have the window repaint the damaged part of the image buffer.
			repaintCanvas(damagedArea, damagedAreas);

			// There's no more damage!
			damagedArea = null;

			damagedAreas.removeAll(damagedAreas);
		
			if(Debug.threads()) System.err.println("" + Thread.currentThread().getName() + ": DONE repairing " + this);
			
		}
		
		}
		
	}
		
	/////////////////////////////////////////////////////////////////////
	//
	// Render the buffer into the canvas.
	//
	/////////////////////////////////////////////////////////////////////
	public void repaintCanvas(Rectangle2D damagedRectangle, LinkedList<Rectangle2D> damagedAreas) {

		Graphics2D g = (Graphics2D)frame.getContentPane().getGraphics();

		// Clip the rectangle and draw the buffer.
		if(Debug.damage()) g.setClip(0, 0, getCanvasWidth(), getCanvasHeight());
		else g.setClip(damagedRectangle);
		
		// Draw the clipped buffer
		g.drawRenderedImage(buffer, new AffineTransform());

		// Paint the damaged rectangle for debugging
		if(Debug.damage() && damagedAreas != null) {
            
			Composite oldComposite = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float).5));

			g.setColor(java.awt.Color.green);
			g.fillRect((int) damagedArea.getX(), (int) damagedArea.getY(), (int) damagedArea.getWidth(), (int) damagedArea.getHeight());

			g.setColor(java.awt.Color.red);
			for(Rectangle2D r : damagedAreas) {
				g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
			}

			g.setComposite(oldComposite);

		}

	}
	
}
