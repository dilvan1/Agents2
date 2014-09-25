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
 *  RegLabel
 */

import java.awt.*;

class RegLabel extends layout.util.Pt implements Reg {

    String label;

    public RegLabel(String label, layout.util.Pt pt) {
        super(pt.x, pt.y);
        this.label = label;
    }

    public void paint(Graphics g, int dx, int dy, int zoom, Color c, boolean fill, int heightY) {
        g.setColor(c);
        g.drawString(label, ((x + dx) * zoom) / 128, (((heightY - y) + dy) * zoom) / 128);
    }
}
