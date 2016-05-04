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
public class BehaviorEditor extends View {

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 0));
	public static final Dec<Real> height = new Dec<Real>(true, View.<Real>parseExpression("(this parentsHeight)"));
	public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));

	public static final Dec<List<View>> children = new Dec<List<View>>(
			"[" + 
			"(a Toolbar vertical=false tools=[" + 
				"(a Label text=\"Behaviors\" font=(this getStyle).headerFont)" + 
				"(a Duplicator " +
					"label=(a Label text=\"Behavior\" font=(this getStyle).italicFont) " +
					"elementToDuplicate=" +
					"(an Evaluate " +
						"functionContext=(a Ref token=\"a\") " +
						"function=(a Ref token=\"Behavior\") " +
						"arguments=" +
							"[" +
								"(an Arg param=\"event\" value=(a Ref)) " +
								"(an Arg param=\"action\" " +
									"value=(a Quote value=(an Evaluate " +
										"functionContext=(a Ref token=\"do\") " +
										"arguments=[" +
											"(an Arg value=(a Ref)) (an Arg value=(a BoolLiteral token=\"true\"))])))]))" +
				"(a Duplicator " +
					"label=(a Label text=\"Keyboard Typed\" font=(this getStyle).italicFont) " +
					"elementToDuplicate=" +
					"(an Evaluate " +
						"functionContext=(a Ref token=\"a\") " +
						"function=(a Ref token=\"Behavior\") " +
						"arguments=" +
							"[" +
								"(an Arg param=\"event\" value=" +
									"(an Evaluate " +
										"functionContext=(a Ref token=\"a\") " +
										"function=(a Possessive possessor=(a Ref token=\"Keyboard\") possession=(a Ref token=\"Typed\")))) " +
								"(an Arg param=\"action\" " +
									"value=(a Quote value=(an Evaluate " +
										"functionContext=(a Ref token=\"do\") " +
										"arguments=[" +
											"(an Arg value=(a Ref)) (an Arg value=(a BoolLiteral token=\"true\"))])))]))" +
				"(a Duplicator label=(a Label text=\"Mouse Clicked\" font=(this getStyle).italicFont) elementToDuplicate=(an Evaluate))" +
				"(a Duplicator label=(a Label text=\"Mouse Moved\" font=(this getStyle).italicFont) elementToDuplicate=(an Evaluate))" +
			"]" +
			")]"
		);

	private Hashtable<Namespace,View> behaviorPanesByType = new Hashtable<Namespace,View>();
	
	public final Listener selectionListener = new ListenerAdapter() {
		public void changed(Property p, Transition t, Element oldValue, Element newValue) {

			View replacementPane = behaviorPanesByType.get(newValue);
			if(replacementPane == null) {
				ArgumentList args = new ArgumentList();
				args.add("type", newValue);
				replacementPane = new Body(args);
				args = new ArgumentList();
				args.add("viewToScroll", replacementPane);
				replacementPane = new ScrollView(null, args);
				behaviorPanesByType.put((Namespace)newValue, replacementPane);
			}
			if(get(children).nth(new Int(2)) != null) get(children).nth(new Int(2)).remove();
			BehaviorEditor.this.get(children).append(replacementPane);
			
		}
	};
	
	public BehaviorEditor(ArgumentList arguments) { super(arguments); }

	public static class Body extends View {

		public static final Dec<Namespace> type = new Dec<Namespace>(new Parameter<Namespace>());
		
		public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
		public static final Dec<Real> height = new Dec<Real>(true, View.<Real>parseExpression("(this parentsHeight)"));

		public static final Dec<Real> hPad = new Dec<Real>(new Real(5));
		public static final Dec<Real> vPad = new Dec<Real>(new Real(5));

		public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));

		public static final Dec<List<View>> children = new Dec<List<View>>(
			"[" +
			"(a BehaviorListView " +
				"property=(type getDeclarationOf \"behaviors\").valueExpression.@items " +
				"model=(type getDeclarationOf \"behaviors\").valueExpression.items)" +
			"]"
		);

		public Body(ArgumentList arguments) { super(arguments); }

	}

}