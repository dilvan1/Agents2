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
 * Wire
 */

import layout.display.Display;
import layout.util.*;

import java.io.IOException;
import java.io.Writer;

public class Wire extends OwnedList<WirePt> {

	public Wire() {
	}

	public Wire(Pt p1, int w, Layer lay) {
		add(new WirePt(p1, 0, Layer.EMPTY));
		add(new WirePt(p1, w, lay));
		get(0).setOwner(this);
		get(1).setOwner(this);
	}

	public Wire(Pt p1, Pt p2, int w, Layer lay) {
		add(new WirePt(p1, 0, Layer.EMPTY));
		add(new WirePt(p2, w, lay));
		get(0).setOwner(this);
		get(1).setOwner(this);
	}

	public Wire(Rectangle rec, Layer lay) {
		rec.coerent();
		int lx = rec.getLx();
		int ly = rec.getLy();
		if (lx == ly) {
			if ((lx - (lx / 2 * 2)) + (ly - (ly / 2 * 2)) != 0)
				throw new RuntimeException("Wire: Rectangle side not even");
			add(new WirePt(rec.c1.x + lx / 2, rec.c1.y + ly / 2, 0, Layer.EMPTY));
			add(new WirePt(rec.c1.x + lx / 2, rec.c1.y + ly / 2, lx, lay));
			get(0).setOwner(this);
			get(1).setOwner(this);
			return;
		}
		if (rec.isHorizontal()) {
			if ((ly - (ly / 2 * 2)) != 0)
				throw new RuntimeException("Wire: Rectangle side not even");
			add(new WirePt(rec.c1.x + ly / 2, rec.c1.y + ly / 2, 0, Layer.EMPTY));
			add(new WirePt(rec.c2.x - ly / 2, rec.c2.y - ly / 2, ly, lay));
			get(0).setOwner(this);
			get(1).setOwner(this);
			return;
		}

		//  Rectangle vertical
		if ((lx - (lx / 2 * 2)) != 0)
			throw new RuntimeException("Wire: Rectangle side not even");
		add(new WirePt(rec.c1.x + lx / 2, rec.c1.y + lx / 2, 0, Layer.EMPTY));
		add(new WirePt(rec.c2.x - lx / 2, rec.c2.y - lx / 2, lx, lay));
		get(0).setOwner(this);
		get(1).setOwner(this);
	}

