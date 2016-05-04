// Created on Jan 24, 2005

package edu.cmu.hcii.citrus.vieweditor;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.behaviors.Draggable;
import edu.cmu.hcii.citrus.views.paints.FilledRectanglePaint;

//
// @author Andrew J. Ko
//
public class Subtyper extends View {

	public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));

	public static final Dec<View> label = new Dec<View>("(a Label text=typeToSubtype.name font=(this getStyle).italicFont)", true);
	public static final Dec<Type> typeToSubtype = new Dec<Type>(new Parameter<Type>(), true);
	public static final Dec<View> enclosingInstance = new Dec<View>(new Parameter<View>(), true);

	public static final Dec<Real> width = new Dec<Real>(true, "(this firstChildsRight)");
	public static final Dec<Real> height = new Dec<Real>(true, "(this firstChildsBottom)");

	public static final Dec<Real> vPad = new Dec<Real>(new Real(5));
	public static final Dec<Real> hPad = new Dec<Real>(new Real(5));

	public static final Dec<List<View>> children = new Dec<List<View>>("[label]");

	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(new NewList<Paint>(
			new FilledRectanglePaint(new Color(0, 0, 0, 255), 0.1, 0, 0, 0, 0, 5, 5)));
	
	public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
		// If the drop fails, then disable it, and reuse it later.
		new Behavior(App.mouse.pointer.dropFailed, new Action() { public boolean evaluate(View t) {
			App.mouse.pointer.getViewPicked().set(enabled, new Bool(false));
			App.mouse.pointer.getViewPicked().set(transparency, new Real(0.0), App.getGlobalStyle().getQuickTransition());
			return true;
		}}),

		// If the drop fails, then disable it, and reuse it later.
		new Behavior(App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {

			View instance = t.get(enclosingInstance);
			
			Type superType = t.get(typeToSubtype);

			Type subType = ((BaseType)superType).createSubType(new Text(superType.getName() + t.get(enclosingInstance).getType().get(BaseType.types).length()));
			t.get(enclosingInstance).getType().get(BaseType.types).append(subType);

			// What parameters does the type require?
			ArgumentList args = new ArgumentList();
			args.enclosingEnvironment = instance;
			if(subType.isTypeOf(Reflection.getJavaType(ElementView.class)).value) {

				Type modelType = subType.getDeclarationOf(new Text("model")).getTypeExpression().getBaseType();
				System.err.println("" + subType + " is an element view, its model is of type " + modelType);
				
				Element<?> instanceModel = instance.get(ElementView.model);
				
				DecInterface suitableDec = null;
				for(DecInterface dec : instanceModel.getType().getDeclarationsToInstantiate()) {
					
					System.err.println("Is " + dec + " suitable?");
					if(dec.getTypeExpression().getBaseType().isTypeOf(modelType).value) {
						suitableDec = dec;
						break;
					}					
					
				}
				if(suitableDec == null) 
					throw new ElementError("I couldn't find a suitable property for " + subType + "'s model", null);
				
				args.add("property", false, instance.get(ElementView.model).getProperty(suitableDec.getName()));

			}
			View newView = (View)subType.instantiate(args);

			// Add a draggable to the view.
			newView.get(behaviors).append(new Draggable());

			t.getWindow().addChild(newView);
			newView.set(left, new Real(App.mouse.pointer.getPosition().getX()));
			newView.set(top, new Real(App.mouse.pointer.getPosition().getY()));

			// Set the transparency of the tile to .5 and pick it up.
			App.mouse.pointer.pickAndHoist.evaluate(newView);
			newView.set(transparency, new Real(1.0), App.getGlobalStyle().getQuickTransition());

			return false;
		
		}})));
	
	public Subtyper(ArgumentList arguments) { super(arguments); }

}