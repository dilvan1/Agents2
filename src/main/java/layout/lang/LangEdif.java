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

package layout.lang;

/*
 *  LangEdif
 */

import layout.util.Gen;
import layout.util.List;
import layout.util.Pt;
import layout.util.Rectangle;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Hashtable;

/**
 * Class LangEdif
 */
public class LangEdif {

	private LexicAnalizer analex;
	public Hashtable libraries = new Hashtable();
	public Hashtable currentLibrary = new Hashtable();
	public Hashtable technology = new Hashtable();
	public String designName;

	public LangEdif(InputStream is) {
		analex = new LexicAnalizer(is);
	}

	//   Add Components
	protected void addView(String cellName, List v1, List v2, Hashtable cell) throws LangException {
		cell.put("PHYSICAL", new String("Component"));
		cell.put("SYMBOLIC", new String("Component"));
	}

	protected Object command(String name, List list) throws LangException {
		if (name.equals("E")) return edif_e(list);
		else if (name.equals("COMMENT")) return edif_comment(list);
		else if (name.equals("LIST")) return edif_list(list);
		else if (name.equals("POINT")) return edif_point(list);
		else if (name.equals("RECTANGLE")) return edif_rectangle(list);
		else if (name.equals("TRANSLATE")) return edif_translate(list);
		else if (name.equals("TRANSFORM")) return edif_transform(list);
		else if (name.equals("FIGUREGROUP")) return edif_figureGroup(list);
		else if (name.equals("QUALIFY")) return edif_qualify(list);
		else if (name.equals("INSTANCE")) return edif_instance(list);
		else if (name.equals("MUSTJOIN")) return edif_mustJoin(list);
		else if (name.equals("DECLARE")) return edif_declare(list);
		else if (name.equals("PORTIMPLEMENTATION")) return edif_portImplementation(list);
		else if (name.equals("BODY")) return edif_body(list);
		else if (name.equals("INTERFACE")) return edif_interface(list);
		else if (name.equals("CONTENTS")) return edif_contents(list);
		else if (name.equals("VIEW")) return edif_view(list);
		else if (name.equals("USERDATA")) return edif_userData(list);
		else if (name.equals("CELL")) return edif_cell(list);
		else if (name.equals("SCALE")) return edif_scale(list);
		else if (name.equals("NUMBERDEFINITION")) return edif_numberDefinition(list);
		else if (name.equals("TECHNOLOGY")) return edif_technology(list);
		else if (name.equals("LIBRARY")) return edif_library(list);
		else if (name.equals("DESIGN")) return edif_design(list);
		else if (name.equals("EDIF")) return edif_edif(list);
		return null;
	}

	protected Object copyComponent(Object cmp) {
		return cmp;
	}

	protected Object copyWireSymbol(Object ob) {
		return new String("WireSymbol");
	}

	// Input:   (BODY { (:Wire {:Wire}):List } )
	// Output:  Null  or:   ("BODY" (:Wire {:Wire}):List )
	//
	protected Object edif_body(List list) throws LangException {
		List newList, portList, auxList;

		if (list.size() == 0) return null;
		portList = Gen.newList();
		for (int aux1 = 0; aux1 < list.size(); aux1++) {
			try {
				auxList = (List) list.get(aux1);
				if (!isWireSymbolList(auxList)) throw new RuntimeException();
			} catch (RuntimeException re) {
				throw new LangException("edif BODY: Accepts figureGroup");
			}
			//    Append the list to portList
			//portList.ensureCapacity(portList.size()+auxList.size());
			for (int aux2 = 0; aux2 < auxList.size(); aux2++)
				portList.add(auxList.get(aux2));
		}
		newList = Gen.newList(2);
		newList.add("BODY");
		newList.add(portList);
		return newList;
	}

