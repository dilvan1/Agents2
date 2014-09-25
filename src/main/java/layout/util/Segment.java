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
 *           Class Segment
 */

public class Segment extends Linea1 {

	public Segment() {
		super();
	}

	public Segment(int x1, int y1, int x2, int y2) {
		super(x1, y1, x2, y2);
	}

	public Segment(Pt p1, Pt p2) {
		super(p1, p2);
	}

	@Override
	public boolean equals(Object obj) {
		try {
			Segment lin = (Segment) obj;
			return (c1.equals(lin.c1) && c2.equals(lin.c2));
		} catch (Exception e) {
			return false;
		}
	}

	private int getDistanceIntervals(int l1x, int l1y, int l2x, int l2y) {
		Pt l1 = new Pt(Math.min(l1x, l1y), Math.max(l1x, l1y));
		Pt l2 = new Pt(Math.min(l2x, l2y), Math.max(l2x, l2y));

		//                if   l1 x-----y     l2 x-----y
		if (l1.y < l2.x) return (l2.x - l1.y);

		//                if   l2 x-----y     l1 x-----y
		if (l2.y < l1.x) return (l1.x - l2.y);

		//                Overlap
		return 0;
	}

	public InterPt getIntersectPt(Segment seg) {
		seg.validate();
		//        InterPt intPt= ((Linea)seg).getIntersectPt(this);
		InterPt intPt = seg.intersectPt(this);
		if (intPt.isIntersec()) {
			if (intPt.getT() >= 0 && intPt.getT() <= 1) {
				intPt = intersectPt(seg);
				intPt.interFlag = (intPt.getT() >= 0 && intPt.getT() <= 1);
				return intPt;
			}
			intPt.interFlag = false;
			return intPt;
		}
		if (!coincident(seg))                 // Test if parallel
		return intPt;
		double t1 = getTCoord(seg.c1);
		double t2 = getTCoord(seg.c2);
		if ((t1 < 1.0 && t1 > 0.0) || (t2 < 1.0 && t2 > 0.0))
			return intPt; // Overlapping
		if (t1 == 0.0 || t2 == 0.0) {
			intPt.t = 0.0;
			intPt.interFlag = true;
			return intPt;
		}
		if (t1 == 1.0 || t2 == 1.0) {
			intPt.t = 1.0;
			intPt.interFlag = true;
			return intPt;
		}
		return intPt;
	}

	public double getLength() {
		return Math.sqrt(Math.pow(c2.x - c1.x, 2) + Math.pow(c2.y - c1.y, 2));
	}

	public double getManhattanDistance(Pt pt) {
		Linea1 auxLine = new Linea1();
		auxLine.c1.set(pt);
		auxLine.setVector(this);
		auxLine.rotateVector(0, 1);
		InterPt tp1 = intersectPt(auxLine);
		InterPt tp2 = auxLine.intersectPt(this);
		double dist = Math.abs(getLength() * tp2.getT());
		if (tp1.getT() < 0)
			return dist + getLength() * (-tp1.getT());
		if (tp1.getT() > 1)
			return dist + getLength() * (tp1.getT() - 1);
		return dist;
	}

	public double getManhattanDistance(Segment seg) {
		InterPt ip1 = intersectPt(seg);
		if (!ip1.isIntersec()) {
			if (isHorizontal())
				return getDistanceIntervals(c1.x, c2.x, seg.c1.x, seg.c2.x) + Math.abs(c1.y - seg.c1.y);
			if (isVertical())
				return getDistanceIntervals(c1.y, c2.y, seg.c1.y, seg.c2.y) + Math.abs(c1.x - seg.c1.x);
			throw new RuntimeException("Segment:: manhattanDistance: No Manhattan not implemented");
		}
		InterPt ip2 = seg.intersectPt(this);

		double dist = 0;
		if (ip2.getT() < 0) dist = seg.getLength() * (-ip2.getT());
		else if (ip2.getT() > 1) dist = seg.getLength() * (ip2.getT() - 1);
		if (ip1.getT() < 0) return dist + getLength() * (-ip1.getT());
		if (ip1.getT() > 1) return dist + getLength() * (ip1.getT() - 1);
		return dist;
	}

	public boolean isIntersect(Segment seg) {
		/*
		 *        Test first if for a manhattan segments connects
		 *        This is done because the majority of segments in
		 *        this application are manhattan style and it will
		 *        speed up the rotine for no crossing manhattan and
		 *        far way common segments (which are the majority of cases).
		 */
		if (Math.max(c1.x, c2.x) < Math.min(seg.c1.x, seg.c2.x) ||
				Math.min(c1.x, c2.x) > Math.max(seg.c1.x, seg.c2.x)) return false;
		if (Math.max(c1.y, c2.y) < Math.min(seg.c1.y, seg.c2.y) ||
				Math.min(c1.y, c2.y) > Math.max(seg.c1.y, seg.c2.y)) return false;

		//	It seems not useful code and will brake for segm. points
		//if (isHorizontal() && seg.isHorizontal())
		//	return (c1.y==seg.c1.y);
		//if (isVertical() && seg.isVertical())
		//	return (c1.x==seg.c1.x);

		if (isManhattan() && seg.isManhattan()) return true;

		//  There should not be any no Manhatan segments
		throw new RuntimeException("Only Manhatan segments allowed.");
		/*
		//                If not ortogonals
		if (getIntersectPt(seg).isIntersec()==true) return true;
		if (isOverlaping(seg)) return true;
		return false;
		 */
	}

	public boolean isOverlaping(Segment seg) {
		if (coincident(seg)) {
			double t1 = getTCoord(seg.c1);
			double t2 = getTCoord(seg.c2);
			return ((t1 < 1.0 && t1 > 0.0) || (t2 < 1.0 && t2 > 0.0));
		} else
			return false;
	}

	public void set(Segment lin) {
		c1.set(lin.c1);
		c2.set(lin.c2);
	}

	@Override
	public String toString() {
		return "Segment = (" + c1 + "," + c2 + " ";
	}
}
