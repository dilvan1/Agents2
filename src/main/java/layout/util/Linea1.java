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
 *           Class Linea1
 */

import java.io.Serializable;

class Linea1 implements Serializable {

    public Pt c1;
    public Pt c2;

    Linea1() {
        c1 = new Pt();
        c2 = new Pt();
    }

    Linea1(int x1, int y1, int x2, int y2) {
        c1 = new Pt(x1, y1);
        c2 = new Pt(x2, y2);
    }

    Linea1(Pt p1, Pt p2) {
        c1 = new Pt(p1);
        c2 = new Pt(p2);
    }
//////////////////////////////////////////////////

    boolean coincident(Linea1 line) {
        //    Overlaping lines don't Intersect
        if (!isParallel(line))
            return false;
        if (isHorizontal())
            return (c1.y == line.c1.y);
        if (isVertical())
            return (c1.x == line.c1.x);
        throw new RuntimeException("No Manhattan not implemented");
    }

    public void compose(Pt point, Pt vec) {
        c1.set(point);
        c2.x = vec.x + c1.x;
        c2.y = vec.y + c1.y;
    }

    public Pt getCenter() {
        return new Pt((c1.x + c2.x) / 2, (c1.y + c2.y) / 2);
    }

    public Pt getPointOfT(double t) {
        return new Pt((int) Math.round(c1.x + getVectorX() * t),
                (int) Math.round(c1.y + getVectorY() * t));
    }

    public double getTCoord(Pt pt) {
        if (getVectorY() == 0.0) {
            if (pt.y != c1.y)
                throw new RuntimeException("Linea:: tCoord: Point isn't in this line");
            return ((pt.x - c1.x) * 1.0) / getVectorX();
        }
        if (getVectorX() == 0.0) {
            if (pt.x != c1.x)
                throw new RuntimeException("Linea:: tCoord: Point isn't in this line");
            return ((pt.y - c1.y) * 1.0) / getVectorY();
        }
        int t1 = ((pt.x - c1.x) * 1) / getVectorX();
        int t2 = ((pt.y - c1.y) * 1) / getVectorY();
        if (t1 != t2)
            throw new RuntimeException("Linea:: tCoord: Point isn't in this line");
        return t1;
    }

    public Pt getVector() {
        return new Pt(c2.x - c1.x, c2.y - c1.y);
    }

    int getVectorX() {
        return c2.x - c1.x;
    }

    int getVectorY() {
        return c2.y - c1.y;
    }

    boolean intersect(Linea1 line) {
        //    Overlaping lines don't Intersect
        return (!isParallel(line) || coincident(line));
    }

    InterPt intersectPt(Linea1 line) {
        //    Overlaping lines don't Intersect
        if (isParallel(line)) {
            return new InterPt(false);
        }

        int x2 = line.c1.x;
        int y2 = line.c1.y;
        int Vx2 = line.getVectorX();
        int Vy2 = line.getVectorY();

        int x1 = c1.x;
        int y1 = c1.y;
        int Vx1 = getVectorX();
        int Vy1 = getVectorY();

		/*   Solving the system:
         *   x= Vx1 t + x1     x= Vx2 t2 + x2
		 *   y= Vy1 t + y1     y= Vy2 t2 + y2
		 */
        return new InterPt(true, ((Vx2 * (y1 - y2) - Vy2 * (x1 - x2)) * 1.0) / (Vy2 * Vx1 - Vx2 * Vy1));
    }

    public boolean isHorizontal() {
        validate();
        return (c1.y == c2.y);
    }

    public boolean isManhattan() {
        return (c1.x == c2.x || c1.y == c2.y);
    }

    public boolean isParallel(Linea1 line) {

        // A parallel line can coinside as well
        validate();
        line.validate();
        return (getVectorY() * line.getVectorX() == getVectorX() * line.getVectorY());
    }

    public boolean isVertical() {
        validate();
        return (c1.x == c2.x);
    }

    public void rotateVector(int x, int y) {
        if ((x == 0 && y == 0) && (x != 0 && y != 0))
            throw new RuntimeException("Linea:: rotateVector:: Only Manhattan vectors allowed");
        Pt vect = getVector();
        if (x == 0 && y > 0) {
            int aux = vect.x;
            vect.x = (-vect.y);
            vect.y = (aux);
        } else if (x < 0 && y == 0) {
            vect.x = (-vect.x);
            vect.y = (-vect.y);
        } else if (x == 0 && y < 0) {
            int aux = vect.x;
            vect.x = (vect.y);
            vect.y = (-aux);
        } else
            return;
        setVector(vect);
    }

    public void rotateVector(Pt vec) {
        rotateVector(vec.x, vec.y);
    }

    public void setVector(Linea1 lin) {
        c2.x = (c1.x + lin.getVectorX());
        c2.y = (c1.y + lin.getVectorY());
    }

    public void setVector(Pt pt) {
        c2.x = (c1.x + pt.x);
        c2.y = (c1.y + pt.y);
    }

    public void validate() {
        if (c1.equals(c2))
            throw new RuntimeException("Coherence test fail");
    }
}