	// Input:   (CELL cellName:String
	//             {("USERDATA" Object Object ... )}
	//             { ("VIEW" viewType:String viewName:String
	//          ( {:String} )
	//                    ( {(portName:String (:Wire {:Wire}):List)} )
	//                    ( {:Wire | :Component} ):Lst<Object>
	//            )
	//          }
	//    )
	// Efect:   LangEdif.currentLibrary.put(cellName:String, (viewName:String
	//                                                 (:Component {:Component}):Component|:List_of_Components) )
	//    Output:  Null  or:   ("CELL" cellName:String)
	//
	Object edif_cell(List list) throws LangException {
		String cellName;
		try {
			cellName = (String) list.get(0);
		} catch (RuntimeException re) {
			throw new LangException("edif CELL: needs name ");
		}
		if (list.size() == 1) return null;
		list.remove(0);
		for (int aux1 = 0; aux1 < list.size(); aux1++)
			if (!(list.get(aux1) instanceof List))
				throw new LangException("edif CELL: wrong objects in the lists");
		//    Extract UserData information
		//
		int aux1;
		for (aux1 = 0; aux1 < list.size() && !list.lst(aux1).get(0).equals("USERDATA"); aux1++) ;
		List userData = Gen.newList();
		if (aux1 < list.size()) {
			userData = (List) list.get(aux1);
			userData.remove(0);
			list.remove(aux1);
		}

		//    Call the virtual function to form a cell
		//
		Hashtable cell = (Hashtable) currentLibrary.get(cellName);
		if (cell == null) {
			cell = new Hashtable();
			currentLibrary.put(cellName, cell);
		}
		addView(cellName, list, userData, cell);

		List newList = Gen.newList(2);
		newList.add(new String("CELL"));
		newList.add(new String(cellName));
		return newList;
	}

	/*
	 * Input:   (COMMENT :"String)
	 * Output:  Null
	 */
	protected Object edif_comment(List list) {
		return null;
	}

	// Input:   (CONTENTS {   (:Wire {:Wire}):List } |
	//              { { ("MUSTJOIN" (:String :String) {(:String :String)} ) }
	//                { ("INSTANCE" instanceName:String cell:Component )    } }
	//    )
	// Output:  Null  or:   ("CONTENTS" (:Wire {:Wire}):List |
	//                      (:Component {:Component}):List
	//          )
	//
	Object edif_contents(List list) throws LangException {
		int aux1;

		if (list.size() == 0) return null;
		try {
			for (aux1 = 0; aux1 < list.size() && isWireSymbolList(list.lst(aux1)); aux1++) ;
		} catch (RuntimeException re) {
			throw new LangException("edif CONTENTS: Acepts figureGroup or instance with mustJoin only");
		}
		//    If all are symbol list
		if (aux1 >= list.size()) {
			List auxFig = Gen.newList();
			for (aux1 = 0; aux1 < list.size(); aux1++)  //maybe wrong
				auxFig.addAll(list.lst(aux1));
			List newList = Gen.newList(2);
			newList.add(new String("CONTENTS"));
			newList.add(auxFig);
			return newList;
		}
		List cellLst = makeContents(list);
		List auxCmp = Gen.newList(cellLst.size());
		for (aux1 = 0; aux1 < cellLst.size(); aux1++)
			auxCmp.add(cellLst.lst(aux1).get(1)); //Caution should test type?
		List newList = Gen.newList(2);
		newList.add(new String("CONTENTS"));
		newList.add(auxCmp);
		return newList;
	}

	// Input:   (DECLARE direction:String declareType:String :String|(:String {:String}) )
	// Output:  ("DECLARE" ( :String {:String} ))
	//
	protected Object edif_declare(List list) throws LangException {
		List newList = Gen.newList(2);
		List strList;

		if (!list.get(0).equals("INPUT") && !list.get(0).equals("OUTPUT") && !list.get(0).equals("INOUT"))
			throw new LangException("edif DECLARE: needs direction equal to INPUT, OUTPUT or INOUT");
		if (!list.get(1).equals("PORT"))
			throw new LangException("edif DECLARE: accepts only port type");
		try {
			List vet1 = (List) list.get(2);
			strList = Gen.newList(vet1.size());

			//    This construct just test all elements as strings
			try {
				for (int aux1 = 0; aux1 < vet1.size(); aux1++)
					strList.add(vet1.get(aux1));
			} catch (RuntimeException re) {
				throw new LangException("edif DECLARE: The ports have to be strings");
			}
		} catch (RuntimeException re) {
			strList = Gen.newList(1);
			try {
				strList.add(list.get(2));
			} catch (RuntimeException re2) {
				throw new LangException("edif DECLARE: The ports have to be strings");
			}
		}
		newList.add(new String("DECLARE"));
		newList.add(strList);
		return newList;
	}

