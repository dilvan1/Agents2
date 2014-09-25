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
 *        Class SArrayList
 */

import java.util.Vector;


/**
 * Class SVector
 */

public class SVector extends Vector implements List, Cloneable {

    public SVector() {
    }

    public SVector(int size) {
        super(size);
    }

    public synchronized void invert() {
        int size = size();
        for (int aux1 = 0; aux1 < size / 2; aux1++) {
            Object obj = get(aux1);
            super.set(aux1, super.get(size - aux1 - 1));
            super.set(size - aux1 - 1, obj);
        }
    }

    public boolean isSlot(Object obj) {
        for (int aux1 = 0; aux1 < size(); aux1++) {
            try {
                if (lst(aux1).get(0).equals(obj))
                    return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    public List lst(int ind) {
        return (List) get(ind);
    }

    public List slot(Object obj) {
        for (int aux1 = 0; aux1 < size(); aux1++) {
            try {
                if (lst(aux1).get(0).equals(obj))
                    return lst(aux1);
            } catch (Exception e) {
            }
        }
        throw new RuntimeException("SList: No slot found");
    }
}
