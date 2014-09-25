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
 *             Class LayerEdif
 */

import layout.lang.LangEdif;
import layout.lang.LangException;
import layout.util.List;

import java.io.InputStream;

class LayerEdif extends LangEdif {

    Consultant c;

    public LayerEdif(InputStream in, Consultant c1) {
        super(in);
        c = c1;
    }
    /*
	 *    Commands to read the Rules file
	 */

    protected Object command(String name, List list) throws LangException {
        if (name.equals("GRIDVALUE")) return edif_gridValue(list);
        else if (name.equals("LAYERSNAMES")) return edif_layersNames(list);
        else if (name.equals("MINWIDTH")) return edif_minWidth(list);
        else if (name.equals("MINSPACING")) return edif_minSpacing(list);
        else if (name.equals("MINOVERLAPING")) return edif_minOverlaping(list);
        else if (name.equals("RULESDB")) return edif_rulesDB(list);
        return super.command(name, list);
    }

    /* Input:   (GRIDVALUE :Real)
     * Output:  :Null
     */
    Object edif_gridValue(List list) throws LangException {
        try {
            c.gridValue = ((Double) list.get(0)).doubleValue();
            c.convDist = 1 / c.gridValue;
            return null;
        } catch (RuntimeException re) {
            throw new LangException("edif GRIDVALUE: Should have a float");
        }
    }

    /* Input:   (LAYERSNAMES :List)
     * Output:  :Null
     */
    Object edif_layersNames(List list) throws LangException {
        try {
            c.layersNames = ((List) list.get(0));
            return null;
        } catch (RuntimeException re) {
            throw new LangException("edif LAYERSNAMES: Should have a list of layers");
        }
    }

    /* Input:   (MINOVERLAPING :String :String :Real)
     * Output:  :Null
     */
    Object edif_minOverlaping(List list) throws LangException {
        try {
            int res = (int) Math.ceil(((Double) list.get(2)).doubleValue() * c.convDist);
            if (res < 0) res = -1;
            c.minOverlapingTable.put((String) list.get(0) + (String) list.get(1), new Integer(res));
            return null;
        } catch (RuntimeException re) {
            throw new LangException("edif MINOVERLAPING: Should have 2 layer and a float");
        }
    }

    /* Input:   (MINSPACING :String :String :Real)
     * Output:  :Null
     */
    Object edif_minSpacing(List list) throws LangException {
        try {
            int res = (int) Math.ceil(((Double) list.get(2)).doubleValue() * c.convDist);
            if (res < 0) res = -1;
            c.minSpacingTable.put((String) list.get(0) + (String) list.get(1), new Integer(res));
            return null;
        } catch (RuntimeException re) {
            throw new LangException("edif MINSPACING: Should have 2 layer and a float");
        }
    }

    /* Input:   (MINWIDTH :String :Real)
     * Output:  :Null
     */
    Object edif_minWidth(List list) throws LangException {
        try {
            int res = (int) Math.ceil(((Double) list.get(1)).doubleValue() * c.convDist);
            if (res < 0) res = -1;
            c.minWidthTable.put((String) list.get(0), new Integer(res));
            return null;
        } catch (RuntimeException re) {
            throw new LangException("edif MINWIDTH: Should have 1 layer and a float");
        }
    }

    /* Input:   (RULESDB)
     * Output:  :Null
     */
    Object edif_rulesDB(List list) throws LangException {
        return null;
    }
}