	// Input:   (DESIGN designName:String (QUALIFY libraryName:String cellName:String))
	// Efect: designName()= designName:String
	//    Output:  ("DESIGN" libraryName:String cellName:String)
	//
	Object edif_design(List list) throws LangException {
		List qualify;
		try {
			designName = (String) list.get(0);
			qualify = (List) list.get(1);
		} catch (RuntimeException re) {
			throw new LangException("edif DESIGN: needs designName and qualify");
		}

		if (!qualify.get(0).equals("QUALIFY") || qualify.size() != 3)
			throw new LangException("edif DESIGN: needs one no read-only qualify");

		List newList = Gen.newList(3);
		newList.add("DESIGN");
		newList.add(qualify.get(1));
		newList.add(qualify.get(2));
		return newList;
	}

	/*
	 * Input:   (E :Int :Int)
	 * Output:  :Real
	 */
	protected Object edif_e(List list) throws LangException {
		try {
			return new Double(((Integer) list.get(0)).intValue() * Math.pow(10, ((Integer) list.get(1)).intValue()));
		} catch (RuntimeException re) {
			throw new LangException("edif E: Should have 2 integers");
		}
	}

	// Input:   (EDIF edifFileName:String
	//             ("DESIGN" libraryName:String cellName:String)
	//             ("LIBRARY" libraryName:String)
	//             { ("LIBRARY" libraryName:String) }
	//    )
	// Efect:   LangEdif.design().ap( cellAssignedInDesignDescription:DesignCmp)
	//    Output:  ("EDIF")
	//
	Object edif_edif(List list) throws LangException {
		int aux1;

		try {
			String name = (String) list.get(0);
		} catch (RuntimeException re) {
			throw new LangException("edif EDIF: Needs a name");
		}

		try {
			for (aux1 = 1; aux1 < list.size() && !list.lst(aux1).get(0).equals("DESIGN"); aux1++) ;
			if (aux1 >= list.size()) throw new RuntimeException();
		} catch (RuntimeException re) {
			throw new LangException("edif EDIF: Needs a Design");
		}
		String designLibName = (String) list.lst(aux1).get(1);
		String designCellName = (String) list.lst(aux1).get(2);
		list.remove(aux1);

		try {
			for (aux1 = 1; aux1 < list.size() && list.lst(aux1).get(0).equals("LIBRARY"); aux1++) ;
			if (aux1 < list.size()) throw new RuntimeException();
		} catch (RuntimeException re) {
			throw new LangException("edif EDIF: Unknown element in the list");
		}
		//
		//
		//    Tests if there are more than one Technology

		Hashtable designLib = (Hashtable) libraries.get(designLibName);
		if (designLib == null || designLib.get(designCellName) == null)
			throw new LangException("edif EDIF: Unknown DESIGN's design or cell name");
		endProcessing(((Hashtable) designLib.get(designCellName)).get("SYMBOLICAL"));
		List newList = Gen.newList(1);
		newList.add("EDIF");
		return newList;
	}

	// Input:   (FIGUREGROUP name:String { :Rectangle })
	// Output:  Null  or:   ( :Wire {:Wire} ):List
	//
	Object edif_figureGroup(List list) throws LangException {
		List newList = Gen.newList();
		String str;

		try {
			str = (String) list.get(0);
		} catch (RuntimeException re) {
			throw new LangException("edif FIGUREGROUP: needs figureGroupName");
		}
		if (!validFigureGroup(str))
			throw new LangException("edif FIGUREGROUP: Has to have a valid figureGroupName");
		try {
			for (int aux1 = 1; aux1 < list.size(); aux1++)
				newList.add(newWireSymbol((Rectangle) list.get(aux1), str));
		} catch (RuntimeException re) {
			throw new LangException("edif FIGUREGROUP: " + re.getMessage());
			//Accepts only rectangles");
		}
		return newList;
	}

