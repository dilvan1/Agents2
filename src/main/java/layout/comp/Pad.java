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
 *  Pad
 */

import java.io.IOException;
import java.io.Writer;

public class Pad extends Component {
    public Pad(int refI, int num, int comp) {
        super(refI, num);
    }

    public void printEdif(Writer out) throws IOException {

        out.write("      (cell " + name + "\n" +
                "         (userData cellFunction pad)" + "\n" +
                "         (view maskLayout Physical" + "\n" +
                "            (interface" + "\n" +
                "               (declare inout port pad)" + "\n" +
                "               (portImplementation pad" + "\n");

        for (int aux1 = 0; aux1 < getTerms().at(0).getBody().size(); aux1++) {
            getTerms().at(0).getBody().at(aux1).printEdif(out);
        }

        out.write("               )" + "\n" +
                "            )" + "\n" +
                "            (contents" + "\n");

        for (int aux1 = 0; aux1 < getBody().size(); aux1++) {
            getBody().at(aux1).printEdif(out);
        }

        out.write("            )" + "\n" +
                "         )" + "\n" +
                "      )" + "\n");
    }

    public String toString() {
        return "Pad \n" + super.toString();
    }
}
