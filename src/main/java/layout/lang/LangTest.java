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

package layout.lang;

import java.io.IOException;

public class LangTest {
	public static void main(String argv[]) throws IOException, LangException {
		LangEdif edif = new LangEdif(null);

		System.out.print(edif.eval(

				"(EDIF bicmos.cir                                                                                                                        \n" +
						"   (status (EDIFVersion 0 9 5) (EDIFLevel 0) )                                        \n" +
						"   (design bicmos (qualify bicmos mainCell))                                        \n" +
						"   (library bicmos                                                                                                                \n" +
						"      (technology es2                                                                                                        \n" +
						"         (numberDefinition MKS                                                                                \n" +
						"            (scale distance 1 (e 25 -8)))                                                \n" +
						"      )                                                                                                                                                \n" +
						"      (cell STDNODE99                                                                                                        \n" +
						"         (userData cellFunction node)                                                                \n" +
						"         (view maskLayout Physical                                                                        \n" +
						"            (interface                                                                                                        \n" +
						"               (declare inout port node) )                                                \n" +
						"            (contents                                                                                                        \n" +
						"               (figureGroup NDIFF (rectangle (point 58 33)  (point 86 45)))                \n" +
						"               (figureGroup MET1  (rectangle (point 40 56)  (point 412 98)))                \n" +
						"            )                                                                                                                                                                                        \n" +
						"         )                                                                                                                                                                                                \n" +
						"      )                                                                                                                                                                                                        \n" +
						"      (cell NPN                                                                                                                                                                                \n" +
						"         (userData cellFunction NPN)                                                                                                                        \n" +
						"         (view maskLayout Physical                                                                                                                                \n" +
						"            (interface                                                                                                                                                                \n" +
						"               (declare input port base)                                                                                                                \n" +
						"               (declare inout port (list colector emiter))                                                                \n" +
						"               (portImplementation base                                                                                                                \n" +
						"                  (figureGroup MET1 (rectangle (point 33 33)  (point 55 55))))        \n" +
						"               (portImplementation colector                                                                                                        \n" +
						"                  (figureGroup MET1 (rectangle (point 132 20) (point 154 68))))        \n" +
						"               (portImplementation emiter                                                                                                        \n" +
						"                  (figureGroup MET1 (rectangle (point 81 33)  (point 103 55))))        \n" +
						"            )                                                                                                                                                                                        \n" +
						"            (contents                                                                                                                                                                \n" +
						"               (figureGroup MET1 (rectangle (point 33 33)  (point 55 55)))                \n" +
						"               (figureGroup CONT (rectangle (point 40 40)  (point 48 48)))                \n" +
						"               (figureGroup MET1 (rectangle (point 132 20)  (point 154 68)))                \n" +
						"               (figureGroup CONT (rectangle (point 139 27)  (point 147 35)))                \n" +
						"               (figureGroup CONT (rectangle (point 139 53)  (point 147 61)))                \n" +
						"               (figureGroup MET1 (rectangle (point 81 33)  (point 103 55)))                \n" +
						"               (figureGroup CONT (rectangle (point 88 40)  (point 96 48)))                \n" +
						"               (figureGroup PDIFF (rectangle (point 33 33)  (point 55 55)))                \n" +
						"               (figureGroup NDIFF (rectangle (point 132 20)  (point 154 68)))        \n" +
						"               (figureGroup NDIFF (rectangle (point 81 33)  (point 103 55)))                \n" +
						"               (figureGroup PBASE (rectangle (point 20 20)  (point 116 68)))                \n" +
						"               (figureGroup BCCD (rectangle (point 0 0)  (point 162 88)))                        \n" +
						"               (figureGroup NWELL (rectangle (point 0 0)  (point 162 88)))                \n" +
						"            )                                                                                                                                \n" +
						"         )                                                                                                                                        \n" +
						"      )                                                                                                                                                \n" +
						"      (cell mainCell                                                                                                        \n" +
						"         (userData cellFunction main)                                                                \n" +
						"         (view symbolic Symbolical                                                                        \n" +
						"            (contents                                                                                                        \n" +
						"               (instance NPN Physical cmp0 (transform (translate 212 119) ))        \n" +
						"               (instance NPN Physical cmp1 (transform (translate 212 -9) ))        \n" +
						"               (instance STDNODE99 Physical cmp9 )                        \n" +
						"               (mustJoin                                                                                                 \n" +
						"                  (qualify cmp9 node)                                                                \n" +
						"                  (qualify cmp0 BASE)                                                         \n" +
						"                  (qualify cmp1 COLECTOR)                                                 \n" +
						"               )                                                                                                                        \n" +
						"               (mustJoin                                                                                                 \n" +
						"                  (qualify cmp9 node)                                                         \n" +
						"                  (qualify cmp0 EMITER)                                                         \n" +
						"                  (qualify cmp1 BASE)                                                         \n" +
						"                  (qualify cmp1 EMITER)                                                         \n" +
						"                  (qualify cmp0 COLECTOR)                                                 \n" +
						"               )                                                                                                                        \n" +
						"            )                                                                                                                                \n" +
						"         )                                                                                                                                        \n" +
						"      )                                                                                                                                                \n" +
						"   )                                                                                                                                                        \n" +
						")                                                                                                                                                                \n"
				));

		System.out.print("\n\nLibraries: " + edif.libraries + "\n\n");
		System.out.print("Technology: " + edif.technology + "\n\n");
		System.in.read();
	}
}