	// Input:   (INSTANCE cellName:String|(QUALIFY libraryName:String cellName:String) viewName:String
	//              instanceName:String [ ("TRANSFORM" { ("TRANSLATE" :Int :Int) }) ] )
	// Output:  ("INSTANCE" instanceName:String cell:Component )
	//
	Object edif_instance(List list) throws LangException {
		List newList;
		Hashtable cell;
		String viewName, instanceName;

		try {
			viewName = (String) list.get(1);
			instanceName = (String) list.get(2);
		} catch (RuntimeException re) {
			throw new LangException("edif INSTANCE: needs cellReference viewName instanceName ");
		}
		try {
			List qualif = (List) list.get(0);
			if (!qualif.get(0).equals("QUALIFY") || qualif.size() != 3)
				throw new LangException("edif INSTANCE: needs cellReference should be a name or a no read-only QUALIFY ");
			try {
				cell = (Hashtable) ((Hashtable) libraries.get(qualif.get(1))).get(qualif.get(2));
				if (cell == null) throw new LangException("edif INSTANCE: Unknown cell");
			} catch (RuntimeException re) {
				throw new LangException("edif INSTANCE: Unknown library");
			}
		} catch (RuntimeException re) {
			try {
				cell = (Hashtable) currentLibrary.get(list.get(0));
				if (cell == null)
					throw new LangException("edif INSTANCE: Unknown cell");
			} catch (RuntimeException re2) {
				throw new LangException("edif INSTANCE: cellReference expected");
			}
		}

		Object cellView = cell.get(viewName);
		if (cellView == null)
			throw new LangException("edif INSTANCE: can't find view");

		newList = Gen.newList();
		newList.add(new String("INSTANCE"));
		newList.add(instanceName);
		newList.add(copyComponent(((List) cellView).get(0)));//Takes first component

		Object cmp = newList.get(2);
		if (list.size() > 3) {
			try {
				if (!((List) list.get(3)).get(0).equals("TRANSFORM") ||
						list.size() != 4) throw new RuntimeException();
			} catch (RuntimeException re) {
				throw new LangException("edif INSTANCE: Acepts only one Transform");
			}

			List transfLst = (List) list.get(3);
			for (int aux1 = 1; aux1 < transfLst.size(); aux1++)
				if (transfLst.get(aux1) instanceof List) {
					List transf = (List) transfLst.get(aux1);
					if (transf.get(0).equals("TRANSLATE"))
						translateComponent(cmp, ((Integer) transf.get(1)).intValue(), ((Integer) transf.get(2)).intValue());
					else
						throw new LangException("edif INSTANCE: Acepts only list Translate");
				} else
					try {
						transformComponent(cmp, ((String) transfLst.get(aux1)));
					} catch (RuntimeException re2) {
						throw new LangException("edif INSTANCE: Translate expected: " + re2.getMessage());
					}
		}
		return newList;
	}
	// Input:   (INTERFACE { ("DECLARE" ( :String {:String} )) }
	//               { ("PORTIMPLEMENTATION" portName:String (:Wire {:Wire}):List ) }
	//               [ ("BODY" (:Wire {:Wire}):List ) ]
	//    )
	// Output:  Null  or:   ("INTERFACE" ( {:String} ))
	//                                 ( {(portName:String (:Wire {:Wire}):List)} )
	//                                 ( {:Wire} ):List
	//          )
	//

	protected Object edif_interface(List list) throws LangException {

		if (list.size() == 0) return null;

		List declareLst = Gen.newList();
		List portLst = Gen.newList();
		List bodyLst = Gen.newList();
		boolean flagBode = false;
		List list2;
		String nameCommand;

		for (int aux1 = 0; aux1 < list.size(); aux1++) {
			try {
				list2 = (List) list.get(aux1);
			} catch (RuntimeException re) {
				throw new LangException("edif INTERFACE: all should be lists");
			}
			nameCommand = (String) list2.get(0);
			if (nameCommand.equals("DECLARE"))
				declareLst.addAll(list2.lst(1));
			else if (nameCommand.equals("PORTIMPLEMENTATION")) {
				list2.remove(0);
				portLst.add(list.get(aux1));
			} else if (nameCommand.equals("BODY") && flagBode == false) {
				flagBode = true;
				bodyLst.addAll(list2.lst(1));
			} else
				throw new LangException("edif INTERFACE: wrong object in the list");
		}
		List newList = Gen.newList(4);
		newList.add("INTERFACE");
		newList.add(declareLst);
		newList.add(portLst);
		newList.add(bodyLst);
		return newList;
	}

