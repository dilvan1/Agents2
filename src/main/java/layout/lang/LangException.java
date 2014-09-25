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

/////////////////////////////////////////////////////////////////////////////////////
//
//                     General library for reading files in any language
//    Derived from version on C (UNICAMP 11/08/90) derived from Prolog 
//                        (UNICAMP 29/01/90)
//
//                      UKC 29/09/91 - spicelib
//                      UKC 30/11/92
//                Versao Java UNICAMP 31/05/96 
//

//////////////////////////////////////////////////////////////////////////////////////
//             Class LexicException
//////////////////////////////////////////////////////////////////////////////////////

public class LangException extends Exception {

    public LangException() {
        super();
    }

    public LangException(String mes) {
        super(mes);
    }
}
