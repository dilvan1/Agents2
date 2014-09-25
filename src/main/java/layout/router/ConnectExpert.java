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

package layout.router;

/*
 *  ConnectExpert
 */

import layout.comp.*;
import layout.display.Dsp;
import layout.util.*;

class ConnectExpert implements Runnable {
	private final RouterExpert router;
	private final Operator lastOperator;
	private final Pt pointer;
	private final Wire wire;
	private final int costNum;

	//    Variables for no-jess version
	private final List opList = Gen.newList();

	ConnectExpert(RouterExpert router, Operator lastOp, Pt newPointer, Wire wire, int costAnt) {
		this.router = router;
		lastOperator = lastOp;
		pointer = newPointer;
		this.wire = wire;
		costNum = costAnt;
	}

	void addOpt(Operator op) {
		opList.add(op);
	}

	/**
	 * ************************************
	 * OPERATORS
	 * *************************************
	 */

	void applyOperator(Operator opRete, Wire wire, Pt pointer) {

		if (opRete.getName().equals("CHANGE_LAYER")) {
			operator_CHANGE_LAYER(opRete, wire, pointer);
			return;
		}
		if (opRete.getName().equals("CHANGE_DIRECTION")) {
			operator_CHANGE_DIRECTION(opRete, wire, pointer);
			return;
		}
		if (opRete.getName().equals("GET_ROUND")) {
			operator_GET_ROUND(opRete, wire);
			return;
		}
		if (opRete.getName().equals("GOTO_XY")) {
			operator_GOTO_XY(opRete, wire);
			return;
		}
		throw new RuntimeException("Unknown operator: " + opRete.getName() + " " + opRete.getType() + ".");
	}

	int cost() {
		return costNum;
	}