	// Input:   (LIBRARY libraryName:String
	//       { ("CELL" cellName:String) }
	//       { ("TECHNOLOGY" technologyName:String) }
	// Efect:   LangEdif.libraries.ap( (libraryName:String ().join(currentLibrary) ))
	//    Output:  Null  or:   ("LIBRARY" libraryName:String)
	//
	Object edif_library(List list) throws LangException {
		String libName;
		try {
			libName = (String) list.get(0);
		} catch (RuntimeException re) {
			throw new LangException("edif LIBRARY: needs a name");
		}

		if (list.size() == 1) return null;

		try {
			for (int aux1 = 1; aux1 < list.size(); aux1++)
				if (!list.lst(aux1).get(0).equals("CELL") &&
						!list.lst(aux1).get(0).equals("TECHNOLOGY"))
					throw new LangException("edif LIBRARY: Unknown element in the list");
		} catch (RuntimeException re) {
			throw new LangException("edif LIBRARY: wrong objects in the lists");
		}

		//    Test if there is more than 1 Tech
		if (technology.size() > 1)
			throw new LangException("edif LIBRARY: This program can have just one TECHNOLOGY");

		libraries.put(libName, currentLibrary);

		List newList = Gen.newList(2);
		newList.add("LIBRARY");
		newList.add(libName);
		return newList;
	}

	// Input:   (LIST {:Anything} )
	// Output:  Null  or:   (:Anything {:Anything})
	//
	protected Object edif_list(List list) {
		return list;
	}

	// Input:   (MUSTJOIN { ("QUALIFY" :String :String) } )
	// Output:  Null  or:   ("MUSTJOIN" (:String :String) {(:String :String)} )
	//
	protected List edif_mustJoin(List list) throws LangException {
		List newList;

		if (list.size() == 0) return null;
		newList = Gen.newList(3);
		newList.add(new String("MUSTJOIN"));
		try {
			List auxList;
			for (int aux1 = 0; aux1 < list.size(); aux1++) {
				if (!list.lst(aux1).get(0).equals("QUALIFY"))
					throw new RuntimeException();
				auxList = Gen.newList(2);
				auxList.add(list.lst(aux1).get(1));
				auxList.add(list.lst(aux1).get(2));
				newList.add(auxList);
			}
		} catch (RuntimeException re) {
			throw new LangException("edif MUSTJOIN: all should be QUALIFY lists");
		}
		return newList;
	}

	// Input:   (NUMBERDEFINITION "MKS" {("SCALE" "DISTANCE" :Number :Number)} )
	// Output:  Null  or:   ("NUMBERDEFINITION" "DISTANCE" numberInMetersCorrespondingOneUnit)
	//
	Object edif_numberDefinition(List list) throws LangException {
		int aux1, aux2;

		if (list.size() == 0)
			throw new LangException("edif NUMBERDEFINITION: Should have a numberDefinitionName");

		if (!list.get(0).equals("MKS"))
			throw new LangException("edif:: NUMBERDEFINITION: Accepts only MKS as nuberDefinitionName");

		aux2 = -1;
		try {
			for (aux1 = 1; aux1 < list.size(); aux1++) {
				if (!list.lst(aux1).get(0).equals("SCALE"))
					throw new RuntimeException();
				aux2 = aux1;
			}
		} catch (RuntimeException re) {
			throw new LangException("edif:: NUBERDEFINITION: Unknown option");
		}

		if (aux2 == -1) return null;

		List lstScale = (List) list.get(aux2);
		List newList = Gen.newList(3);
		newList.add("NUMBERDEFINITION");
		newList.add("DISTANCE");
		newList.add(new Double(((Number) lstScale.get(3)).doubleValue() /
				((Number) lstScale.get(2)).doubleValue()));
		return newList;
	}

