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

package layout.comp;

/*
 * DesignCmp
 */

import layout.display.Display;
import layout.display.Dsp;
import layout.util.*;

import java.io.IOException;
import java.io.Writer;

public class DesignCmp extends SVector {

    List fets;
    List pads;
    List electricNodes;

    //Careful designName is used only in load, equal doesn't test it
    public String designName;

    Rectangle designArea;

    public DesignCmp() {
    }

    /**
     * This method was created in VisualAge.
     */
    public void add(int ind, Object obj) {
        if (((Component) obj).reference == -1)
            throw new RuntimeException("Component with ref -1");
        super.add(ind, obj);
    }

    public boolean add(Object obj) {
        if (((Component) obj).reference == -1)
            throw new RuntimeException("Component with ref -1");
        return super.add(obj);
    }

    /**
     * This method was created in VisualAge.
     */
    public boolean addAll(int ind, java.util.Collection c) {
        throw new RuntimeException("Not implemented.");
    }

    /**
     * This method was created in VisualAge.
     */
    public boolean addAll(java.util.Collection c) {
        throw new RuntimeException("Not implemented.");
    }

    public Component at(int pos) {
        return (Component) get(pos);
    }

    public Wire changeLayer(NoCrash[] path, Linea pointer, Wire wire, int ind, Layer layerTo) {

        WirePt wirePt = wire.at(ind);
        Layer via = Consultant.db.getViaLayer(wirePt.layer(), layerTo);

        //    Test if the two layers are connectable
        if (wirePt.layer.equals(layerTo))
            throw new RuntimeException("The two layers are equal");
        //CHECK_ERR(!Consultant.areLayersConnectable(wirePt.layer(), layerTo),
        //          "DesignCmp:: changeLayer: The two layers aren't connectable");
        //CHECK_ERR(pointer.vectorX()==0 && pointer.vectorY()==0, "DesignCmp:: changeLayer: Pointer vector equal 0");

        //    If there is a via in wire

        //        Calculates the minimum margin form centre to the edge of the smallest
        //        layer around the via.
    /*
	margin= min((Consultant.getMinWidth(via)/2 + 
	   Consultant.getMinOverlaping(wirePt.layer(), via) + 
	   Consultant.getWireWidth(wirePt.layer())/2),
	  (Consultant.getMinWidth(via)/2 + 
	   Consultant.getMinOverlaping(layerTo, via) + 
	   Consultant.getWireWidth(layerTo)/2));
	*/
        int margin = Math.min((Consultant.db.getMinWidth(via) + Consultant.db.getMinOverlaping(wirePt.layer(), via) * 2 + Consultant.db.getWireWidth(wirePt.layer())), (Consultant.db.getMinWidth(via) + Consultant.db.getMinOverlaping(layerTo, via) * 2 + Consultant.db.getWireWidth(layerTo)));

        //    Calculates the width of all the layers involved
        int widthVia = Consultant.db.getMinWidth(via);
        int widthL1 = widthVia + 2 * Consultant.db.getMinOverlaping(wirePt.layer(), via);
        int widthL2 = widthVia + 2 * Consultant.db.getMinOverlaping(layerTo, via);

        //    Calculates a big rectangle that envelops all the possible vias
        int maxWidthVia = margin * 2 + widthVia + 1;
        int maxWidthL1 = margin * 2 + widthL1 + 1;
        int maxWidthL2 = margin * 2 + widthL2 + 1;
        Pt auxPt = new Pt();
        auxPt.set(pointer.c1);
        //Dsp.get2().addRect(new Rectangle(auxPt.x-maxWidthL1/2, auxPt.y-maxWidthL1/2, auxPt.x+maxWidthL1/2, auxPt.y+maxWidthL1/2), java.awt.Color.yellow);
        //Dsp.get2().addRect(new Rectangle(auxPt.x-maxWidthL2/2, auxPt.y-maxWidthL2/2, auxPt.x+maxWidthL2/2, auxPt.y+maxWidthL2/2), java.awt.Color.green);

	/*    Get the list of layers crashed by the big rectangle, the actual
	 *    connection layers will be tested against this list only.
	 */
        Wire auxWire = new Wire();
        auxWire.add(auxPt, 0, Layer.EMPTY);
        auxWire.add(auxPt, maxWidthL1, wirePt.layer());
        auxWire.add(auxPt, maxWidthVia, via);
        auxWire.add(auxPt, maxWidthL2, layerTo);

        //    DRAW( auxWire1 )

        List lstCrash = getCrashes(path, auxWire);

        //    Try the inicial point (without any shift)
        auxWire.at(1).width = widthL1;
        auxWire.at(2).width = widthVia;
        auxWire.at(3).width = widthL2;
        boolean flagOk = !isCrashing(lstCrash, path, auxWire);
        if (flagOk) {
            Dsp.get2().addWire(auxWire);
            return auxWire;
        }
        Wire auxWire1 = auxWire;
        auxWire = new Wire();

        //    If it didn't got it do the shifting in the same line of the wire

	/*
	 *    Calculates the vector that will guide the shifting
	 *    If this vector is 0 make it equal to -->
	 */
        Linea auxPointer = new Linea();
        auxPointer.set(pointer);
        Pt lastPt = new Pt();
        lastPt.set(auxPt);
        for (int aux1 = 0; aux1 < 2 && !flagOk; aux1++) {
            auxPointer.rotateVector(-1, 0);
            for (float aux2 = (float) 0.25; aux2 <= 1 && !flagOk; aux2 = aux2 + (float) 0.25) {
                auxPt = auxPointer.getPointOfT(Math.floor(margin * aux2));
                auxWire1.translate(auxPt.x - lastPt.x, auxPt.y - lastPt.y);
                flagOk = !isCrashing(lstCrash, path, auxWire1);
                if (flagOk) {
                    auxWire = getConnectionFromSegmentWireToPoint(wire, ind, auxWire1.at(1));
                    if (isCrashing(lstCrash, path, auxWire))
                        flagOk = false;
                    else
                        auxWire.join(auxWire1);
                }
                lastPt.set(auxPt);
                Dsp.get2().addRect(new Rectangle(auxPt.x - widthL1 / 2, auxPt.y - widthL1 / 2, auxPt.x + widthL1 / 2, auxPt.y + widthL1 / 2), java.awt.Color.cyan);
            }
        }
        if (flagOk) {
            Dsp.get2().addWire(auxWire);
            return auxWire;
        }

        //    If didn't got it do the shifting perpendicular to the wire

	/*
	 *    To get the crossing position shift it to the edge of the wire if
	 *    the pointer isn't at the midle.
	 */
        //Segment seg(wire[ind-1].pt(), wirePt.pt());
        //seg.centre(auxPt);
        //if (auxPt!=pointer.c1()) {
        //    pointer.vector(auxPt);
        //    pointer.coordT( -(Consultant.getMinWidth(via)/2 +
        //                    Consultant.getMinOverlaping(wirePt.layer(), via)) ,auxPointer.c1());
        //    auxPointer.setVector(auxPt);
        //} else
        auxPointer.set(pointer);
        auxPt.set(pointer.c1);
        for (int aux1 = 0; aux1 < 2 && !flagOk; aux1++) {
            switch (aux1) {
                case 0:
                    auxPointer.rotateVector(0, 1);
                    break;
                case 1:
                    auxPointer.rotateVector(-1, 0);
            }
            for (float aux2 = (float) 0.25; aux2 <= 1 && !flagOk; aux2 = aux2 + (float) 0.25) {
                auxPt = auxPointer.getPointOfT(Math.floor(margin * aux2));
                auxWire1.translate(auxPt.x - lastPt.x, auxPt.y - lastPt.y);
                flagOk = !isCrashing(lstCrash, path, auxWire1);
                if (flagOk) {
                    auxWire = getConnectionFromSegmentWireToPoint(wire, ind, auxWire1.at(1));
                    if (isCrashing(lstCrash, path, auxWire))
                        flagOk = false;
                    else
                        auxWire.join(auxWire1);
                }
                lastPt.set(auxPt);
                Dsp.get2().addRect(new Rectangle(auxPt.x - widthL1 / 2, auxPt.y - widthL1 / 2, auxPt.x + widthL1 / 2, auxPt.y + widthL1 / 2), java.awt.Color.cyan);
            }
        }
        if (flagOk) {
            Dsp.get2().addWire(auxWire);
            return auxWire;
        }
        return null;
    }

