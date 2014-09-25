/***************************************************************************

 Agents 2.0 - VLSI Cell Generator.
 Copyright (C) 2000  Dilvan Moreira

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 Contact: dilvan@computer.org
 Paper mail: Rua Dr Domingos Faro 150, Ap 14
 Jardim Alvorada
 13562-320 Sao Carlos-SP
 BRAZIL

 ****************************************************************************/

package layout.placer;

/*
 *        PlacerExpert
 */

import layout.comp.*;
import layout.display.Display;
import layout.display.Dsp;
import layout.util.Gen;
import layout.util.List;
import layout.util.Rectangle;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class PlacerExpert {

	/**
	 * Insert the method's description here.
	 * Creation date: (10/11/2000 9:07:11 AM)
	 */
	static public DesignCmp initDesign(DesignCmp design) {

		//                Prepare the design
		//                Test if the distance between points is the gridValue (or the smallest possible)
		//if (!technology().empty()) {
		//   double dist= RTYPE(Real, technology.lst(0).slot(String("DISTANCE"))[1]).toReal();
		//   if (dist!=consultant.getGridValue())
		//      error.fatal("INIT:: The distance between points should be equal to the process resolution");
		//}

		/*
		 *    Calculates design area
		 */
		Rectangle area = design.getEnvelope();

		//        Add a margin for contacts
		int met1Width = Consultant.db.getWireWidth(Layer.MET1);
		area.c1.x = area.c1.x - met1Width; //mod Evandro
		area.c2.x = area.c2.x + met1Width; //mod Evandro
		area.c1.y = area.c1.y - met1Width;
		area.c2.y = area.c2.y + met1Width;
		design.setDesignArea(area);

		design.refComponents();

		/*
	Display dsp = Dsp.getDisplay("TESTE");
	edif.design.drawOut(dsp);
	Thread.currentThread().sleep(1000*60*60);
	System.exit(0);
		 */

		return design;
	}
	static public DesignCmp initDesign(String fileCir) throws Exception {

		//    Read the file from disk
		BufferedInputStream cir = new BufferedInputStream(new FileInputStream(fileCir));
		CompEdif edif = new CompEdif(cir);
		edif.eval();
		//Dsp.println(edif.eval());
		//Dsp.print("\n\nLibraries: " + edif.libraries + "\n\n");
		//Dsp.print("Technology: " + edif.technology + "\n\n");
		//Dsp.print("Design: " + edif.design + "\n\n");

		return initDesign(edif.design);
	}
	static public void main(String[] args) throws Exception {

		String rulesFile = null;
		String cirFile = null;
		String[] routersName = null;

		//	Read args
		for (int aux1 = 0; aux1 < args.length; aux1++)
			if (args[aux1].equals("-rules")) {
				aux1++;
				rulesFile = args[aux1];
			} else if (args[aux1].equals("-circuit")) {
				aux1++;
				cirFile = args[aux1];
			} else if (args[aux1].equals("-port")) {
				aux1++;
				TCP_PORT = Integer.parseInt(args[aux1]);
			} else if (args[aux1].equals("-ncircuits")) {
				aux1++;
				CIRCUITS_NUMBER = Integer.parseInt(args[aux1]);
			} else if (args[aux1].equals("-nrouters")) {
				aux1++;
				MAX_ROUTERS = Integer.parseInt(args[aux1]);
			} else if (args[aux1].equals("-routers")) {
				aux1++;
				int num = Integer.parseInt(args[aux1]);
				routersName = new String[num];
				for (int aux2 = 0; aux2 < num; aux2++) {
					aux1++;
					routersName[aux2] = args[aux1];
				}
			} else {
				System.out.println("Wrong argument for Router");
				return;
			}

		//	Equalize num of servers with routers
		if (routersName != null)
			MAX_ROUTERS = routersName.length;

		// Init the rules consultant
		Consultant.db = new Consultant(rulesFile);
		//    Get the layers names and create them.
		Layer.makeLayers(Consultant.db.getLayersNames());

		//
		PlacerExpert exp = new PlacerExpert(initDesign(cirFile), routersName);
		exp.run();
		System.out.println("finished");

	}

	final static int POPULATION_NUMBER = 5;
	final static int GENERATION_NUMBER = 40;
	final static int EVAL_INTERACTIONS = 8;
	final static int SLEEP_TIME = 300;

	static int CIRCUITS_NUMBER = 1;

	static int MAX_ROUTERS = 8;
	static int TCP_PORT = 9090;
	List circuits = Gen.newList();
	//
	DesignCmp design;
	List fetsNmos, fetsPmos, bipolars, cells;

	List team;

	Eval eval;

	Comm[] routers;

	public PlacerExpert(DesignCmp design1, String[] routersName) throws IOException {
		design = design1;
		team = Gen.newList();

		//	Init the communicators
		if (routersName != null) {
			routers = new Comm[routersName.length];
			for (int aux1 = 0; aux1 < routersName.length; aux1++)
				routers[aux1] = new Comm(routersName[aux1], TCP_PORT);
		} else {
			routers = new Comm[MAX_ROUTERS];
			for (int aux1 = 0; aux1 < MAX_ROUTERS; aux1++)
				routers[aux1] = new Comm(null, 0);
		}

	}

	List createCollumns() {
		team.add(new AbuttedAgent(this));
		((AbuttedAgent) team.get(0)).behaviorAbutted();

		//                Transform the fets in columns
		for (int aux3 = 0; aux3 < team.size(); aux3++) {
			//System.out.println("Agent " + aux3);

			List auxLst = Gen.newList();
			auxLst.add(((AbuttedAgent) team.get(aux3)).comps);
			((AbuttedAgent) team.get(aux3)).comps = auxLst;
			//System.out.println( ((AbuttedAgent) team.get(aux3)).comps);
		}
		/*-
		 *                Test the candidates against each other
		 *                Best= [ FirstAbuted SecondAbuted [2 4 1 3]  SOURCE DRAIN]
		 */
		List best = Gen.newList();
		do {
			best.clear();
			for (int aux1 = 0; aux1 < team.size(); aux1++)
				for (int aux2 = aux1 + 1; aux2 < team.size(); aux2++) {
					List report = ((AbuttedAgent) team.get(aux1)).behaviorGroup((AbuttedAgent) team.get(aux2));
					//System.out.println("Report\n" + report + " ll:" + best);
					if (report != null)
						if (best.isEmpty() || (best.lst(2).size() < report.lst(0).size())) {
							best.clear();
							best.addAll(report);
							best.add(0, new Integer(aux2));
							best.add(0, new Integer(aux1));
						}
				}

			//                Join the two best candidates
			if (!best.isEmpty()) {
				AbuttedAgent agt1 = (AbuttedAgent) team.get(((Integer) best.get(0)).intValue());
				AbuttedAgent agt2 = (AbuttedAgent) team.get(((Integer) best.get(1)).intValue());
				best.remove(0);
				best.remove(0);
				agt1.makeConn(best, agt2);
				team.remove(agt2);
			}
		} while (!best.isEmpty());
		List groups = Gen.newList();
		for (int aux3 = 0; aux3 < team.size(); aux3++)
			groups.add(0, AbuttedAgent.makeLines(((AbuttedAgent) team.get(aux3)).comps));
		groups.invert();
		return groups;
	}

	void initCompLists() {
		fetsNmos = Gen.newList();
		fetsPmos = Gen.newList();
		bipolars = Gen.newList();
		cells = Gen.newList();

		//                Create nmos and pmos  lists
		for (int aux1 = 0; aux1 < design.size(); aux1++) {
			Component cell = design.get(aux1);
			if (cell instanceof Fet) {
				Fet fet = (Fet) cell;
				if (fet.tecn.equals(Fet.NMOS))
					fetsNmos.add(fet);
				else if (fet.tecn.equals(Fet.PMOS))
					fetsPmos.add(fet);
			}

			//                Create bipolar  list
			else if (cell instanceof Bipolar)
				bipolars.add(cell);
			else if (!(cell instanceof Pad) && !(cell instanceof ElectricNode))
				cells.add(cell);
		}
	}

	/**
	 * This method was created in VisualAge.
	 */
	public void run() {

		initCompLists();
		List groups = createCollumns();
		eval = new Eval(design, groups);
		design = null;

		//
		Display dsp = Dsp.getDisplay("Placer");

		//
		List genes = Gen.newList();

		ThreadGroup routerPool = new ThreadGroup("routerPool");

		//	Run Populations
		for (int numPop = 0; numPop < POPULATION_NUMBER; numPop++) {
			eval.resetPopulation();

			//Run Generations
			for (int aux1 = 0; aux1 < GENERATION_NUMBER; aux1++) {

				//	Generate placement
				if (!eval.run(EVAL_INTERACTIONS))
					throw new RuntimeException("Eval Error.");
				List gene = eval.getBest();

				//	Verify if eval is generating equal placements
				if (genes.contains(gene)) {
					Dsp.println("same gene");
					continue;
				}
				genes.add(gene);

				//
				dsp.clear();
				eval.getDesign().drawOut(dsp);
				dsp.refresh();

				//	Find a free router
				Comm router;
				for (router = null; router == null; ) {
					for (Comm router2 : routers)
						if (router2.isFree()) {
							router = router2;
							break;
						}
					if (router == null)
						try {
							Thread.currentThread().sleep(SLEEP_TIME);
						} catch (InterruptedException e) {
							throw new RuntimeException("Wait interrupted");
						}
				}

				//	Get a design if there is one
				if (router.getDesign() != null) {
					circuits.add(router.getDesign());

					router.getDesign().drawOut(Dsp.getDisplay("Routed"));

					//	If it generated the number of desired circuits
					if (circuits.size() >= CIRCUITS_NUMBER)
						return;
				}

				//	Route the design
				router.setDesign(eval.getDesign());
				Thread routerThread = new Thread(routerPool, router, "n" + aux1);
				routerThread.start();

				/*
			// if the numbers of Routers is bigger than MAX_ROUTERS wait
			while (routerPool.activeCount() > MAX_ROUTERS) {
				try {
					Thread.currentThread().sleep(300);
				} catch (InterruptedException e) {
					throw new RuntimeException("Wait interrupted");
				}
			}

			//	Start new router
			Thread routerThread = new Thread(routerPool, new RouterExpert(des1), "n" + aux1);
			routerThread.start();
				 */
			}
		}
		while (routerPool.activeCount() > 0 && circuits.size() < CIRCUITS_NUMBER)
			try {
				Thread.currentThread().sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				throw new RuntimeException("Wait interrupted");
			}
	}

	/*
//   Print Circuit
DesignCmp designCmp = exp.design;
for (int aux1 = 0; aux1 < designCmp.getElectricNodes().size(); aux1++) {
Dsp.println("NODE " + aux1 + ": ");
ElectricNode node = (ElectricNode) designCmp.getElectricNodes().get(aux1);
if (node.comps == null) {
Dsp.println("     comp NULL");
continue;
}
for (int aux2 = 0; aux2 < node.comps.size(); aux2++) {
Dsp.println("     comp ref: " + node.comps.at(aux2).reference + " term: " + node.comps.at(aux2).termName);
}
}
	 */
}
