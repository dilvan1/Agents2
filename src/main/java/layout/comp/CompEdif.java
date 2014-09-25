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
 *         CompEdif
 */

import layout.lang.LangEdif;
import layout.lang.LangException;
import layout.util.Gen;
import layout.util.List;
import layout.util.Rectangle;
import layout.util.Symbol;

import java.io.InputStream;
import java.util.Hashtable;

public class CompEdif extends LangEdif {

	// Maybe put this function in the SList
	static boolean validate(List lst, String cname) {
		try {
			Class cl = Class.forName(cname);
			for (int aux1 = 0; aux1 < lst.size(); aux1++)
				if (!cl.isAssignableFrom(lst.get(aux1).getClass())) return false;
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public DesignCmp design;

	public CompEdif(InputStream is) {
		super(is);
	}

	/*    Reads the components, first the particulars and then the general characteristics
	 *
	 * auxView=    [ { ("VIEW" viewType:String viewName:String
	 *       ( {:String} ))
	 *            ( {(portName:String (:Wire {:Wire}):List)} )
	 *            ( {:Wire | :Component} ):Lst<Object>
	 *        ) } ]
	 *
	 *    auxOption= ( ["COMPACTX"] ["COMPACTY"] )
	 *
	 * cellList= ( cellName:String viewName:String )
	 *
	 * Output:
	 * cellList= ( cellName:String viewName:String
	 *        (:Component {:Component}):Component|:List_of_Components) )
	 */
	protected List addComponent(String cellFunction, String cellName, List auxView, List auxOption) throws LangException {

		//   String cellName= (String) cellLst.elementAt(0);
		List declare = (List) auxView.get(0);
		List implem = (List) auxView.get(1);
		List body = (List) auxView.get(2);
		List cellLst = Gen.newList();

		//    The particulars
		if (cellFunction.equals("NMOS") || cellFunction.equals("PMOS")) {
			if (declare.size() != 3 || !declare.contains("SOURCE") ||
					!declare.contains("GATE") || !declare.contains("DRAIN"))
				throw new LangException("edif CELL: Fets have to have 3 ports named source, gate and drain");
			if (cellFunction.equals("NMOS"))
				cellLst.add(new Fet(-1, -1, Fet.NMOS));
			else
				cellLst.add(new Fet(-1, -1, Fet.PMOS));
		} else if (cellFunction.equals("NPN") || cellFunction.equals("PNP")) {
			if (declare.size() != 3 || !declare.contains("COLECTOR") ||
					!declare.contains("BASE") || !declare.contains("EMITER"))
				throw new LangException("edif CELL: Bipolars have to have 3 ports named colector, base and emiter");
			if (cellFunction.equals("NPN"))
				cellLst.add(new Bipolar(-1, -1, Bipolar.NPN));
			else
				cellLst.add(new Bipolar(-1, -1, Bipolar.PNP));
		} else if (cellFunction.equals("NODE")) {
			if (declare.size() != 1)
				throw new LangException("edif CELL: Nodes have to have 1 port");
			cellLst.add(new ElectricNode(-1, -1));
		} else if (cellFunction.equals("PAD")) {
			if (declare.size() != 1)
				throw new LangException("edif CELL: Pads have to have 1 port");
			//    Does compact do something?
			//
			//int auxComp= Pad::NO_COMPACT;
			//while (!auxOption.empty()) {
			//   if (auxOption[0] == "COMPACTX") auxComp= auxComp | Pad::COMPACT_X;
			//   if (auxOption[0] == "COMPACTY") auxComp= auxComp | Pad::COMPACT_Y;
			//   auxOption.del(0);
			//}
			cellLst.add(new Pad(-1, -1, 0 /*auxComp*/));
		} else
			cellLst.add(new Component(-1, -1));

		//    Common characteristics
		Component cmp = (Component) cellLst.get(cellLst.size() - 1);
		cmp.name = cellName;
		cmp.getBody().addAll(body);
		for (int aux1 = 0; aux1 < declare.size(); aux1++) {
			cmp.getTerms().add(new Term());
			cmp.getTerms().get(aux1).name = new Symbol((String) declare.get(aux1));
			int aux2;
			for (aux2 = 0; aux2 < implem.size() &&
					!declare.get(aux1).equals(implem.lst(aux2).get(0));
					aux2++)
				;
			if (aux2 < implem.size()) {
				List auxImplementation = (List) implem.lst(aux2).get(1);
				//         if (auxImplementation.number()>1)
				//            throw new LangException("edif CELL: Port has implementa
				cmp.getTerms().get(aux1).getBody().addAll(auxImplementation);

				//= (Wire) auxImplementation.get(0);
				//cmp.terms.at(aux1).body.setOwner(cmp.terms);
				//IF_DEBUG_ON(cmp.terms()[aux1].body().makeListConstant();)
				//IF_DEBUG_ON(APPLY(cmp.terms()[aux1].body(), makeListConstant()))
			} else if (!(cmp instanceof ElectricNode))
				throw new LangException("edif CELL: Port lacks implementation");
		}
		return cellLst;
	}

	/* list=    [ { ("VIEW" viewType:String viewName:String
	 *       ( {:String} ))
	 *            ( {(portName:String (:Wire {:Wire}):List)} )
	 *            ( {:Wire | :Component} ):Lst<Object>
	 *        ) } ]
	 *
	 *    userData= ["NMOS" | "PMOS" | "PAD" ["COMPACTX"] ["COMPACTY"] | "NODE" | "MAIN"]
	 *
	 * cell=  Cell Hash
	 *
	 * Output:
	 * cell= cell.put(viewName:String,
	 *        (:Component {:Component}):Component|:List_of_Components) )
	 */
	@Override
	protected void addView(String cellName, List list, List userData, Hashtable cell) throws LangException {

		//    Find cellFunction
		String cellFunction = "";
		if (!userData.isEmpty()) {
			cellFunction = (String) userData.get(0);
			userData.remove(0);
		}

		//    Find and extract the correct VIEW (SYMBOLIC or MASKLAYOUT)
		String getView;
		if (cellFunction.equals("MAIN"))
			getView = "SYMBOLIC";
		else
			getView = "MASKLAYOUT";
		if (cell.get(getView) != null)
			throw new LangException("edif CELL: View already read.");
		List auxView = Gen.newList();
		for (int aux1 = 0; aux1 < list.size(); aux1++)
			if (((List) list.get(aux1)).get(0).equals("VIEW")) {
				if (((List) list.get(aux1)).get(1).equals(getView)) {
					auxView.clear();
					auxView.addAll((List) list.get(aux1));
					auxView.remove(0);
					auxView.remove(0);
				}
			} else
				throw new LangException("edif CELL: Unknown element in the list");

		if (auxView.isEmpty())
			throw new LangException("edif CELL: AuxView empty, special error");
		String viewName = (String) auxView.remove(0);
		List viewList;
		if (!cellFunction.equals("MAIN")) {
			if (!validate((List) auxView.get(2), "layout.comp.Wire"))
				throw new LangException("edif CELL: Component cell with other cell's calls on it");
			viewList = addComponent(cellFunction, cellName, auxView, userData);
		} else {
			if (!validate((List) auxView.get(1), "layout.comp.Component"))
				throw new LangException("edif CELL: Component main should have only cell's calls on it");
			viewList = Gen.newList();
			viewList.addAll((List) auxView.get(2));
		}
		cell.put(viewName, viewList);
	}

	@Override
	protected Object copyComponent(Object cmp) {
		return ((Component) cmp).clone();
	}

	@Override
	protected Object copyWireSymbol(Object ob) {
		Wire wire = ((Wire) ob).clone();
		wire.setOwner(null);
		return wire;
	}

	@Override
	protected void endProcessing(Object obj) throws LangException {
		List cell = (List) obj;
		if (!validate(cell, "layout.comp.Component"))
			throw new LangException("edif EDIF: Design isn't the main cell");
		design = new DesignCmp();
		while (!cell.isEmpty())
			design.add((Component) cell.remove(0));
		currentLibrary.clear();
		libraries.clear();
		design.designName = designName;
	}

	@Override
	protected boolean isWireSymbol(Object ob) {
		return (ob instanceof Wire);
	}

	/*   list=
	 *              { { ("MUSTJOIN" (:String :String) {(:String :String)} ) }
	 *                { ("INSTANCE" instanceName:String cell:Component )    } }
	 */
	@Override
	protected List makeContents(List list) throws LangException {
		/*-
		 *    It goes through the list of components giving then the correct reference number
		 *    and pointing the nodes corectly to the components
		 */
		int compNum = 0;
		int fetNum = 0;
		int bipolarNum = 0;
		int cellNum = 0;
		int padNum = 0;
		int nodeNum = 0;

		List cellLst = Gen.newList();

		for (int aux1 = 0; aux1 < list.size(); aux1++) {
			List inst = (List) list.get(aux1);
			if (inst.get(0).equals("INSTANCE")) {
				Component cmp = (Component) inst.get(2);
				cmp.reference = compNum;
				compNum++;
				if (cmp instanceof Fet) {
					cmp.number = fetNum;
					fetNum++;
				} else if (cmp instanceof Bipolar) {
					cmp.number = bipolarNum;
					bipolarNum++;
				} else if (cmp instanceof Pad) {
					cmp.number = padNum;
					padNum++;
				} else if (cmp instanceof ElectricNode) {
					cmp.number = nodeNum;
					nodeNum++;
				} else {
					cmp.number = cellNum;
					cellNum++;
				}
				inst.remove(0);
				cellLst.add(list.remove(aux1));     // Was a putReg
				aux1--;
			} else if (!inst.get(0).equals("MUSTJOIN"))
				throw new LangException("edif CONTENTS: Acepts figureGroup or instance with mustJoin only");
		}

		//    Points the nodes to the correct place
		int aux2, aux3;
		for (int aux1 = 0; aux1 < list.size(); aux1++) {
			List inst = (List) list.get(aux1);
			inst.remove(0);
			for (aux2 = 0; aux2 < inst.size() && cellLst.isSlot(((List) inst.get(aux2)).get(0)); aux2++)
				;
			if (aux2 < inst.size())
				throw new LangException("edif CONTENTS: Unknown cell in mustJoin");
			for (aux2 = 0; aux2 < inst.size() &&
					!(cellLst.slot(inst.lst(aux2).get(0)).get(1) instanceof ElectricNode);
					aux2++)
				;
			if (aux2 >= inst.size())
				throw new LangException("edif CONTENTS: Each net has to have a node");
			ElectricNode node = (ElectricNode) cellLst.slot(inst.lst(aux2).get(0)).get(1);
			inst.remove(aux2);
			for (aux2 = 0; aux2 < inst.size(); aux2++) {
				Component cmp = (Component) cellLst.slot(((List) inst.get(aux2)).get(0)).get(1);

				//    Checking to see if the terminal exists:
				for (aux3 = 0; aux3 < cmp.getTerms().size() && !((List) inst.get(aux2)).get(1).equals(cmp.getTerms().get(aux3).name.toString()); aux3++)
					;
				if (aux3 >= cmp.getTerms().size())
					throw new LangException("edif CONTENTS: Unknown port in mustJoin");

				//    Put the node address in the term
				cmp.getTerms().get(aux3).electricNode = node.number;
				/*-
				 *    Create new entry in the node's comps list for the component and creates a sub-net
				 *    in node.routingNets holding only the component term body
				 */
				String termName = (String) inst.lst(aux2).get(1);
				if (node.comps == null)
					node.comps = new CompRegList();
				node.comps.add(new CompReg());
				node.comps.last().reference = cmp.reference;
				node.comps.last().termName = new Symbol(termName);
				node.comps.last().routingNet = -1;

				// SHOULD BE CHANGED
				//
				//    If component is a pad verify if its a special pad
				if ((cmp instanceof Pad) && (cmp.name.equals("VSS") || cmp.name.equals("VDD")))
					node.name = cmp.name;
			}
		}
		return cellLst;
	}

	@Override
	protected Object newWireSymbol(Rectangle rec, String layer) {
		return new Wire(rec, Layer.getLayer(layer));
	}

	@Override
	protected void transformComponent(Object obj, String trans) {
		Component cmp = (Component) obj;

		if (trans.equals("R0")) return;
		if (trans.equals("R90")) {
			cmp.rotate(0, 1);
			return;
		}
		if (trans.equals("R180")) {
			cmp.rotate(-1, 0);
			return;
		}
		if (trans.equals("R270")) {
			cmp.rotate(0, -1);
			return;
		}
		if (trans.equals("MX")) {
			cmp.mirrorX();
			return;
		}
		if (trans.equals("MY")) {
			cmp.mirrorY();
			return;
		}
		if (trans.equals("MYR90")) {
			cmp.mirrorY();
			cmp.rotate(0, 1);
			return;
		}
		if (trans.equals("MXR90")) {
			cmp.mirrorX();
			cmp.rotate(0, 1);
			return;
		}
		throw new RuntimeException("Unknown transformation");
	}

	@Override
	protected void translateComponent(Object obj, int x, int y) {
		((Component) obj).translate(x, y);
	}

	@Override
	protected List userData(List list) throws LangException {
		/*
		 * list= [userDataName:String {:Anything}]
		 * userData= empty
		 *       or  ("CELLFUNCTION" "NMOS" | "PMOS" | "NPN" | "PNP" | "PAD" ["COMPACTX"] ["COMPACTY"] | "NODE" | "MAIN")
		 */
		if (list.isEmpty())
			throw new LangException("edif USERDATA: Needs a userDataName");
		if (!list.get(0).equals("CELLFUNCTION"))
			return Gen.newList();
		if (list.size() < 2 && !(list.get(1) instanceof String))
			throw new LangException("edif USERDATA: CellFunction has to have a type");
		String funName = (String) list.get(1);
		if (!funName.equals("NMOS") && !funName.equals("PMOS") && !funName.equals("NPN") && !funName.equals("PNP") &&
				!funName.equals("PAD") && !funName.equals("NODE") && !funName.equals("MAIN"))
			throw new LangException("edif USERDATA: CellFunction has to be Nmos, Pmos, pad, node or main");
		if (list.size() > 2) {
			if (!funName.equals("PAD"))
				throw new LangException("edif USERDATA: CellFunction pad with unknown option");
			if (!list.get(2).equals("COMPACTX") && !list.get(2).equals("COMPACTY"))
				throw new LangException("edif USERDATA: CellFunction pad with unknown option");
			if (list.size() > 3 && !list.get(3).equals("COMPACTX") && !list.get(3).equals("COMPACTY"))
				throw new LangException("edif USERDATA: CellFunction pad with too many options");
		}
		list.remove(0);
		return list;
	}

	// Change that
	@Override
	protected boolean validFigureGroup(String s) {
		try {
			Layer.getLayer(s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
