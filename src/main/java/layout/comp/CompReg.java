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
 *        Class CompReg
 */

import layout.util.Symbol;

import java.io.Serializable;

public class CompReg implements Cloneable, Serializable {

	public Symbol termName;
	public int reference;
	public int routingNet;

	public CompReg() {
		reference = 0;
		routingNet = -1;
	}

	public CompReg(Symbol name, int ref) {
		termName = name;
		reference = ref;
		routingNet = -1;
	}

	@Override
	public CompReg clone() {
		try {
			return (CompReg) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		try {
			CompReg reg = (CompReg) obj;
			return (reference == reg.reference && termName.equals(reg.termName) &&
					routingNet == reg.routingNet);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Comp " + reference + " " + termName + " " + routingNet + " ";
	}
}
