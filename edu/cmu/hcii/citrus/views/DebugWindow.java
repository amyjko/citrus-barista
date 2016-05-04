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

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.layouts.*;


import java.util.Timer;
import java.util.TimerTask;

public class DebugWindow extends Window {

	public static final Dec<Text> memoryUsage = new Dec<Text>(new Text(""));
	public static final Dec<Text> fps = new Dec<Text>(new Text(""));

	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(

			"[" +
			"(a CheckboxWithName property = debug.damage)" +
			
			"]"));
		/*
			new NewList<View>(
		make(Button.class, arg(Button.label, make(Label.class, arg(Label.text, text("Collect garbage")))),
			arg(Button.action, quote(new Action() { 
				public boolean evaluate(View t) { 
					System.gc();
					Runtime.getRuntime().runFinalization();
					((DebugWindow)t.getWindow()).afterGC = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024);
					return true; 
				}}))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.memory)))),
//		make(DynamicLabel.class, arg(DynamicLabel.property, memoryUsage)),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.propertyFinalization)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.elementFinalization)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.events)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.worker)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.damage)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.postDamage)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.clip)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.children)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.layout)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.paintBoundaries)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.navigation)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.translation)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.elementTranslation)))),
		make(CheckboxWithName.class, arg(CheckboxWithName.property, dot(quote(Debug.debug), getProp(Debug.fps))))
//		make(DynamicLabel.class, arg(DynamicLabel.property, fps))
	));
	*/
	
	public long lastMem = 0, thisMem = 0, afterGC = 0;

	public static final Dec layout = new Dec<Layout>(new VerticalLayout(-1, 0, 10));
	
	public static final Dec<Real> width = new Dec<Real>(true, "(this parentsWidth)");
	public static final Dec<Real> height = new Dec<Real>(true, "(this parentsHeight)");
	
	public DebugWindow() {

		super("Debug Window", false, 250, 500);

		final Timer memoryStringTimer = new Timer(true);		
		final Action setStrings = new Action() { public boolean evaluate(View t) {
			if(Debug.memory()) {
				lastMem = thisMem;
				thisMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024);
				t.set(memoryUsage, new Text("" + thisMem + 
					" of " + (Runtime.getRuntime().totalMemory() / (1024)) + 
					" K in use" + 
					"\n" + (thisMem - lastMem > 0 ? "+" : "") + (thisMem - lastMem) + 
					"\n" + afterGC + " after last collection"+ ""));
			}
			if(Debug.fps())
				t.set(fps, new Text("" + (int)App.getAverageFPS() + " fps"));			
			return true;
		}};

		final TimerTask stringSetter = new TimerTask() { public void run() {
			if(Debug.memory() || Debug.fps()) App.enqueueAction(DebugWindow.this, setStrings);
		}};

		afterGC = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024);

//		addChild(new DynamicLabel(FPSString));
//
//		addChild(new Button(new Label("Print # of properties"), new Action() { public boolean act(Tile t) {
//			System.err.println("" + Property.getNumberOfProperties() + " properties have been created.");
//			return true;
//		}}));
//		addChild(new Button(new Label("Show the default font's property listeners"), new Action() { public boolean act(Tile t) {
//			System.err.println("***********************\nDefault font property's listeners:\n");
//			int count = 0;
//			Iterator i = App.getLookAndFeel().getDefaultFont().getFontProperty().getPropertyListeners();
//			while(i.hasNext()) { System.err.print(i.next() + ", "); count++; }
//			System.err.println("\n\n" + count + " listeners\n***********************\n");
//			return true;
//		}}));
//		addChild(new Button(new Label("Show style editor window"), new Action() { public boolean act(Tile t) {
//			Window style = new Window("Style Window", false, 300, 500);
//			style.layout.set(new VerticalLayout(-1, 5, 5));
//			style.addChild(new Slider(App.getStyle().getIndentation(), 100, true));
//			style.addChild(new Slider(App.getStyle().getHorizontalSpacing(), 100, true));
//			style.addChild(new Slider(App.getStyle().getVerticalSpacing(), 100, true));
//			style.setLocation(0, 500);
//			App.show(style);
//			return true;
//		}}));

		memoryStringTimer.scheduleAtFixedRate(stringSetter, 0, 500);

	}
	
}