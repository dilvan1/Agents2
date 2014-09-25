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
 *           Class Linea
 */

public class Linea extends Linea1 {

    public Linea() {
    }

    public Linea(int x1, int y1, int x2, int y2) {
        super(x1, y1, x2, y2);
    }

    public Linea(Pt p1, Pt p2) {
        super(p1, p2);
    }

    public boolean equals(Object obj) {
        try {
            Linea lin = (Linea) obj;
            return (c1.equals(lin.c1) && c2.equals(lin.c2));
        } catch (Exception e) {
            return false;
        }
    }

    public InterPt getIntersectPt(Linea line) {
        return intersectPt(line);
    }

    public boolean isCoincident(Linea line) {
        return coincident(line);
    }

    public void set(Linea lin) {
        c1.set(lin.c1);
        c2.set(lin.c2);
    }

    public String toString() {
        return "Linea = " + c1 + "," + c2 + " ";
    }
}
