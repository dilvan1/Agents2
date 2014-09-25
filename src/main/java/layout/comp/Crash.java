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
 *        Class Crash
 */

import java.util.List;

public class Crash {

	Wire wire;
	int ind;
	int distance;
	List rec;

	public Crash(Wire w, int i) {
		wire = w;
		ind = i;
		distance = -1;
	}

	public Crash(Wire w, int i, int d) {
		wire = w;
		ind = i;
		distance = d;
	}

	public Crash(Wire w, int i, int d, List r) {
		wire = w;
		ind = i;
		distance = d;
		rec = r;
	}

	public int getDistance() {
		if (distance < 0) throw new RuntimeException("No distance, not a vector crash");
		return distance;
	}

	public int getInd() {
		return ind;
	}

	public List getNet() {
		return rec;
	}

	public Wire getWire() {
		return wire;
	}

	public WirePt getWirePt() {
		return wire.get(ind);
	}
}
