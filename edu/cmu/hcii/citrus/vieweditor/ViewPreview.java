package edu.cmu.hcii.citrus.vieweditor;

import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.layouts.*;
import edu.cmu.hcii.citrus.views.paints.*;
import edu.cmu.hcii.citrus.views.widgets.SelectionHandles;

// Created on Nov 16, 2004

//
// @author Andrew J. Ko
//
public class ViewPreview extends View {


	// The instance of the type being edited.
	public static final Dec<View> instance = new Dec<View>(new Parameter<View>(), true);
	
	public static final Dec<Layout> layout = new Dec<Layout>(new VerticalLayout(-1, 0, 0));	
	public static final Dec<Real> width = new Dec<Real>(true, View.<Real>parseExpression("(this parentsWidth)"));
	public static final Dec<Real> height = new Dec<Real>(true, View.<Real>parseExpression("(this parentsHeight)"));

	public static final Dec<List<Paint>> background = new Dec<List<Paint>>(new NewList<Paint>(new GridPaint(Color.lightGrey, 0.5, 20.0)));

	public static final Dec<List<View>> children = new Dec<List<View>>(View.<List<View>>parseExpression(
		"[" +
		"(a Toolbar vertical=false tools=[" + 
			"(a Label text=\"Preview\" font=(this getStyle).headerFont)" +
			"(a Subtyper typeToSubtype=View enclosingInstance=instance)" +
			"(a Subtyper typeToSubtype=Checkbox enclosingInstance=instance)" +
			"(a Subtyper typeToSubtype=TextField enclosingInstance=instance)" +
			"(a Subtyper typeToSubtype=Label enclosingInstance=instance)" +
			"(a Subtyper typeToSubtype=ScrollView enclosingInstance=instance)" +
			"(a Subtyper typeToSubtype=Toggle enclosingInstance=instance)" +
		"])" +
		"(a ScrollView viewToScroll=(a Preview))" +
		"]"
	));

	public final Listener selectionListener = new ListenerAdapter() {
		public void changed(Property p, Transition t, Element oldValue, Element newValue) {
			
			View viewInstance = (View)ViewPreview.this.getFirstChildOfType((Namespace)newValue);
			ViewPreview.this.getFirstChildOfType(SelectionHandles.class).set(SelectionHandles.selection, viewInstance);

		}
	};

	public ViewPreview(ArgumentList arguments) { super(arguments); }
	
	public static class InstanceContainer extends View {

		public static final Dec<List<View>> children = new Dec<List<View>>("[instance]");
		
		public static final Dec<Real> width = new Dec<Real>(true, "(this parentsWidth)");
		public static final Dec<Real> height = new Dec<Real>(true, "(this parentsHeight)");

		public InstanceContainer(ArgumentList arguments) { super(arguments); }

	}
	
	public static class Preview extends View {

		public View viewUnderCursor = null;

		public Preview(ArgumentList arguments) { super(arguments); }

		public static final Dec<List<View>> children = new Dec<List<View>>("[(an InstanceContainer) (a SelectionHandles)]");

		public static final Dec<List<Paint>> background = new Dec<List<Paint>>(new NewList<Paint>(
			new RectanglePaint(Color.grey, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0, 0),
			new FilledRectanglePaint(Color.lightGrey, .25, 0.0, 0.0, 0.0, 0.0, 0, 0)));

//		public static final Dec<List<Paint>> content = new Dec<List<Paint>>("[(a TextPaint text=\"Parent\" new NewList<Paint>(
//			new TextPaint("Parent", App.getGlobalStyle().get(Style.headerFont), Color.grey, 0.5, false, TextLayout.CENTERED, TextPaint.VERTICALLY_CENTERED)));

		public static final Dec<Real> left = new Dec<Real>(true, new BaseElement<Real>() { 
			public Real evaluate(Element<?> context) {
				if(context.get(parent) == null) return new Real(0.0);
				else return new Real((context.get(parent).get(width).value - ((View)context).paddedWidth().value) / 2);
			}});

		public static final Dec<Real> top = new Dec<Real>(true, new BaseElement<Real>() { 
			public Real evaluate(Element<?> context) {
				if(context.get(parent) == null) return new Real(0.0);
				else return new Real((((View)context).getParent().get(height).value - ((View)context).paddedHeight().value) / 2);								
			}});
		
		public static final Dec<Real> width = new Dec<Real>(true, "((this parentsWidth) times 0.75)");
		public static final Dec<Real> height = new Dec<Real>(true, "((this parentsHeight) times 0.75)");

		public static final Dec<Bool> clipsChildren = new Dec<Bool>(new Bool(false));