    /**
     * This method was created in VisualAge.
     */
    public synchronized Object clone() {

        //	Create new empty Design
        DesignCmp design = new DesignCmp();
        design.designName = designName;
        design.designArea = new Rectangle(designArea);

        //	Copy all cloneable comp
        for (int aux1 = 0; aux1 < size(); aux1++) {
            Component cmp = at(aux1);
            if (cmp instanceof ElectricNode)
                continue;
            design.add(cmp.clone());
        }

        //	Copy all electricnodes
        for (int aux1 = 0; aux1 < size(); aux1++) {
            Component cmp = at(aux1);
            if (cmp instanceof ElectricNode)
                design.add(((ElectricNode) cmp).cloneInDesign(design));
        }

        //	Ref components
        design.refComponents();
        return design;
    }

    public void connectInSameLayer(Layer layer) {
        int width = Consultant.db.getWireWidth(layer);
        boolean flagConnect = false;
        for (int aux1 = 0; aux1 < electricNodes.size(); aux1++) {

            //    Tries only the unconnected nets
            ElectricNode node = (ElectricNode) getElectricNodes().get(aux1);
            List nets = node.routingNets;
            if (nets.size() <= 1)
                continue;
            flagConnect = false;
            for (int aux2 = 0; aux2 < nets.size(); ) {
			/*-
			 *    If last was no connection take the one after aux2
			 *    aux2 will point to the first one with figs of the layer (from auxNext)
			 *    If it was a connection take the first one with figs of tha layer
			 *    pointed by auxNext
			 */
                int aux3 = aux2;
                aux3++;
                flagConnect = false;
                for (; !flagConnect && aux3 < nets.size(); ) {
                    List net1 = (List) nets.get(aux2);
                    List net2 = (List) nets.get(aux3);
                    for (int aux4 = 0; aux4 < net1.size() && !flagConnect; aux4++) {
                        Wire wire1 = (Wire) net1.get(aux4);
                        Segment seg = new Segment();
                        NoCrash[] path = new NoCrash[2];
                        Rectangle ele1 = new Rectangle();
                        Rectangle ele2 = new Rectangle();
                        for (int ind1 = 1; ind1 < wire1.size() && !flagConnect; ind1++) {
                            if (!wire1.at(ind1).layer().equals(layer))
                                continue;
                            ele1.set(wire1.at(ind1 - 1), wire1.at(ind1), wire1.at(ind1).width());
                            seg.c1 = ele1.getCenter();
                            path[1] = new NoCrash(wire1, ele1, wire1.at(ind1).layer());
                            path[0] = null; //dd(0, new Integer(-1));

                            for (int aux5 = 0; aux5 < net2.size() && !flagConnect; aux5++) {
                                Wire wire2 = (Wire) net2.get(aux5);
                                for (int ind2 = 1; ind2 < wire2.size() && !flagConnect; ind2++) {
                                    if (!wire2.at(ind2).layer().equals(layer))
                                        continue;
                                    ele2.set(wire2.at(ind2 - 1), wire2.at(ind2), wire2.at(ind2).width());
                                    seg.c2 = ele2.getCenter();
                                    path[0] = new NoCrash(wire2, ele2, wire2.at(ind2).layer());

                                    //DRAW( "(vect " << seg.c1().x() << " " << seg.c1().y() << " " <<
                                    //      seg.c2().x() << " " <<seg.c2().y() << " 'orange)" )

                                    if ((ele1.isVertical() || !ele1.isHorizontalAligned(ele2)) && ele1.isVerticalAligned(ele2)) {
                                        //    Find the average
                                        seg.c1.x = Math.max(ele1.c1.x, ele2.c1.x);
                                        seg.c2.x = seg.c1.x + (Math.min(ele1.c2.x, ele2.c2.x) - seg.c1.x) / 2;
                                        seg.c1.x = seg.c2.x;
                                        //    Chose the two extremes for the wire
                                        int min1 = Math.min(wire1.at(ind1 - 1).y, wire1.at(ind1).y);
                                        int min2 = Math.min(wire2.at(ind2 - 1).y, wire2.at(ind2).y);
                                        int max1 = Math.max(wire1.at(ind1 - 1).y, wire1.at(ind1).y);
                                        int max2 = Math.max(wire2.at(ind2 - 1).y, wire2.at(ind2).y);
                                        seg.c1.y = Math.min(min1, min2);
                                        seg.c2.y = Math.max(max1, max2);
                                    } else if (ele1.isHorizontalAligned(ele2)) {

                                        //    Find the average
                                        seg.c1.y = Math.max(ele1.c1.y, ele2.c1.y);
                                        seg.c2.y = seg.c1.y + (Math.min(ele1.c2.y, ele2.c2.y) - seg.c1.y) / 2;
                                        seg.c1.y = seg.c2.y;

                                        //    Chose the two extremes for the wire
                                        int min1 = Math.min(wire1.at(ind1 - 1).x, wire1.at(ind1).x);
                                        int min2 = Math.min(wire2.at(ind2 - 1).x, wire2.at(ind2).x);
                                        int max1 = Math.max(wire1.at(ind1 - 1).x, wire1.at(ind1).x);
                                        int max2 = Math.max(wire2.at(ind2 - 1).x, wire2.at(ind2).x);
                                        seg.c1.x = Math.min(min1, min2);
                                        seg.c2.x = Math.max(max1, max2);
                                    } else
                                        continue;
                                    Wire auxWire = new Wire();
                                    auxWire.add(seg.c1, 0, Layer.EMPTY);
                                    auxWire.add(seg.c2, width, layer);
                                    if (!isCrashing(path, auxWire)) {
                                        //DRAW(auxWire)
                                        //if (wire1.canJoin(auxWire)) wire1.join(auxWire);
                                        //else
                                        // if (wire2.canJoin(auxWire)) wire2.join(auxWire);
                                        // else {
                                        node.getBody().add(0, auxWire.clone());
                                        nets.lst(aux2).add(0, node.getBody().get(0));

                                        //    If auxNext is equal to aux3 point it to the next
                                        node.joinRoutingNets(aux2, aux3);
                                        flagConnect = true;
                                    }
                                }
                            }
                        }
                    }
                    if (!flagConnect)
                        aux3++;
                }

                //    If connected aux2 won't move to allow the list being rescanned
                if (!flagConnect)
                    aux2++; //aux2= auxNext;
            }
        }
    }

