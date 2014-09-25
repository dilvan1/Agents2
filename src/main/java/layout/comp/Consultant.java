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
 *             Class Consultant
 */

import layout.util.List;
import layout.util.Pt;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;

public class Consultant implements Serializable {

	transient static public Consultant db;
	List layersNames;
	double convDist = 0.0;
	double gridValue = 0.0;
	Hashtable minWidthTable;
	Hashtable minOverlapingTable;
	Hashtable minSpacingTable;
	Hashtable interconnectWireTable;

	/*
    static long polyCost= 3;
	static long met1Cost= 1;
	static long met2Cost= 1;
	static long contCost= 20;
	static long viaCost=  20;
	static long ndiffCost= 4;
	static long pdiffCost= 4;
	 */

	static int polyCost = 8;  //4
	static int met1Cost = 2;
	static int met2Cost = 3;  // 2
	static int contCost = 100; //40; // 30
	static int viaCost = 100; //40; // 30
	static int ndiffCost = 1;
	static int pdiffCost = 1;

	public Consultant(String file) throws IOException, layout.lang.LangException {

		minWidthTable = new Hashtable();
		minOverlapingTable = new Hashtable();
		minSpacingTable = new Hashtable();
		interconnectWireTable = new Hashtable();
		BufferedInputStream strInp = new BufferedInputStream(new FileInputStream(file));
		LayerEdif l = new LayerEdif(strInp, this);

		l.eval();
		strInp.close();
		//System.out.println(this);
		makeInterconnectWireList(); //Maybe wrong
	}

	public boolean areLayersConnectable(Layer l1, Layer l2) {
		return (((l1.equals(Layer.NDIFF) || l1.equals(Layer.PDIFF) || l1.equals(Layer.POLY) || l1.equals(Layer.MET2)) &&
				l2.equals(Layer.MET1)) ||
				((l2.equals(Layer.NDIFF) || l2.equals(Layer.PDIFF) || l2.equals(Layer.POLY) || l2.equals(Layer.MET2)) &&
						l1.equals(Layer.MET1))
				);
	}

	public double getGridValue() {
		return gridValue;
	}
	//   static int ndiffCost= 7;
	//   static int pdiffCost= 7;

	public int getLayerCost(Layer layer) {
		if (layer.equals(Layer.POLY)) return polyCost;
		if (layer.equals(Layer.CONT)) return contCost;
		if (layer.equals(Layer.MET1)) return met1Cost;
		if (layer.equals(Layer.VIA)) return viaCost;
		if (layer.equals(Layer.MET2)) return met2Cost;
		if (layer.equals(Layer.NDIFF)) return ndiffCost;
		if (layer.equals(Layer.PDIFF)) return pdiffCost;
		throw new RuntimeException("Consultant:: getLayerCost: Unknown layer");
	}

	public List getLayersNames() {
		return layersNames;
	}

	public int getMinOverlaping(Layer l1, Layer l2) {
		Object obj = minOverlapingTable.get(l1.toString() + l2.toString());
		if (obj == null)
			obj = minOverlapingTable.get(l2.toString() + l1.toString());
		return (obj == null ? -1 : ((Number) obj).intValue());
	}
	//public List getLayersNames()
	//{
	//   //if (layersNames==null) layersNames= (List) query("( layersNames )\n");
	//   return layersNames;
	//}

	public int getMinWidth(Layer l1) {
		Object obj = minWidthTable.get(l1.toString());
		return (obj == null ? -1 : ((Number) obj).intValue());
	}

	public Layer getViaLayer(Layer l1, Layer l2) {
		if ((l1.equals(Layer.MET1) && l2.equals(Layer.MET2)) || (l1.equals(Layer.MET2) && l2.equals(Layer.MET1)))
			return Layer.VIA;
		//   if( !( ( (l1.equals(Layer.MET1) && (l2.equals(Layer.NDIFF) || l2.equals(Layer.PDIFF) || l2.equals(Layer.POLY)) ) ||
		//                  (l2==MET1 && (l1==NDIFF || l1==PDIFF || l1==POLY))   )) ,
		//             " getViaLayer: Forbiden via connection");
		return Layer.CONT;
	}

	public Wire getWireToInterconnect(Layer lay1, Layer lay2) {

		// Change for CHECK_ERRwhhen change areSlot
		Wire auxWire = (Wire) interconnectWireTable.get(lay1.toString() + lay2.toString());
		if (auxWire == null) throw new RuntimeException("LayersExpert:: getWireToInterconnect: Can't find wire");
		auxWire = auxWire.clone();
		return auxWire;
	}

	public int getWireWidth(Layer layer) {
		return getMinWidth(layer);
	}

	public int getWiringMargin(Layer l1, Layer l2) {
		Object obj = minSpacingTable.get(l1.toString() + l2.toString());
		if (obj == null)
			obj = minSpacingTable.get(l2.toString() + l1.toString());
		return (obj == null ? -1 : ((Number) obj).intValue());
	}

