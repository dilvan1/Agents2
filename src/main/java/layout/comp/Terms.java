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
 *        Terms
 */

import layout.util.Symbol;

public class Terms extends OwnedList<Term> {

	@Override
	public Terms clone() { return (Terms) super.clone();}

	public Term get(Symbol ind) {
		//        int aux1;
		//        for (aux1 = 0; aux1 < size() && !ind.equals(get(aux1).name); aux1++) ;
		//        return get(aux1);

		for (Term term: this)
			if (ind.equals(term.name))
				return term;
		return null;
	}

	boolean isEqualBodies(Terms terms2) {
		int aux1;
		for (aux1 = 0; aux1 < size() &&
				aux1 < terms2.size() &&
				get(aux1).getBody().equals(terms2.get(aux1).getBody());
				aux1++)
			;
		return (aux1 >= size() && aux1 >= terms2.size());
	}
}
