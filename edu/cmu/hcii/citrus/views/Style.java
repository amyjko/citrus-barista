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
import edu.cmu.hcii.citrus.views.paints.*;
import edu.cmu.hcii.citrus.views.transitions.Uniform;

public class Style extends BaseElement<Style> {
	
	// Fonts
	public static final Dec<FontFace> plainFont = new Dec<FontFace>(new FontFace("Lucida Grande", "plain", 12));
	public static final Dec<FontFace> biggerPlainFont = new Dec<FontFace>(new FontFace("Lucida Grande", "plain", 14));
	public static final Dec<FontFace> italicFont = new Dec<FontFace>(new FontFace("Arial", "italic", 12));	
	public static final Dec<FontFace> biggerItalicFont = new Dec<FontFace>(new FontFace("Arial", "italic", 14));
	public static final Dec<FontFace> boldFont = new Dec<FontFace>(new FontFace("Gill Sans", "bold", 12));	
	public static final Dec<FontFace> biggerBoldFont = new Dec<FontFace>(new FontFace("Geneva", "bold", 14));
	public static final Dec<FontFace> headerFont = new Dec<FontFace>(new FontFace("Arial", "bold", 16));
	public static final Dec<FontFace> fixedWidthFont = new Dec<FontFace>(new FontFace("Courier New", "plain", 12));
	public static final Dec<FontFace> smallFont = new Dec<FontFace>(new FontFace("Lucida Grande", "plain", 8));

	// Colors
	public static final Dec<Color> fontColor = new Dec<Color>(new Color(0, 0, 0, 255));
	public static final Dec<Color> darkerColor = new Dec<Color>(new Color(251, 154, 31, 255));
	public static final Dec<Color> darkColor = new Dec<Color>(new Color(255, 201, 80, 255));
	public static final Dec<Color> lightColor = new Dec<Color>(new Color(253, 219, 148, 255));
	public static final Dec<Color> lighterColor = new Dec<Color>(new Color(255, 245, 207, 255));
	public static final Dec<Color> selectionColor = new Dec<Color>(new Color(200, 200, 255, 255));
	public static final Dec<Color> keyboardFocusColor = new Dec<Color>(new Color(94, 110, 255, 255));
	public static final Dec<Color> errorColor = new Dec<Color>(new Color(255, 0, 0, 85));
	public static final Dec<Color> dropFeedbackColor = new Dec<Color>(new Color(0, 255, 0, 255));

	// Spacing
	private static final double indentationConstant = 10;
	private static final double roundednessConstant = 15;

	public static final Dec<Real> padding = new Dec<Real>(new Real(3.0));
	public static final Dec<Real> lineSpacing = new Dec<Real>(new Real(3.0));
	public static final Dec<Real> horizontalSpacing = new Dec<Real>(new Real(10.0));
	public static final Dec<Real> indentation = new Dec<Real>(new Real(indentationConstant));
	public static final Dec<Real> roundedness = new Dec<Real>(new Real(roundednessConstant));
	
	// Layouts
	public static final Dec<Layout> verticalLayout = new Dec<Layout>();
	public static final Dec<Layout> horizontalLayout = new Dec<Layout>();
		
	// Default strokes
	public static final Dec<Real> keyboardFocusStrokeWidth = new Dec<Real>(new Real(3));
	
	// Background styles.
	public static final Dec<Paint> backgroundPaint = new Dec<Paint>();
	public static final Dec<Paint> lighterBackgroundPaint = new Dec<Paint>();
	public static final Dec<FilledRectanglePaint> textBackgroundPaint = new Dec<FilledRectanglePaint>(
			"(a FilledRectanglePaint primaryColor=lighterColor secondaryColor=lighterColor cornerWidth=10.0 cornerHeight=10.0)");
	public static final Dec<Paint> textFieldErrorPaint = new Dec<Paint>();

	// Checkbox
	public static final Dec<Paint> checkmarkPaint = new Dec<Paint>();
	public static final Dec<Paint> checkboxPaint = new Dec<Paint>();

	// Button
	public static final Dec<Paint> buttonUpPaint = new Dec<Paint>(new PaintPaint(
			new FilledRectanglePaint(Color.lightGray, 1.0, 0.0, 0.0, 0.0, 0.0, roundednessConstant, roundednessConstant),
			new RectanglePaint(Color.gray, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, roundednessConstant, roundednessConstant)));
	public static final Dec<Paint> buttonDownInPaint = new Dec<Paint>(new PaintPaint(
			new FilledRectanglePaint(Color.gray, 1.0, 0.0, 0.0, 0.0, 0.0, roundednessConstant, roundednessConstant),
			new RectanglePaint(Color.gray, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, roundednessConstant, roundednessConstant)));
	public static final Dec<Paint> buttonDownOutPaint = new Dec<Paint>(new PaintPaint(
			new FilledRectanglePaint(Color.lightGray, 1.0, 0.0, 0.0, 0.0, 0.0, roundednessConstant, roundednessConstant),
			new RectanglePaint(Color.gray, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, roundednessConstant, roundednessConstant)));