	public boolean isConnectionLayer(Layer layer) {
		return (layer.equals(Layer.CONT) || layer.equals(Layer.VIA));
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (9/21/2000 1:48:35 PM)
	 */
	public boolean isViaForLayer(Layer l1, Layer l2) {

		//	Via first
		if (l2.equals(Layer.POLY) && l1.equals(Layer.CONT)) return true;
		if (l2.equals(Layer.MET1) && (l1.equals(Layer.CONT) || l1.equals(Layer.VIA))) return true;
		if (l2.equals(Layer.MET2) && l1.equals(Layer.VIA)) return true;

		//	Layer first
		if (l1.equals(Layer.POLY) && l2.equals(Layer.CONT)) return true;
		if (l1.equals(Layer.MET1) && (l2.equals(Layer.CONT) || l2.equals(Layer.VIA))) return true;
		if (l1.equals(Layer.MET2) && l2.equals(Layer.VIA)) return true;

		return false;
	}

	public boolean isWiringLayer(Layer layer) {
		return (layer.equals(Layer.POLY) || layer.equals(Layer.MET1) || layer.equals(Layer.MET2));
	}

	void makeInterconnectWireList() {
		Wire auxWire;
		Pt auxPt = new Pt();
		Pt auxPt2 = new Pt();

		// Make interconnectWire List
		//    interconnectWireTable
		Layer lay1 = null;
		Layer lay2 = null;
		for (int aux1 = 0; aux1 < 8; aux1++) {
			switch (aux1) {
			case 0:
				lay1 = Layer.POLY;
				lay2 = Layer.MET1;
				break;
			case 1:
				lay1 = Layer.MET1;
				lay2 = Layer.POLY;
				break;
			case 2:
				lay1 = Layer.MET1;
				lay2 = Layer.MET2;
				break;
			case 3:
				lay1 = Layer.MET2;
				lay2 = Layer.MET1;
				break;
			case 4:
				lay1 = Layer.NDIFF;
				lay2 = Layer.MET1;
				break;
			case 5:
				lay1 = Layer.MET1;
				lay2 = Layer.NDIFF;
				break;
			case 6:
				lay1 = Layer.PDIFF;
				lay2 = Layer.MET1;
				break;
			case 7:
				lay1 = Layer.MET1;
				lay2 = Layer.PDIFF;
				break;
			}
			auxWire = new Wire();
			interconnectWireTable.put(lay1.toString() + lay2.toString(), auxWire);
			Layer via = getViaLayer(lay1, lay2);

			//    Calculates the width of all the layers involved
			int widthVia = getMinWidth(via);
			int widthL1 = widthVia + 2 * getMinOverlaping(lay1, via);
			int widthL2 = widthVia + 2 * getMinOverlaping(lay2, via);

			auxWire.add(auxPt, 0, Layer.EMPTY);
			auxWire.add(auxPt, widthL1, lay1);
			auxWire.add(auxPt, widthVia, via);
			auxWire.add(auxPt, widthL2, lay2);
		}

		//    SHOULD BE CHANGED
		for (int aux1 = 0; aux1 < 2; aux1++) {
			switch (aux1) {
			case 0:
				lay1 = Layer.POLY;
				lay2 = Layer.MET2;
				break;
			case 1:
				lay1 = Layer.MET2;
				lay2 = Layer.POLY;
				break;
			}
			auxWire = new Wire();
			interconnectWireTable.put(lay1.toString() + lay2.toString(), auxWire);
			Layer via1 = getViaLayer(lay1, Layer.MET1);
			Layer via2 = getViaLayer(Layer.MET1, lay2);

			//    Calculates the width of all the layers involved
			int widthVia = getMinWidth(via1);
			int widthL1 = widthVia + 2 * getMinOverlaping(lay1, via1);
			int widthMET1 = widthVia + 2 * getMinOverlaping(Layer.MET1, via1);

			auxWire.add(auxPt, 0, Layer.EMPTY);
			auxWire.add(auxPt, widthL1, lay1);
			auxWire.add(auxPt, widthVia, via1);

			//    Second connection
			widthVia = getMinWidth(via2);
			widthMET1 = Math.min(widthMET1, widthVia + 2 * getMinOverlaping(Layer.MET1, via2));
			int widthL2 = widthVia + 2 * getMinOverlaping(lay2, via2);

			auxPt2.x = auxPt.x + getWiringMargin(via1, via2);
			auxPt2.y = auxPt.y;

			auxWire.add(auxPt2, widthMET1, Layer.MET1);
			auxWire.add(auxPt2, widthVia, via2);
			auxWire.add(auxPt2, widthL2, lay2);
		}
	}

	@Override
	public String toString() {
		return "Consultant: \nGridValue " + gridValue +
				"\n\nLayers Names: " + layersNames +
				"\n\nMin Width: " + minWidthTable +
				"\n\nMin Overlaping: " + minOverlapingTable +
				"\n\nMin Spacing: " + minSpacingTable;
	}

	public boolean unconnectableLayer(Layer layer) {

		// IT SHOULD BE ASKED IN THE DB
		return (layer.equals(Layer.CONT) || layer.equals(Layer.VIA));
	}
}
