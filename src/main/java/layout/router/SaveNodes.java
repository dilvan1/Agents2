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
 * This type was created in VisualAge.
 */

import java.util.*;
import layout.comp.DesignCmp;
import layout.comp.ElectricNode;
import layout.util.Gen;

class SaveNodes {
    List saveRefs;
    List saveAreas;
    List saveNodes;

    /**
     * This method was created in VisualAge.
     */
    List getAreas() {
        return saveAreas;
    }

    /**
     * This method was created in VisualAge.
     */
    List getReferences() {
        return saveRefs;
    }

    /**
     * This method was created in VisualAge.
     */
    void restore(DesignCmp design) {

        //        Put the old nodes back
        for (int aux1 = 0; aux1 < saveRefs.size(); aux1++) {
            ElectricNode node = (ElectricNode) design.getByReference(((Integer) saveRefs.get(aux1)).intValue());
            node.setRoutingData(saveNodes.get(aux1));
        }
        saveRefs = null;
        saveAreas = null;
        saveNodes = null;
    }

    /**
     * This method was created in VisualAge.
     *
     * param node ElectricNode
     */
    void unwire(DesignCmp design, List wireLst, List areaLst) {

        //	Test to see if space empty
        if (saveRefs != null || saveNodes != null)
            throw new RuntimeException("SaveNode not empty.");

        saveRefs = wireLst;
        saveAreas = areaLst;
        saveNodes = Gen.newList(wireLst.size());
        //        Save a copy of the nodes and unwire them
        for (int aux1 = 0; aux1 < saveRefs.size(); aux1++) {
            ElectricNode node = (ElectricNode) design.getByReference(((Integer) saveRefs.get(aux1)).intValue());
            saveNodes.add(node.getRoutingData());
            node.unwire();
        }
    }
}
