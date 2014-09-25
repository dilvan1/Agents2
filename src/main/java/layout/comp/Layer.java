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
 * Layer
 */

import layout.util.Symbol;

import java.io.Externalizable;
import java.util.Hashtable;
import java.util.List;

public class Layer extends Symbol implements Externalizable {

	static public Layer getLayer(String str) {
		Layer l = (Layer) layers.get(str);
		if (l != null) return l;
		throw new RuntimeException("Layer: Invalid layer.");
	}

	static public void makeLayers(List lays) {
		//if (layers.size()>8) throw new RuntimeException("Layer: Can't make layers.");
		for (int aux1 = 0; aux1 < lays.size(); aux1++)
			new Layer(((String) lays.get(aux1)).toUpperCase());
	}

	static Hashtable layers = new Hashtable(); //has to come first

	public static Layer EMPTY = new Layer("EMPTY");
	public static Layer NDIFF = new Layer("NDIFF");
	public static Layer PDIFF = new Layer("PDIFF");
	public static Layer POLY = new Layer("POLY");
	public static Layer CONT = new Layer("CONT");
	public static Layer MET1 = new Layer("MET1");
	public static Layer VIA = new Layer("VIA");
	public static Layer MET2 = new Layer("MET2");

	public static Layer PWELL = new Layer("PWELL");

	public static Layer NWELL = new Layer("NWELL");

	public Layer() {
	}

	private Layer(String name) {
		super(name);
		if (layers.get(name) == null)
			layers.put(name, this);
	}
}
