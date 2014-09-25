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

public class Terms extends OwnedList {

    /**
     * Insert the method's description here.
     * Creation date: (9/15/2000 11:05:36 PM)
     */
    public void add(int ind, Object obj) {
        super.add(ind, (Term) obj);
    }

    public boolean add(Object obj) {
        return super.add((Term) obj);
    }

    public Term at(int pos) {
        return (Term) get(pos);
    }

    public Term at(Symbol ind) {
        int aux1;
        for (aux1 = 0; aux1 < size() && !ind.equals(at(aux1).name); aux1++) ;
        return at(aux1);
    }

    boolean isEqualBodies(Terms terms2) {
        int aux1;
        for (aux1 = 0; aux1 < size() &&
                aux1 < terms2.size() &&
                at(aux1).getBody().equals(terms2.at(aux1).getBody());
             aux1++)
            ;
        return (aux1 >= size() && aux1 >= terms2.size());
    }

    public Object set(int pos, Object obj) {
        return super.set(pos, (Term) obj);
    }
}
