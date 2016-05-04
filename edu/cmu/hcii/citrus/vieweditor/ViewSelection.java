// Created on Mar 7, 2005

package edu.cmu.hcii.citrus.vieweditor;

import java.util.Hashtable;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.paints.RectanglePaint;

//
// @author Andrew J. Ko
//
public class ViewSelection extends View {

	public static final Dec<View> selection = new Dec<View>((Element)null, true);
	
	public static final Dec<Real> left = 
		new Dec<Real>(true, new BaseElement<Real>() {
			public Real evaluate(Element<?> env) {
				View sel = env.get(selection);
				if(sel == null) return new Real(0.0);
				else {		
					System.err.println("Selection's left is " + sel.get(left));
					return sel.get(left);
				}
			}			
		});

	public static final Dec<Real> top = 
		new Dec<Real>(true, new BaseElement<Real>() {
			public Real evaluate(Element<?> env) {
				View sel = env.get(selection);
				if(sel == null) return new Real(0.0);
				else return sel.get(top);
			}			
		});

	public static final Dec<Real> width = 
		new Dec<Real>(true, new BaseElement<Real>() {
			public Real evaluate(Element<?> env) {
				View sel = env.get(selection);
				if(sel == null) return new Real(0.0);
				else {
					System.err.println("Selection's width is " + sel.get(width));
					return sel.get(width);
				}
			}			
		});

	public static final Dec<Real> height = 
		new Dec<Real>(true, new BaseElement<Real>() {
			public Real evaluate(Element<?> env) {
				View sel = env.get(selection);
				if(sel == null) return new Real(0.0);
				else {
					System.err.println("Selection's height is " + sel.get(height));
					return sel.get(height);
				}
			}			
		});

	public final Listener selectionListener = new ListenerAdapter() {
		public void changed(Property p, Transition t, Element oldValue, Element newValue) {
			System.err.println("New value is " + newValue);
			if(newValue instanceof Namespace) set(selection, (View)ViewSelection.this.getParent().getFirstChildOfType((Namespace)newValue));
			else set(selection, null);
			System.err.println("Selection is " + get(selection));
		}
	};
	
	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(new NewList<Paint>(
		new RectanglePaint(Color.blue, 0.8, 2.0, 0, 0, 0, 0, 0, 0)));

	public static final Dec<Bool> focusable = new Dec<Bool>(new Bool(false));	

	public ViewSelection(ArgumentList arguments) { super(arguments); }
	
}