	public Wire(Segment s1, int w, Layer lay) {
		add(new WirePt(s1.c1, 0, Layer.EMPTY));
		add(new WirePt(s1.c2, w, lay));
		get(0).setOwner(this);
		get(1).setOwner(this);
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (9/15/2000 11:56:19 PM)
	 */
	//public void add(int ind, Object obj) {
	//    super.add(ind, (WirePt) obj);
	//}

	//public boolean add(Wire ptr) {
	//    return super.add((WirePt) ptr);
	//}

	public boolean add(Pt auxPt, int wid, Layer str) {
		return add(new WirePt(auxPt, wid, str));
	}

	@Override
	public Wire clone() {
		return (Wire) super.clone();
	}

	//public boolean addAll(Collection c) {
	//    throw new RuntimeException("addAll not supported.");
	//}

	//public WirePt get(int pos) {
	//    return (WirePt) get(pos);
	//}

	public void drawOut(Display out) {
		Rectangle rec = new Rectangle();
		for (int auxLast = 0, aux1 = 1; aux1 < size(); auxLast++, aux1++) {
			rec.set(get(auxLast), get(aux1), get(aux1).width());
			out.addRect(rec, get(aux1).layer().toString());
		}
	}

	public WiringPoint getClosestWiringPoints(Wire wire) {
		Segment seg1 = new Segment();
		Segment seg2 = new Segment();
		WiringPoint wp = new WiringPoint();

		for (int aux1 = 1; aux1 < size(); aux1++) {
			if (!Consultant.db.isWiringLayer(get(aux1).layer())) continue;
			seg1.c1.set(get(aux1 - 1));
			seg1.c2.set(get(aux1));
			boolean seg1C1EqualsC2 = seg1.c1.equals(seg1.c2);
			for (int aux2 = 1; aux2 < wire.size(); aux2++) {
				if (!Consultant.db.isWiringLayer(wire.get(aux2).layer())) continue;
				seg2.c1.set(wire.get(aux2 - 1));
				seg2.c2.set(wire.get(aux2));
				double auxDist;
				if (seg1C1EqualsC2)
					auxDist = (seg2.c1.equals(seg2.c2))
					? Math.abs(seg1.c1.x - seg2.c1.x) + Math.abs(seg1.c1.y - seg2.c1.y)
							: seg2.getManhattanDistance(seg1.c1);
					else auxDist = (seg2.c1.equals(seg2.c2))
							? seg1.getManhattanDistance(seg2.c1)
									: seg1.getManhattanDistance(seg2);

							if (auxDist >= 0 && (wp.dist >= auxDist || wp.dist < 0)) {
								wp.dist = auxDist;
								wp.ele1 = aux1;
								wp.ele2 = aux2;
							}
			}
		}
		if (wp.dist >= 0) return wp;
		else return null;
	}

	public List getCrashes(NoCrash[] nets, Wire wire) {
		Rectangle rec = new Rectangle();
		List lstCrash = Gen.newList();
		for (int aux1 = 1; aux1 < size(); aux1++) {
			WirePt elem = get(aux1);
			rec.set(get(aux1 - 1), elem, elem.width());
			for (int last2 = 0, aux2 = 1; aux2 < wire.size(); last2++, aux2++)
				if (testCrash(nets, rec, elem.layer(), wire.get(last2), wire.get(aux2)))
					lstCrash.add(new Crash(this, aux1));
		}
		return lstCrash;
	}

	public List getCrashesPointer(NoCrash[] nets, Linea vect, Layer layer, int width) {
		Rectangle rec = new Rectangle();
		Rectangle recGen = new Rectangle();

		List lstCrash = Gen.newList();
		for (int aux1 = 1; aux1 < size(); aux1++) {
			WirePt elem = get(aux1);
			rec.set(get(aux1 - 1), elem, elem.width());
			int guard = Consultant.db.getWiringMargin(layer, elem.layer());
			if (guard != -1) {
				guard = guard + (int) Math.ceil(width / 2.0) - 1;
				recGen.c1.x = (rec.c1.x - guard);
				recGen.c1.y = (rec.c1.y - guard);
				recGen.c2.x = (rec.c2.x + guard);
				recGen.c2.y = (rec.c2.y + guard);
				InterPt itPt = recGen.getIntersectPt(vect);
				if (itPt.isIntersec() && testKeepCrash(nets, rec, elem.layer(), layer)) {
					int dist = (int) Math.floor(itPt.getT());
					if (dist < 0)
						throw new RuntimeException("getCrashesPointer: Distance < 0");
					int aux3;
					for (aux3 = 0; aux3 < lstCrash.size() &&
							((Crash) lstCrash.get(aux3)).getDistance() < dist;
							aux3++)
						;
					lstCrash.add(aux3, new Crash(this, aux1, dist));
				}
			}
		}
		return lstCrash;
	}

	public Rectangle getEnvelope() {
		if (size() < 2)
			throw new RuntimeException("envelope: Wire is empty.");
		Rectangle rec = new Rectangle(get(0), get(1), get(1).width());
		Rectangle auxRec = new Rectangle(0, 0, 0, 0);
		for (int auxLast = 1, aux1 = 2; aux1 < size(); auxLast++, aux1++) {
			auxRec.set(get(auxLast), get(aux1), get(aux1).width());
			if (rec.c1.x > auxRec.c1.x) rec.c1.x = (auxRec.c1.x);
			if (rec.c1.y > auxRec.c1.y) rec.c1.y = (auxRec.c1.y);
			if (rec.c2.x < auxRec.c2.x) rec.c2.x = (auxRec.c2.x);
			if (rec.c2.y < auxRec.c2.y) rec.c2.y = (auxRec.c2.y);
		}
		return rec;
	}

	public Rectangle getEnvelope(Layer layer) {
		Rectangle rec = null;
		Rectangle auxRec = new Rectangle();
		for (int auxLast = 0, aux1 = 1; aux1 < size(); auxLast++, aux1++) {
			if (!get(aux1).layer().equals(layer)) continue;
			if (rec == null) {
				rec = new Rectangle(get(auxLast), get(aux1), get(aux1).width());
				continue;
			}
			auxRec.set(get(auxLast), get(aux1), get(aux1).width());
			if (rec.c1.x > auxRec.c1.x) rec.c1.x = auxRec.c1.x;
			if (rec.c1.y > auxRec.c1.y) rec.c1.y = auxRec.c1.y;
			if (rec.c2.x < auxRec.c2.x) rec.c2.x = auxRec.c2.x;
			if (rec.c2.y < auxRec.c2.y) rec.c2.y = auxRec.c2.y;
		}
		return rec;
	}

	/**
	 * Get an estimation of the number of crashes
	 */
	public int getEstimatedCrashes(Wire wire) {
		Segment seg1 = new Segment();
		Segment seg2 = new Segment();

		int numCrash = 0;
		for (int last1 = 0, aux1 = 1; aux1 < size(); last1++, aux1++) {
			seg1.c1.set(get(last1));
			seg1.c2.set(get(aux1));
			for (int last2 = 0, aux2 = 1; aux2 < wire.size(); last2++, aux2++) {
				seg2.c1.set(wire.get(last2));
				seg2.c2.set(wire.get(aux2));
				if (seg1.isIntersect(seg2))
					numCrash++;
			}
		}
		return numCrash;
	}

	public InterPt getIntersectPointer(Linea pointer) {
		Rectangle rec = new Rectangle();
		InterPt t = new InterPt(false);
		for (int auxLast = 0, aux1 = 1; aux1 < size(); auxLast++, aux1++) {
			rec.set(get(auxLast), get(aux1), get(aux1).width());
			InterPt itPt = rec.getIntersectPt(pointer);
			if (itPt.isIntersec() && (!t.isIntersec() || itPt.getT() < t.getT()))
				t = new InterPt(true, itPt.getT());
		}
		return t;
	}

	public WirePt getLastSegment() {
		return get(size() - 1);
	}

	public List getWiringLayersTouchesPointer(List lstRec, Linea vect, int width) {
		Rectangle recGen = new Rectangle();

		List lstCrash = Gen.newList();
		for (int aux1 = 1; aux1 < size(); aux1++) {
			WirePt elem = get(aux1);
			if (!Consultant.db.isWiringLayer(elem.layer()))
				continue;
			int guard = (int) Math.ceil(width / 2.0);
			recGen.set(get(aux1 - 1), elem, elem.width());
			recGen.c1.x = recGen.c1.x - guard;
			recGen.c1.y = recGen.c1.y - guard;
			recGen.c2.x = recGen.c2.x + guard;
			recGen.c2.y = recGen.c2.y + guard;
			InterPt inter = recGen.getIntersectPt(vect);
			if (inter.isIntersec()) {
				int dist = (int) Math.floor(inter.getT());
				int aux3;
				for (aux3 = 0; aux3 < lstCrash.size() &&
						((Crash) lstCrash.get(aux3)).getDistance() < dist;
						aux3++)
					;
				lstCrash.add(aux3, new Crash(this, aux1, dist, lstRec));
			}
		}
		return lstCrash;
	}

	@Override
	public void invert() {
		int aux1, aux2;
		for (aux1 = 0, aux2 = 1; aux2 < size(); aux1++, aux2++) {
			get(aux1).width = get(aux2).width;
			get(aux1).layer = get(aux2).layer;
		}
		get(aux1).width = 0;
		get(aux1).layer = Layer.EMPTY;

		super.invert();
	}

	public boolean isConnected(Wire wire) {
		Rectangle rec1 = new Rectangle();
		Rectangle rec2 = new Rectangle();
		for (int auxLast1 = 0, aux1 = 1; aux1 < size(); auxLast1++, aux1++) {
			rec1.set(get(auxLast1), get(aux1), get(aux1).width());
			for (int auxLast2 = 0, aux2 = 1; aux2 < wire.size(); auxLast2++, aux2++)
				if (get(aux1).layer().equals(wire.get(aux2).layer())) {
					rec2.set(wire.get(auxLast2), wire.get(aux2), wire.get(aux2).width());
					if (rec1.isTouching(rec2))
						return true;
				}
		}
		return false;
	}

	public boolean isCrashing(NoCrash[] nets, Wire wire) {
		Rectangle rec = new Rectangle();
		for (int last1 = 0, aux1 = 1; aux1 < size(); last1++, aux1++) {
			WirePt elem = get(aux1);
			rec.set(get(last1), elem, elem.width());
			for (int last2 = 0, aux2 = 1; aux2 < wire.size(); last2++, aux2++)
				if (testCrash(nets, rec, elem.layer(), wire.get(last2), wire.get(aux2)))
					return true;
		}
		return false;
	}

	public boolean isCrashing(NoCrash[] nets, Wire wire, int ind) {
		WirePt elem = get(ind);
		Rectangle rec = new Rectangle(get(ind - 1), elem, elem.width());
		for (int last2 = 0, aux2 = 1; aux2 < wire.size(); last2++, aux2++)
			if (testCrash(nets, rec, elem.layer(), wire.get(last2), wire.get(aux2)))
				return true;
		return false;
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (9/16/2000 2:53:16 AM)
	 */
	 public boolean isJoinable(Wire wire) {

		//	Are joinable and of the same material
		if (size() == 0 || wire.size() == 0)
			return true;
		if (!getLastSegment().isJoinable(wire.get(0)))
			return false;

		//	Create protecrtion over end
		NoCrash[] nets = new NoCrash[2];
		nets[0] = new NoCrash(this, size() - 1);
		nets[1] = new NoCrash(wire, 1);

		/*
    if (isCrashing(nets, wire)){
	System.out.println("SHort circuit"+getLastSegment().layer()+ " + "+ wire.get(1).layer() + "   "+ wire.getLastSegment().layer());
	return false;
	}
	return true;
		 */

		Rectangle rec = new Rectangle();
		for (int last1 = 0, aux1 = 1; aux1 < size(); last1++, aux1++) {
			WirePt elem = get(aux1);
			rec.set(get(last1), elem, elem.width());
			for (int last2 = 0, aux2 = 1; aux2 < wire.size(); last2++, aux2++)
				if (testCrash(nets, rec, elem.layer(), wire.get(last2), wire.get(aux2)) &&
						!Consultant.db.isViaForLayer(elem.layer(), wire.get(aux2).layer()))
					return false;
		}
		return true;
	 }

	 /**
	  *
	  */
	 public boolean isRewirable() {

		 //        Test if the wire is re-wirable
		 int aux1;
		 for (aux1 = 1; aux1 < size() && (Consultant.db.isWiringLayer(get(aux1).layer()) || Consultant.db.isConnectionLayer(get(aux1).layer())); aux1++)
			 ;
		 if (aux1 < size())
			 return false;

		 //        Test if isn't all poly
		 for (aux1 = 1; aux1 < size() && get(aux1).layer().equals(Layer.POLY); aux1++) ;
		 return (aux1 < size());
	 }

	 public void join(Wire wire) {
		 if (wire.size() == 0) return;
		 if (size() != 0) {
			 WirePt aux1 = get(size() - 1);
			 WirePt aux2 = wire.get(0);

			 //    Connect the wire to the change via (It can be a bit dangerous)
			 if (aux1.x != aux2.x || aux1.y != aux2.y) {
				 if (get(size() - 2).y == aux1.y)
					 aux1.x = (aux2.x);
				 else
					 aux1.y = (aux2.y);
				 if (aux1.x != aux2.x || aux1.y != aux2.y)
					 throw new RuntimeException("join: The end of the wires don't meet");
			 }
			 wire.remove(0);
		 }
		 while (wire.size() > 0)
			 add(wire.remove(0));
	 }

	 public void mirrorX() {
		 for (WirePt w: this)
			 w.mirrorX();
	 }

	 public void mirrorY() {
		 for (WirePt w: this)
			 w.mirrorY();
	 }

	 public void printEdif(Writer out) throws IOException {
		 Rectangle rec = new Rectangle();
		 for (int auxLast = 0, aux1 = 1; aux1 < size(); auxLast++, aux1++) {
			 rec.set(get(auxLast), get(aux1), get(aux1).width());
			 out.write("                  (figureGroup " + get(aux1).layer() +
					 " (rectangle (point " + rec.c1.x + " " + rec.c1.y + ") " +
					 " (point " + rec.c2.x + " " + rec.c2.y + ")))\n");
		 }
	 }

	 public void rotate(int x, int y) {
		 for (WirePt w: this)
			 w.rotate(x, y);
	 }

	 /**
	  * Insert the method's description here.
	  * Creation date: (9/15/2000 11:59:10 PM)
	  */
	 //public Object set(int ind, Object obj) {
	 //    return super.set(ind, obj);
	 //}

	 private boolean testCrash(NoCrash[] net, Rectangle rec, Layer layer, WirePt wirePt1, WirePt wirePt2) {
		 int guard = Consultant.db.getWiringMargin(wirePt2.layer(), layer);
		 if (guard == -1)
			 return false;
		 guard = guard + (int) Math.ceil(wirePt2.width() * 0.5) - 1;
		 Rectangle recGen = new Rectangle(rec.c1.x - guard, rec.c1.y - guard, rec.c2.x + guard, rec.c2.y + guard);
		 if ((wirePt1.x == wirePt2.x) && (wirePt1.y == wirePt2.y)) {

			 //	if 2nd wire segment is a square
			 if (recGen.isInside(wirePt2))
				 if (testKeepCrash(net, rec, layer, wirePt2.layer()))
					 return true;
		 } else {

			 //if 2nd wire segment is a segment not a square
			 Segment seg = new Segment(wirePt1, wirePt2);
			 //if (recGen.getIntersectSeg(seg).isIntersec() && testKeepCrash(net, rec, layer, wirePt2.layer())) {
			 if (recGen.isIntersect(seg) && testKeepCrash(net, rec, layer, wirePt2.layer()))
				 return true;
		 }
		 return false;
	 }

	 private boolean testKeepCrash(NoCrash[] net, Rectangle rec, Layer layer1, Layer layer2) {
		 if (layer1.equals(Layer.EMPTY) || layer2.equals(Layer.EMPTY))
			 throw new RuntimeException("testKeepCrash: Empty layers");

		 //    Test if the two layers shouldn't touch even if in the same layer (vias , etc)
		 if (Consultant.db.unconnectableLayer(layer1) && Consultant.db.unconnectableLayer(layer2))
			 return true;
		 for (NoCrash aux : net) {
			 if (aux.getRectangle().isTouching(rec)) {

				 //    If contact point is in this wire no crash
				 if (aux.getWire() == this)
					 return false;

				 //    If contact point isn't in this wire but has the same layer no crash
				 //    maybe wrong it will generate small gaps that may not pass a design rule test
				 if (aux.getLayer().equals(layer1))
					 return false;
			 }
		 }
		 return true;
	 }

	 public void translate(int x, int y) {
		 for(WirePt w: this)
			 w.translate(x, y);
	 }
}
