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
 *  Dsp
 */

public class Dsp {

    static Display[] disp = new Display[5];
    static Display disp0 = new Display2();

    static public Display get1() {
        //return disp0;
        return getDisplay(1);
    }

    static public Display get2() {
        return disp0;
        //return getDisplay(2);
    }

    static public Display get3() {
        //return disp0;
        return getDisplay(3);
    }

    static public Display get4() {
        //return disp0;
        return getDisplay(4);
    }

    static Display getDisplay(int num) {
        if (disp[num] == null)
            disp[num] = getDisplay(String.valueOf(num));
        return disp[num];
    }

    /**
     * This method was created in VisualAge.
     */
    static public Display getDisplay(String name) {
        return new Display1(name);
    }

    static public void println(Object str) {
        //	System.out.println(str);
    }

    static public void printOperator(String str) {
        //System.out.println(str);
    }
}