    public void drawOut(Display out) {
        for (int aux1 = 0; aux1 < size(); aux1++) {
            at(aux1).drawOut(out);
        }
        //out.addRect(designArea, Color.pink);
    }

    public Component getByReference(int ref) {
        for (int aux1 = 0; aux1 < this.size(); aux1++) {
            if (this.at(aux1).reference == ref)
                return this.at(aux1);
        }
        throw new RuntimeException("Can't find component in the list");
    }

    public WiringPoint getClosestWiringPoints(List l1, List l2) {
        if (l1.isEmpty())
            throw new RuntimeException("L1 net is empty");
        if (l2.isEmpty())
            throw new RuntimeException("L2 net is empty");
        WiringPoint wp = null;
        for (int aux1 = 0; aux1 < l1.size(); aux1++) {
            Wire wire1 = (Wire) l1.get(aux1);
            for (int aux2 = 0; aux2 < l2.size(); aux2++) {
                Wire wire2 = (Wire) l2.get(aux2);
                WiringPoint auxWp = wire1.getClosestWiringPoints(wire2);
                if ((auxWp != null) && (wp == null || wp.dist >= auxWp.dist)) {
                    wp = auxWp;
                    wp.wire1 = aux1;
                    wp.wire2 = aux2;
                }
            }
        }
        return wp;
	/*
	IF_DEBUG_ON(
	if (smallDist<0) {
	DRAW("(clearCanvas)")
	for(long aux=0; aux<l1.number(); aux++) {
	DRAW(RTYPE(Wire, l1[aux]))
	}
	for(aux=0; aux<l2.number(); aux++){
	DRAW(RTYPE(Wire, l2[aux]))
	}
	cout << "{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{      ERROR        }}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}" << endl;
	getchar();
	Component& cmp= Component::findComponentByWire(RTYPE(Wire, l1[0]));
	DRAW(cmp)
	}
	)
	////   CHECK_ERR(smallDist==0, "DesignCmp:: getClosestPoint: The two nets touch each other");
	//CHECK_ERR(smallDist<0, "DesignCmp:: getClosestPoint: The two nets can't be wired");
	////CHECK if really needed
	*/
    }

