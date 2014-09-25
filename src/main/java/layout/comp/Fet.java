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
 *  Fet
 */

import layout.util.Symbol;

import java.io.IOException;
import java.io.Writer;

public class Fet extends Component {

    public static Symbol SOURCE = new Symbol("SOURCE");
    public static Symbol GATE = new Symbol("GATE");
    public static Symbol DRAIN = new Symbol("DRAIN");

    public static Symbol NMOS = new Symbol("NMOS");
    public static Symbol PMOS = new Symbol("PMOS");

    public Symbol tecn;
    boolean swapFlag = false;

    public Fet(int refI, int numI, Symbol tecI) {
        super(refI, numI);
        tecn = tecI;
    }

    public void drawOut(layout.display.Display out) {
        super.drawOut(out);
        layout.util.Rectangle env = getEnvelope();
        out.addLabel(String.valueOf(reference), env.c1);

        env.c1 = (layout.util.Pt) getTerms().at(DRAIN).getBody().at(0).at(0);
        out.addLabel("D", env.c1);
        env.c1 = (layout.util.Pt) getTerms().at(SOURCE).getBody().at(0).at(0);
        out.addLabel("S", env.c1);
    }

    public boolean equals(Object obj) {
        try {
            return (super.equals(obj) && tecn.equals(((Fet) obj).tecn));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isGateConnected(Fet fet) {
        return (getTerms().at(GATE).electricNode == fet.getTerms().at(GATE).electricNode);
    }

    public boolean isPassPair(Fet fet) {
        return (
                (getTerms().at(SOURCE).electricNode == fet.getTerms().at(SOURCE).electricNode &&
                        getTerms().at(DRAIN).electricNode == fet.getTerms().at(DRAIN).electricNode) ||
                        (getTerms().at(SOURCE).electricNode == fet.getTerms().at(DRAIN).electricNode &&
                                getTerms().at(DRAIN).electricNode == fet.getTerms().at(SOURCE).electricNode));
    }

    public boolean isSameFetTecn(Fet fet) {
        return (tecn.equals(fet.tecn));
    }

    public void printEdif(Writer out) throws IOException {
        out.write("      (cell " + name + "\n" +
                "         (userData cellFunction " + tecn + ")" + "\n" +
                "         (view maskLayout Physical" + "\n" +
                "            (interface" + "\n" +
                "               (declare input port gate)" + "\n" +
                "               (declare inout port (list source drain))" + "\n" +
                "               (permutable source drain)" + "\n" +
                "               (portImplementation gate" + "\n");
        for (int aux1 = 0; aux1 < getTerms().at(Fet.GATE).getBody().size(); aux1++) {
            getTerms().at(Fet.GATE).getBody().at(aux1).printEdif(out);
        }
        out.write("               )" + "\n" +
                "               (portImplementation drain" + "\n");
        for (int aux1 = 0; aux1 < getTerms().at(Fet.DRAIN).getBody().size(); aux1++) {
            getTerms().at(Fet.DRAIN).getBody().at(aux1).printEdif(out);
        }
        out.write("               )" + "\n" +
                "               (portImplementation source" + "\n");
        for (int aux1 = 0; aux1 < getTerms().at(Fet.SOURCE).getBody().size(); aux1++) {
            getTerms().at(Fet.SOURCE).getBody().at(aux1).printEdif(out);
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

    public void swapDS() {
        /*        In the future change for non permutable Fets
		 *                 W R O N G
		 *
		 *                THE PROGRAM CONSIDER ALWAYS THE DRAIN IN THE LEFT SIDE
		 *                AND THE SOURECE ON THE RIGHT SIDE
		 */
		/*
		 Int aux1;
	  
		 aux1= terms()[SOURCE].electricNode();
		 terms()[SOURCE].electricNode()= terms()[DRAIN].electricNode();
		 terms()[DRAIN].electricNode()= aux1;
		*/
        Term drain = getTerms().at(DRAIN);
        getTerms().at(SOURCE).name = DRAIN;
        drain.name = SOURCE;
        swapFlag = !swapFlag;
    }

    public String toString() {
        return "Fet Tecn- " + tecn + "\n" + super.toString();
    }
}
