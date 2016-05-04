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
import edu.cmu.hcii.citrus.*;
import edu.cmu.hcii.citrus.views.*;
import edu.cmu.hcii.citrus.views.devices.*;
import edu.cmu.hcii.citrus.views.transitions.*;
import edu.cmu.hcii.citrus.views.behaviors.*;
import edu.cmu.hcii.citrus.views.paints.*;
import edu.cmu.hcii.citrus.views.widgets.*;

public class Citrus {

	// Requires at least one argument and it must be a Citrus source file containing an init procedure.
	// a Citrus application bundle.
	public static final void main(String[] args) {
		
		// Start the bootstrapping sequence
		Boot.init();

		// Hack to load all of the relevant packages.
		View view = new View();
		Keyboard keyboard = new Keyboard();
		Transition t = new FastToSlow(200);
		Moveable b = new Moveable();
		Paint p = new RectanglePaint();
		// Stupid hack to load cause the class loader to load the widget package..
		if(Toggle.class != null) p = p;

		if(args.length == 0) {
			System.err.println("Trying to run the Citrus interpreter? You must provide a command such as this:");
			System.err.println("\n\tjava -jar Citrus.jar program.citrus");
			System.err.println("\nwhere program.citrus is a file with an init procedure.");
			System.exit(0);
		}
		
		// Load the Citrus file that was given
		String mainClass = args[0];

		try {

			Unit unit = CitrusParser.unit(new Text(mainClass));
	
			System.err.println("\n\n");
			
			// Find the init method in the file
			Element<?> init = unit.get(Unit.init);
			
			if(init == null) {
				System.err.println("" + mainClass + " has no initialization expression.");
			}
			else {
				init.evaluate(unit);
			}

		} catch(Exception e) { System.err.println("" + e); }
		
//		Interpreter i = new Interpreter(app);
//		i.readEvalPrint();
		
	}

	public void loadAllOfTheJavaFiles() {
		
//		if(args.length > 0) {
//
//			if(args[0].endsWith("xml")) {
//				project = (BaseElement)XMLParser.readXMLFrom(new Text(args[0]));
//			}
//			else {
//				List<BaseElement> units = new List<BaseElement>();
//				for(int i = 0; i < args.length; i++) {
//	
//					try {
//						
//						System.out.println("Reading file " + args[i] + " . . .");
//						BaseElement file = JavaUtilities.readCompilationUnit(args[i]);
//						units.append(file);
//
//						Text javaText = (Text)Evaluate.eval(file, file, new Ref("toJava"), new List());
//						System.out.println("I read " + JavaUtilities.tabify(javaText, null));
//						
//					} catch (ParseException e) {
//						System.out.println("Java Parser Version 1.1:  Encountered errors during parsing of " + args[i]);
//						System.out.println(e.getMessage());
//					}
//				}
//	
//				project = JavaUtilities.createProject(new Text("NoName"), units);
//			}
//
//		}
//
//		// Add java.lang to the known packages.
//		if(project != null) {
//			Set projects = ((Set)((BaseElement)app.get("env")).get("projects"));
//			projects.removeAll();
//			projects.add(project);
//		}
//		BaseElement environment = (BaseElement)app.get("env");
//			JavaUtilities.setEnvironmentsPackages(environment);
		
	}
	
}