	void expert() {

		//Append directions to OPTIONS

		//(defrule Connect_subnets*refinement*problemSpace*changelayerFromNone
		//"Append directions to OPTIONS from NONE"
		if (lastOperator.getDirection() == null) {
			//        Append directions
			Linea aux = new Linea(0, 0, 0, 1);
			//aul.setVector( RTYPE(Pt, GFIRST.slot(TARGET_DIRECTIONS)[1]));
			opList.add(new Operator("CHANGE_DIRECTION", aux.getVector()));
			aux.rotateVector(0, 1);
			opList.add(new Operator("CHANGE_DIRECTION", aux.getVector()));
			aux.rotateVector(0, 1);
			opList.add(new Operator("CHANGE_DIRECTION", aux.getVector()));
			aux.rotateVector(0, 1);
			opList.add(new Operator("CHANGE_DIRECTION", aux.getVector()));
		}

		// Connect_subnets*refinement*problemSpace*connectSubnets*fromQuestionsWithDirections
		//"Append directions to OPTIONS 90 degrees from the directions of the command"
		if (lastOperator.getDirection() != null) {
			//        Append directions
			Linea auxVec = new Linea(0, 0, 1, 0);
			auxVec.setVector(lastOperator.getDirection());
			auxVec.rotateVector(0, 1);
			opList.add(new Operator("CHANGE_DIRECTION", auxVec.getVector()));
			auxVec.rotateVector(-1, 0);
			opList.add(new Operator("CHANGE_DIRECTION", auxVec.getVector()));
		}

		//(defrule Connect_subnets*refinement*problemSpace*connectSubnets*AddChangeLayer
		//"CHANGE Layer OPTIONS"
		if (!lastOperator.getName().equals("CHANGE_LAYER") &&
				!lastOperator.getName().equals("GET_ROUND") &&
				getSavings() >= router.routerData.getChangeCost()) {

			//    Append Change layer
			Layer currentLayer = wire.getLastSegment().layer();
			if (currentLayer.equals(Layer.POLY))
				opList.add(new Operator("CHANGE_LAYER", Layer.MET1));
			else if (currentLayer.equals(Layer.MET1)) {
				opList.add(new Operator("CHANGE_LAYER", Layer.MET2));
				opList.add(new Operator("CHANGE_LAYER", Layer.POLY));
			} else if (currentLayer.equals(Layer.MET2))
				opList.add(new Operator("CHANGE_LAYER", Layer.MET1));
		}

		//(defrule Connect_subnets*refinement*problemSpace*connectSubnets*addGetRoundFromGotoXYBlocked
		//"Add GET_ROUND if last action was GOTO_XY to a block"
		if (lastOperator.getName().equals("GOTO_XY") &&
				lastOperator.getStype() != null &&
				lastOperator.getStype().equals("GOTO_BLOCK")) {
			Linea aux = new Linea(0, 0, 1, 0);
			aux.setVector(lastOperator.getDirection());
			aux.rotateVector(0, 1);
			Operator opt1 = new Operator("GET_ROUND", aux.getVector());
			opt1.component = lastOperator.getComponent();
			opt1.wire = lastOperator.getWire();
			opt1.element = lastOperator.getElement();
			opList.add(opt1);
			aux.rotateVector(-1, 0);
			opt1 = new Operator("GET_ROUND", aux.getVector());
			opt1.component = lastOperator.getComponent();
			opt1.wire = lastOperator.getWire();
			opt1.element = lastOperator.getElement();
			opList.add(opt1);
		}

		//(defrule Connect_subnets*propose*operator*AnyAvailableInOptionsList
		//Try all options available
		while (!opList.isEmpty() &&
				!Thread.currentThread().isInterrupted() &&
				(router.best == null || router.best.getCost() > cost())) {
			Operator opt = (Operator) opList.get(0);
			Dsp.printOperator(opt.getName());
			applyOperator(opt, wire, pointer);
			opList.remove(0);
		}
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (9/17/2000 1:23:56 PM)
	 */
	int getSavings() {
		int bestCost = (router.best == null) ? router.routerData.getAverageCost() * RouterExpert.MULT_AVERAGE : router.best.getCost();
		return bestCost - cost();
	}

	Wire getWire() {
		return wire;
	}

	/*
	 *        CHANGE DIRECTION
	 */
	void operator_CHANGE_DIRECTION(Operator opRete, Wire wire, Pt pointer) {

		DesignCmp design = router.routerData.getDesign();
		ElectricNode node = router.routerData.getNode();
		Pt targetPoint = router.routerData.getTargetPoint();

		int ind = wire.size() - 1;
		WirePt wirePt = wire.get(ind);
		Layer currentLayer = wirePt.layer();

		Pt direction = opRete.getDirection();
		int width = Consultant.db.getWireWidth(currentLayer);
		int minBlockDist = width * RouterExpert.MIN_BLOCK_DIST;

		int subNet1 = router.routerData.getSubnet1();
		Rectangle envlp2 = router.routerData.getEnvlp2();

		/*
		 *        Find the distance from here to the target point and to the border
		 *         of the envelop
		 */
		int distTarget, distEdge;
		if (direction.x != 0) {
			if (direction.x != 1 && direction.x != -1)
				throw new RuntimeException("Direction vector module != 1");
			distTarget = (targetPoint.x - wirePt.x) * direction.x;
			int dist1 = (envlp2.c1.x - wirePt.x) * direction.x;
			int dist2 = (envlp2.c2.x - wirePt.x) * direction.x;
			if (dist1 * dist2 != 0 && dist1 / Math.abs(dist1) != dist2 / Math.abs(dist2)) distEdge = -1;
			else if (Math.abs(dist1) < Math.abs(dist2)) distEdge = dist1;
			else distEdge = dist2;
		} else {
			if (direction.y != 1 && direction.y != -1)
				throw new RuntimeException("Direction vector module != 1");
			distTarget = (targetPoint.y - wirePt.y) * direction.y;
			int dist1 = (envlp2.c1.y - wirePt.y) * direction.y;
			int dist2 = (envlp2.c2.y - wirePt.y) * direction.y;
			if (dist1 * dist2 != 0 && dist1 / Math.abs(dist1) != dist2 / Math.abs(dist2)) distEdge = -1;
			else if (Math.abs(dist1) < Math.abs(dist2)) distEdge = dist1;
			else distEdge = dist2;
		}
		/*
		 *        To compensate the no-zero width of the wire
		 *        Obs: distEdge and distTarget are only valid if > 0
		 */
		if (distEdge > 0) distEdge = distEdge + (int) Math.ceil(width * 0.5);

		Dsp.println("DIRECTION       " + direction + " Pt1: " + wirePt + " Pt2: " + targetPoint +
				" Envlp: " + envlp2 + "  distTarget: " + distTarget + " distEdge: " + distEdge);

		//        Find paths2
		List paths2 = Gen.newList();
		for (int auxc = 0; auxc < node.routingNets.size(); auxc++)
			if (auxc != subNet1)
				paths2.add(node.routingNets.get(auxc));

		Linea auxPointer = new Linea();
		auxPointer.c1.set(wirePt);
		auxPointer.setVector(direction);
		List lstTouch = design.getWiringLayersTouchesPointer(paths2, auxPointer, width);

		//        Test if it doesn't touches anything
		//NoCrash[] paths= new NoCrash[1];
		if (lstTouch.isEmpty()) {
			opRete.ret = "NO_TOUCH";

			if (distTarget > minBlockDist) {
				int distCrash;
				NoCrash[] paths = new NoCrash[1];
				paths[0] = new NoCrash(wire, ind);
				List lstCrash = design.getCrashesPointer(paths, auxPointer, currentLayer, width);

				//        New stuff to prevent reference bug
				int crashCmp = -1;
				int crashWire = -1;
				int crashElement = -1;
				if (!lstCrash.isEmpty()) {
					Wire wireCrash = ((Crash) lstCrash.get(0)).getWire();
					Component cmpCrash = Component.getComponentByWire(wireCrash);
					if (cmpCrash.reference != node.reference) {
						crashCmp = cmpCrash.reference;
						crashWire = cmpCrash.getBody().indexOf(wireCrash);
						crashElement = ((Crash) lstCrash.get(0)).getInd();
					} else
						crashCmp = -1;
					distCrash = ((Crash) lstCrash.get(0)).getDistance();
				} else {
					crashCmp = -1;
					distCrash = distTarget * RouterExpert.DIST_CRASH_FOR_NOCRASH;
					;
				}

				//        Test if distance smaller than crash and add GOTO_XY
				if (distTarget < distCrash - 1) {
					if (distTarget > minBlockDist) {
						Operator op = new Operator("GOTO_XY", direction);
						op.distance = distTarget;
						addOpt(op);
					}
				} else if (distCrash - 1 > minBlockDist && crashCmp != -1) {
					Operator op = new Operator("GOTO_XY", direction);
					op.distance = distCrash - 1;
					op.stype = "GOTO_BLOCK";
					op.component = crashCmp;
					op.wire = crashWire;
					op.element = crashElement;
					addOpt(op);
				} else {
					//        If FULL BLOCKED try a GO ROUND

					//        Add the full blocked option only for the first net or change layer
					Linea auxDir = new Linea(0, 0, 0, 0);
					auxDir.c1 = new Pt(0, 0);
					auxDir.c2 = direction;
					Linea auxInv = new Linea(auxDir.c1, auxDir.c2);

					//        Finds the vector that points in the oposite as pointer
					auxInv.c2.set(pointer);
					auxInv.rotateVector(-1, 0);
					auxDir.rotateVector(0, 1);

					//        Test to avoid going in the direction oposite to pointer
					if (auxDir.c2 != auxInv.c2 && crashCmp != -1) {
						Operator op = new Operator("GET_ROUND", auxDir.c2);
						op.component = crashCmp;
						op.wire = crashWire;
						op.element = crashElement;
						addOpt(op);
					}
					auxDir.rotateVector(-1, 0);

					//        Test to avoid going in the direction oposite to pointer
					if (auxDir.c2 != auxInv.c2 && crashCmp != -1) {
						Operator op = new Operator("GET_ROUND", auxDir.c2);
						op.component = crashCmp;
						op.wire = crashWire;
						op.element = crashElement;
						addOpt(op);
					}
				}

				//        Add GOTO envelope
				if (distEdge > minBlockDist && distEdge < distCrash - 1 - minBlockDist) {
					Operator op = new Operator("GOTO_XY", direction);
					op.distance = distEdge;
					addOpt(op);
				}
			}
		} else {
			/*
			 *        Test if touch list touched two at the same place of diferent layers
			 *        get the one with the best layer
			 */
			//VERIFICAR se esta realmente funcionando para a distancia minima
			int touchInd;
			for (touchInd = 0;
					touchInd < 3 &&
					touchInd < lstTouch.size() &&
					((Crash) lstTouch.get(touchInd)).getWire().get(((Crash) lstTouch.get(touchInd)).getInd()).layer() != wirePt.layer();
					touchInd++)
				;
			if (touchInd != 0 && touchInd < 3 && touchInd < lstTouch.size() &&
					((Crash) lstTouch.get(touchInd)).getDistance() - ((Crash) lstTouch.get(touchInd)).getDistance() < minBlockDist)
				for (; touchInd > 0; touchInd--) lstTouch.remove(0);

			Wire wire2 = ((Crash) lstTouch.get(0)).getWire();
			int ind2 = ((Crash) lstTouch.get(0)).getInd();
			int distance = ((Crash) lstTouch.get(0)).getDistance();

			NoCrash[] paths = new NoCrash[2];
			paths[0] = new NoCrash(wire, ind);
			paths[1] = new NoCrash(wire2, ind2);

			Segment seg = new Segment();
			seg.c1.set(auxPointer.c1);
			seg.c2 = auxPointer.getPointOfT(distance + Consultant.db.getMinWidth(wire2.get(ind2).layer()));

			Wire directWire = new Wire();
			directWire.add(new WirePt(seg.c1, 0, Layer.EMPTY));
			directWire.add(new WirePt(seg.c2, width, wirePt.layer()));

			//        Test if it doesn't make a connection
			Wire newWire = design.makeConnection(paths, auxPointer, directWire, lstTouch);
			if (newWire.isEmpty()) {
				List lstCrash = design.getCrashesPointer(paths, auxPointer, currentLayer, width);
				//TEST if crash was on the same net as well        Probably outdated
				//

				//        Test if it doesn't crash anything
				if (lstCrash.isEmpty() || ((Crash) lstCrash.get(0)).getDistance() >= ((Crash) lstTouch.get(0)).getDistance()) {
					opRete.ret = "CANT_MAKE_CONNECTION";

					//        Add GOTO envelope It has to be a bit away from the touch
					//
					int touchDist = ((Crash) lstTouch.get(0)).getDistance();
					if (distEdge > minBlockDist && distEdge < touchDist - minBlockDist) {
						Operator op = new Operator("GOTO_XY", direction);
						op.distance = distEdge;
						addOpt(op);
					}
				} else {

					//        Create reroute ConnectExpert agent
					reroute(node, lstCrash, wire, directWire, direction);

					//        New stuff to prevent reference bug
					int crashCmp;
					int crashWire = -1;
					int crashElement = -1;
					Wire wireCrash = ((Crash) lstCrash.get(0)).getWire();
					Component cmpCrash = Component.getComponentByWire(wireCrash);
					if (!cmpCrash.equals(node)) {
						crashCmp = cmpCrash.reference;
						crashWire = cmpCrash.getBody().indexOf(wireCrash);
						crashElement = ((Crash) lstCrash.get(0)).getInd();
					} else
						crashCmp = -1;

					//        Test if the first crash is just in front (can't move forward)
					int distCrash = ((Crash) lstCrash.get(0)).getDistance();
					if (distCrash - 1 <= minBlockDist) {
						opRete.ret = "FULL_BLOCKED";
						/*
						 *        Add the full blocked option only for the first net or change layer
						 *        rerouter wasn't tested maybe is problematic
						 */
						//                        if (!GFIRST.slot(LAST_ACTION).lst(1).isSlot(DIRECTION)) {
						//MUDAR
						if (crashCmp != -1) {
							Linea auxDir = new Linea(0, 0, 0, 0);
							auxDir.c1 = new Pt(0, 0);
							auxDir.c2.set(direction);
							Linea auxInv = new Linea(auxDir.c1, auxDir.c2);

							//        Finds the vector that points in the oposite as pointer
							auxInv.c2.set(pointer);
							auxInv.rotateVector(-1, 0);
							auxDir.rotateVector(0, 1);

							//        Test to avoid going in the direction oposite to pointer
							if (!auxDir.c2.equals(auxInv.c2)) {
								Operator op = new Operator("GET_ROUND", auxDir.c2);
								op.component = crashCmp;
								op.wire = crashWire;
								op.element = crashElement;
								addOpt(op);
							}
							auxDir.rotateVector(-1, 0);

							//        Test to avoid going in the direction oposite to pointer
							if (!auxDir.c2.equals(auxInv.c2)) {
								Operator op = new Operator("GET_ROUND", auxDir.c2);
								op.component = crashCmp;
								op.wire = crashWire;
								op.element = crashElement;
								addOpt(op);
							}
						}

						//        Common crash
					} else {
						opRete.ret = "BLOCKED";

						//        Test if distance smaller than crash and add GOTO_XY
						if (crashCmp != -1) {
							Operator op = new Operator("GOTO_XY", direction);
							op.distance = distCrash - 1;
							op.stype = "GOTO_BLOCK";
							op.component = crashCmp;
							op.wire = crashWire;
							op.element = crashElement;
							addOpt(op);
						}

						//        Add GOTO envelope
						if (distEdge > minBlockDist && distEdge < distCrash - 1 - minBlockDist) {
							Operator op = new Operator("GOTO_XY", direction);
							op.distance = distEdge;
							addOpt(op);
						}
					}
				}
			} else if (!wire.isJoinable(newWire))
				opRete.ret = "SHORT CIRCUIT";
			else {
				opRete.ret = "SUCCESS";

				Wire wire1 = wire.clone();
				int cost1 = cost() + design.getWireCost(newWire);
				wire1.join(newWire);
				router.tryAsBestWire(cost1, wire1, node.routingNets.indexOf(((Crash) lstTouch.get(0)).getNet()));

				//        Add GOTO envelope
				int touchDist = ((Crash) lstTouch.get(0)).getDistance();
				if (distEdge > minBlockDist && distEdge < touchDist - 1 - minBlockDist) {
					Operator op = new Operator("GOTO_XY", direction);
					op.distance = distEdge;
					addOpt(op);
				}
			}
		}
	}

	/**
	 * CHANGE_LAYER
	 */
	void operator_CHANGE_LAYER(Operator opRete, Wire wire, Pt pointer) {//throws ReteException{

		DesignCmp design = router.routerData.getDesign();

		int ind = wire.size() - 1;
		WirePt wirePt = wire.get(ind);
		//int        savings=  bestCost - cost();

		NoCrash[] paths = new NoCrash[1];
		paths[0] = new NoCrash(wire, ind);

		Layer layerTo = opRete.layer;
		opRete.fromLayer = wirePt.layer();

		Linea auxPointer = new Linea();
		auxPointer.c1.set(wirePt);
		auxPointer.setVector(pointer);
		if (pointer.x == 0 && pointer.y == 0)
			auxPointer.setVector(new Pt(1, 0));
		Wire newWire = design.changeLayer(paths, auxPointer, wire, ind, layerTo);
		if (newWire != null && design.isInDesignBoundaries(newWire)) {
			int wireCost = design.getWireCost(newWire);
			//if (wireCost>savings)
			//COUT("KILL |||||| LAYER: " << wireCost << " Savings: " << savings << " BestCost: " << bestCost << endl)
			if (wireCost <= getSavings() && wire.isJoinable(newWire)) {
				Wire aux = wire.clone();
				wireCost = wireCost + cost();
				aux.join(newWire);
				router.addAgent(new ConnectExpert(router, opRete, pointer, aux, wireCost));
			}
		} else
			opRete.ret = "CANT_CHANGE";
	}

	/*
	 *        GET ROUND
	 */
	void operator_GET_ROUND(Operator opRete, Wire wire) {

		DesignCmp design = router.routerData.getDesign();
		Pt direction = opRete.getDirection();

		int ind = wire.size() - 1;
		WirePt wirePt = wire.get(ind);
		Layer currentLayer = wirePt.layer();

		int crashCmp = opRete.component;

		Component cmp2 = design.getByReference(crashCmp);
		Wire wire2 = cmp2.getBody().get(opRete.wire);
		int ind2 = opRete.element;

		int width = Consultant.db.getWireWidth(currentLayer);
		int margin = Consultant.db.getWiringMargin(currentLayer, wire2.get(ind2).layer());

		NoCrash[] paths = new NoCrash[1];
		paths[0] = new NoCrash(wire, ind);

		Rectangle object = new Rectangle();
		object.set(wire2.get(ind2 - 1), wire2.get(ind2), wire2.get(ind2).width());

		if (margin == -1) margin = 0;
		//throw new RuntimeException("Margin equal -1 in rule Connect_net*implement*operator*getRound");
		Linea auxPointer = new Linea();
		auxPointer.c1.set(wirePt);
		auxPointer.setVector(direction);

		//IF_DEBUG_ON(
		layout.display.Dsp.get2().addVector(auxPointer, java.awt.Color.blue);
		//END

		int guard = margin + (int) Math.ceil(width * 0.5);
		Linea line1;
		Linea line2;
		if (auxPointer.isVertical()) {
			line1 = new Linea(object.c1.x - guard, object.c1.y - guard,
					object.c2.x + guard, object.c1.y - guard);
			line2 = new Linea(object.c1.x - guard, object.c2.y + guard,
					object.c2.x + guard, object.c2.y + guard);
		} else {
			line1 = new Linea(object.c1.x - guard, object.c1.y - guard,
					object.c1.x - guard, object.c2.y + guard);
			line2 = new Linea(object.c2.x + guard, object.c1.y - guard,
					object.c2.x + guard, object.c2.y + guard);
		}

		//    Get distance to edge
		double dist1 = auxPointer.getIntersectPt(line1).getT();
		double dist2 = auxPointer.getIntersectPt(line2).getT();
		double dist = Math.max(dist1, dist2);
		//   CHECK_ERR(dist<=0, "ConnectExpert:: Distance smaller than 0 in rule Connect_net*implement*operator*getRound");
		Pt auxPt = auxPointer.getPointOfT(dist);
		Wire newWire = new Wire();
		newWire.add(new WirePt(auxPointer.c1, 0, Layer.EMPTY));
		newWire.add(new WirePt(auxPt, width, currentLayer));

		//        Test to see if the wire is affordable
		int wireCost = design.getWireCost(newWire);
		//if (wireCost>savings)
		//COUT( "KILL <<<<<< : WireCOST: " << wireCost << " Savings: " << savings << " BestCost: " << bestCost << endl)

		/*
#ifdef DEBUG_ON
   List lstCrash;
   if (wireCost>savings || dist<=0 || design.getCrashes(paths, newWire, lstCrash) || !design.inDesignBoundaries(newWire)) {
	  if (dist>0 && !lstCrash.empty()) {
		 Rectangle auxRec;
		 auxRec.set(auxPointer.c1(), auxPt, width);
		 DRAW( "(rect " << auxRec.c1().x() << " " << auxRec.c1().y() << " " << auxRec.c2().x()
						<< " " << auxRec.c2().y() << " 'navyBlue)" )
		 Wire& auxWire= RTYPE(Wire, lstCrash.lst(0)[0]);
		 Int& auxInd= RTYPE(Int, lstCrash.lst(0)[1]);
		 auxRec.set(auxWire[auxInd-1].pt(), auxWire[auxInd].pt(), auxWire[auxInd].width());
		 DRAW( "(rect " << auxRec.c1().x() << " " << auxRec.c1().y() << " "
						<< auxRec.c2().x() << " " << auxRec.c2().y() << " 'black)" )
	  }
#else  */
		if (wireCost > getSavings() ||
				dist <= 0 ||
				design.isCrashing(paths, newWire) ||
				!design.isInDesignBoundaries(newWire))
			//#endif
			opRete.ret = "CANT_GET";
		else if (!wire.isJoinable(newWire))
			opRete.ret = "SHORT_CIRCUIT";
		else {
			auxPt = auxPointer.getVector();
			Wire aux = wire.clone();
			wireCost = wireCost + cost();
			aux.join(newWire);
			router.addAgent(new ConnectExpert(router, opRete, auxPt, aux, wireCost));
		}
	}

	/*
	 *        GOTO XY
	 */
	void operator_GOTO_XY(Operator opRete, Wire wire) {

		DesignCmp design = router.routerData.getDesign();
		Pt direction = opRete.getDirection();

		int ind = wire.size() - 1;
		WirePt wirePt = wire.get(ind);
		Layer currentLayer = wirePt.layer();

		int width = Consultant.db.getWireWidth(currentLayer);
		int distance = opRete.distance;

		NoCrash[] paths = new NoCrash[1];
		paths[0] = new NoCrash(wire, ind);

		Linea auxPointer = new Linea();
		auxPointer.c1.set(wirePt);
		auxPointer.setVector(direction);

		//        It works because newPointer is unitary ( 1 T is equal to 1 unity)
		Pt auxPt = auxPointer.getPointOfT(distance);
		Wire newWire = new Wire();
		newWire.add(new WirePt(auxPointer.c1, 0, Layer.EMPTY));
		newWire.add(new WirePt(auxPt, width, currentLayer));

		//        Test to see if the wire is affordable
		int wireCost = design.getWireCost(newWire);
		//if (wireCost>savings)
		//COUT("KILL <<<<<< : WireCOST: " << wireCost << " Savings: " << savings << " BestCost: " << bestCost << endl)
		if (wireCost > getSavings() || distance < 1 || design.isCrashing(paths, newWire) || !design.isInDesignBoundaries(newWire))
			opRete.ret = "CANT_GO";
		else if (!wire.isJoinable(newWire))
			opRete.ret = "SHORT_CIRCUIT";
		else {

			auxPt = auxPointer.getVector();
			//DRAW( newWire )
			Wire aux = wire.clone();
			wireCost = wireCost + cost();
			aux.join(newWire);
			router.addAgent(new ConnectExpert(router, opRete, auxPt, aux, wireCost));
		}
	}

	/**
	 * <tt>
	 * Crash lists: (It's a MIXED list, it doesn't own Wire)
	 * From getCrash:
	 * [Wire (Wire&) Segment (Int)]
	 * From getCrashesPointer:
	 * [ Wire (Wire&) Segment (Int) Distance (Int) ]
	 * From getWiringLayersTouchesPointer:
	 * [ Wire (Wire&) Segment (Int) Distance (Int) SubnetList (Ind)]
	 * :note this functions gets the crashes to a list of wires, SubnetList is a pointer
	 * to that list.
	 * </tt>
	 */
	boolean reroute(ElectricNode node, List lstCrash, Wire wire, Wire directWire, Pt direction) {

		/*
		 *        Find the width of the wire trying to connect
		 *        and calculate how clean the way should be
		 */
		int dist = ((Crash) lstCrash.get(0)).getDistance() + 2 * directWire.get(1).width();

		/*    Test if the first crash and the region around it
		 *    is removable.
		 */
		List lstNodes = Gen.newList();
		for (int aux1 = 0; aux1 < lstCrash.size() && dist >= ((Crash) lstCrash.get(aux1)).getDistance(); aux1++) {
			Wire wireCrash = ((Crash) lstCrash.get(aux1)).getWire();
			Component cmp = Component.getComponentByWire(wireCrash);

			//	Cmp is not a node
			if (!(cmp instanceof ElectricNode)) {
				Dsp.println("HAS NO NODE");
				return false;
			}

			//	Cmp is the same node as this
			if (cmp.reference == node.reference) {
				Dsp.println("SAME NODE");
				return false;
			}

			//	Cmp is a not fully routed node
			if (((ElectricNode) cmp).routingNets.size() != 1) {
				Dsp.println("NOT YET COMPLETELY ROUTED NET");
				return false;
			}

			//	Cmp is locked for rerouting to prevent deadlock
			if (((ElectricNode) cmp).isRerouteLocked()) {
				Dsp.println("HAS LOCK");
				return false;
			}

			//	the wire crashed is not rewirable
			if (!wireCrash.isRewirable()) {
				//wireCrash.drawOut(Dsp.get3());
				Dsp.println("HAS NO WIRING LAYERS: " + wireCrash); //.get(1).layer());
				continue;
				//return false;
				/*
			List auxWires= router.getRewirableWires(cmp.body);
			if (auxWires.isEmpty() && ((Crash)lstCrash.get(0)).getDistance()<10)
			return false;
			System.out.print("CRASH:");
			for(int at=0; at<lstCrash.size(); at++) {
			System.out.print(((Crash) lstCrash.get(at)).getDistance() + "," +
			((Crash) lstCrash.get(at)).getWirePt().layer() + "  ");
			}
			System.out.println();
				 */
			}


			/*        Create a list with the node and the wires to be
			 *                deleted [ Node Wire1 Wire2 ... ]
			 */
			if (!lstNodes.contains(cmp))
				lstNodes.add(cmp);
		}
		if (lstNodes.size() > 3) {
			Dsp.println("::::::::::: The node number is too big >3 ::::::::");
			return false;
		}
		router.addAgentInQueue(new ConnectExpert(router, new Operator("REROUTE"), direction, wire, cost()), lstNodes);
		return true;
	}

	@Override
	public void run() {

		//    Define the last action
		lastOperator.type = "LAST_ACTION";

		expert();
	}
}
