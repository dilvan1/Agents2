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

package layout.util;

/*
 *                Class InterPt
 */

import java.io.Serializable;

public class InterPt implements Serializable {

	boolean interFlag;
	double t;

	public InterPt() {
		interFlag = false;
	}

	public InterPt(boolean flag) {
		interFlag = flag;
	}

	public InterPt(boolean flag, double it) {
		interFlag = flag;
		t = it;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			InterPt ipt = (InterPt) obj;
			return (t == ipt.t && interFlag == ipt.interFlag);
		} catch (Exception e) {
			return false;
		}
	}

	public double getT() {
		return t;
	}

	public boolean isIntersec() {
		return interFlag;
	}

	@Override
	public String toString() {
		return "InterPt = " + interFlag + " " + t + " ";
	}
}