	// Slider
	public static final Dec<Paint> horizontalSliderTrackPaint = new Dec<Paint>();
	public static final Dec<Paint> verticalSliderTrackPaint = new Dec<Paint>();
	public static final Dec<Paint> sliderKnobPaint = new Dec<Paint>();
	public static final Dec<Image> sliderKnobImage = new Dec<Image>(Images.getImage("Slider.png"));

	// Split pane
	public static final Dec<Paint> splitPanePaint = new Dec<Paint>("(a PaintPaint)");
	public static final Dec<Paint> splitPaneHorizontalDividerPaint = new Dec<Paint>("(a FilledRectanglePaint primaryColor=darkColor)");
	public static final Dec<Paint> splitPaneVerticalDividerPaint = new Dec<Paint>("(a FilledRectanglePaint primaryColor=darkColor)");

	// Scrollbars
	public static final Dec<Paint> scrollBarPaint = new Dec<Paint>(
		"(a PaintPaint paint=[" +
				"(a FilledRectanglePaint primaryColor=darkerColor cornerWidth=10.0 cornerHeight=10.0)" +
				"(a RectanglePaint primaryColor=darkColor cornerWidth=10.0 cornerHeight=10.0)" +
				"])"
		);
			
	public static final Dec<Paint> scrollBarTrackPaint =
	new Dec<Paint>(
		"(a PaintPaint paint=[" +
			"(a FilledRectanglePaint primaryColor=(a Color r=220.0 g=220.0 b=220.0) cornerWidth=10.0 cornerHeight=10.0)" +
			"(a RectanglePaint primaryColor=(a Color r=180.0 g=180.0 b=180.0) cornerWidth=10.0 cornerHeight=10.0)" +
		"])"
	);

	public static final Dec<Real> scrollBarWidth = new Dec<Real>(new Real(10));
		
	// Menus
	public static final Dec<Paint> popupMenuPaint = new Dec<Paint>();
	public static final Dec<Paint> menuPaint = new Dec<Paint>(
	"(a PaintPaint paint=[" +
		"(a FilledRectanglePaint " +
			"primaryColor=(a Color r=200.0 g=200.0 b=200.0) " +
			"alpha=0.3 " +
			"left=-8.0 " +
			"right=-8.0 " +
			"bottom=-8.0 "+
			"cornerWidth=4.0 " +
			"cornerHeight=4.0 " +
		")" +
		"(a FilledRectanglePaint primaryColor=darkColor)" +
		"(a RectanglePaint primaryColor=(a Color r=200.0 g=200.0 b=200.0))" +
	"])"
	);

	public static final Dec<Paint> menuItemPaint = new Dec<Paint>();	
	public static final Dec<Paint> overMenuItemPaint = new Dec<Paint>();
	
	public Paint getPopupMenuPaint() { return get(popupMenuPaint); }
	public Paint getMenuPaint() { return get(menuPaint); }
	public Paint getMenuItemPaint() { return get(menuItemPaint); }
	public Paint getOverMenuItemPaint() { return get(overMenuItemPaint); }

