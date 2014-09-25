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
 *  ElectricNode
 */

import layout.util.Gen;
import layout.util.List;

import java.io.IOException;
import java.io.Writer;

/**
 * <p>This class represents the circuit's nodes.</p>
 * <p>Its <tt>body</tt> has the actual wiring of a circuit.</p>
 */
public class ElectricNode extends Component {

	class RoutingData {
		WireList body;
		CompRegList comps;
		List routingNets;
	}
	/**
	 * This method was created in VisualAge.
	 */
	public static int getInd(List lst, Object obj) {
		for (int aux1 = 0; aux1 < lst.size(); aux1++)
			if (obj == lst.get(aux1))
				return aux1;
		return -1;
	}

	public CompRegList comps = new CompRegList();

	public List routingNets = Gen.newList();        // [ [ RecFig RecFig ...] [ RecFig RecFig ...] ...]

	volatile private boolean lockRerouteFlag;

	public ElectricNode(int refI, int num) {
		super(refI, num);
		lockRerouteFlag = false;
	}

	@Override
	public ElectricNode clone() {
		ElectricNode node = (ElectricNode) super.clone();

		//        Copy comps contents
		node.comps = new CompRegList();
		for (CompReg reg: comps)
			node.comps.add(reg.clone());
		if (routingNets == null) return node;
		if (routingNets.isEmpty()) {
			node.routingNets = Gen.newList();
			return node;
		}
		throw new RuntimeException("Clone not possible for ElectricNode");
		/*
	//        Copy routing nets
	node.routingNets = Gen.newList();

	for (int aux1 = 0; aux1 < routingNets.size(); aux1++) {
	List net = Gen.newList();
	List thisNet = routingNets.lst(aux1);
	for (int aux2 = 0; aux2 < thisNet.size(); aux2++) {
	//if (getComponentByWire((Wire) thisNet.get(aux2)) != this)
	net.add(thisNet.get(aux2));
	//else
	//   net.add(node.body.at(body.indexOf(thisNet.get(aux2))));
	}
	node.routingNets.add(net);
	}

	return node;
		 */
	}