    Wire getConnectionFromSegmentWireToPoint(Wire wire, int ind, Pt pt) {

        Wire auxWire = new Wire();
        Linea line1 = new Linea(wire.at(ind - 1), wire.at(ind));

        //    If wire segmente isn't a segment
        if (line1.c1.equals(line1.c2)) {
            Layer layer = wire.at(ind).layer();
            int width = Consultant.db.getWireWidth(layer);
            auxWire.add(line1.c1, 0, Layer.EMPTY);
            if (pt.x != line1.c1.x)
                auxWire.add(new Pt(pt.x, line1.c1.y), width, layer);
            if (pt.y != line1.c1.y)
                auxWire.add(pt, width, layer);
        } else {

            //    If wire segment is a segment
            Linea line2 = new Linea();
            line2.c1.set(pt);
            line2.setVector(line1);
            line2.rotateVector(0, 1);
            InterPt auxT = line1.getIntersectPt(line2);
            line2.c2 = line1.getPointOfT(auxT.getT());
            if (auxT.getT() < 0) {
                Layer layer = wire.at(ind).layer();
                int width = Consultant.db.getWireWidth(layer);
                auxWire.add(line1.c1, 0, Layer.EMPTY);
                if (!line2.c1.equals(line2.c2))
                    auxWire.add(line2.c2, width, layer);
                auxWire.add(pt, width, layer);
            } else if (auxT.getT() > 1) {
                Layer layer = wire.at(ind).layer();
                int width = Consultant.db.getWireWidth(layer);
                auxWire.add(line1.c2, 0, Layer.EMPTY);
                if (!line2.c1.equals(line2.c2))
                    auxWire.add(line2.c2, width, layer);
                auxWire.add(pt, width, layer);
            } else {
                if (!line2.c1.equals(line2.c2)) {
                    Layer layer = wire.at(ind).layer();
                    int width = Consultant.db.getWireWidth(layer);
                    auxWire.add(line2.c2, 0, Layer.EMPTY);
                    auxWire.add(pt, width, layer);
                }
            }
        }
        return auxWire;
    }

    public List getCrashes(NoCrash[] nets, Wire wire) {
        List lstCrash = Gen.newList();
        for (int aux1 = 0; aux1 < size(); aux1++)
            for (int aux2 = 0; aux2 < at(aux1).getBody().size(); aux2++) {
                Wire wireTo = at(aux1).getBody().at(aux2);
                lstCrash.addAll(wireTo.getCrashes(nets, wire));
            }
        return lstCrash;
    }

    public List getCrashesPointer(NoCrash[] nets, Linea vect, Layer layer, int width) {

        //IF_DEBUG
        Dsp.get2().addVector(vect, java.awt.Color.orange);
        List lstCrash = Gen.newList();
        for (int aux1 = 0; aux1 < size(); aux1++) {
            for (int aux2 = 0; aux2 < at(aux1).getBody().size(); aux2++) {
                Wire wireTo = at(aux1).getBody().at(aux2);
                List auxCrash = wireTo.getCrashesPointer(nets, vect, layer, width);

                //    Join the two listing keeping the corect order of elements
                joinCrashLists(lstCrash, auxCrash);
            }
        }
        return lstCrash;
    }

    public Rectangle getDesignArea() {
        return new Rectangle(designArea);
    }

    public List getElectricNodes() {
        return electricNodes;
    }

    public Rectangle getEnvelope() {
        if (isEmpty())
            return null;
        Rectangle rec = null;
        for (int aux1 = 0; aux1 < size(); aux1++) {
            Rectangle auxRec = at(aux1).getEnvelope();
            if (auxRec == null)
                continue;
            if (rec == null) {
                rec = auxRec;
                continue;
            }
            if (rec.c1.x > auxRec.c1.x)
                rec.c1.x = auxRec.c1.x;
            if (rec.c1.y > auxRec.c1.y)
                rec.c1.y = auxRec.c1.y;
            if (rec.c2.x < auxRec.c2.x)
                rec.c2.x = auxRec.c2.x;
            if (rec.c2.y < auxRec.c2.y)
                rec.c2.y = auxRec.c2.y;
        }
        return rec;
    }

    public int getEstimatedCrashes(Wire wire) {
        int numCrash = 0;
        for (int aux1 = 0; aux1 < size(); aux1++)
            for (int aux2 = 0; aux2 < at(aux1).getBody().size(); aux2++) {
                int aux3 = at(aux1).getBody().at(aux2).getEstimatedCrashes(wire);
                numCrash = numCrash + aux3;
            }
        return numCrash;
    }

    public List getFets() {
        return fets;
    }

    public Wire getFreeWire(int length, Layer layer1, Layer layer2) {

        //    Finds the direct connection between two points
        Wire auxWire = new Wire();
        auxWire.add(new Pt(0, 0), 0, Layer.EMPTY);

        //    For the diffusion
        if (layer1.equals(Layer.PDIFF) || layer1.equals(Layer.NDIFF) || layer2.equals(Layer.PDIFF) || layer2.equals(Layer.NDIFF)) {
            Wire auxWire2 = Consultant.db.getWireToInterconnect(layer1, Layer.MET1);
            auxWire.join(auxWire2);
            auxWire.add(new Pt(length, 0), Consultant.db.getWireWidth(Layer.MET1), Layer.MET1);
            auxWire2 = Consultant.db.getWireToInterconnect(Layer.MET1, layer2);
            auxWire2.translate(length, 0);
            auxWire.join(auxWire2);
            return auxWire;
        }

        //    For the equal layers
        if (layer1.equals(layer2)) {
            auxWire.add(new Pt(length, 0), Consultant.db.getWireWidth(layer1), layer1);
            return auxWire;
        }

        //    For the different layers
        Wire auxWire2 = Consultant.db.getWireToInterconnect(layer1, layer2);
        if (Consultant.db.getLayerCost(layer1) <= Consultant.db.getLayerCost(layer2)) {
            auxWire.add(new Pt(length, 0), Consultant.db.getWireWidth(layer1), layer1);
            auxWire2.translate(length, 0);
            auxWire.join(auxWire2);
        } else {
            auxWire.join(auxWire2);
            auxWire.add(new Pt(length, 0), Consultant.db.getWireWidth(layer1), layer1);
        }
        return auxWire;
    }

