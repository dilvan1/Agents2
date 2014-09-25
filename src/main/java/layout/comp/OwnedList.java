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
 * Class OwnedList
 */

import java.util.Collection;
import java.util.Iterator;
import layout.util.SVector;

public class OwnedList extends SVector implements Owned, Cloneable {

    private Object owner;

    public void add(int pos, Object obj) {
        super.add(pos, obj);
        own(obj);
    }

    public boolean add(Object obj) {
        if (super.add(obj)) {
            own(obj);
            return true;
        }
        return false;
    }

    public boolean addAll(int ind, Collection c) {
        if (super.addAll(ind, c)) {
            for (Iterator i = c.iterator(); i.hasNext(); ) {
                own(i.next());
            }
            return true;
        }
        if (c.isEmpty())
            return false;
        throw new RuntimeException("addAll failed.");
    }

    public boolean addAll(Collection c) {
        if (super.addAll(c)) {
            for (Iterator i = c.iterator(); i.hasNext(); ) {
                own(i.next());
            }
            return true;
        }
        if (c.isEmpty())
            return false;
        throw new RuntimeException("addAll failed.");
    }

    public void clear() {
        for (int aux1 = 0; aux1 < size(); aux1++) {
            ((Owned) get(aux1)).setOwner(null);
        }
        super.clear();
    }

    public Object clone() {
        OwnedList lst = (OwnedList) super.clone();
        for (int aux1 = 0; aux1 < size(); aux1++) {
            Owned aux = (Owned) ((Owned) get(aux1)).clone();
            lst.set(aux1, aux);
            ((Owned) get(aux1)).setOwner(this);
        }
        lst.setOwner(null);
        return lst;
    }

    public Object getOwner() {
        return owner;
    }

    private void own(Object obj) {
        if (((Owned) obj).getOwner() != null)
            throw new RuntimeException("Trying to own an owned object.");
        ((Owned) obj).setOwner(this);
    }

    public Object remove(int pos) {
        Owned obj = (Owned) super.remove(pos);
        obj.setOwner(null);
        return obj;
    }

    public boolean remove(Object obj) {
        if (super.remove(obj)) {
            if (obj != null)
                ((Owned) obj).setOwner(null);
            return true;
        }
        return false;
    }

    public boolean removeAll(Collection c) {
        throw new RuntimeException("Method not implemented.");
    }

    public boolean retainAll(Collection c) {
        throw new RuntimeException("Method not implemented.");
    }

    public Object set(int pos, Object obj) {
        Owned aux = (Owned) super.set(pos, obj);
        aux.setOwner(null);
        own(obj);
        return aux;
    }

    public void setOwner(Object obj) {
        owner = obj;
    }
}