	// Input:   (POINT :Int :Int)
	// Output:  :Pt
	//
	protected Object edif_point(List list) throws LangException {
		try {
			return new Pt(((Integer) list.get(0)).intValue(), ((Integer) list.get(1)).intValue());
		} catch (RuntimeException re) {
			throw new LangException("edif POINT: Should have 2 integers");
		}
	}

	// Input:   (PORTIMPLEMENTATION portName:String { (:Wire {:Wire}):List } )
	// Output:  Null  or:   ("PORTIMPLEMENTATION" portName:String (:Wire {:Wire}):List )
	//
	protected Object edif_portImplementation(List list) throws LangException {
		List newList, portList, auxList;
		String portName;

		if (list.size() == 1) return null;
		try {
			portName = (String) list.get(0);
		} catch (RuntimeException re) {
			throw new LangException("edif PORTIMPLEMENTATION: Has to have a valid port name");
		}

		portList = Gen.newList();
		for (int aux1 = 1; aux1 < list.size(); aux1++) {
			try {
				auxList = (List) list.get(aux1);
				if (!isWireSymbolList(auxList)) throw new RuntimeException();
			} catch (RuntimeException re) {
				throw new LangException("edif FIGUREGROUP: Accepts figureGroup and instances");
			}
			portList.addAll(auxList);
		}
		newList = Gen.newList(3);
		newList.add("PORTIMPLEMENTATION");
		newList.add(portName);
		newList.add(portList);
		return newList;
	}

	// Input:   (QUALIFY :String [:String])
	// Output:  ("QUALIFY" :String [:String] )
	//
	protected Object edif_qualify(List list) throws LangException {
		List newList = Gen.newList(list.size() + 1);

		newList.add(new String("QUALIFY"));
		try {
			int aux1;
			for (aux1 = 0; aux1 < list.size(); aux1++)
				newList.add(list.get(aux1));
			if (aux1 > 2) throw new RuntimeException();
		} catch (RuntimeException re) {
			throw new LangException("edif QUALIFY: Acepts only 1 or 2 strings");
		}
		return newList;
	}

	// Input:   (RECTANGLE :Pt :Pt)
	// Output:  :Rectangle
	//
	protected Object edif_rectangle(List list) throws LangException {
		try {
			return new Rectangle(((Pt) list.get(0)), ((Pt) list.get(1)));
		} catch (RuntimeException re) {
			throw new LangException("edif RECTANGLE: Should have 2 points");
		}
	}

	// Input:   (SCALE UnityType:String :Number :Number)
	// Output:  Null  or:   ("SCALE" "DISTANCE" :Number :Number)
	//
	Object edif_scale(List list) throws LangException {
		List newList = Gen.newList(4);
		newList.add("SCALE");
		try {
			newList.add(list.get(0));
			newList.add(list.get(1));
			newList.add(list.get(2));
		} catch (RuntimeException re) {
			throw new LangException("edif SCALE: Should be :String :Number :Number");
		}

		String unit = (String) list.get(0);
		if (!unit.equals("DISTANCE"))
			if (unit.equals("CAPACITANCE") || unit.equals("CURRENT") || unit.equals("RESISTENCE") ||
					unit.equals("TEMPERATURE") || unit.equals("TIME") || unit.equals("VOLTAGE"))
				return null;
			else
				throw new LangException("edif SCALE: Unknown UnitType");
		return newList;
	}

