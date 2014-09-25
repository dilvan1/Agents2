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

/*
 *       AbuttedAgent Class
 */

import layout.comp.Bipolar;
import layout.comp.Fet;
import layout.util.Gen;
import layout.util.List;
import layout.util.Symbol;

class AbuttedAgent {

    PlacerExpert placer;
    AbuttedAgent other;
    List comps;
    List retList;

    AbuttedAgent(PlacerExpert exp) {
        placer = exp;
        comps = Gen.newList();
    }

    boolean allConnected(List list) {
        /*-
		 *                Test all lines are connect and returns a ordered list
		 *                One line one connection
		 *
		 *                list= [ [2 5] [2  3 4] [3  2] ]
		 *                    | |
		 *                    | | possible lines in B
		 *
		 *           Returns list= [ [5] [4] [2] ]
		 */

        //                It test all lines in list
        boolean lastFlag = false;
        for (int aux1 = 0; aux1 < list.size(); aux1++) {
            List l1 = (List) list.get(aux1);
			/*-
			 *                It tests all possible numbers, but the last, to discover if
			 *                they appear only once in all list.
			 */
            int aux2, auxNext;
            for (aux2 = 0, auxNext = 1;
                 auxNext < l1.size() && !noNumber((Integer) l1.get(aux2), list, aux1);
                 auxNext++, aux2++)
                ;
			/*-
			 *                Clear all the numbers but the one that appears only once or
			 *                the last
			 */
            lastFlag = (auxNext >= l1.size());
            Object auxNum = l1.remove(aux2);
            l1.clear();
            l1.add(auxNum);
			/*-
			 *                If the number cleared was the last, clear all occurrences
			 *                of it in the remaining list [aux1 onwards]
			 */
            if (!lastFlag) continue;
            for (aux2 = aux1, aux2++; aux2 < list.size(); aux2++) {
                List l2 = (List) list.get(aux2);
                int aux3;
                for (aux3 = 0; aux3 < l2.size() && !l2.get(aux3).equals(l1.get(0)); aux3++) ;
                if (aux3 < l2.size()) {
                    l2.remove(aux3);

                    //                If the list empties there isn't enough connections
                    if (l2.isEmpty()) return false;
                }
            }
        }
        return true;
    }

    void behaviorAbutted() {
        expert("ABUTTED");
    }

    List behaviorGroup(AbuttedAgent abut) {
	/*-
	 *                report= [ [[1 2] [2 4] [4 1]]  SOURCE DRAIN]
	 *                report= [ [[1 2] [2 1]]        SOURCE]
	 *                report= [ [[1 2] [2 1] [3 4]]  ]
	 *                           Connection order    Sides
	 */
        retList = null;
        other = abut;
        expert("GROUP");
        return retList;
    }

