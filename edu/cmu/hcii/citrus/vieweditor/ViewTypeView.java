// Created on Feb 26, 2005

package edu.cmu.hcii.citrus.vieweditor;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.layouts.*;
import edu.cmu.hcii.citrus.views.paints.*;

//
// @author Andrew J. Ko
//
public class ViewTypeView extends ElementView {

	// The type we're editing
	public static final Dec<Type> model = new Dec<Type>(new Parameter<Type>(), true);
	// The type or inner type currently selected
	public static final Dec<Type> selection = new Dec<Type>((Element<Type>)null, true);

	public static final Dec<Element> modelInstance = new Dec<Element>();	
	public static final Dec<ElementView> viewInstance = new Dec<ElementView>((Element)null, true);	

	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 0));

	public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
	public static final Dec<Real> height = new Dec<Real>(true, View.<Real>parseExpression("(this parentsHeight)"));

	public static final RectanglePaint selectionBorder = new RectanglePaint(Color.blue, 0.5, 4.0, -2.0, -2.0, -2.0, -2.0, 0, 0);

	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(
		"[" +
		"(a Header)" +
		"(a Editors)" +
		"]"
	));
	
	public ViewTypeView(ArgumentList arguments) {

		super(arguments);

		if(!get(model).isTypeOf(Reflection.getJavaType(ElementView.class)).value) {
			System.err.println("" + get(model) + " is not a type of ElementView");
			System.exit(0);
		}
		
		DecInterface modelDec = get(model).getDeclarationOf(ElementView.model.get(Dec.name));				
		TypeExpression modelType = modelDec.getTypeExpression();
		
		// Make an instance to act as the model of the view.
		set(modelInstance, modelType.getBaseType().instantiate(null));

		// Make an instance of the view, passing the model we just created.
		ArgumentList args = new ArgumentList();		
		args.add("model", get(modelInstance));
		Translator.noTranslation = true;
		set(viewInstance, (ElementView)get(model).instantiate(args));
		get(viewInstance).get(children);
		get(viewInstance).set(enabled, new Bool(false));
		Translator.noTranslation = false;
		
		// Add the listeners to the selection property for each of the sub panels.
		getPropertyByDeclaration(selection).addListener(getFirstChildOfType(PropertyEditor.class).selectionListener);
		getPropertyByDeclaration(selection).addListener(getFirstChildOfType(PaintEditor.class).selectionListener);
		getPropertyByDeclaration(selection).addListener(getFirstChildOfType(BehaviorEditor.class).selectionListener);
		getPropertyByDeclaration(selection).addListener(getFirstChildOfType(ChildrenEditor.class).selectionListener);
		getPropertyByDeclaration(selection).addListener(getFirstChildOfType(ViewPreview.class).selectionListener);

		// Set the current selection to the view type this editor represents.
		set(selection, getViewType());

	}
	
	private void disableChildren(View t) {

		t.set(enabled, new Bool(false));
		for(View child : t.get(children)) disableChildren(child);
		
	}

	public static View findTileOfType(View t, Namespace type) {
		
		if(t.getType() == type) return t;
		View returnTile = null;
		for(View child : t.get(children)) {
			returnTile = findTileOfType(child, type);
			if(returnTile != null) return returnTile;
		}
		return null;
		
	}
	
	public View getViewInstance() { return get(viewInstance); }
	public Type getViewType() { return get(model); }
	
	public static class Header extends View {
		
		public static final Dec<Layout> layout = new Dec<Layout>(new HorizontalLayout(0, 5));

		public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
		public static final Dec<Real> height = new Dec<Real>(true, "(this tallestChildsHeight)");

		public static final Dec<Real> hPad = new Dec<Real>(new Real(5));
		public static final Dec<Real> vPad = new Dec<Real>(new Real(5));

		public static final Dec<List<Paint>> background = new Dec<List<Paint>>(View.<List<Paint>>parseExpression("[(this getStyle).lighterBackgroundPaint]"));

		public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(
			"[" +
			"(a Label text=\"a\")" +
			"(a TextField property=model.@name font=(this getStyle).headerFont)" +
			"(a Label text=\"is a\")" +
			"(a TextField property=model.@prototype font=(this getStyle).headerFont)" +
			"(a Button label=(a Label text=\"Save\") action='(model write \"Blah\"))" +
			/*
				ElementWriter.writeElementToFile(Language.getViewsPathname() + edType.getLanguageName() + java.io.File.separator +
				   type.get(BaseType.name) + 
				   ".element", 
				   type);
			*/
			"(a Button label=(a Label text=\"Test\")) " +
			/*
				Window w = new Window("Test", true, 400, 400);
				w.set(View.layout, new CenteredLayout(0));
				
				BaseType type = t.getParent().get(model);
				type.consolidate();
				
				// Assume this is an element view and make an element for it to be a view of.
				Dec ed = type.getDeclarationOf(ElementView.model);
				Type edType = ed.get(DeclaDec);
				Element elementToView = edType.make(null);
				ArgumentList args = new ArgumentList();
				args.put(ed, elementToView);			
				View newTile = (View)type.make(args);
				
				w.addChild(newTile);
				App.show(w);
			*/
			"]"
		));

		public Header(ArgumentList arguments) { super(arguments); }

	}

	public static class Editors extends View {
		
		public static final Dec<Real> width = new Dec<Real>(true, "(this parentsWidth)");
		public static final Dec<Real> height = new Dec<Real>(true, "(this parentsRemainingHeight)");
		
		public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(
			"[" +
			"(a SplitView vertical=true split=0.4" +
				"one=" +
				"(a SplitView vertical=false split=0.33 " +
					"one=(a ViewPreview instance=viewInstance)" +
					"two=(a SplitView vertical=false split=0.5 " +
						"one=(a ChildrenEditor type=model) " +
						"two=(a PaintEditor))" +
				")" +
				"two=" +
					"(a SplitView vertical=false split=0.5 " + 
						"one=(a PropertyEditor) " +
						"two=(a BehaviorEditor) " +
					")" +
			")" +
			"]"
		));

		public Editors(ArgumentList arguments) { super(arguments); }
		
	}

}