    //    Maybe they should be in the router
    public Wire getFreeWire(Pt targetPt, WirePt wpt1, WirePt wpt2) {

        //    Finds the direct connection between two points
        Wire auxWire = new Wire();
        auxWire.add(wpt1, 0, Layer.EMPTY);

        //    If layer1 == layer2 and it's difusion and a big line or not straight, then goto MET1
        if (wpt1.layer().equals(Layer.PDIFF) || wpt1.layer().equals(Layer.NDIFF) || wpt2.layer().equals(Layer.PDIFF) || wpt2.layer().equals(Layer.NDIFF)) {
            if (wpt1.layer().equals(wpt2.layer()) && wpt1.width() * 20 > wpt1.getManhattanDistance(targetPt) && (wpt1.x == targetPt.x || wpt1.y == targetPt.y)) {

                //    Make a straight line
                auxWire.add(new WirePt(targetPt, Consultant.db.getWireWidth(wpt1.layer()), wpt1.layer()));
            } else {
                if (!wpt1.layer().equals(Layer.MET1)) {
                    Wire auxWire2 = Consultant.db.getWireToInterconnect(wpt1.layer(), Layer.MET1);
                    auxWire2.translate(wpt1.x, wpt1.y);
                    auxWire.join(auxWire2);
                }
                if (wpt1.x == targetPt.x || wpt1.y == targetPt.y) {
                    auxWire.add(targetPt, Consultant.db.getWireWidth(Layer.MET1), Layer.MET1);
                } else {
                    auxWire.add(new Pt(targetPt.x, wpt1.y), Consultant.db.getWireWidth(Layer.MET1), Layer.MET1);
                    auxWire.add(targetPt, Consultant.db.getWireWidth(Layer.MET1), Layer.MET1);
                }
                if (!wpt2.layer().equals(Layer.MET1)) {
                    Wire auxWire2 = Consultant.db.getWireToInterconnect(Layer.MET1, wpt2.layer());
                    auxWire2.translate(targetPt.x, targetPt.y);
                    auxWire.join(auxWire2);
                }
            }
            return auxWire;
        }

        //    For the other layers
        if (wpt1.x == targetPt.x || wpt1.y == targetPt.y) {
            auxWire.add(targetPt, Consultant.db.getWireWidth(wpt1.layer()), wpt1.layer());
        } else {
            auxWire.add(new Pt(targetPt.x, wpt1.y), Consultant.db.getWireWidth(wpt1.layer()), wpt1.layer());
            auxWire.add(targetPt, Consultant.db.getWireWidth(wpt1.layer()), wpt1.layer());
        }
        if (!wpt1.layer().equals(wpt2.layer())) {
            Wire auxWire2 = Consultant.db.getWireToInterconnect(wpt1.layer(), wpt2.layer());
            auxWire2.translate(targetPt.x, targetPt.y);
            auxWire.join(auxWire2);
        }
        return auxWire;
    }

    public static Rectangle getNetEnvelope(List lst) {
        if (lst.isEmpty())
            throw new RuntimeException("Net list is empty.");
        Rectangle rec = ((Wire) lst.get(0)).getEnvelope();
        for (int aux1 = 1; aux1 < lst.size(); aux1++) {
            Rectangle auxRec = ((Wire) lst.get(aux1)).getEnvelope();
            if (rec.c1.x > auxRec.c1.x)
                rec.c1.x = auxRec.c1.x;
            if (rec.c1.y > auxRec.c1.y)
                rec.c1.y = auxRec.c1.y;
            if (rec.c2.x < auxRec.c2.x)
                rec.c2.x = auxRec.c2.x;
            if (rec.c2.y < auxRec.c2.y)
                rec.c2.y = auxRec.c2.y;
        }
        return rec;
    }

    public List getPads() {
        return pads;
    }

    public static Rectangle getRoutingNetsEnvelope(List lst) {
        if (lst.isEmpty())
            throw new RuntimeException("RoutingNets is empty.");
        Rectangle rec = getNetEnvelope(lst.lst(0));
        for (int aux1 = 1; aux1 < lst.size(); aux1++) {
            Rectangle auxRec = getNetEnvelope(lst.lst(aux1));
            if (rec.c1.x > auxRec.c1.x)
                rec.c1.x = auxRec.c1.x;
            if (rec.c1.y > auxRec.c1.y)
                rec.c1.y = auxRec.c1.y;
            if (rec.c2.x < auxRec.c2.x)
                rec.c2.x = auxRec.c2.x;
            if (rec.c2.y < auxRec.c2.y)
                rec.c2.y = auxRec.c2.y;
        }
        return rec;
    }

    //    Maybe it should be in the Consultant
    public int getWireCost(Wire wire) {
        Rectangle rec = new Rectangle();
        int cost = 0;
        for (int aux1 = 1, last = 0; aux1 < wire.size(); aux1++, last++) {
            rec.set(wire.at(last), wire.at(aux1), wire.at(aux1).width());
            cost = cost + rec.getArea() * Consultant.db.getLayerCost(wire.at(aux1).layer());
        }
        return cost;
    }