    void expert(String behavior) {

        //        First group of rules
        if (behavior.equals("ABUTTED")) {

            //RULE(        "Abuted*propose*operator*gateConected",
            if (comps.isEmpty() &&
                    (!placer.fetsPmos.isEmpty() || !placer.fetsNmos.isEmpty())) {
                if (!placer.fetsPmos.isEmpty()) {
                    comps.add(placer.fetsPmos.remove(0));

                    //                For the Pmos fets
                    for (int aux1 = 0; aux1 < placer.fetsPmos.size(); ) {
                        if (((Fet) placer.fetsPmos.get(aux1)).isGateConnected((Fet) comps.get(0)))
                            comps.add(placer.fetsPmos.remove(aux1));
                        else
                            aux1++;
                    }
                } else
                    comps.add(placer.fetsNmos.remove(0));

                //                For the Nmos fets
                for (int aux1 = 0; aux1 < placer.fetsNmos.size(); ) {
                    if (((Fet) placer.fetsNmos.get(aux1)).isGateConnected((Fet) comps.get(0)))
                        comps.add(placer.fetsNmos.remove(aux1));
                    else
                        aux1++;
                }
            }

            // RULE(        "Abuted*propose*operator*passPair",
            if (comps.size() == 1) {
                if (!placer.fetsPmos.isEmpty()) {
                    //Check this
                    //                 comps.ap(cont().fetsPmos().pick(0));

                    //                For the Pmos fets
                    for (int aux1 = 0; aux1 < placer.fetsPmos.size(); ) {
                        if (((Fet) placer.fetsPmos.get(aux1)).isPassPair((Fet) comps.get(0)))
                            comps.add(placer.fetsPmos.remove(aux1));
                        else
                            aux1++;
                    }
                }
                // else
                //            comps.ap(cont().fetsNmos().pick(0));

                //                For the Nmos fets
                for (int aux1 = 0; aux1 < placer.fetsNmos.size(); ) {
                    if (((Fet) placer.fetsNmos.get(aux1)).isPassPair((Fet) comps.get(0)))
                        comps.add(placer.fetsNmos.remove(aux1));
                    else
                        aux1++;
                }
            }

            //RULE(        "Abuted*propose*operator*bipolarPair",
            if (comps.isEmpty() &&
                    placer.fetsPmos.isEmpty() &&
                    placer.fetsNmos.isEmpty() &&
                    !placer.bipolars.isEmpty()
                    ) {
                comps.add(placer.bipolars.remove(0));

                //                For the Bipolars
                for (int aux1 = 0; aux1 < placer.bipolars.size(); ) {
                    if (((Bipolar) placer.bipolars.get(aux1)).isConnected((Bipolar) comps.get(0)))
                        comps.add(placer.bipolars.remove(aux1));
                    else
                        aux1++;
                }
            }

            //RULE(        "Abuted*propose*operator*otherCells",
            if (comps.isEmpty() &&
                    placer.fetsPmos.isEmpty() &&
                    placer.fetsNmos.isEmpty() &&
                    placer.bipolars.isEmpty() &&
                    !placer.cells.isEmpty()
                    ) {
                comps.add(placer.cells.remove(0));
            }

            //RULE("Abuted*Reproduce",
            if (!placer.fetsPmos.isEmpty() ||
                    !placer.fetsNmos.isEmpty() ||
                    !placer.bipolars.isEmpty() ||
                    !placer.cells.isEmpty()
                    ) {
                reproduce();
            }
            return;
        }

        //        Second group of rules
        else if (behavior.equals("GROUP")) {
            List lstTest = Gen.newList();

            // RULE(        "Abuted*implement*operator*abortEval*NoFetGroups",
            if (!(comps.lst(0).get(0) instanceof Fet) ||
                    !(other.comps.lst(0).get(0) instanceof Fet)) {
                return;
            }

            // RULE(        "Abuted*implement*operator*evalGroups*SingCol&SingCol",
            if (comps.size() == 1 &&
                    other.comps.size() == 1) {
                lstTest.clear();
                lstTest.add(Gen.newList(Fet.DRAIN, Fet.DRAIN));
                lstTest.add(Gen.newList(Fet.DRAIN, Fet.SOURCE));
                lstTest.add(Gen.newList(Fet.SOURCE, Fet.SOURCE));
                lstTest.add(Gen.newList(Fet.SOURCE, Fet.DRAIN));
                retList = testConn(comps.lst(0), other.comps.lst(0), lstTest);
                return;
            }

            // RULE(        "Abuted*implement*operator*evalGroups*MultCol&SingCol",
            if (comps.size() > 1 &&
                    other.comps.size() == 1) {
                lstTest.clear();
                lstTest.add(Gen.newList(Fet.DRAIN, Fet.DRAIN));
                lstTest.add(Gen.newList(Fet.DRAIN, Fet.SOURCE));
                retList = testConn(comps.lst(0), other.comps.lst(0), lstTest);
                if (retList == null) {
                    lstTest.clear();
                    lstTest.add(Gen.newList(Fet.SOURCE, Fet.DRAIN));
                    lstTest.add(Gen.newList(Fet.SOURCE, Fet.SOURCE));
                    retList = testConn(comps.lst(comps.size() - 1), other.comps.lst(0), lstTest);
                }
                return;
            }

            // RULE(        "Abuted*implement*operator*evalGroups*SingCol&MultCol",
            if (comps.size() == 1 &&
                    other.comps.size() > 1) {
                lstTest.clear();
                lstTest.add(Gen.newList(Fet.DRAIN, Fet.DRAIN));
                lstTest.add(Gen.newList(Fet.SOURCE, Fet.DRAIN));
                retList = testConn(comps.lst(0), other.comps.lst(0), lstTest);
                if (retList == null) {
                    lstTest.clear();
                    lstTest.add(Gen.newList(Fet.DRAIN, Fet.SOURCE));
                    lstTest.add(Gen.newList(Fet.SOURCE, Fet.SOURCE));
                    retList = testConn(comps.lst(0), other.comps.lst(other.comps.size() - 1), lstTest);
                }
                return;
            }

            // RULE(        "Abuted*implement*operator*evalGroups*MultCol&MultCol",
            if (comps.size() > 1 &&
                    other.comps.size() > 1) {
                //        TEST next column for biger colums
                lstTest.clear();
                lstTest.add(Gen.newList(Fet.DRAIN, Fet.DRAIN));
                retList = testConn(comps.lst(0), other.comps.lst(0), lstTest);
                if (retList == null) {
                    lstTest.clear();
                    lstTest.add(Gen.newList(Fet.DRAIN, Fet.SOURCE));
                    retList = testConn(comps.lst(0), other.comps.lst(other.comps.size() - 1), lstTest);
                    if (retList == null) {
                        lstTest.clear();
                        lstTest.add(Gen.newList(Fet.SOURCE, Fet.DRAIN));
                        retList = testConn(comps.lst(comps.size() - 1), other.comps.lst(0), lstTest);
                        if (retList == null) {
                            lstTest.clear();
                            lstTest.add(Gen.newList(Fet.SOURCE, Fet.SOURCE));
                            retList = testConn(comps.lst(comps.size() - 1), other.comps.lst(other.comps.size() - 1), lstTest);
                        }
                    }
                }
                return;
            }
        } else throw new RuntimeException("Unknown behavior.");
    }

