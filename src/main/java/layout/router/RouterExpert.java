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
 *        RouterExpert
 */
//tODO  the design consider a crash the limits of the design generating change layer

import layout.comp.*;
import layout.display.Dsp;
import layout.util.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RouterExpert implements Runnable {

    final static int AGENTS_QUEUE_MAX_SIZE = 20;
    final static int MAX_AGENTS_LOOP_BEST = 70;
    final static int MAX_AGENTS_LOOP = 100;
    final static int MULT_AVERAGE = 1000;
    final static int MAX_UNWIRE_LOOPS_WITH_WIRE = 5;
    final static int MAX_UNWIRE_LOOPS_WITHOUT_WIRE = 20;
    final static int UNWIRED_COST_RATIO = 3;

    final static int MIN_BLOCK_DIST = 2; //1; // /2; // didnt work in java= *2;
    final static int DIST_CRASH_FOR_NOCRASH = 10000;

    static int MAX_CONNECT_THREADS = 4;
    static int TCP_PORT = 9090;

    private DesignCmp design;
    volatile BestWire best;
    BestWire bestTotal;
    volatile boolean flagSearch;

    List nodeRoutingQueue = Gen.newList();
    List nodeRoutingAreas = Gen.newList();
    int currentRoutingNode;

    List agents = Gen.newList();
    List agentsQueue = Gen.newList();

    String errorMessage;
    SaveNodes saveNodes = new SaveNodes(); //Stack of saved nodes
    RouterData routerData;

    public RouterExpert(DesignCmp design) {
        this.design = design;
    }

    synchronized void addAgent(ConnectExpert agt) {
        if (Thread.currentThread().isInterrupted()) return;
        agents.add(agt);
    }

    //   long    distToTarget;
    //   PtrList aux1,auxLast;
    //   Pt      wireCoord;
    //
    // TALVEZ ORDENAR AS MUDANCAS DE LAYER PARA O FIM (PARA AS 10 OU 20 PRIMEIRAS
    //
    //   //        Put in order, the one nearest the target first
    //   Pt& target= RTYPE(Pt, routerData().slot(TARGET_POINT)[1]);
    //   RTYPE(ConnectExpert, *agt).getWireCoord(wireCoord);
    //   distToTarget= target.manhattanDistance(wireCoord);
    ////   COUT( "Target Pt: " << target << " dist: " << distToTarget << endl)
    //   for (agents.first(aux1); !aux1.null(); aux1.next()) {
    //      RTYPE(ConnectExpert, agents[aux1]).getWireCoord(wireCoord);
    //      if (distToTarget<target.manhattanDistance(wireCoord)) {
    ////         COUT( endl << "   WIre CLOSER: " << distToTarget << "  " << target.manhattanDistance(wireCoord) << " " << endl<<endl)
    //         break;
    //     }
    //      auxLast= aux1;
    //   }
    //   if (auxLast.null()) agents.push(agt);
    //   else agents.apAfter(auxLast, agt);
    synchronized void addAgentInQueue(ConnectExpert agt, List unwireNodes) {

	/*-
     *        agentsQueue= [ agent (ConnectExpert) nodesToUnwired (Lst<ElectricNode>) areas]
	 *
	 *
	 *   nodeRoutingQueue =     n0    n1    n2    n3    n4    n5    n6    n7    n8    ...
	 *                                            |- currentRoutingNode
	 *    unwireNodes= [ n2 n5]
	 *
	 *    Possible unwireLst= [n2 n3 n4 n5 n6]
	 *
	 *    n2 and n5 will automaticaly be in the unwiredNode list because they are the one being asked
	 *    to be unwired. The possible unwireLst include all nodes from the n2 (the lowest indice) to
	 *    n6 (one before the currentRoutingNode). It is then tested which nodes have an area in comom
	 *    with the nodes n2 and n5 (or another node included in the unwireLst). If n3 area don't intersect
	 *    n2 area, n4 intersect n2, and n6 intersect n4 the unwiredLst will be:
	 *        [n2 n4 n5 n6]
	 */

        //CHECK_ERR(unwireNodes.empty(), "RouterExpert:: addAgentInQueue: No nodes to be unwired");

        if (Thread.currentThread().isInterrupted()) return;

        List unwireAreas = Gen.newList();
        List unwireLst = Gen.newList();

        //        Add the nodes unwired up to this point
        unwireLst.addAll(saveNodes.getReferences());
        unwireAreas.addAll(saveNodes.getAreas());

        //        Add the nodes to be unwired
        for (int aux1 = 0; aux1 < unwireNodes.size(); aux1++) {
            ElectricNode node = (ElectricNode) unwireNodes.get(aux1);
            unwireLst.add(new Integer(node.reference));
            unwireAreas.add(DesignCmp.getRoutingNetsEnvelope(node.routingNets));
        }


        //        Find the node higher in the list
        int num1;
        int ind = currentRoutingNode;
        for (int aux1 = 0; aux1 < unwireLst.size(); aux1++) {
            for (num1 = 0; num1 < nodeRoutingQueue.size() && !nodeRoutingQueue.get(num1).equals(unwireLst.get(aux1)); num1++)
                ;
            if (num1 >= nodeRoutingQueue.size())
                throw new RuntimeException("Can't find node");
            if (num1 < ind)
                ind = num1;
        }

        //        Find the list of possible unwired modes
        for (; ind < currentRoutingNode; ind++) {
//		DesignCmp design = routerData.getDesign();
//		ElectricNode node = (ElectricNode) design.getByReference(((Integer) nodeRoutingQueue.get(ind)).intValue());
//		Rectangle area = DesignCmp.getRoutingNetsEnvelope(node.routingNets);

            //        Test if node is one asked to be unwired
            if (unwireLst.contains(nodeRoutingQueue.get(ind))) {
//			unwireLst.add(node);
//			unwireArea.add(area);
                continue;
            }
		

		/*-        Test if node share area with a node to
		 *        to be unwired
		 */
            DesignCmp design = routerData.getDesign();
            ElectricNode node = (ElectricNode) design.getByReference(((Integer) nodeRoutingQueue.get(ind)).intValue());
            Rectangle area = DesignCmp.getRoutingNetsEnvelope(node.routingNets);
            int aux1;
            for (aux1 = 0; aux1 < unwireAreas.size() && !((Rectangle) unwireAreas.get(aux1)).isTouching(area); aux1++) ;
            if (aux1 < unwireAreas.size()) {

                //	A node can be rerouted locked but not be in the path of other
                if (node.isRerouteLocked()) {
                    return;
                }
                unwireLst.add(new Integer(node.reference));
                unwireAreas.add(area);
            }
        }

        //        Put in the queue, the smaller number of nodes to be rerouted first
        //CHECK_ERR( unwireLst.empty(), "RouterExpert:: addAgentInQueue: Unwire list empty");
        int numNodes = unwireLst.size();
        int aux1;
        for (aux1 = 0; aux1 < agentsQueue.size() && numNodes > agentsQueue.lst(aux1).lst(1).size(); aux1++) ;

        //        Convert from reference to integer reference
//	List auxRef = Gen.newList();
//	while (!unwireLst.isEmpty()) {
//		auxRef.add(new Integer(((Component) unwireLst.get(0)).reference));
//		unwireLst.remove(0);
//	}

        agentsQueue.add(aux1, Gen.newList(agt, unwireLst, unwireAreas));
        //        Limit the queue
        if (agentsQueue.size() > AGENTS_QUEUE_MAX_SIZE)
            agentsQueue.remove(agentsQueue.size() - 1);
    }

    RouterData calculateRouterData(DesignCmp design, ElectricNode node, WiringPoint wp, int net1, int net2, Rectangle envlp1, Rectangle envlp2) {

	   /*
		*        data= [ design node subnet_1 target_point min_cost]
		*/
        Wire wire2 = (Wire) node.routingNets.lst(net2).get(wp.getWire2());
        WirePt wirePt1 = ((Wire) node.routingNets.lst(net1).get(wp.getWire1())).at(wp.getWirePt1());
        WirePt wirePt2 = wire2.at(wp.getWirePt2());

        RouterData data = new RouterData();

        data.design = design;
        data.node = node;
        data.subnet1 = net1;
        data.envlp2 = envlp2;

        //        Calculate the cost of a layer change
        Wire auxWire = Consultant.db.getWireToInterconnect(Layer.POLY, Layer.MET1);

        data.changeCost = design.getWireCost(auxWire);

		/*-
		 *        Calculate TargePoint
		 *
		 *                                            |
		 *            auxWire1.at(ele1)  * - - - - - -|
		 *                                            |
		 *                               seg
		 */
        Linea seg = new Linea(wire2.at(wp.getWirePt2() - 1), wire2.at(wp.getWirePt2()));
        Pt targetPt;

        if (seg.c1.equals(seg.c2)) {
            targetPt = seg.c1;
        } else {
            Linea auxLine = new Linea();
            auxLine.c1.set(wirePt1);
            auxLine.setVector(seg);
            auxLine.rotateVector(0, 1);
            InterPt ip = seg.getIntersectPt(auxLine);
            if (ip.getT() <= 0) targetPt = seg.c1;
            else if (ip.getT() >= 1) targetPt = seg.c2;
            else targetPt = seg.getPointOfT(ip.getT());
        }
        data.targetPoint = targetPt;

        //        Calculate the average cost to conect in this node
        Rectangle area = DesignCmp.getRoutingNetsEnvelope(node.routingNets);
        int dist = (3 * Math.max(area.getLx(), area.getLy()) + Math.min(area.getLx(), area.getLy())) / 4;
        auxWire = design.getFreeWire(dist, wirePt1.layer(), wirePt2.layer());
        int wireCost = design.getWireCost(auxWire);

        data.averageCost = wireCost;

        //data.bestCost= wireCost*1000;

        //        Calculate minimun wire
        dist = envlp1.getDistanceX(envlp2) + envlp1.getDistanceY(envlp2) + Consultant.db.getWireWidth(wirePt1.layer());

        auxWire = design.getFreeWire(dist, wirePt1.layer(), wirePt2.layer());
        int wireCost2 = design.getWireCost(auxWire);
        if (wireCost < wireCost2) data.minCost = wireCost;
        else data.minCost = design.getWireCost(auxWire);

        return data;

        //DRAW( "(vect " << auxWire1[ele1].pt().x() << " " << auxWire1[ele1].pt().y() << " " <<targetPt.x() << " " <<targetPt.y() << " 'black)" )
    }

    boolean connectNode(DesignCmp design, ElectricNode node) {

        List nets = node.routingNets;
        int net1 = 0;
        int net2 = 1;
        while (net2 < nets.size()) {
            if (!connectSubnets(design, node, net1, net2)) {
                Dsp.println("NET NOT ROUTED !!!!!!!!!!!!!!!!!!  " + net2);
                net2++;
            } else
                net2 = 1;
            Dsp.get2().clear();
            design.drawOut(Dsp.get2());
        }

        if (net2 != 1) {
            Dsp.println("NODE NOT ROUTED " + node.reference);
            return false;
        }
        return true;
    }

    /**
     * <tt>
     * Calculate the envelop of each net
     * Chose a subnet to begin
     * Find the beggining points on both nets
     * Call the routine to connect the nets
     * If it fails try the other way around.</tt>
     */
    boolean connectSubnets(DesignCmp design, ElectricNode node, int net1, int net2) {
	/*-
	 *        Not so: Get the net envelope and order then (routing goes from the smaller to
	 *        the bigger)
	 */
        int loopCount = -1;
        Rectangle envlp1 = design.getNetEnvelope(node.routingNets.lst(net1));
        Rectangle envlp2 = design.getNetEnvelope(node.routingNets.lst(net2));
        if (envlp1.getArea() > envlp2.getArea()) {
            int aux = net1;
            net1 = net2;
            net2 = aux;
            Rectangle envlp = envlp1;
            envlp1 = envlp2;
            envlp2 = envlp;
        }

        //    Flips added but not very good
        for (int countFlips = 0; countFlips < 2; countFlips++) {
            WiringPoint wp = design.getClosestWiringPoints(node.routingNets.lst(net1), node.routingNets.lst(net2));
            if (wp.getDist() == 0) {
                System.out.println("getClosestWiringPoints dist =0 ");
                return false;
            }

            routerData = calculateRouterData(design, node, wp, net1, net2, envlp1, envlp2);
            int minCost = routerData.getMinCost();
            int averageCost = routerData.getAverageCost();

            //        Calculate the first wire
            Wire auxWire1 = (Wire) node.routingNets.lst(net1).get(wp.getWire1());
            Wire wire = new Wire();
            wire.add(new WirePt(auxWire1.at(wp.getWirePt1() - 1), 0, Layer.EMPTY));
            wire.add(auxWire1.at(wp.getWirePt1()).clone());
            agentsQueue.clear();
            agentsQueue.add(Gen.newList(new ConnectExpert(this, new Operator("NONE"), new Pt(0, 0), wire, 0)));
            agentsQueue.lst(0).add(Gen.newList());
            agentsQueue.lst(0).add(Gen.newList());

		/*-
		 *        It will keep trying until the cost is smaller than minCost + 15%
		 *        or it run out of options of routing
		 *
		 *        Initialize the best data
		 */
            bestTotal = null;
            flagSearch = true;
            ThreadGroup connectPool = new ThreadGroup("connectPool");
            while (!agentsQueue.isEmpty() && flagSearch) {
                loopCount++;
                Dsp.println("\nROUTING  Node Ref: " + node.reference + " Num: " + node.getBody().size() + " LEVEL: " + loopCount + " Loop: " + countFlips);

                //        Prepare the agents to run the actual wire search
                List unwiredNodesRef = agentsQueue.lst(0).lst(1);
                saveNodes.unwire(design, unwiredNodesRef, agentsQueue.lst(0).lst(2));
                agents.clear();
                agents.add(agentsQueue.lst(0).remove(0));
                agentsQueue.remove(0);
                Dsp.println("QUEUE agentsQueue: " + agentsQueue.size());

                //        Do the atual search for paths
			/*-
			 *        Append the new Agent and makes the routing
			 *        for the subnet, requisiton for rerouter are directed to agentsReroute
			 */
                best = null;
                int agentsLoopCount = -1;
                do {

                    //	If no agents in queue or the max number of agents is ruuning
                    if (agents.isEmpty() ||
                            connectPool.activeCount() >= MAX_CONNECT_THREADS) {
                        try {
                            Thread.currentThread().sleep(100);
                        } catch (Exception e) {
                        }
                        continue;
                    }


                    //	Get first the Metal1 agents
                    ConnectExpert agent = null;
                    //synchronized (this) {
                    //	for (int aux1 = 0; aux1 < agents.size(); aux1++) {
                    //		if (((ConnectExpert) agents.get(aux1)).getWire().getLastSegment().layer().equals(Layer.MET1)) {
                    //			agent = (ConnectExpert) agents.remove(aux1);
                    //			break;
                    //		}
                    //	}
                    //	if (agent == null)
                    agent = (ConnectExpert) agents.remove(0);
                    //}

                    //        If agent is not too costly run it
                    if (best == null || agent.cost() < best.getCost()) {
                        agentsLoopCount++;
                        Thread threadAgent = new Thread(connectPool, agent, "connect" + agentsLoopCount);
                        threadAgent.start();
                    }
                    agent = null;

                } while (flagSearch &&
                        ((best == null && bestTotal == null) || agentsLoopCount < MAX_AGENTS_LOOP_BEST) &&
                        (agentsLoopCount < MAX_AGENTS_LOOP) &&
                        (!agents.isEmpty() || connectPool.activeCount() > 0));

                //
                int num1 = connectPool.activeCount();
                if (num1 != 0) {

                    //	Kill the still active threads
                    Thread[] tr = new Thread[num1];
                    num1 = connectPool.enumerate(tr);
                    for (int aux1 = 0; aux1 < num1; aux1++)
                        tr[aux1].interrupt();
                }
                agents.clear();
                Dsp.println("CYCLE ENDED   Min:" + minCost + " Average: " + averageCost);

			/*-
			 *        Save the state back and compute any re-routing needed to
			 */
                if (best != null && (bestTotal == null || best.getCost() + UNWIRED_COST_RATIO * unwiredNodesRef.size() * averageCost < bestTotal.getCost() + UNWIRED_COST_RATIO * bestTotal.getUnwired().size() * averageCost)) {
                    if (bestTotal != null) {
                        Dsp.println(" OLD best: Cost: " + bestTotal.getCost() + " Num. Nodes: " + bestTotal.getUnwired().size() + "\n NEW best: Cost: " + best.getCost() + " Num. Nodes: " + unwiredNodesRef.size() + "\n DIFERENCE: " + bestTotal.getCost() + 3 * bestTotal.getUnwired().size() * minCost + "   " + best.getCost() + 3 * unwiredNodesRef.size() * minCost);
                    }
                    bestTotal = new BestWire(best, unwiredNodesRef); // Gets the list of nodes
                    best = null;
                    if (bestTotal.getCost() < UNWIRED_COST_RATIO * averageCost && bestTotal.getUnwired().size() < 3)
                        flagSearch = false;
                }

                //        Stops the search if the number of tries is too big
                if (bestTotal == null) {
                    if (loopCount > MAX_UNWIRE_LOOPS_WITHOUT_WIRE)
                        flagSearch = false;
                } else {
                    if (loopCount > MAX_UNWIRE_LOOPS_WITH_WIRE)
                        flagSearch = false;
                }

                //        Put the old nodes back
                saveNodes.restore(design);
            }
            Dsp.println("\n:: END  all NODE routing ::");

            //  Make flip if a wire was not find
            if (bestTotal == null && countFlips == 0) {
                Dsp.printOperator("::::::::::::::::: INVERTION :::::::::::::::::");
                int aux = net1;
                net1 = net2;
                net2 = aux;
                Rectangle envlp = envlp1;
                envlp1 = envlp2;
                envlp2 = envlp;
            }
        }
        if (bestTotal == null) {
            errorMessage = "Router:: Routing subnets: Can't find a connection between the two subnets";
            return false;
        }

	/*
	 	 *        Put the wire in the node
	 *        and unwire any necessary node
	 */
        node.getBody().add(0, new Wire());

        //        Take out the terminal wire copied into the wire
        bestTotal.getWire().remove(0);
        node.getBody().at(0).join(bestTotal.getWire());
        node.routingNets.lst(net1).add(0, node.getBody().get(0));
        node.joinRoutingNets(net1, bestTotal.getNet());

	/*
	 *        Unwire the necessary nodes and put them in order
	 *        after the current node.
	 */
        List unwireNodes = bestTotal.getUnwired();
        unwireNodes.invert();
        for (int aux1 = 0; aux1 < unwireNodes.size(); aux1++) {
            ElectricNode nodeSave = (ElectricNode) design.getByReference(((Integer) unwireNodes.get(aux1)).intValue());
            nodeSave.unwire();
            node.setRerouteLocked();

            //
            int aux2;
            for (aux2 = 0; aux2 < nodeRoutingQueue.size() && ((Integer) nodeRoutingQueue.get(aux2)).intValue() != nodeSave.reference; aux2++)
                ;
            nodeRoutingQueue.remove(aux2);
            currentRoutingNode--;
            nodeRoutingQueue.add(currentRoutingNode + 1, new Integer(nodeSave.reference));
        }
        return true;
    }

    void expert() {

        // Ref components
        design.refComponents();

        //    Init routing nets
        design.initRoutingNets();
        design.connectInSameLayer(Layer.NDIFF);
        design.connectInSameLayer(Layer.PDIFF);
        design.connectInSameLayer(Layer.POLY);
        if (!design.makeDifusionToMetal1()) {
            errorMessage = "Router:: General Connections: Can't make diffusion to metal1 connection.";
            design = null;
            return;
        }
        design.drawOut(Dsp.get2());
        //   designI.connectInSameLayer(MET1); antigo do C++

        //        Ordene the nodes by size the smaller first
        nodeRoutingQueue.clear();
        nodeRoutingAreas.clear();
        List nodes = design.getElectricNodes();
        for (int aux1 = 0; aux1 < nodes.size(); aux1++) {
            ElectricNode node = (ElectricNode) nodes.get(aux1);
            if (node.routingNets.size() == 1)
                continue;
            Rectangle rect = design.getRoutingNetsEnvelope(node.routingNets);
            int netArea = rect.getArea();
            //DRAW( "(rect " << rect.c1().x() << " " << rect.c1().y() << " " << rect.c2().x() << " " << rect.c2().y() << " 'black)" )
            int num1;
            for (num1 = 0; num1 < nodeRoutingAreas.size() && netArea >= ((Rectangle) nodeRoutingAreas.get(num1)).getArea(); num1++)
                ;
            nodeRoutingAreas.add(num1, rect);
            nodeRoutingQueue.add(num1, new Integer(node.reference));
        }

        //        Include the wired nodes in the beggining
        for (int aux1 = 0; aux1 < nodes.size(); aux1++) {
            ElectricNode node = (ElectricNode) nodes.get(aux1);
            if (node.routingNets.size() == 1) {
                Rectangle rect = design.getRoutingNetsEnvelope(node.routingNets);
                nodeRoutingAreas.add(0, rect);
                nodeRoutingQueue.add(0, new Integer(node.reference));
            }
        }
        for (currentRoutingNode = 0; currentRoutingNode < nodeRoutingQueue.size(); currentRoutingNode++) {
            int auxNode = ((Integer) nodeRoutingQueue.get(currentRoutingNode)).intValue();
            if (!connectNode(design, (ElectricNode) design.getByReference(auxNode))) {
                design = null;
                return;
            }
        }

        //        Put nwell around pmos
        makeWellSubstratContacts(design);
    }

    public DesignCmp getDesign() {
        return design;
    }

    static public void main(String[] args) throws IOException {

        boolean NO_SERVER = false;
        String rulesFile = null;
        String cirFile = null;

        //	Read args
        for (int aux1 = 0; aux1 < args.length; aux1++) {

            //	For file version
            if (args[aux1].equals("-noserver")) {
                NO_SERVER = true;
            } else if (args[aux1].equals("-rules")) {
                aux1++;
                rulesFile = args[aux1];
            } else if (args[aux1].equals("-circuit")) {
                aux1++;
                cirFile = args[aux1];
            } else if (args[aux1].equals("-port")) {
                aux1++;
                TCP_PORT = Integer.parseInt(args[aux1]);
            } else if (args[aux1].equals("-nthread")) {
                aux1++;
                MAX_CONNECT_THREADS = Integer.parseInt(args[aux1]);
            } else {
                System.out.println("Wrong argument for Router");
                return;
            }
        }

        //	If it is running file version
        if (NO_SERVER) {
            try {
                // Init the rules consultant
                Consultant.db = new Consultant(rulesFile);

                //    Get the layers names and create them.
                Layer.makeLayers(Consultant.db.getLayersNames());

                DesignCmp design = layout.placer.PlacerExpert.initDesign(cirFile);
                RouterExpert rp = new RouterExpert(design);
                rp.run();
                if (rp.getDesign() == null) {
                    System.out.println("NO DESIGN");
                } else {
                    System.out.println("DESIGN OK!");
                    rp.getDesign().drawOut(Dsp.get3());
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            return;
        }

        //	If it is running the server version
        Consultant.db = null;

        //	Opening the socket
        ServerSocket soc = new ServerSocket(TCP_PORT);

        //	Route circuits
        for (int aux1 = 0; true; aux1++) {
            Socket clientSoc = soc.accept();
            new Comm("Router:" + aux1, clientSoc).start();
        }
        //soc.close();
    }

    /**
     * Bad routine must be revised
     */
    static boolean makeWellSubstratContacts(DesignCmp design) {
        int aux1;
		/*
		 *  Make the contact from Nwell to VDD
		 */

        //    Find the envelop around the P fets
        Rectangle rec = null;
        for (aux1 = 0; aux1 < design.getFets().size(); aux1++) {
            if (!((Fet) design.getFets().get(aux1)).tecn.equals(Fet.PMOS)) continue;
            Component cmp = (Component) design.getFets().get(aux1);
            for (int aux2 = 0; aux2 < cmp.getBody().size(); aux2++) {
                Rectangle auxRec = cmp.getEnvelope(Layer.PDIFF);
                if (auxRec == null) continue;
                if (rec == null) rec = auxRec;
                if (rec.c1.x > auxRec.c1.x) rec.c1.x = auxRec.c1.x;
                if (rec.c1.y > auxRec.c1.y) rec.c1.y = auxRec.c1.y;
                if (rec.c2.x < auxRec.c2.x) rec.c2.x = auxRec.c2.x;
                if (rec.c2.y < auxRec.c2.y) rec.c2.y = auxRec.c2.y;
            }
        }

        //  Add the envelope of PDIFF areas in nodes
        for (aux1 = 0; aux1 < design.getElectricNodes().size(); aux1++) {
            Component cmp = (Component) design.getElectricNodes().get(aux1);
            for (int aux2 = 0; aux2 < cmp.getBody().size(); aux2++) {
                Rectangle auxRec = cmp.getEnvelope(Layer.PDIFF);
                if (auxRec == null) continue;
                if (rec.c1.x > auxRec.c1.x) rec.c1.x = auxRec.c1.x;
                if (rec.c1.y > auxRec.c1.y) rec.c1.y = auxRec.c1.y;
                if (rec.c2.x < auxRec.c2.x) rec.c2.x = auxRec.c2.x;
                if (rec.c2.y < auxRec.c2.y) rec.c2.y = auxRec.c2.y;
            }
        }

        //  Test if rec hasn't even lx or ly
        if (rec.getLx() % 2 > 0) rec.c2.x = rec.c2.x + 1;
        if (rec.getLy() % 2 > 0) rec.c2.y = rec.c2.y + 1;

        //  Create NWEEL
        Wire nwell = new Wire(rec, Layer.NWELL);
        nwell.at(1).setWidth(nwell.at(1).width() + 2 * Consultant.db.getMinOverlaping(Layer.NWELL, Layer.PDIFF));

        //  Find the VDD Pad
        for (aux1 = 0; aux1 < design.getPads().size() && !((Pad) design.getPads().get(aux1)).name.equals("VDD"); aux1++)
            ;
        Pad pad = (Pad) design.getPads().get(aux1);

        //    Make the contact from VDD pad to NWELL
        NoCrash[] paths = new NoCrash[1];
        Linea auxPointer = new Linea();
        Layer layerTo = Layer.NDIFF;
        Wire contactVDD = null;

        lpad1:
        for (aux1 = 0; aux1 < pad.getBody().size(); aux1++) {
            Wire wire = pad.getBody().at(aux1);
            for (int ind = 0; ind < wire.size(); ind++) {
                WirePt wirePt = wire.at(ind);
                if (!wirePt.layer().equals(Layer.MET1)) continue;
                paths[0] = new NoCrash(wire, ind);

                Segment auxLine = new Segment(wire.at(ind - 1), wirePt);
                for (double auxT = 0.2; auxT < 1.0; auxT = auxT + 0.1) {
                    auxPointer.c1.set(auxLine.getPointOfT(auxT));
                    auxPointer.setVector(new Pt(1, 0));

                    // To fix a bug
                    if (auxPointer.c1.x < rec.c1.x) continue;

                    contactVDD = design.changeLayer(paths, auxPointer, wire, ind, layerTo);
                    if (contactVDD != null) break lpad1;
                }
            }
        }
        if (contactVDD == null) return false;

        //    Find the VDD node
        for (aux1 = 0; aux1 < design.getElectricNodes().size() && !((Component) design.getElectricNodes().get(aux1)).name.equals("VDD"); aux1++)
            ;
        ElectricNode node = (ElectricNode) design.getElectricNodes().get(aux1);

        //        Add Contact Wire to VDD node
        node.getBody().add(contactVDD);

        //  Add a Ndiff stretch from change layer to NWELL to VDD node
        node.getBody().add(new Wire(
                contactVDD.getLastSegment(),
                new Pt(contactVDD.getLastSegment().x, nwell.at(0).y),
                Math.max(Consultant.db.getWireWidth(Layer.NWELL),
                        contactVDD.getLastSegment().width() + 2 * Consultant.db.getMinOverlaping(Layer.NWELL, layerTo)),
                Layer.NWELL));

        //  Add NWELL to VDD node
        node.getBody().add(nwell);

		/*
		 *        Make the substrate contact from VSS
		 */

        //  Find VSS pad
        for (aux1 = 0; aux1 < design.getPads().size() && !((Pad) design.getPads().get(aux1)).name.equals("VSS"); aux1++)
            ;
        Pad padVss = (Pad) design.getPads().get(aux1);

        //  Make contact from VSS to substrate
        layerTo = Layer.PDIFF;
        Wire contactVSS = null;

        lpad2:
        for (aux1 = 0; aux1 < padVss.getBody().size(); aux1++) {
            Wire wire = padVss.getBody().at(aux1);
            for (int ind = 0; ind < wire.size(); ind++) {
                WirePt wirePt = wire.at(ind);
                if (!wirePt.layer().equals(Layer.MET1)) continue;

                paths[0] = new NoCrash(wire, ind);
                Segment auxLine = new Segment(wire.at(ind - 1), wirePt);
                for (double auxT = 0.2; auxT < 1.0; auxT = auxT + 0.1) {
                    auxPointer.c1.set(auxLine.getPointOfT(auxT));
                    auxPointer.setVector(new Pt(1, 0));

                    contactVSS = design.changeLayer(paths, auxPointer, wire, ind, layerTo);
                    if (contactVSS != null) break lpad2;
                }
            }
        }
        if (contactVSS == null) return false;

        //        Find the VSS node
        for (aux1 = 0; aux1 < design.getElectricNodes().size() && !((ElectricNode) design.getElectricNodes().get(aux1)).name.equals("VSS"); aux1++)
            ;
        ElectricNode nodeVss = (ElectricNode) design.getElectricNodes().get(aux1);

        //        Add contact to VSS node
        nodeVss.getBody().add(contactVSS);
        return true;
    }

    /**
     * This method was created in VisualAge.
     */
    public void run() {
        System.out.println("Th begin:" + Thread.currentThread().getName());
        expert();
        if (design == null)
            System.out.println("DESIGN fail " + Thread.currentThread().getName());
        else {
            System.out.println("DESIGN OK! " + Thread.currentThread().getName());
            //design.drawOut(Dsp.getDisplay("Router"));
        }
    }

    synchronized void tryAsBestWire(int cost, Wire wire, int net) {
	/*-
	 *        best= [ cost (Int)  wire (Wire)  netConnected (Int)]
	 *        bestTotal= [ cost (Int)  wire (Wire)  netConnected (Int)  nodesToReroute (Lst<ElectricNode>)]
	 */
        //CHECK_ERR(cost==0, "RouterExpert:: tryAsBestWire: Cost 0");
        //CHECK_ERR(wire.empty(), "RouterExpert:: tryAsBestWire: Wire empty");
        //CHECK_ERR(net<0, "RouterExpert:: tryAsBestWire: Ilegal net (<0)");

        //	Test if wire loops
        //if (wire.isALoop()) {
        //	System.out.println("Wire Loops !!!");
        //	System.exit(0);
        //	return;
        //}

        if (best != null && cost >= best.getCost())
            return;
        best = new BestWire((Wire) wire.clone(), net, cost);

        //   if (cost < minCost*1.15) flagSearch= FALSE;
        if (cost < routerData.getMinCost())
            flagSearch = false;
        //routerData.bestCost = cost;
    }
}
