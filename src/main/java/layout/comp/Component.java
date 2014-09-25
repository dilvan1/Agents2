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
 *  Component
 */

import layout.display.Display;
import layout.util.Rectangle;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

public class Component implements Serializable, Cloneable {

    public int reference;
    public int number;
    public String name;
    private Terms terms;
    private WireList body;

    public Component(int refInit, int numberInit) {
        reference = refInit;
        number = numberInit;
        terms = new Terms();
        terms.setOwner(this);
        body = new WireList();
        body.setOwner(this);
    }

    public Object clone() {
        Component cp = null;
        try {
            cp = (Component) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported  exception");
        }
        cp.terms = (Terms) terms.clone();
        cp.terms.setOwner(cp);
        cp.body = (WireList) body.clone();
        cp.body.setOwner(cp);
        return cp;
    }

    public void drawOut(Display out) {
        for (int aux1 = 0; aux1 < body.size(); aux1++)
            body.at(aux1).drawOut(out);
    }

    public boolean equals(Object obj) {
        try {
            Component cmp = (Component) obj;
            return (reference == cmp.reference && number == cmp.number &&
                    name.equals(cmp.name) && body.equals(cmp.body) &&
                    terms.equals(cmp.terms));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method was created in VisualAge.
     */
    public WireList getBody() {
        return body;
    }

    public static Component getComponentByWire(Wire wire) {
        try {
            Object owner;
            for (owner = wire.getOwner(); owner != null && !(owner instanceof Component); owner = ((Owned) owner).getOwner())
                ;
            return (Component) owner;
        } catch (Exception e) {
            throw new RuntimeException("Owning chain doesn't have a Component");
        }
    }
    //        method not used
    /*
	public InterPt getIntersectPoint(Linea pointer) {
		InterPt t= new InterPt(false, Double.MAX_VALUE);
		for (int aux1=0; aux1<body.size(); aux1++) {
			InterPt auxT= body.at(aux1).getIntersectPointer(pointer);
			if (auxT.isIntersec() && auxT.getT()<t.getT()) 
				t= auxT;
		}
		return t;
	}
	*/

    public Rectangle getEnvelope() {
        if (body.size() == 0)
            return null;
        Rectangle rec = body.at(0).getEnvelope();
        for (int aux1 = 1; aux1 < body.size(); aux1++) {
            Rectangle auxRec = body.at(aux1).getEnvelope();
            if (rec.c1.x > auxRec.c1.x) rec.c1.x = auxRec.c1.x;
            if (rec.c1.y > auxRec.c1.y) rec.c1.y = auxRec.c1.y;
            if (rec.c2.x < auxRec.c2.x) rec.c2.x = auxRec.c2.x;
            if (rec.c2.y < auxRec.c2.y) rec.c2.y = auxRec.c2.y;
        }
        return rec;
    }

    public Rectangle getEnvelope(Layer layer) {
        Rectangle rec = null;
        for (int aux1 = 0; aux1 < body.size(); aux1++) {
            Rectangle auxRec = body.at(aux1).getEnvelope(layer);
            if (auxRec == null) continue;
            if (rec == null) {
                rec = auxRec;
                continue;
            }
            if (rec.c1.x > auxRec.c1.x) rec.c1.x = auxRec.c1.x;
            if (rec.c1.y > auxRec.c1.y) rec.c1.y = auxRec.c1.y;
            if (rec.c2.x < auxRec.c2.x) rec.c2.x = auxRec.c2.x;
            if (rec.c2.y < auxRec.c2.y) rec.c2.y = auxRec.c2.y;
        }
        return rec;
    }

    public static Term getTermByWire(Wire wire) {
        try {
            Object owner;
            for (owner = wire.getOwner(); owner != null && !(owner instanceof Term); owner = ((Owned) owner).getOwner())
                ;
            return (Term) owner;
        } catch (Exception e) {
            throw new RuntimeException("Owning chain doesn't have a Term");
        }
    }

    /**
     * This method was created in VisualAge.
     */
    public Terms getTerms() {
        return terms;
    }

    public boolean isConnected(Component cmp) {
        for (int aux1 = 0; aux1 < terms.size(); aux1++) {
            for (int aux2 = 0; aux2 < cmp.terms.size(); aux2++) {
                if (terms.at(aux1).electricNode == cmp.terms.at(aux2).electricNode)
                    return true;
            }
        }
        return false;
    }

    public boolean isNumber(Integer i) {
        return (number == i.intValue());
    }

    public boolean isRef(Integer i) {
        return (reference == i.intValue());
    }

    public static boolean isSameTerminal(Wire w1, Wire w2) {
        try {
            return (((Owned) w1.getOwner()).getOwner() == ((Owned) w2.getOwner()).getOwner() &&
                    ((Owned) w1.getOwner()).getOwner() instanceof Term);
        } catch (Exception e) {
            return false;
        }
    }

    public void mirrorX() {
        for (int aux1 = 0; aux1 < terms.size(); aux1++) {
            for (int aux2 = 0; aux2 < terms.at(aux1).getBody().size(); aux2++) {
                terms.at(aux1).getBody().at(aux2).mirrorX();
            }
        }
        for (int aux1 = 0; aux1 < body.size(); aux1++) {
            body.at(aux1).mirrorX();
        }
    }

    public void mirrorY() {
        for (int aux1 = 0; aux1 < terms.size(); aux1++) {
            for (int aux2 = 0; aux2 < terms.at(aux1).getBody().size(); aux2++) {
                terms.at(aux1).getBody().at(aux2).mirrorY();
            }
        }
        for (int aux1 = 0; aux1 < body.size(); aux1++) {
            body.at(aux1).mirrorY();
        }
    }

    public void printEdif(Writer out) throws IOException {

        //          "         (userData cellFunction cell) \n" +

        out.write("      (cell " + name + "           \n" +
                "         (view maskLayout Physical \n" +
                "            (interface             \n");

        for (int aux1 = 0; aux1 < terms.size(); aux1++) {
            out.write("               (declare inout port " + terms.at(aux1).name + ")\n" +
                    "               (portImplementation " + terms.at(aux1).name + " \n");
            for (int aux2 = 0; aux2 < terms.at(aux1).getBody().size(); aux2++) {
                terms.at(aux1).getBody().at(aux2).printEdif(out);
            }
            out.write("               )\n");
        }
        out.write("            )         \n" +
                "            (contents \n");
        for (int aux1 = 0; aux1 < body.size(); aux1++) {
            body.at(aux1).printEdif(out);
        }
        out.write("            )\n" +
                "         )   \n" +
                "      )      \n");
    }

    public void rotate(int x, int y) {
        for (int aux1 = 0; aux1 < terms.size(); aux1++) {
            for (int aux2 = 0; aux2 < terms.at(aux1).getBody().size(); aux2++) {
                terms.at(aux1).getBody().at(aux2).rotate(x, y);
            }
        }
        for (int aux1 = 0; aux1 < body.size(); aux1++) {
            body.at(aux1).rotate(x, y);
        }
    }

    /**
     * This method was created in VisualAge.
     */
    public void setBody(WireList b) {
        body = b;
        body.setOwner(this);
    }

    public String toString() {
        return "Cmp Ref " + reference + " No " + number +
                " Name " + name + "Terms: " + terms + "Body: " + body;
    }

    public void translate(int x, int y) {
        for (int aux1 = 0; aux1 < terms.size(); aux1++) {
            for (int aux2 = 0; aux2 < terms.at(aux1).getBody().size(); aux2++) {
                terms.at(aux1).getBody().at(aux2).translate(x, y);
            }
        }
        for (int aux1 = 0; aux1 < body.size(); aux1++) {
            body.at(aux1).translate(x, y);
        }
    }
}
