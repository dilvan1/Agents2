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
 *             Class Symbol
 */

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Symbol implements Externalizable {

    private String str;

    public Symbol() {
    }

    public Symbol(String s) {
        str = s.intern();
    }

    public Symbol(Symbol s) {
        str = s.str;
    }

    public boolean equals(Object obj) {
        try {
            return str == ((Symbol) obj).str;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean equalsStr(String strg) {
        return str.equals(strg);
    }

    public void readExternal(ObjectInput inp) throws IOException, ClassNotFoundException {
        str = (String) inp.readObject();
        str = str.intern();
    }
    /*
    public boolean equals(Object obj) {
		try { return str==((Symbol) obj).str; }
		catch (Exception e) {
			try { return str.equals((String) obj);}
			catch (Exception e2) { return false;}
		}
	}
	*/

    public String toString() {
        return str;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(str);
    }
}
