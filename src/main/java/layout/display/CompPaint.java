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

package layout.display;

/*
 *  CompPaint
 */

import java.awt.*;

class CompPaint extends Canvas {

	int width = 450, height = 300;
	Dimension size = new Dimension(width, height);
	Display1 disp;

	CompPaint(Display1 d) {
		disp = d;
	}

	@Override
	public Dimension getMinimumSize() {
		return size;
	}

	@Override
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	@Override
	public void paint(Graphics g) {

		//        Clear the area
		java.awt.Rectangle rec = g.getClipBounds();
		g.clearRect(rec.x, rec.y, rec.width, rec.height);

		int aux2 = 0;
		int aux1 = 0;
		for (aux1 = 0; aux1 < disp.layer.size(); aux1++) {
			if (!disp.showL.isIndexSelected(aux1)) continue;
			LayerSetup stp = (LayerSetup) disp.layer.get(aux1);

			boolean fill = disp.fillL.isIndexSelected(aux1);
			for (aux2 = 0; aux2 < disp.lst[aux1].size(); aux2++)
				((Reg) disp.lst[aux1].get(aux2)).paint(g, disp.dx, disp.dy, disp.zoom, stp.color, fill, disp.area.getLy());
		}

		for (aux2 = 0; aux2 < disp.extras.size(); aux2++)
			((Reg) disp.extras.get(aux2)).paint(g, disp.dx, disp.dy, disp.zoom, null, false, disp.area.getLy());
		//this.size.setSize(disp.area.getLx(), disp.area.getLy());
		size.setSize(width, height);
	}
}
