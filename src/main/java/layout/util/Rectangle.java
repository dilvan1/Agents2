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
 * Rectangle
 */

import java.io.Serializable;

public class Rectangle implements Serializable {

    public Pt c1;
    public Pt c2;

    public Rectangle() {
        c1 = new Pt();
        c2 = new Pt(1, 0);
    }

    public Rectangle(int p1, int p2, int p3, int p4) {
        c1 = new Pt(p1, p2);
        c2 = new Pt(p3, p4);
    }

    public Rectangle(Pt p1, int width) {
        c1 = new Pt();
        c2 = new Pt();
        set(p1, width);
    }

    public Rectangle(Pt pc1, Pt pc2) {
        c1 = new Pt(pc1);
        c2 = new Pt(pc2);
    }

    public Rectangle(Pt p1, Pt p2, int width) {
        c1 = new Pt();
        c2 = new Pt();
        set(p1, p2, width);
    }

    public Rectangle(Rectangle rec) {
        c1 = new Pt(rec.c1);
        c2 = new Pt(rec.c2);
    }

    public void coerent() {
        if (c1.equals(c2) || c1.x >= c2.x || c1.y >= c2.y)
            throw new RuntimeException("Rectangle:: Coherence test fail");
    }

    public boolean equals(Object obj) {
        try {
            Rectangle rec = (Rectangle) obj;
            return (c1.equals(rec.c1) && c2.equals(rec.c2));
        } catch (Exception e) {
            return false;
        }
    }

    public int getArea() {
        return getLx() * getLy();
    }

    public Pt getCenter() {
        return new Pt(c1.x + getLx() / 2, c1.y + getLy() / 2);
    }

    public int getDistanceX(Rectangle rec) {

        //                if   c1-----c2     rec.c1-----rec.c2()
        if (c2.x < rec.c1.x) return (rec.c1.x - c2.x);

        //                if   rec.c1-----rec.c2     c1-----c2()
        if (rec.c2.x < c1.x) return (c1.x - rec.c2.x);

        //                Overlap
        return 0;
    }

    public int getDistanceY(Rectangle rec) {

        //                if   c1-----c2     rec.c1-----rec.c2()
        if (c2.y < rec.c1.y) return (rec.c1.y - c2.y);

        //                if   rec.c1-----rec.c2     c1-----c2()
        if (rec.c2.y < c1.y) return (c1.y - rec.c2.y);

        //                Overlap
        return 0;
    }

    public InterPt getIntersectPt(Linea pointer) {
        coerent();
        Linea line1, line2;
        if (pointer.isHorizontal()) {
            if (c2.y < Math.min(pointer.c1.y, pointer.c2.y) ||
                    c1.y > Math.max(pointer.c1.y, pointer.c2.y)) {
                return new InterPt();
            }
            double b = pointer.c2.x - pointer.c1.x;
            double t1 = (c1.x - pointer.c1.x) / b;
            double t2 = (c2.x - pointer.c1.x) / b;
            if (t1 < 0 || t2 < 0)
                return new InterPt();
            return new InterPt(true, Math.min(t1, t2));
        }
        if (pointer.isVertical()) {
            if (c2.x < Math.min(pointer.c1.x, pointer.c2.x) ||
                    c1.x > Math.max(pointer.c1.x, pointer.c2.x)) {
                return new InterPt();
            }
            double b = pointer.c2.y - pointer.c1.y;
            double t1 = (c1.y - pointer.c1.y) / b;
            double t2 = (c2.y - pointer.c1.y) / b;
            if (t1 < 0 || t2 < 0)
                return new InterPt();
            return new InterPt(true, Math.min(t1, t2));
        }
        throw new RuntimeException("IntersectPointer: Vector isn't manhattan");
    }

    public InterPt getIntersectSeg(Segment seg) {
        coerent();
        if (seg.isHorizontal()) {
            if (c2.y < Math.min(seg.c1.y, seg.c2.y) ||
                    c1.y > Math.max(seg.c1.y, seg.c2.y)) {
                return new InterPt();
            }
            double b = seg.c2.x - seg.c1.x;
            double t1 = (c1.x - seg.c1.x) / b;
            double t2 = (c2.x - seg.c1.x) / b;
            if (t1 < 0 || t2 < 0 || t1 > 1 || t2 > 1)
                return new InterPt();
            return new InterPt(true, Math.min(t1, t2));
        }
        if (seg.isVertical()) {
            if (c2.x < Math.min(seg.c1.x, seg.c2.x) ||
                    c1.x > Math.max(seg.c1.x, seg.c2.x)) {
                return new InterPt();
            }
            double b = seg.c2.y - seg.c1.y;
            double t1 = (c1.y - seg.c1.y) / b;
            double t2 = (c2.y - seg.c1.y) / b;
            if (t1 < 0 || t2 < 0 || t1 > 1 || t2 > 1)
                return new InterPt();
            return new InterPt(true, Math.min(t1, t2));
        }
        throw new RuntimeException("Segment isn't manhattan");
    }

    public int getLx() {
        return (c2.x - c1.x);
    }