	// Input:   (TECHNOLOGY technologyName:String {("NUMBERDEFINITION" "DISTANCE" Ratio:Number)} )
	// Efect:   LangEdif.technology.put( "DISTANCE", Ratio:Number)
	//    Output:  Null  or:   ("TECHNOLOGY" technologyName:String)
	//
	Object edif_technology(List list) throws LangException {
		int aux1, aux2;
		String tecnName;

		try {
			tecnName = (String) list.get(0);
		} catch (RuntimeException re) {
			throw new LangException("edif TECHNOLOGY: Should have a technologyName");
		}

		aux2 = -1;
		try {
			for (aux1 = 1; aux1 < list.size(); aux1++) {
				if (!list.lst(aux1).get(0).equals("NUMBERDEFINITION"))
					throw new RuntimeException();
				aux2 = aux1;
			}
		} catch (RuntimeException re) {
			throw new LangException("edif:: TECHNOLOGY: Unknown option");
		}

		if (aux2 == -1) return null;

		Object dist = list.lst(aux2).get(2);

		Hashtable auxItem = (Hashtable) technology.get(tecnName);
		if (auxItem != null)
			auxItem.put("DISTANCE", dist);
		else {
			technology.put(tecnName, new Hashtable(1));
			((Hashtable) technology.get(tecnName)).put("DISTANCE", dist);
		}

		List newList = Gen.newList();
		newList.add("TECHNOLOGY");
		newList.add(tecnName);
		return newList;
	}

	// Input:   (TRANSFORM { ("TRANSLATE" :Int :Int) })
	// Output:  Null  or:   ("TRANSFORM" { ("TRANSLATE" :Int :Int) } {R0} {R90} {R180} {R270}
	//          {MX} {MY} {MYR90} {MXR90}  )
	//
	protected Object edif_transform(List list) throws LangException {
		List newList;
		int aux1;
		String str;

		if (list.size() == 0)
			return null;
		newList = Gen.newList();
		newList.add(new String("TRANSFORM"));
		for (aux1 = 0; aux1 < list.size(); aux1++) {
			if (list.get(aux1) instanceof String) {
				str = (String) list.get(aux1);
				if (!str.equals("R0") && !str.equals("R90") && !str.equals("R180") && !str.equals("R270") &&
						!str.equals("MX") && !str.equals("MY") && !str.equals("MYR90") && !str.equals("MXR90")) break;
			} else
				try {
					str = (String) ((List) list.get(aux1)).get(0);
					if (!str.equals("TRANSLATE")) break;
				} catch (RuntimeException re) {
					throw new LangException("edif TRANSFORM: Accepts only TRANSLATE and transformations (RX, MX, etc)");
				}
			newList.add(list.get(aux1));
		}
		if (aux1 < list.size())
			throw new LangException("edif TRANSFORM: Accepts only TRANSLATE and transformations (RX, MX, etc)");
		return newList;
	}

	// Input:   (TRANSLATE :Int :Int)
	// Output:  ("TRANSLATE" :Int :Int)
	//
	protected Object edif_translate(List list) throws LangException {
		List newList = Gen.newList();
		newList.add(new String("TRANSLATE"));
		try {
			newList.add(list.get(0));
			newList.add(list.get(1));
		} catch (RuntimeException re) {
			throw new LangException("edif TRANSLATE: Should have 2 integers");
		}
		return newList;
	}

	// Input:   (USERDATA userDataName:String {:Anything})
	//    Output:  Null  or:   ("USERDATA" Object Object ... )
	//
	Object edif_userData(List list) throws LangException {
		List lstUserData = userData(list);

		if (lstUserData.size() == 0) return null;
		List newList = Gen.newList(2);
		newList.add(new String("USERDATA"));
		newList.addAll(lstUserData);
		return newList;
	}