    public List getWiringLayersTouchesPointer(List lstNets, Linea vect, int width) {
	/*-
	 *    Takes all the wires segments in lstNets that touch ArrayList
	 */
        List lstTouch = Gen.newList();
        for (int net = 0; net < lstNets.size(); net++) {
            List lstRec = (List) lstNets.get(net);
            for (int aux1 = 0; aux1 < lstRec.size(); aux1++) {
                Wire wireTo = (Wire) lstRec.get(aux1);
                List auxTouch = wireTo.getWiringLayersTouchesPointer(lstRec, vect, width);

                //    Join the two listing keeping the corect order of elements
                joinCrashLists(lstTouch, auxTouch);
            }
        }
        return lstTouch;
    }

    public synchronized void initRoutingNets() {
        for (int aux1 = 0; aux1 < electricNodes.size(); aux1++) {
            ((ElectricNode) electricNodes.get(aux1)).initRoutingNets(this);
        }
    }

    public boolean isCrashing(NoCrash[] nets, Component cmp) {
        for (int aux1 = 0; aux1 < cmp.getBody().size(); aux1++) {
            if (isCrashing(nets, cmp.getBody().at(aux1)))
                return true;
        }
        return false;
    }

    public boolean isCrashing(NoCrash[] nets, Wire wire) {
        for (int aux1 = 0; aux1 < this.size(); aux1++) {
            for (int aux2 = 0; aux2 < this.at(aux1).getBody().size(); aux2++) {
                if (this.at(aux1).getBody().at(aux2).isCrashing(nets, wire))
                    return true;
            }
        }
        return false;
    }

    public boolean isCrashing(List lstCrash, NoCrash[] nets, Wire wire) {
        for (int aux1 = 0; aux1 < lstCrash.size(); aux1++) {
            Crash crash = (Crash) lstCrash.get(aux1);
            if (crash.getWire().isCrashing(nets, wire, crash.getInd()))
                return true;
        }
        return false;
    }

    public boolean isInDesignBoundaries(Component cmp) {
        for (int aux1 = 0; aux1 < cmp.getBody().size(); aux1++) {
            if (isInDesignBoundaries(cmp.getBody().at(aux1)))
                return true;
        }
        return false;
    }

    public boolean isInDesignBoundaries(Wire wire) {
        Rectangle rec = new Rectangle();
        Rectangle cell = getDesignArea();
        for (int aux1 = 1, last = 0; aux1 < wire.size(); aux1++, last++) {
            rec.set(wire.at(last), wire.at(aux1), wire.at(aux1).width());
            if (!cell.isInside(rec))
                return false;
        }
        return true;
    }

    /**
     * Join the two listing keeping the corect order of elements
     */
    static void joinCrashLists(List lst, List aux) {
        int aux3 = 0;
        while (!aux.isEmpty()) {
            int dist1 = ((Crash) aux.get(0)).getDistance();
            for (; aux3 < lst.size() && dist1 >= ((Crash) lst.get(aux3)).getDistance(); aux3++) ;
            if (aux3 >= lst.size()) {
                lst.addAll(aux);
                aux.clear();
            } else {
                lst.add(aux3, aux.remove(0));
                aux3++;
            }
        }
    }

    public Wire makeConnection(NoCrash[] path, Linea pointer, Wire directWire, List lstTouch) {
	/*-
	 *    The 2 worst possible cases when a connection is aproached:
	 *
	 * _____________________________    _____________________________
	 * *Target************|*+*+*+*+|    *Target*********************|
	 * *******************|+*+*+*+*|    ****************************|
	 * *******************|*+*+*+*+|    ****************************|
	 * -------------------|--------      OR    -------------------|--------
	 *                    |++++++++|                       |+*+*+*++|
	 *                    |++++++++|                       |*+*+*+*+|
	 *                    |++++++++|                       |--------|
	 *                    |++++++++|                       |++++++++|
	 *                    |++++++++|                       |++++++++|
	 *                    |Pointer+|                       |Pointer+|
	 */

        //   CHECK_ERR(pointer.vectorX()   TEST IF pointer is unitary
        //
        // TAKE OUT firstSegment length

        Wire wire2 = ((Crash) lstTouch.get(0)).getWire();
        int touch = ((Crash) lstTouch.get(0)).getInd();
        WirePt ele = directWire.at(1);
        WirePt eleTo = wire2.at(touch);
        int width = Consultant.db.getMinWidth(ele.layer());
        Segment seg = new Segment(directWire.at(0), directWire.at(1));
        Wire newWire = new Wire();
        newWire.add(directWire.at(0).clone());
        newWire.add(directWire.at(1).clone());
        if (eleTo.layer().equals(ele.layer())) {
            if (!isCrashing(path, newWire)) {
                Pt auxPt = pointer.getVector();
                pointer.c1.set(seg.c2);
                pointer.setVector(auxPt);
                //DRAW(newWire)
                return newWire;
            } else {
                newWire.clear();
                return newWire;
            }
        }

        // MAYBE CHANGE THIS IN THE FUTURE
        if (!eleTo.layer().equals(ele.layer()) && !Consultant.db.areLayersConnectable(ele.layer(), eleTo.layer())) {
            newWire.clear();
            return newWire;
        }

        //IF_DEBUG_ON(
        Dsp.get2().addRect(new Rectangle(newWire.at(0), newWire.at(1), newWire.at(1).width()), java.awt.Color.green);
        Linea auxPtr = new Linea();
        List lstCrash = getCrashes(path, newWire);
        if (lstCrash.isEmpty()) {
            auxPtr.c1.set(seg.c2);
            auxPtr.setVector(pointer);
            Wire auxWire = changeLayer(path, auxPtr, newWire, newWire.size() - 1, eleTo.layer());
            if (auxWire != null) {
                newWire.join(auxWire);

                //    Connect the wire to the change via
                auxWire = getConnectionFromSegmentWireToPoint(wire2, touch, newWire.at(newWire.size() - 1));
                if (auxWire.isEmpty())
                    return newWire;
                if (!isCrashing(path, auxWire)) {
                    auxWire.invert();
                    newWire.join(auxWire);
                    //DRAW(newWire)
                    return newWire;
                }
                newWire.clear();
                return newWire;
            }
        }

        //    Try to change the length of the wire
        double widthT = (width * 1.0) / seg.getLength();
        double step = widthT;
        int numberOfTries = 4;
        if (numberOfTries * step < 1 && !isCrashing(lstCrash, path, newWire)) {
            newWire.at(1).set(seg.getPointOfT(1 - step));
            for (int aux1 = 0; aux1 < numberOfTries && 1 - step > 0; aux1++) {
                newWire.at(1).set(seg.getPointOfT(1 - step));
                if (!isCrashing(lstCrash, path, newWire)) {
                    auxPtr.c1.set(seg.c2);
                    auxPtr.setVector(pointer);
                    Wire auxWire = changeLayer(path, auxPtr, newWire, newWire.size() - 1, eleTo.layer());
                    if (auxWire != null) {
                        newWire.join(auxWire);

                        //    Connect the wire to the change via
                        auxWire = getConnectionFromSegmentWireToPoint(wire2, touch, newWire.at(newWire.size() - 1));
                        if (auxWire.isEmpty())
                            return newWire;
                        if (!isCrashing(path, auxWire)) {
                            auxWire.invert();
                            newWire.join(auxWire);
                            //DRAW(newWire);
                            return newWire;
                        }
                        newWire.clear();
                        return newWire;
                    }
                }
                step = step + widthT;
            }
        }
        newWire.clear();
        return newWire;
    }