	// Text fields
	public static final Dec<Real> textFieldPadding = new Dec<Real>(new Real(3.0));
//	set(focusPaint, 
//			new RectanglePaint(get(keyboardFocusColor), .8, real(textFieldPadding),
//				  -real(textFieldPadding) / 2, -real(textFieldPadding) / 2, 
//				  -real(textFieldPadding) / 2, -real(textFieldPadding) / 2, 
//				  roundednessConstant, roundednessConstant));
//
	public static final Dec<Paint> focusPaint = new Dec<Paint>(
	"(a RectanglePaint " +
		"primaryColor=keyboardFocusColor " +
		"alpha=0.8 stroke=3.0 " +
		"left=-1.5 top=-1.5 right=-1.5 bottom=-1.5 " +
		"cornerWidth=15.0 cornerHeight=15.0)"		
	
	);
	public static final Dec<Paint> listFocusPaint = new Dec<Paint>(new FilledRectanglePaint(Color.white, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
	
	// Animation
	public Transition getQuickTransition() { return new Uniform(150); }
	public Transition getQuickerTransition() { return new Uniform(100); }
	public Transition getQuickestTransition() { return new Uniform(50); }
	
	// Drag and Drop
	
	private static Paint dropReplaceFeedback = new FilledRectanglePaint(Color.green, 0.5, 0.0, 0.0, 0.0, 0.0, 5, 5);
	private static Paint dragShadow = new FilledRectanglePaint(Color.grey, 0.1, 5.0, 5.0, -5.0, -5.0, 15, 15);
	private static Paint insertBeforeFeedback = new LinePaint(Color.green, 0.5, 4.0, true, 0.0, 0.0, 1.0, 0.0);
	private static Paint insertAfterFeedback = new LinePaint(Color.green, 0.5, 4.0, true, 0.0, 1.0, 1.0, 1.0);

	private static Color dropErrorColor = new Color(255, 0, 0, 255);
	private static Paint badDropReplaceFeedback = new FilledRectanglePaint(dropErrorColor, 0.5, 0.0, 0.0, 0.0, 0.0, 5, 5);
	private static Paint badInsertBeforeFeedback = new LinePaint(dropErrorColor, 0.5, 4.0, true, 0.0, 0.0, 1.0, 0.0);
	private static Paint badInsertAfterFeedback = new LinePaint(dropErrorColor, 0.5, 4.0, true, 0.0, 1.0, 1.0, 1.0);

	public Paint getDragShadow() { return dragShadow; }
	public Paint getReplaceFeedback() { return dropReplaceFeedback; }
	public Paint getInsertBeforeFeedback() { return insertBeforeFeedback; }
	public Paint getInsertAfterFeedback() { return insertAfterFeedback; }

	public Paint getBadReplaceFeedback() { return badDropReplaceFeedback; }
	public Paint getBadInsertBeforeFeedback() { return badInsertBeforeFeedback; }
	public Paint getBadInsertAfterFeedback() { return badInsertAfterFeedback; }
	
	public static final Dec<Paint> comboBoxArrowPaint = new Dec<Paint>(new ImagePaint(Images.getImage("comboBoxArrow.png")));

	public Image getComboBoxArrowImage() { return Images.getImage("comboBoxArrow.png"); }
	public Paint getComboBoxArrowPaint() { return get(comboBoxArrowPaint); }

	public Style(Namespace type, ArgumentList args) { super(type, args); init(); }
	public Style() { init(); }
	
	public void init() {
		
//		// Backgrounds
		set(backgroundPaint,
				new PaintPaint(
					new FilledRectanglePaint(get(darkColor), 1.0, 0.0, 0.0, 0.0, 0.0, roundednessConstant, roundednessConstant),
					new RectanglePaint(get(lightColor), 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, roundednessConstant, roundednessConstant)));
		set(lighterBackgroundPaint, 
				new PaintPaint(
					new FilledRectanglePaint(get(lightColor), get(lighterColor), 1.0, 0.0, 0.0, 0.0, 0.0, roundednessConstant, roundednessConstant),
					new RectanglePaint(get(lighterColor), 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, roundednessConstant, roundednessConstant)));
//
//		// Checkbox
		set(checkmarkPaint, 
				((new PaintPaint()).addPaint(
					new LinePaint(Color.black, 1.0, 2.0, true, 0.25, 0.25, 0.5, 0.75))).addPaint(
					new LinePaint(Color.black, 1.0, 2.0, true, 0.5, 0.75, 1.25, -0.25)));

		set(checkboxPaint, new FilledRectanglePaint(Color.gray, 0.5, 0.0, 0.0, 0.0, 0.0, 5, 5));

		// Slider
		set(horizontalSliderTrackPaint, new LinePaint(Color.gray, 1.0f, 1.0, true, 0.0, 0.5, 1.0, 0.5));
		set(verticalSliderTrackPaint, new LinePaint(Color.gray, 1.0f, 1.0, true, 0.5, 0.0, 0.5, 1.0));
		set(sliderKnobPaint, new ImagePaint(Images.getImage("Slider.png")));

		// Menus
		set(menuItemPaint, 
				new FilledRectanglePaint(get(darkColor), 1.0f, 0.0, 0.0, 0.0, 0.0, 0, 0));
		set(overMenuItemPaint, 
				new FilledRectanglePaint(get(darkerColor), 1.0f, 0.0, 0.0, 0.0, 0.0, 0, 0));
//
		// Text field
		set(textFieldErrorPaint, 
				new FilledRectanglePaint(get(errorColor), .8,
					  -real(textFieldPadding) / 2, -real(textFieldPadding) / 2, 
					  -real(textFieldPadding) / 2, -real(textFieldPadding) / 2, 
					  roundednessConstant, roundednessConstant));

	}

}