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

package layout.placer;

/**
 * This type was created in VisualAge.
 */

import layout.util.Gen;
import layout.util.List;

class Gene {
	/*-
	 *                   Gene = [ [2  FLIP         [1 2 4 3]         dx  dy gap ] ... ]
	 *                 groupNumber-|  |-Flip flag  |- order of lines |   |   |- Gap between the groups parts
	 *                                                               |   |- Y coord of the group
	 *                                                               |- X coord of the group
	 */
	private final int groupNumber;
	private boolean flip;
	private final List linesOrder;
	private int dx;
	private int dy;
	private int gap;

	/**
	 * This method was created in VisualAge.
	 */
	Gene(Gene gene) {

		groupNumber = gene.groupNumber;
		flip = gene.flip;
		//this.linesOrder= linesOrder;
		dx = gene.dx;
		dy = gene.dy;
		gap = gene.gap;

		linesOrder = Gen.newList();
		for (int aux1 = 0; aux1 < gene.linesOrder.size(); aux1++)
			linesOrder.add(new Integer(((Number) gene.linesOrder.get(aux1)).intValue()));
	}

	/**
	 * This type was created in VisualAge.
	 */
	Gene(int groupNumber, boolean flip, List linesOrder, int dx, int dy, int gap) {
		this.groupNumber = groupNumber;
		this.flip = flip;
		this.linesOrder = linesOrder;
		this.dx = dx;
		this.dy = dy;
		this.gap = gap;
	}

	int getDx() {
		return dx;
	}

	int getDy() {
		return dy;
	}

	boolean getFlip() {
		return flip;
	}

	int getGap() {
		return gap;
	}

	int getGroupNumber() {
		return groupNumber;
	}

	List getLinesOrder() {
		return linesOrder;
	}

	/**
	 * This method was created in VisualAge.
	 */
	 void setDx(int dx) {
		 this.dx = dx;
	 }

	 /**
	  * This method was created in VisualAge.
	  */
	 void setDy(int dy) {
		 this.dy = dy;
	 }

	 /**
	  * This method was created in VisualAge.
	  */
	 void setFlip(boolean flip) {
		 this.flip = flip;
	 }

	 /**
	  * This method was created in VisualAge.
	  */
	 void setGap(int gap) {
		 this.gap = gap;
	 }
}