    public boolean makeDifusionToMetal1() {

        boolean flagOk = true;
        for (int aux1 = 0; aux1 < getElectricNodes().size(); aux1++) {
            ElectricNode node = (ElectricNode) getElectricNodes().get(aux1);

            //    Tries only the unconnected nets
            if (node.routingNets.size() <= 1)
                continue;
            for (int aux2 = 0; aux2 < node.routingNets.size(); aux2++) {
                List net = (List) node.routingNets.get(aux2);

                //    Go on if there are only diffusion figs in the net
                //SHOULD BE REVISED TO DETECT NO CONNECTION TO MET1 POLY or MET2
                flagOk = true;
                for (int aux3 = 0; aux3 < net.size() && flagOk; aux3++) {
                    Wire w1 = (Wire) net.get(aux3);
                    for (int aux4 = 1; aux4 < w1.size() && flagOk; aux4++) {
                        Layer lay = w1.at(aux4).layer();
                        if (!lay.equals(Layer.NDIFF) && !lay.equals(Layer.PDIFF))
                            flagOk = false;
                    }
                }
                if (!flagOk)
                    continue;
                flagOk = false;
                Layer layerTo = Layer.MET1;
                for (int aux3 = 0; aux3 < net.size() && !flagOk; aux3++) {
                    Wire wire = (Wire) net.get(aux3);
                    Linea auxPointer = new Linea();
                    Segment seg = new Segment();
                    NoCrash[] paths = new NoCrash[1];
                    for (int aux4 = 1; !flagOk && aux4 < wire.size(); aux4++) {
                        paths[0] = new NoCrash(wire, aux4);
                        seg.c1 = wire.at(aux4 - 1);
                        seg.c2 = wire.at(aux4);
                        auxPointer.c1 = seg.getCenter();
                        if (seg.c1 == seg.c2 || seg.isHorizontal())
                            auxPointer.setVector(new Pt(1, 0));
                        else
                            auxPointer.setVector(new Pt(0, 1));
                        Wire newWire = changeLayer(paths, auxPointer, wire, aux4, layerTo);
                        if (newWire != null) {
                            node.getBody().add(0, newWire);
                            net.add(0, newWire);
                            flagOk = true;
                        }
                    }
                }
                if (!flagOk)
                    return false;
            }
        }
        return true;
    }

    protected static void makeLibrary(Component cmpIn, List library, List translationLst) {

        // library: [ component<Component> ... ]

        //    Find the component translation and if it was mirrowed
        Component cmp = (Component) cmpIn.clone();
        Rectangle rect = cmp.getEnvelope();
        cmp.translate(-rect.c1.x, -rect.c1.y);

        //    Compensate Mirrowed Fets
        if ((cmpIn instanceof Fet) && ((Fet) cmpIn).swapFlag) {

            //    Swap back
            ((Fet) cmp).swapDS();

            //    Mirror to compensate
            cmp.mirrorY();
            cmp.translate(rect.getLx(), 0);
        }
	/*
	//    Find if component already in the library
	for (aux1=0; aux1<library.size() && !cmpIn.name.equals(((Component) library.get(aux1)).name); aux1++);
	if (aux1>=library.size()) {
	
	//    Include the component to the library, it will be the reference component
	library.add(cmp);
	}
	*/

        //    Find if component already in the library
        int aux1;
        for (aux1 = 0; aux1 < library.size() && !cmpIn.name.equals(((Component) library.get(aux1)).name); aux1++) ;
        if (aux1 < library.size()) {
            Component libCmp = (Component) library.get(aux1);
            //CHECK_ERR(!equalNames(cmp.terms(), library[aux1].terms()) ,
            //       "DesignCmp:: makeLibrary: Component was swaped library");
            if (!cmp.getTerms().isEqualBodies(libCmp.getTerms()) || !cmp.getBody().equals(libCmp.getBody())) {
			/*IF_DEBUG_ON(
			cmp.mirrorY();
			cmp.translate(rect.lx(), 0);
			CHECK_ERR(!equalBodies(cmp.terms(), library[aux1].terms()) || cmp.body()!=library[aux1].body(),
			   "DesignCmp:: makeLibrary: Component doesn't match library");
			)*/
                translationLst.add(Gen.newList(cmp.name, new Integer(cmp.reference), "MY", new Pt(rect.c1.x + rect.getLx(), rect.c1.y)));
            } else {
                translationLst.add(Gen.newList(cmp.name, new Integer(cmp.reference), new Pt(rect.c1.x, rect.c1.y)));
            }
        } else {

            //    Include the component to the library, it will be the reference component
            //        library.add(cmpIn.clone());
            //        Component cmp= (Component) library.get(library.size()-1);
            library.add(cmp);

            //    Bring the reference componet to the 0,0 coords
            //rect= cmp.getEnvelope();
            //cmp.translate(-rect.c1.x, -rect.c1.y);
            translationLst.add(Gen.newList(cmp.name, new Integer(cmp.reference), new Pt(rect.c1.x, rect.c1.y)));
        }
    }