	// Input:   (VIEW viewType:String viewName:String
	//          [ ("INTERFACE" ( {portName:String} ))
	//                         ( {(portName:String (:Wire {:Wire}):List)} )
	//                         ( {:Wire} ):List
	//             )
	//          ]
	//          [ ("CONTENTS" (:Wire {:Wire}):List |
	//              (:Component {:Component}):DesignCmp
	//            )
	//          ]
	//    )
	//    Output:  Null  or:   ("VIEW" viewType:String viewName:String
	//                  ( {portName:String} ))
	//                            ( {(portName:String (:Wire {:Wire}):List)} )
	//                            ( {:Wire | :Component} ):Lst<Object>
	//                )
	//
	protected Object edif_view(List list) throws LangException {
		String typeView;

		try {
			typeView = (String) list.get(0);
			String name = (String) list.get(1);
		} catch (RuntimeException re) {
			throw new LangException("edif VIEW: needs type abd name");
		}

		if (list.size() == 2 || (!typeView.equals("SYMBOLIC") && !typeView.equals("MASKLAYOUT")))
			return null;

		List portNames = Gen.newList(2);     // ports names
		List portImple = Gen.newList(2);     // ports implementation
		List bodyCont = Gen.newList(2);      // (body + implementations) or contents
		List contentsAux = Gen.newList(2);
		boolean flagInterface = false;
		boolean flagContents = false;
		List list2;
		String nameCommand;

		for (int aux1 = 2; aux1 < list.size(); aux1++) {
			try {
				list2 = (List) list.get(aux1);
			} catch (RuntimeException re) {
				throw new LangException("edif VIEW: wrong objects in the lists");
			}

			nameCommand = (String) list2.get(0);
			if (nameCommand.equals("INTERFACE") && flagInterface == false) {
				flagInterface = true;
				portNames.addAll(list2.lst(1));
				portImple.addAll(list2.lst(2));
				bodyCont.addAll(list2.lst(3));
			} else if (nameCommand.equals("CONTENTS") && flagContents == false) {
				flagContents = true;
				contentsAux.addAll(list2.lst(1));
			} else
				throw new LangException("edif VIEW: wrong object in the list");
		}
		if (bodyCont.size() == 0)          // If there is no body add contents
			bodyCont.addAll(contentsAux);
		else {
			List wireList;
			// If body is used add the portImplem
			for (int aux1 = 0; aux1 < portImple.size(); aux1++) {
				wireList = (List) portImple.lst(aux1).get(1);
				for (int aux2 = 0; aux1 < wireList.size(); aux1++)
					bodyCont.add(copyWireSymbol(wireList.get(aux2)));
			}
		}

		List newList = Gen.newList(6);
		newList.add(new String("VIEW"));
		newList.add(list.get(0));
		newList.add(list.get(1));
		newList.add(portNames);      // ports names
		newList.add(portImple);      // ports implementation
		newList.add(bodyCont);    // (body + implementations) or contents
		return newList;
	}

	protected void endProcessing(Object ob) throws LangException {
	}

	public Object eval() throws IOException, LangException {
		analex.read();
		if (!analex.word().equals("("))
			throw new LangException("( expected not " + analex.word());
		return evalInternal();
	}

	public Object eval(String command) throws IOException, LangException {
		StringBufferInputStream buffer = new StringBufferInputStream(command);
		analex = new LexicAnalizer(buffer);
		analex.read();
		if (!analex.word().equals("("))
			throw new LangException("( expected not " + analex.word());
		return evalInternal();
	}

	protected Object evalInternal() throws IOException, LangException {
		String commandName, palavra;
		List list = Gen.newList(3);

		analex.read("WORD");
		commandName = analex.word().toUpperCase();
		analex.read();
		while (!analex.word().equals(")")) {
			palavra = analex.word().toUpperCase();
			if (palavra.equals("(")) {
				Object newObj = evalInternal();
				if (newObj != null) list.add(newObj);
			} else if (analex.lexicClass().equals("INTEGER")) list.add(new Integer(analex.integer()));
			else if (palavra.length() == 0) throw new LangException("EOF not expected");
			else list.add(palavra);
			analex.read();
		}
		return command(commandName, list);
	}

	protected boolean isWireSymbol(Object ob) {
		return ob.equals("WireSymbol");
	}

	protected boolean isWireSymbolList(List lst) {
		int aux1;
		for (aux1 = 0; aux1 < lst.size() && isWireSymbol(lst.get(aux1)); aux1++) ;
		return (aux1 >= lst.size());
	}

	// Correct ref and points nodes
	protected List makeContents(List v1) throws LangException {
		return Gen.newList();
	}

	protected Object newWireSymbol(Rectangle r, String layer) {
		return new String("WireSymbol");
	}

	protected void transformComponent(Object cmp, String trans) {
	}

	protected void translateComponent(Object cmp, int x1, int x2) {
	}

	protected List userData(List v1) throws LangException {
		return Gen.newList();
	}

	protected boolean validFigureGroup(String s) {
		return true;
	}
}
