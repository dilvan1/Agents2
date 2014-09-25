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
 *        Class NoCrash
 */

import layout.util.Rectangle;

public class NoCrash {

	Wire wire;
	Rectangle rec;
	Layer layer;

	public NoCrash(Wire wire, int ind) {
		this.wire = wire;
		rec = new Rectangle(wire.get(ind - 1), wire.get(ind), wire.get(ind).width());
		layer = wire.get(ind).layer();
	}

	public NoCrash(Wire wire, Rectangle rec, Layer layer) {
		this.wire = wire;
		this.rec = new Rectangle(rec);
		this.layer = layer;
	}

	public Layer getLayer() {
		return layer;
	}

	public Rectangle getRectangle() {
		return rec;
	}

	public Wire getWire() {
		return wire;
	}
}
