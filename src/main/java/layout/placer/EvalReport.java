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
class EvalReport {
	private final int numCrashes;
	private final int cost;

	EvalReport(int numCrashes, int cost) {
		this.numCrashes = numCrashes;
		this.cost = cost;
	}

	int getCost() {
		return cost;
	}

	int getNumCrashes() {
		return numCrashes;
	}

	/**
	 * This method was created in VisualAge.
	 */
	boolean isBetterThan(EvalReport report) {
		double total = (getNumCrashes() - report.getNumCrashes()) / (report.getNumCrashes() * 1.0);
		total = total + (getCost() - report.getCost()) / (report.getCost() * 1.0);
		return (total < 0);
	}
}