    int getPartner(List lstConn, int num1) {
        for (int aux1 = 0; aux1 < lstConn.size(); aux1++) {
            if (((Integer) lstConn.lst(aux1).get(0)).intValue() == num1) {
                return ((Integer) lstConn.lst(aux1).get(1)).intValue();
            }
        }
        return -1;
    }

    void makeColumns(List lines, List columns) {
        columns.clear();
        while (!lines.lst(0).isEmpty()) {
            columns.add(0, Gen.newList());
            for (int aux1 = 0; aux1 < lines.size(); aux1++) {
                columns.lst(0).add(lines.lst(aux1).remove(0));
            }
        }
        columns.invert();
        lines.clear();
    }

    void makeConn(List report, AbuttedAgent group2) {
		/*-
		 *                Report= [ [[1 2] [2 4] [4 1]]  SOURCE DRAIN]
		 */
        // Discover why when there is one fet the swap is done
        // after and not during the positioning part?
        //System.out.println("MAKECONN\n" + report + "\n\n" + comps + "\n\n2nd\n" + group2.comps + "\n\n");
        AbuttedAgent group1 = this;
        List lstConn = (List) report.get(0);

        //                Position the groups to connect straight
        if (group1.comps.size() > 1) {
            if (report.get(1).equals(Fet.DRAIN)) {

                //                Swaps DS in a list of fet's columns
                group1.comps.invert();
                for (int aux1 = 0; aux1 < group1.comps.size(); aux1++) {
                    List lst = group1.comps.lst(aux1);
                    for (int aux2 = 0; aux2 < lst.size(); aux2++) {
                        Fet fet = (Fet) lst.get(aux2);
                        if (fet != null) fet.swapDS();
                    }
                }
            }
            report.remove(1);
        }
        if (group2.comps.size() > 1) {
            if (report.get(1).equals(Fet.SOURCE)) {

                //                Swaps DS in a list of fet's columns
                group2.comps.invert();
                for (int aux1 = 0; aux1 < group2.comps.size(); aux1++) {
                    List auxLst = (List) group2.comps.get(aux1);
                    for (int aux2 = 0; aux2 < auxLst.size(); aux2++) {
                        Fet fet = (Fet) auxLst.get(aux2);
                        if (fet != null) fet.swapDS();
                    }
                }
            }
            report.remove(1);
        }
		/*-
		 *                Make then line and append the other line to the end
		 *                of the corespondent line
		 */
        List gp1 = makeLines(group1.comps);
        List gp2 = makeLines(group2.comps);
        int gp1ColNumber = gp1.lst(0).size();                        // extra for java version
        int gp2ColNumber = gp2.lst(0).size();
        for (int num1 = 0; num1 < gp1.size(); num1++) {
            int num2 = getPartner(lstConn, num1);
            if (num2 != -1) {
                List line1 = (List) gp1.get(num1);
                List line2 = (List) gp2.get(num2);

                //                Join the lines
                if (line1.size() == 1) {
                    if (line2.size() == 1) {

                        //                Two single columns
                        Fet f1 = (Fet) line1.get(0);
                        Fet f2 = (Fet) line2.get(0);
                        if (f1.getTerms().at(Fet.DRAIN).electricNode == f2.getTerms().at(Fet.DRAIN).electricNode)
                            f1.swapDS();
                        else if (f1.getTerms().at(Fet.SOURCE).electricNode == f2.getTerms().at(Fet.SOURCE).electricNode)
                            f2.swapDS();
                        else if (f1.getTerms().at(Fet.DRAIN).electricNode == f2.getTerms().at(Fet.SOURCE).electricNode) {
                            f1.swapDS();
                            f2.swapDS();
                        }
                    } else {

                        //                One single columns
                        Fet f1 = (Fet) line1.get(0);
                        Fet f2 = (Fet) line2.get(0);
                        if (f1.getTerms().at(Fet.DRAIN).electricNode == f2.getTerms().at(Fet.DRAIN).electricNode)
                            f1.swapDS();
                    }
                } else if (line2.size() == 1) {

                    //                One single columns
                    Fet f1 = (Fet) line1.get(line1.size() - 1);
                    Fet f2 = (Fet) line2.get(0);
                    if (f1.getTerms().at(Fet.SOURCE).electricNode == f2.getTerms().at(Fet.SOURCE).electricNode)
                        f2.swapDS();
                }
                gp1.lst(num1).addAll(gp2.lst(num2));
                gp2.lst(num2).clear();
            } else {
				/*-
				 *                If there is no line append Nulls the size
				 *                of the gp2 lines
				 */
                for (num2 = gp2ColNumber; num2 > 0; num2--) {
                    gp1.lst(num1).add(null);
                }
            }
        }

        //                Delete all connected lines (empties) in gp2
        for (int aux1 = 0; aux1 < gp2.size(); ) {
            if (gp2.lst(aux1).isEmpty())
                gp2.remove(aux1);
            else
                aux1++;
        }

        //                If there are lines in gp2 add to gp1
        while (!gp2.isEmpty()) {

            //                Push Nulls the size of the gp1 lines
            for (int num1 = gp1ColNumber; num1 > 0; num1--) {
                gp2.lst(0).add(0, null);
            }

            //                If NMOS add on top if PMOS add on botton
            if (((Fet) gp2.lst(0).get(gp2.lst(0).size() - 1)).tecn.equals(Fet.NMOS))
                gp1.add(gp2.remove(0));
            else
                gp1.add(0, gp2.remove(0));
        }
        makeColumns(gp1, group1.comps);
    }

