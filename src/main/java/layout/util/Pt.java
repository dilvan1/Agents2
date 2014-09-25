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

/**
 *Class that represents a generic point x,y
 *
 *@author Dilvan Moreira
 *@version 1.0
 */

import java.io.Serializable;

public class Pt implements Serializable {

	public int x, y;

	public Pt() {
		x = 0;
		y = 0;
	}

	public Pt(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Pt(Pt pt) {
		x = pt.x;
		y = pt.y;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			Pt pt = (Pt) obj;
			return (x == pt.x && y == pt.y);
		} catch (Exception e) {
			return false;
		}
	}

	public int getManhattanDistance(Pt p1) {
		return (Math.abs(x - p1.x) + Math.abs(y - p1.y));
	}

	public void mirrorX() {
		y = -y;
	}

	public void mirrorY() {
		x = -x;
	}

	public void rotate(int x, int y) {
		int aux;
		if (x == 0 && y > 0) {
			aux = this.x;
			this.x = -this.y;
			this.y = aux;
		} else if (x < 0 && y == 0) {
			this.x = -this.x;
			this.y = -this.y;
		} else if (x == 0 && y < 0) {
			aux = this.x;
			this.x = this.y;
			this.y = -aux;
		} else
			throw new RuntimeException("Illegal rotation values");
		return;
	}

	public void rotate90() {
		int x;
		x = this.x;
		this.x = -y;
		y = x;
	}

	public void set(Pt pt) {
		x = pt.x;
		y = pt.y;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}

	public void translate(int x1, int y1) {
		x = x + x1;
		y = y + y1;
	}
}
