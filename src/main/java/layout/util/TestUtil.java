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

package layout.util;

/*
           Class TestGeome
 */

public class TestUtil {

    public static void main(String argv[]) {

        Rectangle rec = new Rectangle(5, 6, 5, 5);
        Pt ar[] = new Pt[9];
        Pt pt = new Pt(1, 2);
        Pt pt2 = new Pt(1, 2);
        pt.y = pt2.y;

        Object obj1, obj2;
        List vet = Gen.newList();

        vet.add(Gen.newList());
        ((List) vet.get(0)).add(new Pt());
        ((List) vet.get(0)).add(rec);

        List vet2 = Gen.newList();

        vet2.add(Gen.newList());
        ((List) vet2.get(0)).add(new Pt());
        ((List) vet2.get(0)).add(new Rectangle(5, 6, 5, 5));

//          vet2=vet;

        System.out.println("Array1 " + vet);
        System.out.println("Array2 " + vet2);

        if (vet.equals(vet2)) System.out.println(" Sao iguais");
        else System.out.println(" Sao diferentes");

        Rectangle r1 = new Rectangle(5, 6, 5, 5);
        Rectangle r2 = new Rectangle(5, 6, 5, 5);
        Object ob1 = r2;
        Object ob2 = r1;
        if (r1.equals(r2)) System.out.println("E igual");
        if (r1.equals(ob1)) System.out.println("E igual1");
        if (ob1.equals(r1)) System.out.println("E igual2");
        if (ob1.equals(ob2)) System.out.println("E igual3");
        try {
            System.in.read();
        } catch (Exception e) {
        }
    }
}
