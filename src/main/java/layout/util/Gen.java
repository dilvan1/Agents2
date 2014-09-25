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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class Gen
 */
public class Gen {

    /*
     *                very DANGEROUS function
     */
    static public Object deepCopy(Object obj) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buf);
        out.writeObject(obj);
        ObjectInputStream inp = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()));
        return inp.readObject();
    }

    static public List newList() {
        return new SVector();
    }

    static public List newList(int size) {
        return new SVector(size);
    }

    static public List newList(Object obj) {
        List lst = new SVector(1);
        lst.add(obj);
        return lst;
    }

    static public List newList(Object obj1, Object obj2) {
        List lst = new SVector(1);
        lst.add(obj1);
        lst.add(obj2);
        return lst;
    }

    static public List newList(Object obj1, Object obj2, Object obj3) {
        List lst = new SVector(1);
        lst.add(obj1);
        lst.add(obj2);
        lst.add(obj3);
        return lst;
    }

    static public List newList(Object obj1, Object obj2, Object obj3, Object obj4) {
        List lst = new SVector(1);
        lst.add(obj1);
        lst.add(obj2);
        lst.add(obj3);
        lst.add(obj4);
        return lst;
    }
}