    public int getLy() {
        return (c2.y - c1.y);
    }

    public boolean isHorizontal() {
        return (getLx() >= getLy());
    }

    public boolean isHorizontalAligned(Rectangle rec) {
        coerent();
        rec.coerent();
        if (c2.y < rec.c1.y || c1.y > rec.c2.y) return false;
        return true;
    }

    public boolean isInside(Pt pt) {
        coerent();
        return (pt.x >= c1.x && pt.x <= c2.x && pt.y >= c1.y && pt.y <= c2.y);
    }

    public boolean isInside(Rectangle rec) {
        coerent();
        rec.coerent();
        return (rec.c1.x >= c1.x && rec.c1.y >= c1.y && rec.c2.x <= c2.x && rec.c2.y <= c2.y);
    }

    /*

    coerent();
    Linea line1, line2;
    if (pointer.isVertical()) {
        line1= new Linea(c1.x, c1.y, c2.x, c1.y);
        line2= new Linea(c1.x, c2.y, c2.x, c2.y);
    } else {
        if (!pointer.isHorizontal()) throw new
            RuntimeException("Rectangle:: IntersectPointer: Vector isn't manhattan");
        line1= new Linea(c1.x, c1.y, c1.x, c2.y);
        line2= new Linea(c2.x, c1.y, c2.x, c2.y);
    }
    InterPt tL1_Ptr= line1.getIntersectPt(pointer);
    if (tL1_Ptr.getT()>1 || tL1_Ptr.getT()<0) {
        tL1_Ptr.interFlag= false;
        return tL1_Ptr;
    }
    InterPt tPtr_L1= pointer.getIntersectPt(line1);
    InterPt tPtr_L2= pointer.getIntersectPt(line2);
    if (tPtr_L1.getT() >=0 && tPtr_L2.getT() >=0) {
        if (tPtr_L1.getT() < tPtr_L2.getT())
            return tPtr_L1;
        else
            return tPtr_L2;
    }
    tL1_Ptr.interFlag= false;
    return tL1_Ptr;
}
*/
    public boolean isIntersect(Segment seg) {
        if (!seg.isManhattan())
            throw new RuntimeException("Only orthogonal segments");
        coerent();
        if (c2.x < Math.min(seg.c1.x, seg.c2.x)) return false;
        if (c1.x > Math.max(seg.c1.x, seg.c2.x)) return false;
        if (c2.y < Math.min(seg.c1.y, seg.c2.y)) return false;
        if (c1.y > Math.max(seg.c1.y, seg.c2.y)) return false;
        return true;
    }

    public boolean isTouching(Rectangle rec) {
        if (c2.x < rec.c1.x || c1.x > rec.c2.x) return false;
        if (c2.y < rec.c1.y || c1.y > rec.c2.y) return false;
        return true;
    }

    public boolean isVertical() {
        return (getLx() <= getLy());
    }

    public boolean isVerticalAligned(Rectangle rec) {
        coerent();
        rec.coerent();
        if (c2.x < rec.c1.x || c1.x > rec.c2.x) return false;
        return true;
    }

    /*
            coerent();
            Segment  seg1, seg2;
            if (seg.isHorizontal()) {
                seg1= new Segment(c1.x, c1.y, c1.x, c2.y);
                seg2= new Segment(c2.x, c1.y, c2.x, c2.y);
            } else {
                if (!seg.isVertical())
                    throw new RuntimeException("Segment not ortogonal not implemented");
                seg1= new Segment(c1.x, c1.y, c2.x, c1.y);
                seg2= new Segment(c1.x, c2.y, c2.x, c2.y);
            }
            InterPt t1= seg.getIntersectPt(seg1);
            InterPt t2= seg.getIntersectPt(seg2);

            if (t1.isIntersec()) {
                if (t2.isIntersec()) {
                    t1.t= Math.min(t1.getT(), t2.getT());
                    return t1;
                }
                return t1;
            }
            return t2;
        }
    */
    public void rectify() {
        int x = Math.min(c1.x, c2.x);
        int y = Math.min(c1.y, c2.y);
        c2.x = (Math.max(c1.x, c2.x));
        c2.y = (Math.max(c1.y, c2.y));
        c1.x = (x);
        c2.y = (y);
        coerent();
    }

    public void set(Pt pt, int width) {
        width = width / 2;
        c1.x = (pt.x - width);
        c1.y = (pt.y - width);
        c2.x = (pt.x + width);
        c2.y = (pt.y + width);
        coerent();
    }

    public void set(Pt p1, Pt p2, int width) {
        int x1 = Math.min(p1.x, p2.x);
        int y1 = Math.min(p1.y, p2.y);
        int x2 = Math.max(p1.x, p2.x);
        int y2 = Math.max(p1.y, p2.y);

        width = width / 2;
        c1.x = (x1 - width);
        c1.y = (y1 - width);
        c2.x = (x2 + width);
        c2.y = (y2 + width);
        coerent();
    }

    public void set(Rectangle rec) {
        c1.set(rec.c1);
        c2.set(rec.c2);
    }

    public String toString() {
        return "Rec " + c1 + " " + c2;
    }
}
