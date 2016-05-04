package edu.cmu.hcii.citrus.vieweditor;

import java.util.Hashtable;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.layouts.*;
import edu.cmu.hcii.citrus.views.widgets.ScrollView;

// Created on Nov 16, 2004

//
// @author Andrew J. Ko
//
public class ChildrenEditor extends View {

	public static final Dec<Namespace> type = new Dec<Namespace>();
	
	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 0));
	
	public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
	public static final Dec<Real> height = new Dec<Real>(true, View.<Real>parseExpression("(this parentsHeight)"));

	public static final Dec<List<View>> children = new Dec<List<View>>("[(a Header)]");

	private Hashtable<Namespace,View> childrenPanesByType = new Hashtable<Namespace,View>();
	
	public final Listener selectionListener = new ListenerAdapter() {
		public void changed(Property p, Transition t, Element oldValue, Element newValue) {

			View replacementPane = childrenPanesByType.get(newValue);
			if(replacementPane == null) {
				ArgumentList args = new ArgumentList();
				args.add("type", newValue);
				replacementPane = new Body(args);
				args = new ArgumentList();
				args.add("viewToScroll", replacementPane);
				replacementPane = new ScrollView(null, args);
				childrenPanesByType.put((Namespace)newValue, replacementPane);
			}
			if(get(children).nth(new Int(2)) != null) get(children).nth(new Int(2)).remove();
			ChildrenEditor.this.get(children).append(replacementPane);
			
		}
	};

	public ChildrenEditor(ArgumentList arguments) { super(arguments); }
	
	public static class Header extends View {

		public static final Dec<Layout> layout = new Dec<Layout>(new HorizontalLayout(0, 5));
		public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
		public static final Dec<Real> height = new Dec<Real>(true, "(this tallestChildsHeight)");
		public static final Dec<Real> hPad = new Dec<Real>(new Real(5));
		public static final Dec<Real> vPad = new Dec<Real>(new Real(5));

		public static final Dec<List<Paint>> background = new Dec<List<Paint>>(View.<List<Paint>>parseExpression("[(this getStyle).lighterBackgroundPaint]"));

		public static final Dec<List<View>> children = new Dec<List<View>>(
		"[" + 
		"(a Label text=\"Children\" font=(this getStyle).headerFont)" + 
		"]"
		);

		public Header(ArgumentList arguments) { super(arguments); }

	}

	// A view of the default value expression for the type's children Dec.
	public static class Body extends View {

		public static final Dec<Namespace> type = new Dec<Namespace>((Element)null, true);
		
		public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));

		public static final Dec<Real> hPad = new Dec<Real>(new Real(5));
		public static final Dec<Real> vPad = new Dec<Real>(new Real(5));

		public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
		public static final Dec<Real> height = new Dec<Real>(true, View.<Real>parseExpression("(this parentsHeight)"));
		
		public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
			new Behavior(App.mouse.leftButton.pressed, new Action() { 
				public boolean evaluate(View t) {
					ViewTypeView ted = (ViewTypeView)ownerOfType(Reflection.getJavaType(ViewTypeView.class));
//					ted.getSelectionProperty().set(ted.getViewInstance());
					return true;
				}})));

		public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(
			"[" +
			"((type getDeclarationOf \"children\").valueExpression.@items toView)" +
			"]"
		));
		
		public Body(ArgumentList arguments) { super(arguments); }
		
	}
}