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
 * Term Class
 */

import layout.util.Symbol;

import java.io.Serializable;

public class Term implements Owned, Cloneable, Serializable {
	public Symbol name;
	public int electricNode;
	private WireList body;
	private Object owner;

	public Term() {
		electricNode = -1;
		body = new WireList();
		body.setOwner(this);
	}

	@Override
	public Term clone() {
		Term trm = null;
		try {
			trm = (Term) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported  exception");
		}
		trm.body = (WireList) body.clone();
		trm.body.setOwner(trm);
		trm.setOwner(null);
		return trm;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			Term term = (Term) obj;
			return (name.equals(term.name) &&
					electricNode == term.electricNode && body.equals(term.body));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * This method was created in VisualAge.
	 */
	public WireList getBody() {
		return body;
	}

	@Override
	public Object getOwner() {
		return owner;
	}

	@Override
	public void setOwner(Object obj) {
		owner = obj;
	}

	@Override
	public String toString() {
		return "Term " + name + " " + electricNode + " " + body;
	}
}
