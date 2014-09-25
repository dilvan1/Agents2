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
 * Class WiringPoint
 */

public class WiringPoint {
    double dist;
    int ele1;
    int ele2;
    int wire1;
    int wire2;

    public WiringPoint() {
        dist = -100000;
        ele1 = 0;
        ele2 = 0;
        wire1 = -1;
        wire2 = -1;
    }

    public double getDist() {
        return dist;
    }

    public int getWire1() {
        return wire1;
    }

    public int getWire2() {
        return wire2;
    }

    public int getWirePt1() {
        return ele1;
    }

    public int getWirePt2() {
        return ele2;
    }
}