    public Component pick(int pos) {
        return (Component) remove(pos);
    }

    public void printEdif(Writer out) throws IOException {

        List library = Gen.newList();
        List translationLst = Gen.newList();
        boolean flagStandardNode = false;

        out.write("(EDIF " + designName + ".cir" + "\n" + "   (status (EDIFVersion 0 9 5) (EDIFLevel 0) )" + "\n" + "   (design " + designName + " (qualify " + designName + " mainCell))" + "\n" + "   (library " + designName + "\n");
        for (int aux1 = 0; aux1 < size(); aux1++) {
            if (get(aux1) instanceof Pad) {
                at(aux1).printEdif(out);
                translationLst.add(Gen.newList(at(aux1).name, new Integer(at(aux1).reference)));
            } else if (get(aux1) instanceof ElectricNode) {
                if (at(aux1).getBody().isEmpty()) {
                    flagStandardNode = true;
                    translationLst.add(Gen.newList("stdNode", new Integer(at(aux1).reference)));
                } else {
                    //String auxName((*this)[aux1].name());
                    //Strstream auxStr((*this)[aux1].name());
                    String auxName = at(aux1).name + at(aux1).reference;
                    at(aux1).printEdif(out);
                    //translationLst.ap( new_LIST((*this)[aux1].name(), (*this)[aux1].reference()));
                    translationLst.add(Gen.newList(auxName, new Integer(at(aux1).reference)));
                    //(*this)[aux1].name()= auxName;
                }
            } else {
                makeLibrary(at(aux1), library, translationLst);
            }
        }

        //    Add StdNode
        if (flagStandardNode) {
            out.write("      (cell stdNode" + "\n" + "         (userData cellFunction node)" + "\n" + "         (view maskLayout Physical" + "\n" + "            (interface (declare inout port node))))" + "\n");
        }

        //    Add library
        for (int aux1 = 0; aux1 < library.size(); aux1++) {
            ((Component) library.get(aux1)).printEdif(out);
        }

        //    Put instances
        out.write("      (cell mainCell" + "\n" + "         (userData cellFunction main)" + "\n" + "         (view symbolic Symbolical" + "\n" + "            (contents" + "\n");

        //    Put instances
        for (int aux1 = 0; aux1 < translationLst.size(); aux1++) {
            List inst = translationLst.lst(aux1);
            out.write("               (instance " + inst.get(0) + " Physical cmp" + inst.get(1) + " ");
            if (inst.size() > 2) {
                out.write("(transform ");
                for (int num1 = 2; num1 < inst.size(); num1++) {
                    if (inst.get(num1) instanceof Pt) {
                        Pt pt = (Pt) inst.get(num1);
                        out.write("(translate " + pt.x + " " + pt.y + ") ");
                    } else if (inst.get(num1) instanceof String) {
                        out.write(inst.get(num1) + " ");
                    }
                }
                out.write(")");
            }
            out.write(") \n");
        }

        //    Put MustJoin
        for (int aux1 = 0; aux1 < electricNodes.size(); aux1++) {
            ElectricNode node = (ElectricNode) electricNodes.get(aux1);
            out.write("               (mustJoin \n" + "                  (qualify cmp" + node.reference + " node) \n");
            for (int aux2 = 0; aux2 < node.comps.size(); aux2++) {
                out.write("                  (qualify cmp" + node.comps.at(aux2).reference + " " + node.comps.at(aux2).termName + ") \n");
            }
            out.write("               ) \n");
        }

        //    Finish
        out.write("            )" + "\n" + "         )" + "\n" + "      )" + "\n" + "   )" + "\n" + ")" + "\n");
    }

    public void refComponents() {
        refFets();
        refElectricNodes();
        refPads();
    }

    public synchronized void refElectricNodes() {
        electricNodes = Gen.newList();
        for (int aux1 = 0; aux1 < size(); aux1++) {
            if (get(aux1) instanceof ElectricNode)
                electricNodes.add(get(aux1));
        }
    }

    public synchronized void refFets() {
        fets = Gen.newList();
        for (int aux1 = 0; aux1 < size(); aux1++) {
            if (get(aux1) instanceof Fet)
                fets.add(get(aux1));
        }
    }

    public synchronized void refPads() {
        pads = Gen.newList();
        for (int aux1 = 0; aux1 < size(); aux1++) {
            if (get(aux1) instanceof Pad)
                pads.add(get(aux1));
        }
    }

    /**
     * This method was created in VisualAge.
     */
    public void set(int ind, Component obj) {
        if (obj.reference == -1)
            throw new RuntimeException("Component with ref -1");
        super.set(ind, obj);
    }

    public synchronized void setDesignArea(Rectangle rec) {
        designArea = new Rectangle(rec);
    }
}
