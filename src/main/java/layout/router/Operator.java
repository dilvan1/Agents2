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
 *        Operator
 */
//import java.beans.*;

import layout.comp.Layer;
import layout.util.Pt;

class Operator {
	String name;
	private Pt direction;
	int distance;
	String type;
	String stype;
	int component;
	int wire;
	int element;
	Layer fromLayer;
	Layer layer;
	String ret;

	//public void addPropertyChangeListener(PropertyChangeListener l) {}
	//public void removePropertyChangeListener(PropertyChangeListener l) {}
	Operator(String name) {
		this.name = name;
	}

	Operator(String name, Layer layer) {
		this.name = name;
		this.layer = layer;
	}

	Operator(String name, Pt direction) {
		this.name = name;
		this.direction = new Pt(direction);
	}

	public int getComponent() {
		return component;
	}

	public Pt getDirection() {
		return direction;
	}

	public int getDistance() {
		return distance;
	}

	public int getElement() {
		return element;
	}

	public Layer getFromLayer() {
		return fromLayer;
	}

	public Layer getLayer() {
		return layer;
	}

	public String getName() {
		return name;
	}

	public String getReturn() {
		return ret;
	}

	public String getStype() {
		return stype;
	}

	public String getType() {
		return type;
	}

	public int getWire() {
		return wire;
	}
}
