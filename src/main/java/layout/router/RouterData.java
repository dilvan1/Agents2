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

/*
 *        RouterData
 */

//import java.beans.*;

import layout.comp.DesignCmp;
import layout.comp.ElectricNode;
import layout.util.Pt;
import layout.util.Rectangle;

class RouterData {
	DesignCmp design;
	ElectricNode node;
	int subnet1;
	Rectangle envlp2;
	int changeCost;
	Pt targetPoint;
	int averageCost;
	//int           bestCost;
	int minCost;

	//    public void addPropertyChangeListener(PropertyChangeListener l) {}
	//    public void removePropertyChangeListener(PropertyChangeListener l) {}
	public int getAverageCost() {
		return averageCost;
	}

	public int getChangeCost() {
		return changeCost;
	}

	public DesignCmp getDesign() {
		return design;
	}

	public Rectangle getEnvlp2() {
		return envlp2;
	}

	public int getMinCost() {
		return minCost;
	}

	public ElectricNode getNode() {
		return node;
	}

	public int getSubnet1() {
		return subnet1;
	}

	public Pt getTargetPoint() {
		return targetPoint;
	}
}
