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
public class PaintEditor extends View {

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 0));
	public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
	public static final Dec<Real> height = new Dec<Real>(true, View.<Real>parseExpression("(this parentsHeight)"));

	public static final Dec<List<View>> children = new Dec<List<View>>(
		"[" +
		"(a Toolbar tools=[" +
			"(a Label text=\"Graphics\" font=(this getStyle).headerFont)" +
			"(a Maker typeToMake=FilledRectanglePaint label=(a View width=25.0 height=25.0 background=[(a FilledRectanglePaint)]))" +
			"(a Maker typeToMake=RectanglePaint label=(a View width=25.0 height=25.0 background=[(a RectanglePaint)]))" +
			"(a Maker typeToMake=LinePaint label=(a View width=25.0 height=25.0 background=[(a LinePaint)]))" +
			"(a Maker typeToMake=EllipsePaint label=(a View width=25.0 height=25.0 background=[(a EllipsePaint)]))" +
			"(a Maker typeToMake=PolygonPaint label=(a View width=25.0 height=25.0 background=[(a PolygonPaint points=[(a Point x=0.5 y=0.0) (a Point x=1.0 y=0.5) (a Point x=0.5 y=1.0) (a Point x=0.0 y=0.5)])]))" +
			"(a Maker typeToMake=ArcPaint label=(a View width=25.0 height=25.0 background=[(a ArcPaint)]))" +
			"(a Maker typeToMake=GridPaint label=(a View width=25.0 height=25.0 background=[(a GridPaint)]))" +
		"])" +
		"]"
	);

	// All of the various paint paints by the type
	private Hashtable<Namespace,View> paintPanesByType = new Hashtable<Namespace,View>();
	
	// When the type selection changes, place the appropriate pane.
	public final Listener selectionListener = new ListenerAdapter() {
		public void changed(Property p, Transition t, Element oldValue, Element newValue) {

			View replacementPane = paintPanesByType.get(newValue);
			if(replacementPane == null) {
				ArgumentList args = new ArgumentList();
				args.add("type", newValue);
				replacementPane = new Body(args);
				args = new ArgumentList();
				args.add("viewToScroll", replacementPane);
				replacementPane = new ScrollView(null, args);
				paintPanesByType.put((Namespace)newValue, replacementPane);
			}
			if(get(children).nth(new Int(2)) != null) get(children).nth(new Int(2)).remove();
			PaintEditor.this.get(children).append(replacementPane);
			
		}
	};
	
	public PaintEditor(ArgumentList arguments) { super(arguments); }
	
	public static class Header extends View {

		public static final Dec<Layout> layout = new Dec<Layout>(new HorizontalLayout(0, App.getGlobalStyle().get(Style.horizontalSpacing).value));
		
		public static final Dec<Real> width = new Dec<Real>(true, "(this parentsWidth)");
		public static final Dec<Real> height = new Dec<Real>(true, "(this tallestChildsHeight)");

		public static final Dec<Real> hPad = new Dec<Real>(new Real(5));
		public static final Dec<Real> vPad = new Dec<Real>(new Real(5));

		public static final Dec<List<Paint>> background = new Dec<List<Paint>>("[(this getStyle).lighterBackgroundPaint]");

		public static final Dec<List<View>> children = new Dec<List<View>>(
			"[" +
			"(a Toolbar tools=[" +
				"(a Label text=\"Graphics\" font=(this getStyle).headerFont)" +
				"(a Maker typeToMake=FilledRectanglePaint label=(a View width=25.0 height=25.0 background=[(a FilledRectanglePaint)]))" +
				"(a Maker typeToMake=RectanglePaint label=(a View width=25.0 height=25.0 background=[(a RectanglePaint)]))" +
				"(a Maker typeToMake=LinePaint label=(a View width=25.0 height=25.0 background=[(a LinePaint)]))" +
				"(a Maker typeToMake=GridPaint label=(a View width=25.0 height=25.0 background=[(a GridPaint)]))" +
			"])" +
			"]"
		);

		public Header(ArgumentList args) { super(args); }

	}

	public static class Body extends View {

		public static final Dec<Namespace> type = new Dec<Namespace>(new Parameter<Namespace>());
		
		public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 10));
		
		public static final Dec<Real> width = new Dec<Real>(true, "(this parentsWidth)");
		public static final Dec<Real> height = new Dec<Real>(true, "(this parentsHeight)");

		public static final Dec<Real> hPad = new Dec<Real>(new Real(5));
		public static final Dec<Real> vPad = new Dec<Real>(new Real(5));

		public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));

		// The children are views of each of the paint Dec's default values.
		public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(
			"[" +
			"(a Label text=\"Background\" font=(this getStyle).headerFont)" +
			"((type getDeclarationOf \"background\").@valueExpression toView)" +
			"(a Label text=\"Content\" font=(this getStyle).headerFont)" +
			"((type getDeclarationOf \"content\").@valueExpression toView)" +
			"(a Label text=\"Foreground\" font=(this getStyle).headerFont)" +
			"((type getDeclarationOf \"foreground\").@valueExpression toView)" +
			"]"
		));


		public Body(ArgumentList arguments) { super(arguments); }


	}

}