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
 *  Bipolar
 */

import layout.util.Symbol;

import java.io.IOException;
import java.io.Writer;

public class Bipolar extends Component {

	public static Symbol COLECTOR = new Symbol("COLECTOR");
	public static Symbol BASE = new Symbol("BASE");
	public static Symbol EMITER = new Symbol("EMITER");

	public static Symbol PNP = new Symbol("PNP");
	public static Symbol NPN = new Symbol("NPN");

	Symbol tecn;

	public Bipolar(int refI, int numI, Symbol tecI) {
		super(refI, numI);
		tecn = tecI;
	}

	@Override
	public void drawOut(layout.display.Display out) {
		super.drawOut(out);
		layout.util.Rectangle env = getEnvelope();
		out.addLabel(String.valueOf(reference), env.c1);
	}

	@Override
	public boolean equals(Object obj) {
		try {
			return (super.equals(obj) && tecn.equals(((Bipolar) obj).tecn));
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void printEdif(Writer out) throws IOException {

		out.write("      (cell " + name + "\n" +
				"         (userData cellFunction " + tecn + ")" + "\n" +
				"         (view maskLayout Physical" + "\n" +
				"            (interface" + "\n" +
				"               (declare input port base)" + "\n" +
				"               (declare inout port (list colector emiter))" + "\n" +
				"               (portImplementation base" + "\n");

		for (Wire w : getTerms().get(Bipolar.BASE).getBody())
			w.printEdif(out);

		out.write("               ) \n" +
				"               (portImplementation colector \n");

		for (Wire w : getTerms().get(Bipolar.COLECTOR).getBody())
			w.printEdif(out);

		out.write("               ) \n" +
				"               (portImplementation emiter \n");

		for (Wire w : getTerms().get(Bipolar.EMITER).getBody())
			w.printEdif(out);

		out.write("               ) \n" +
				"            ) \n" +
				"            (contents \n");

		for (Wire w : getBody())
			w.printEdif(out);

		out.write("            ) \n" +
				"         ) \n" +
				"      ) \n");
	}

	@Override
	public String toString() {
		return "Bipolar Tecn- " + tecn + "\n" + super.toString();
	}
}
