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
public class PropertyEditor extends View {

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 0));
	public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
	public static final Dec<Real> height = new Dec<Real>(true, View.<Real>parseExpression("(this parentsHeight)"));

	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(
			"[(a Header)]"
	));

	private Hashtable<Namespace,View> propertyPanesByType = new Hashtable<Namespace,View>();

	public final Listener selectionListener = new ListenerAdapter() {
		public void changed(Property p, Transition t, Element oldValue, Element newValue) {

			View replacementPane = propertyPanesByType.get(newValue);
			if(replacementPane == null) {
				ArgumentList args = new ArgumentList();
				args.add("type", newValue);
				args.enclosingEnvironment = PropertyEditor.this;
				replacementPane = (View)Reflection.getJavaType(Body.class).instantiate(args);
				args = new ArgumentList();
				args.add("viewToScroll", replacementPane);
				replacementPane = new ScrollView(null, args);
				propertyPanesByType.put((Namespace)newValue, replacementPane);
			}
			if(get(children).nth(new Int(2)) != null) get(children).nth(new Int(2)).remove();
			PropertyEditor.this.get(children).append(replacementPane);
			
		}
	};

	public PropertyEditor(ArgumentList arguments) { super(arguments); }

	public static class Header extends View {

		public static final Dec<Layout> layout = new Dec<Layout>(new HorizontalLayout(0, 5));

		public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
		public static final Dec<Real> height = new Dec<Real>(true, "(this tallestChildsHeight)");

		public static final Dec<Real> hPad = new Dec<Real>(new Real(5));
		public static final Dec<Real> vPad = new Dec<Real>(new Real(5));

		public static final Dec<List<Paint>> background = new Dec<List<Paint>>(View.<List<Paint>>parseExpression("[(this getStyle).lighterBackgroundPaint]"));

		public static final Dec<List<View>> children = new Dec<List<View>>(
			"[" +
			"(a Label text=\"Properties\" font=(this getStyle).headerFont)" +
			"(a Duplicator label=(a Label text=\"New Property\" font=(this getStyle).italicFont) elementToDuplicate=(a Dec valueExpression=(a Ref)))" +
			"]"
		);

		public Header(ArgumentList arguments) { super(arguments); }

	}

	public static class Body extends View {

		public static final Dec<Type> type = new Dec<Type>(new Parameter<Type>());

		public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 5, 2));
		public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this rightmostChildsRight)"));
		public static final Dec<Real> height = new Dec<Real>(true, "(this lastChildsBottom)");

		public static final Dec<List<View>> children = new Dec<List<View>>(
			"[" +
			"(a ViewDecListView model=type.properties)" +
			"]"
		);

		public static final When typeChanges = CitrusParser.when(CitrusParser.tokenize(
			"when type.@properties valueChanged implicitly [ has Property property has Bool newValue ] " +
				"(this updateInstanceWith property newValue)"));
	
		public Nothing updateInstanceWith(Property p, Element newValue) {

			// Which declaration owns this property?
			DecInterface<?> dec = (DecInterface)p.ownerOfType(Boot.DECLARATION);
			
			if(dec == null) return null;

			if(dec.getName().equals("behaviors")) {
				return null;
			}
			
			// Set the corresponding property in the instance.
			ViewTypeView viewTypeView = getOwnerOfType(ViewTypeView.class);
			if(viewTypeView == null) {
				return null;
			}
			
			View viewInstance = viewTypeView.get(ViewTypeView.viewInstance);
			
			// Try evaluating the expression
			boolean succeeded = false;
			try {
				Element value = dec.getValueFunction().evaluate(viewInstance);
				succeeded = value.getType().isTypeOf(dec.getTypeExpression().getBaseType()).value;
			} catch(Exception e) {}

			if(succeeded) {
				System.err.println("Reinitialization succeeded! New value is " + viewInstance.get(dec));
				Property property = viewInstance.getProperty(dec.getName());
				property.initialize(null, false, getStyle().getQuickerTransition());
			}

			
			return null;
			
		}
		
		public Body(ArgumentList arguments) { super(arguments); }

	}
}