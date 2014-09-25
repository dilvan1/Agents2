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

package layout.router;

/**
 * Insert the type's description here.
 * Creation date: (9/17/2000 10:35:51 AM)
 * @author:
 */

import layout.comp.Wire;
import layout.util.List;

class BestWire {

    volatile private int cost;
    private Wire wire;
    private int net;
    private List unwired;

    public BestWire(Wire wire, int net, int cost) {
        this.wire = wire;
        this.net = net;
        this.cost = cost;
    }

    /**
     * BestWire constructor comment.
     */
    public BestWire(BestWire best, List unwired) {
        this.wire = best.wire;
        this.net = best.net;
        this.cost = best.cost;
        this.unwired = unwired;
    }

    public int getCost() {
        return cost;
    }

    public int getNet() {
        return net;
    }

    /**
     * Insert the method's description here.
     * Creation date: (9/17/2000 11:28:34 AM)
     */
    public List getUnwired() {
        return unwired;
    }

    public Wire getWire() {
        return wire;
    }
}