		public static final Dec<List<Behavior>> behaviors = new Dec<List<Behavior>>(new List<Behavior>(
				
			new Behavior(App.mouse.leftButton.pressed, new Action() { public boolean evaluate(View t) {

				View viewInstance = t.get(instance);
				View newSelection = null;
				for(View v : App.getViewsUnderCursor()) {
					// If the view instance is clicked, or one of its children for whom the instance's type has a type, select it.
					if(v == viewInstance || 
						(v.isDescendantOf(viewInstance).value && 
						viewInstance.getType().get(BaseType.types).contains((Type)v.getType()).value)) {
						newSelection = v;
						break;
					}
				}
				
				System.err.println("Selection is " + newSelection);
				// If we found a satisfactory view, select it.
				if(newSelection != null)
					t.getOwnerOfType(ViewTypeView.class).set(ViewTypeView.selection, (Type)newSelection.getType());					
				return true;

			}}),
			
			new Behavior(App.mouse.pointer.draggedIn, new Action() { 
				public boolean evaluate(View t) {
					return true;
				}}),
			// When dragged over, highlight the thing that will be dropped on
			new Behavior(App.mouse.pointer.draggedOver, new Action() { public boolean evaluate(View t) {

				Preview vp = (Preview)t;

				View preview = t.get(instance);
		
				// Make sure the element picked is an element view.
				View pick = App.mouse.pointer.getViewPicked();

				// What is this being dropped over? Find the first descendant of the view that is of a type 
				// contained in this preview's type's type list.
				
				View viewUnderCursor = null;
				for(View view : App.getViewsUnderViewPicked()) {
					if(view == preview || 
						(view.isDescendantOf(preview).value && preview.getType().get(BaseType.types).contains((Type)pick.getType()).value)) {
						viewUnderCursor = view;
						break;
					}
				}
				if(viewUnderCursor != null)
					viewUnderCursor.addForegroundPaint(App.getGlobalStyle().getReplaceFeedback());
				
				if(vp.viewUnderCursor != viewUnderCursor && vp.viewUnderCursor != null)
					vp.viewUnderCursor.removeForegroundPaint(App.getGlobalStyle().getReplaceFeedback());
				vp.viewUnderCursor = viewUnderCursor;
					

				return true;
					
			}}),
			new Behavior(App.mouse.pointer.draggedOut, new Action() { 
				public boolean evaluate(View t) {
					Preview vp = (Preview)t;
					if(vp.viewUnderCursor != null)
						vp.viewUnderCursor.removeForegroundPaint(App.getGlobalStyle().getReplaceFeedback());
					return true;
			}}),
			// Assume that the element is not in a list.
			new Behavior(App.mouse.pointer.droppedOver, new Action() { 
				public boolean evaluate(View t) {
					
					Preview vp = (Preview)t;
	
					View pick = App.mouse.pointer.getViewPicked();

					if(vp.viewUnderCursor != null) {
	
						vp.viewUnderCursor.removeForegroundPaint(App.getGlobalStyle().getReplaceFeedback());
	
						// Find the default children list for the type.
						Type viewType = (Type)vp.viewUnderCursor.getType();
						DecInterface childrenDeclaration = null;
						
						for(DecInterface dec : viewType.getDeclarationsDeclared()) {
							if(dec.getName().equals(View.children.getName())) {
								childrenDeclaration = dec;
								break;
							}
						}
						// If this type doesn't override children, make it override it now.
						if(childrenDeclaration == null) {
						
							System.err.println("Warning: children is not overriden by " + viewType);
							
						} else {
							
							List newItems = childrenDeclaration.get(Dec.valueExpression).get(NewList.items);
							System.err.println("Currently has items " + newItems);

							// Where was it dropped, relative to the tile being dropped over?
							java.awt.geom.Point2D global = App.mouse.pointer.getPosition();
							java.awt.geom.Point2D local = vp.viewUnderCursor.globalToLocal(global);
							
							String makeExpression = "";
							if(pick.getType().isTypeOf(Reflection.getJavaType(ElementView.class)).value)
								makeExpression = "(a " + pick.getType().getName() + " property=model.@" + pick.get(ElementView.property).getDeclaration().getName() + ")";
							else makeExpression = "(a " + pick.getType().getName() + ")";

							Evaluate<?> eval = new Evaluate(new Ref("a"), new Ref(pick.getType().getName()));
							if(pick.getType().isTypeOf(Reflection.getJavaType(ElementView.class)).value)
								eval.get(Evaluate.arguments).append(new Arg("property", false, 
										CitrusParser.parse("model.@" + pick.get(ElementView.property).getDeclaration().getName())));
							
							// This would override the default constraint
//							eval.get(Evaluate.arguments).append(new Arg("left", false, new RealLiteral("" + local.getX())));
//							eval.get(Evaluate.arguments).append(new Arg("top", false, new RealLiteral("" + local.getY())));

							newItems.append(eval);

						}
						
						// Get the instance that was automatically created
						View newView = vp.viewUnderCursor.get(children).firstItemOfType((Type)pick.getType());
						System.err.println("" + newView + " was created");
						
						// Remove the view being dragged from wherever its at.
						pick.remove();
	
						// Nullify the tile under the cursor
						vp.viewUnderCursor = null;
	
						return true;
	
					} else return false;
					
				}}
			),
			new Behavior(App.keyboard.BACKSPACE.pressed, new Action() { public boolean evaluate(View t) {
					
//					// Delete the current selection by removing it from the instance and the
//					// default.
//					View selection = ((ViewTypeEditor)t.getFirstElementOwnerOfType(Reflection.getJavaType(ViewTypeEditor.class))).getSelectionProperty().get();
//					Type viewType= ((ViewTypeEditor)t.getFirstElementOwnerOfType(Reflection.getJavaType(ViewTypeEditor.class))).getViewType();
//					
//					if(selection != null && selection.getType() != viewType) {
//
//						// Get the type of the instance to remove.
//						Type typeToRemove = (Type)selection.getType();
//						
//						// Remove the instance.
//						selection.remove(App.getStyle().getQuickerTransition());
//
//						// Remove the type from the type list.
//						viewType.getProperty(Type.types).remove(typeToRemove, null);
//
//						// Find the Make in the default children and remove it.					
//						System.err.println("Haven't removed from make yet.");
//					
//					}

					return true;
				}})));
		
	}

}