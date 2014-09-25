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
 *  RegRectangle
 */

import java.awt.*;

class RegRectangle extends layout.util.Rectangle implements Reg {

    Color color = null;

    public RegRectangle(layout.util.Rectangle rec1) {
        super(rec1.c1, rec1.c2);
    }

    public RegRectangle(layout.util.Rectangle rec1, Color c) {
        super(rec1.c1, rec1.c2);
        color = c;
    }

    public void paint(Graphics g, int dx, int dy, int zoom, Color c, boolean fill, int heightY) {
        int Ly = getLy();
        if (color == null) g.setColor(c);
        else g.setColor(color);
        if (fill)
            g.fillRect(((c1.x + dx) * zoom) / 128, ((heightY - c1.y - Ly + dy) * zoom) / 128, (getLx() * zoom) / 128, (Ly * zoom) / 128);
        else
            g.drawRect(((c1.x + dx) * zoom) / 128, ((heightY - c1.y - Ly + dy) * zoom) / 128, (getLx() * zoom) / 128, (Ly * zoom) / 128);
    }
}