	/**
	 * This method was created in VisualAge.
	 */
	public ElectricNode cloneInDesign(DesignCmp design) {
		ElectricNode node = (ElectricNode) super.clone();

		//        Copy comps contents
		node.comps = new CompRegList();
		for (CompReg reg: comps)
			node.comps.add(reg.clone());

		//        Copy routing nets
		node.routingNets = Gen.newList();
		for (int aux1 = 0; aux1 < routingNets.size(); aux1++) {
			List net = Gen.newList();
			for (int aux2 = 0; aux2 < routingNets.lst(aux1).size(); aux2++) {
				Wire thisWire = (Wire) routingNets.lst(aux1).get(aux2);
				Component cmp = getComponentByWire(thisWire);
				int ind = getInd(cmp.getBody(), thisWire);
				if (cmp != this) {
					Component newcmp = design.getByReference(cmp.reference);
					if (ind == -1)
						for (int aux3 = 0; aux3 < newcmp.getTerms().size(); aux3++) {
							ind = getInd(cmp.getTerms().get(aux3).getBody(), thisWire);
							if (ind != -1) {
								net.add(newcmp.getTerms().get(aux3).getBody().get(ind));
								break;
							}
						}
					else
						net.add(newcmp.getBody().get(ind));
				} else
					net.add(node.getBody().get(ind));
			}
			node.routingNets.add(net);
		}
		return node;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			return (super.equals(obj) && comps.equals(((ElectricNode) obj).comps));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * This method was created in VisualAge.
	 */
	public Object getRoutingData() {

		RoutingData rd = new RoutingData();

		//	Copy body
		rd.body = (WireList) getBody().clone();
		rd.body.setOwner(null);

		//        Copy comps contents
		rd.comps = new CompRegList();
		for (int aux1 = 0; aux1 < comps.size(); aux1++)
			rd.comps.add(comps.get(aux1).clone());

		//        Copy routing nets
		rd.routingNets = Gen.newList();
		for (int aux1 = 0; aux1 < routingNets.size(); aux1++) {
			List net = Gen.newList();
			List thisNet = routingNets.lst(aux1);
			for (int aux2 = 0; aux2 < thisNet.size(); aux2++)
				net.add(thisNet.get(aux2));
			rd.routingNets.add(net);
		}
		return rd;
	}

	/**
	 * Delete all nodes wires pointed by the
	 * routingNets. This won't delete any wire in the node
	 * prior to routing.Error it maybe will in the next step
	 */
	public void initRoutingNets(DesignCmp design) {
		while (!routingNets.isEmpty()) {
			for (int aux1 = 0; aux1 < getBody().size(); )
				if (routingNets.lst(0).contains(getBody().get(aux1)))
					getBody().remove(aux1);
				else
					aux1++;
			//node.body().release( node.routingNets().lst(0)); used only in C++ code
			routingNets.remove(0);
		}

		//    Init the routingNets point then to the components' terminals
		for (CompReg reg : comps) {
			Component cmp = design.getByReference(reg.reference);
			reg.routingNet = routingNets.size();
			/*-
			 * Change this:
			 * Make the body just a reference from the terminal not a copy in the node
			 *         auxWire= (Lst<Wire>*) cmp.terms[node.comps()[aux2].termName()].body().copy();
			 *         auxWire->matchAll(node.routingNets.lst(0));
			 *         node.body().join(*auxWire);
			 *         delete auxWire;
			 */
			List auxLst = Gen.newList();
			routingNets.add(auxLst);
			auxLst.addAll(cmp.getTerms().get(reg.termName).getBody());
		}
	}

	public boolean isRerouteLocked() {
		return lockRerouteFlag;
	}

	public void joinRoutingNets(int n1, int n2) {
		if (n1 == n2)
			throw new RuntimeException("Trying to join the same net");
		for (int aux1 = 0; aux1 < comps.size(); aux1++) {
			if (comps.get(aux1).routingNet == n2)
				comps.get(aux1).routingNet = n1;
			if (comps.get(aux1).routingNet > n2)
				comps.get(aux1).routingNet--;
		}
		routingNets.lst(n1).addAll(routingNets.lst(n2));
		routingNets.remove(n2);
	}

	@Override
	public void printEdif(Writer out) throws IOException {
		if (getBody().isEmpty())
			out.write("      (cell " + name + "\n");
		else
			out.write("      (cell " + name + reference + "\n");
		out.write("         (userData cellFunction node)" + "\n" +
				"         (view maskLayout Physical" + "\n" +
				"            (interface" + "\n" +
				"               (declare inout port node)" + "\n");
		out.write("            )" + "\n" + "            (contents" + "\n");
		for (Wire wire: getBody())
			wire.printEdif(out);
		out.write("            )" + "\n" +
				"         )" + "\n" +
				"      )" + "\n");
	}

	/**
	 * From a wire this routine finds the component it belongs
	 * and makes the <tt>ind</tt> the routing net for that component in
	 * comps.
	 */
	void setCompReference(Wire wire2, int ind) {
		Component cmp = getComponentByWire(wire2);
		if (cmp == this)
			return;
		Term term = getTermByWire(wire2);
		int aux1;
		for (aux1 = 0; aux1 < comps.size() && comps.get(aux1).reference == cmp.reference && comps.get(aux1).termName.equals(term.name); aux1++)
			;
		if (aux1 >= comps.size())
			throw new RuntimeException("Couldn't find comps reference");
		comps.get(aux1).routingNet = ind;
	}

	public void setRerouteLocked() {
		lockRerouteFlag = true;
	}

	/**
	 * This method was created in VisualAge.
	 *
	 * @param rd1 java.lang.Object
	 */
	public void setRoutingData(Object rd1) {

		RoutingData rd = (RoutingData) rd1;

		if (rd.body == null)
			throw new RuntimeException("Empty body");

		//	Restore body
		setBody(rd.body);
		//body.setOwner(this);
		rd.body = null;

		//	Restore comps
		comps = rd.comps;

		//	Restore routingNets
		routingNets = rd.routingNets;
	}

	@Override
	public String toString() {
		return "Electric Node \n" +
				super.toString() +
				" Conections: " + comps +
				"\n Routing Nets: " + routingNets + "\n";
	}

	public void unwire() {
		if (isRerouteLocked())
			throw new RuntimeException("Trying to unroute in a locked node");
		if (routingNets.size() != 1)
			throw new RuntimeException("Trying to unwire an incomplete routed wire");
		List net = routingNets.lst(0);

		//                Delete the unwanted wires
		for (int aux1 = 0; aux1 < getBody().size(); )
			if (getBody().get(aux1).isRewirable()) {
				net.remove(getBody().get(aux1));
				getBody().remove(aux1);
			} else
				aux1++;

		/*                For each wire in list2 take all that are connected together
		 *                and put in the same list
		 */
		Object lst2Ptr = routingNets.get(0);
		routingNets.clear();
		int ind = -1;
		while (!net.isEmpty()) {
			List lst1 = Gen.newList();
			routingNets.add(lst1);
			ind++;
			setCompReference((Wire) net.get(0), ind);
			lst1.add(net.remove(0));
			for (int aux1 = 0; aux1 < lst1.size(); aux1++) {
				Wire wire1 = (Wire) lst1.get(aux1);
				for (int aux2 = 0; aux2 < net.size(); ) {
					Wire wire2 = (Wire) net.get(aux2);
					if (wire1.isConnected(wire2) || isSameTerminal(wire1, wire2)) {
						setCompReference(wire2, ind);
						lst1.add(net.remove(aux2));
					} else
						aux2++;
				}
			}
		}
	}
}