    static List makeLines(List columns) {
        List lines = Gen.newList();
        while (!columns.lst(0).isEmpty()) {
            lines.add(0, Gen.newList());
            for (int aux1 = 0; aux1 < columns.size(); aux1++) {
                lines.lst(0).add(columns.lst(aux1).remove(0));
            }
        }
        lines.invert();
        columns.clear();
        return lines;
    }

    boolean noNumber(Integer num, List list, int aux1) {

        //                Test if a number appers on the rest of the list, aux1 onwards
        for (int aux2 = aux1; aux2 < list.size(); aux2++) {
            if (list.get(aux2).equals(num)) return false;
        }
        return true;
    }

    void reproduce() {
        AbuttedAgent agt = new AbuttedAgent(placer);
        placer.team.add(agt);
        agt.behaviorAbutted();
    }

    List testConn(List fet1, List fet2, List lstTest) {
		/*-
		 *                lstConn= [ [[1 5] [2 4] [3 2]]   SOURCE DRAIN]
		 *                      Conection order       Sides
		 */

        List c1, c2;
        List lstConn = Gen.newList();
        boolean doChange = (fet1.size() > fet2.size());

        //                Order the smallest column first
        if (!doChange) {
            c1 = fet1;
            c2 = fet2;
        } else {
            c1 = fet2;
            c2 = fet1;
            for (int aux1 = 0; aux1 < lstTest.size(); aux1++) {
                lstTest.lst(aux1).invert();
            }
        }

        //                Test the connections
        for (int aux1 = 0; aux1 < c1.size(); aux1++) {
            if (c1.get(aux1) == null) continue;
            lstConn.add(Gen.newList());
            List l1 = (List) lstConn.get(lstConn.size() - 1);
            for (int aux2 = 0; aux2 < c2.size(); aux2++) {
                if (c2.get(aux2) == null ||
                        !((Fet) c1.get(aux1)).tecn.equals(((Fet) c2.get(aux2)).tecn))
                    continue;
                int aux3;
                for (aux3 = 0;
                     aux3 < lstTest.size() &&
                             ((Fet) c1.get(aux1)).getTerms().at((Symbol) lstTest.lst(aux3).get(0)).electricNode !=
                                     ((Fet) c2.get(aux2)).getTerms().at((Symbol) lstTest.lst(aux3).get(1)).electricNode;
                     aux3++)
                    ;
                if (aux3 < lstTest.size()) l1.add(new Integer(aux2));
            }

            //                If it can't connect with any
            if (l1.isEmpty()) return null;
        }
		/*-
		 *                Test all lines are connect and returns a ordered list
		 *                One line one connection
		 */
        if (!allConnected(lstConn)) return null;

        //                Add the other side order
        for (int aux1 = 0, aux2 = 0; aux2 < c1.size(); aux2++) {
            if (c1.get(aux2) == null) continue;
            lstConn.lst(aux1).add(0, new Integer(aux2));
            aux1++;
        }
        if (doChange) {
            for (int i = 0; i < lstConn.size(); i++) {
                lstConn.lst(i).invert();
            }
        }
        List auxLst = Gen.newList();
        auxLst.addAll(lstConn);
        lstConn.clear();
        lstConn.add(auxLst);

        //                Test directions
        if (lstTest.size() == 2) {
            if (lstTest.lst(0).get(0).equals(lstTest.lst(1).get(1))) {
                lstConn.add(lstTest.lst(0).get(0));
            } else {
                lstConn.add(lstTest.lst(0).get(1));
            }
        } else if (lstTest.size() == 1) {
            if (!doChange) {
                lstConn.add(lstTest.lst(0).get(0));
                lstConn.add(lstTest.lst(0).get(1));
            } else {
                lstConn.add(lstTest.lst(0).get(1));
                lstConn.add(lstTest.lst(0).get(0));
            }
        }
        //if (doChange) { System.out.println("DOCHANGE " + lstConn + "\n\n" + fet1 + "\n+++++\n"+fet2 +"-------------\n");}
        return lstConn;
    }

    public String toString() {
        return "=======> Abuted fets: <======" + comps + "\n";
    }
}
