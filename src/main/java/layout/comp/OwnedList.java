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

import layout.util.SVector;

public class OwnedList<T extends Owned> extends SVector<T> implements Owned, Cloneable {

	private Object owner;

	@Override
	public void add(int pos, T obj) {
		super.add(pos, obj);
		own(obj);
	}

	@Override
	public boolean add(T obj) {
		if (super.add(obj)) {
			own(obj);
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		if (super.addAll(c)) {
			for (T name : c)
				own(name);
			return true;
		}
		if (c.isEmpty())
			return false;
		throw new RuntimeException("addAll failed.");
	}

	@Override
	public boolean addAll(int ind, Collection<? extends T> c) {
		if (super.addAll(ind, c)) {
			for (T name : c)
				own(name);
			return true;
		}
		if (c.isEmpty())
			return false;
		throw new RuntimeException("addAll failed.");
	}

	@Override
	public void clear() {
		for (int aux1 = 0; aux1 < size(); aux1++)
			get(aux1).setOwner(null);
		super.clear();
	}

	@Override
	public OwnedList<T> clone() {
		OwnedList<T> lst = (OwnedList<T>) super.clone();
		for (int aux1 = 0; aux1 < size(); aux1++) {
			T aux = (T) get(aux1).clone();
			lst.set(aux1, aux);
			get(aux1).setOwner(this);
		}
		lst.setOwner(null);
		return lst;
	}

	@Override
	public Object getOwner() {
		return owner;
	}

	private void own(Owned obj) {
		if (obj.getOwner() != null)
			throw new RuntimeException("Trying to own an owned object.");
		obj.setOwner(this);
	}

	@Override
	public T remove(int pos) {
		T obj = super.remove(pos);
		obj.setOwner(null);
		return obj;
	}

	public boolean remove(T obj) {
		if (super.remove(obj)) {
			if (obj != null)
				obj.setOwner(null);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection c) {
		throw new RuntimeException("Method not implemented.");
	}

	@Override
	public boolean retainAll(Collection c) {
		throw new RuntimeException("Method not implemented.");
	}

	@Override
	public T set(int pos, T obj) {
		T aux = super.set(pos, obj);
		aux.setOwner(null);
		own(obj);
		return aux;
	}

	@Override
	public void setOwner(Object obj) {
		owner = obj;
	}
}
