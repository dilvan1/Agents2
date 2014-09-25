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

import java.io.IOException;
import java.io.InputStream;

public class LexicAnalizer {

    private int BIGGEST_WORD = 1024;
    private InputStream inpStream;
    private StringBuffer palavra = new StringBuffer();
    private String lexicClass;
    private int intNumber;

    private boolean putBackFlag = false;
    private char putBackChar;

    public LexicAnalizer(InputStream is) {
        inpStream = is;
    }

    public static boolean beginComment(char caracter) {
        return (caracter == ';');
    }

    public static boolean blanc(char caracter) {
        switch (caracter) {
            case ' ':
            case '\n':
            case '\r':
            case '\f':
            case '\b':
            case '\t':
                return true;
        }
        return false;
    }

    public static boolean endComment(char caracter) {
        return (caracter == '\n');
    }

    public static boolean eof(char caracter) {
        return (caracter == ((char) ((int) -1)));
    }

    private char getChar() throws IOException, LangException {
        char c;

        if (putBackFlag) {
            c = putBackChar;
            putBackFlag = false;
        } else {
            c = (char) inpStream.read();
            //System.out.print(c);
            //System.out.flush();
        }
        return c;
    }

    public int integer() {
        return intNumber;
    }

    public static boolean letter(char caracter) {
        if ((caracter >= 'A') && (caracter <= 'Z')) return true;
        if ((caracter >= '0') && (caracter <= '9')) return true;
        if ((caracter >= 'a') && (caracter <= 'z')) return true;
        switch (caracter) {
            case '_':
            case '-':
            case '.':
                return true;
        }
        return false;
    }

    public String lexicClass() {
        return lexicClass;
    }

    public static boolean number(char caracter) {
        switch (caracter) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '-':
                return true;
        }
        return false;
    }

    private void putBack(char c) {
        putBackFlag = true;
        putBackChar = c;
    }

    public void read() throws IOException, LangException {
        read("");
    }

    public void read(String expected) throws IOException, LangException {
        char caracter;
        long aux1;

        palavra.setLength(0);
        caracter = getChar();
        while (blanc(caracter) || beginComment(caracter)) {
            if (beginComment(caracter)) {
                while (!endComment(caracter) && !eof(caracter))
                    caracter = getChar();  // Takes out the comments
                caracter = getChar();
            }
            while (blanc(caracter)) caracter = getChar();   // Takes out the blancs
        }

        // Begin the lexical procedure

	  /* STRING */
        if (caracter == '"') {
            aux1 = 0;
            caracter = getChar();
            while (caracter != '"') {
                caracter = getChar();
                if (caracter == '\\') {
                    char carac2 = getChar();
                    switch (carac2) {
                        case 'n':
                            caracter = '\n';
                            break;
                        case '"':
                            caracter = '"';
                            break;
                        case 'r':
                            caracter = '\r';
                            break;
                        case 't':
                            caracter = '\t';
                            break;
                        case '0':
                            caracter = '\0';
                            break;
                        default:
                            palavra.append('\\');
                            aux1++;
                            caracter = carac2;
                    }
                }
                palavra.append(caracter);
                aux1++;
                if (aux1 >= BIGGEST_WORD)
                    throw new LangException("String too big");
            }
            lexicClass = "STRING";
        } else

	  /* INTEGER */
            if (number(caracter)) {
                aux1 = 0;
                do {
                    palavra.append(caracter);
                    aux1++;
                    if (aux1 >= BIGGEST_WORD)
                        throw new LangException("Number too big");
                    caracter = getChar();
                } while (number(caracter));
                try {
                    intNumber = Integer.parseInt(palavra.toString());
                } catch (NumberFormatException aux) {
                    throw new LangException("Ill formed integer: \"" + palavra.toString() + "\"");
                }
                lexicClass = "INTEGER";
                putBack(caracter);
//System.out.print("INTEGER " + palavra + " " + intNumber);
            } else

   /*  WORDS */
                if (letter(caracter)) {
                    aux1 = 0;
                    do {
                        palavra.append(caracter);
                        aux1++;
                        if (aux1 >= BIGGEST_WORD)
                            throw new LangException("Name too big");
                        caracter = getChar();
                    } while (letter(caracter));
                    lexicClass = "WORD";
                    putBack(caracter);
//System.out.print("WORD " + palavra + " " + intNumber);
                } else

	  /* EOF */
                    if (eof(caracter)) {
                        lexicClass = "EOF";
                    } else {

	  /* SIMBOL */
                        lexicClass = "SYMBOL";
                        palavra.append(caracter);
                    }

        // Test for expected class
        //
        if (!expected.equals("") && !lexicClass.equals(expected))
            throw new LangException(expected + " expected");
    }

    public static boolean realNumber(char caracter) {
        switch (caracter) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'E':
            case 'e':
            case '-':
            case '.':
                return true;
        }
        return false;
    }

    public void setInputStream(InputStream is) {
        inpStream = is;
    }

    public String word() {
        return palavra.toString();
    }
}
