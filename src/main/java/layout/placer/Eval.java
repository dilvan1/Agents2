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

/*-
 *              Eval Class
 */

import java.util.Random;
import layout.comp.*;
import layout.display.Dsp;
import layout.util.Gen;
import layout.util.List;
import layout.util.Pt;
import layout.util.Rectangle;

class Eval {

    //	Constants
    final double MUTATION_PROB = 0.05; // 5% 1%
    final double CROSS_OVER_PROB = 0.1; // 15 %5%

    //final static double GAUSS_MEAN= 0.0;
    final static double GAUSS_DEVIATION = 0.35;
    final static int MAX_PARENTS = 50;
    final static int NUM_TRY_GENERATE = 500;

    final static int SEEDS_SIZE = 8; //4
    final static double MAX_DY_FACTOR = 1.5;
    final static double MAX_GAP_FACTOR = 0.4;

    //	Variables received by the constructor
    DesignCmp design;
    List groups;

    //	Variables internal to the class
    int pmosNmosSeparation;
    int bipolarSeparation;
    List parents, parentsScores;
    List groupsDim, groupsLineDy, groupsColumnDx, groupsAverageDy;
    int groupsAverageDx;
    Random random;

    Eval(DesignCmp design, List groups) {
    /*-
	 *                Groups after invertion:
	 *             1st element Botton line
	 *
	 *                Ele3        pmos pmos pmos pmos
	 *                Ele2        pmos pmos pmos pmos
	 *                Gap
	 *                Ele1        nmos nmos nmos nmos
	 *                Ele0        nmos nmos nmos nmos
	 */
        this.design = design;
        this.groups = groups;

	/*-
	 *                Get the constants
	 *                WRONG WRONG WRONG the getMinOverlaping can return -1
	 */
        pmosNmosSeparation = Math.max(Consultant.db.getMinOverlaping(Layer.NWELL, Layer.PDIFF) + Consultant.db.getWiringMargin(Layer.NWELL, Layer.NDIFF), Consultant.db.getMinOverlaping(Layer.PWELL, Layer.NDIFF) + Consultant.db.getWiringMargin(Layer.PWELL, Layer.PDIFF));
        bipolarSeparation = Math.max(Consultant.db.getWiringMargin(Layer.NWELL, Layer.NWELL), Consultant.db.getWiringMargin(Layer.PWELL, Layer.PWELL));

	/*-
	 *                Get the dimensions for the groups
	 */
        groupsLineDy = Gen.newList();
        groupsColumnDx = Gen.newList();
        groupsDim = Gen.newList();
        groupsAverageDy = Gen.newList();
        Rectangle area = design.getDesignArea();
        int totalDx = 0;
        for (int aux1 = 0; aux1 < groups.size(); aux1++) {
            groupsLineDy.add(0, Gen.newList());
            groupsColumnDx.add(0, Gen.newList());
		/*-
		 *                The line order is inverted to allow crescent order for Y
		 */
            groups.lst(aux1).invert();
            getGroupDimensions(groups.lst(aux1), groupsLineDy.lst(0), groupsColumnDx.lst(0));

            //                Find the dimensions of each group
            int dx = 0;
            int dy = 0;
            for (int aux2 = 0; aux2 < groupsColumnDx.lst(0).size(); aux2++) {
                dx = dx + ((Integer) groupsColumnDx.lst(0).get(aux2)).intValue();
            }
            for (int aux2 = 0; aux2 < groupsLineDy.lst(0).size(); aux2++) {
                dy = dy + ((Integer) groupsLineDy.lst(0).get(aux2)).intValue();
            }
            groupsDim.add(new Pt(dx, dy));

            //System.out.println("Group Dim= " + groupsDim.get(groupsDim.size()-1));

            //                Total dx of all groups
            totalDx = totalDx + dx;
		/*-
		 *                Calculus of the average dy
		 *                Thera are 3: Top cell, gap and down cell
		 *                Dy is the down cell distance
		 */
            dy = area.getLy() - dy;
            if (isGroupFet(groups.lst(aux1)))
                dy = dy - pmosNmosSeparation;
            else if (isGroupBipolar(groups.lst(aux1)))
                dy = dy - bipolarSeparation;
            //CHECK_ERR(dy<=0, "EvalAgent:: initVariables: DY is too small");
            groupsAverageDy.add(new Integer(dy / 3));
        }
        groupsLineDy.invert();
        groupsColumnDx.invert();
        groupsAverageDx = (area.getLx() - totalDx) / (groups.size() + 1);
        //CHECK_ERR(groupsAverageDx<=0, "EvalAgent:: initVariables: DX is too small");
        //System.out.println("Line Dy =  " + groupsLineDy);
        //System.out.println("Column Dx =  " + groupsColumnDx);
        //System.out.println("groupsAverageDy =  " + groupsAverageDy);
        //System.out.println("groupsAverageDx =  " + groupsAverageDx);
    }

    boolean areSwapable(List lst1, List lst2) {
        int aux1;
        for (aux1 = 0; aux1 < lst1.size() && !(lst1.get(aux1) instanceof Fet); aux1++) ;
        if (aux1 >= lst1.size())
            return true;
        Fet fet1 = (Fet) lst1.get(aux1);
        for (aux1 = 0; aux1 < lst2.size() && !(lst2.get(aux1) instanceof Fet); aux1++) ;
        if (aux1 >= lst2.size())
            return true;
        Fet fet2 = (Fet) lst2.get(aux1);
        return (fet1.tecn.equals(fet2.tecn));
    }

    /**
     * Clear all components (Fets, Bipolars and Cells)
     */
    void clearTestComps(DesignCmp design) {
        for (int aux1 = 0; aux1 < design.size(); ) {
            Component cmp = design.at(aux1);
            if (!(cmp instanceof Pad) && !(cmp instanceof ElectricNode))
                design.remove(aux1);
            else
                aux1++;
        }
    }

    void equalizeGroupsDist(List genes) {
	/*-
	 *                   Gene = [ [2  FLIP         [1 2 4 3]         dx  dy gap ] ... ]
	 *                 groupNumber-|  |-Flip flag  |- order of lines |   |   |- Gap between the groups parts
	 *                                                               |   |- Y coord of the group
	 *                                                               |- X coord of the group
	 */

        //                Find total to be redistributed
        int dx = 0;
        for (int aux1 = 0; aux1 < genes.size(); aux1++) {
            Gene gene = (Gene) genes.get(aux1);
            int groupInd = gene.getGroupNumber();

            //                Calculate dx
            dx = dx + gene.getDx();

            //                Equalize dy: It's done similaly to dx
            int goodDy = ((Integer) groupsAverageDy.get(groupInd)).intValue();
            int lastDy = goodDy * 3 - gene.getDy() - gene.getGap();
            while (lastDy > goodDy * 1.5 || lastDy < 0) {
                boolean dyFlag = rndBoolean();
                //int geneDy = ((Integer) gene.get(ind)).intValue();
                int geneDy = (dyFlag) ? gene.getDy() : gene.getGap();

                //                The maximun variation allowed is 50%
                //                 |-     Gives signal     -|  |- Gives the delta-| |-Gives the randon increment-|
                int delta = (int) Math.floor((lastDy / Math.abs(lastDy)) * geneDy * 0.5 * rnd());
                if (delta == 0)
                    delta = (int) Math.floor((-1 * lastDy / Math.abs(lastDy)) * goodDy * 0.5 * rnd());
                if (dyFlag)
                    gene.setDy(geneDy + delta);
                else
                    gene.setGap(geneDy + delta);
                lastDy = lastDy - delta;
            }
        }
	/*-
	 *                There are n dx spaces plus 1 (the space
	 *                between the last cell and the border.
	 */
        int lastDx = groupsAverageDx * (groups.size() + 1) - dx;
	/*-
	 *                LastDx should be bigger than 30% and
	 *                smaller than 130% goodDx
	 *
	 *                As the signal of lastDx controls if the delta is added or subtracted
	 *                it should varies from >0 (add)  > 0 subtracted, then it is subtracted
	 *                of 0.3*groupsAverageDx. This make the relation:
	 *                lastDx>groupsAverageDx* 1.3 || lastDx < groupsAverageDx* 0.3 be:
	 */
        lastDx = (int) Math.ceil(lastDx - groupsAverageDx * 0.3);
        while (lastDx > groupsAverageDx || lastDx < 0) {
            int ind = rndInt(genes.size());
            int geneDx = ((Gene) genes.get(ind)).getDx();

            //                The maximun variation allowed is 50%
            //                 |-     Gives signal     -|  |- Gives the delta-|  |-Gives the randon increment-|
            int delta = (int) Math.floor((lastDx / Math.abs(lastDx)) * geneDx * 0.5 * rnd());
            if (delta == 0)
                delta = (int) Math.floor((-1 * lastDx / Math.abs(lastDx)) * groupsAverageDx * 0.5 * rnd());
            ((Gene) genes.get(ind)).setDx(geneDx + delta);
            lastDx = lastDx - delta;
        }
    }

    EvalReport evalCircuit() {

	/*-
	 *                Route the circuit
	 */

        //	Try to make direct connections


        //	Init the circuit in the same way the routing process does
        design.connectInSameLayer(Layer.NDIFF);
        design.connectInSameLayer(Layer.PDIFF);
        design.connectInSameLayer(Layer.POLY);
        if (!design.makeDifusionToMetal1()) {

            //                Delete all wires included
            design.initRoutingNets();
            return new EvalReport(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        //	Make a estimate of for the remaining connections
        int numCrashes = 1;
        int cost = 0;
        for (int aux1 = 0; aux1 < design.getElectricNodes().size(); aux1++) {
            List nets = ((ElectricNode) design.getElectricNodes().get(aux1)).routingNets;
            while (nets.size() > 1) {

                //                Finds new wire
                WirePt wirePt1 = ((Wire) nets.lst(0).get(0)).at(1);
                WirePt wirePt2 = ((Wire) nets.lst(1).get(0)).at(1);
                Wire wire = design.getFreeWire(wirePt2, wirePt1, wirePt2);
                int aux2 = design.getEstimatedCrashes(wire);


                //                Estatistics
                numCrashes = numCrashes + aux2;
                cost = cost + design.getWireCost(wire);

                //                Apend the wire to the node
                ((ElectricNode) design.getElectricNodes().get(aux1)).getBody().add(0, wire.clone());
                nets.lst(0).add(((ElectricNode) design.getElectricNodes().get(aux1)).getBody().at(0));

                //                Join nets
                nets.lst(0).addAll(nets.lst(1));
                nets.remove(1);
            }
        }

        //                Delete all wires included
        design.initRoutingNets();
        return new EvalReport(numCrashes, cost);
    }

    double gauss() {
        return Math.abs(random.nextGaussian()) * GAUSS_DEVIATION;
        //   return exp(-0.5*sqr((x-GAUSS_MEAN)/GAUSS_DEVIATION));
    }

    boolean generate(List genes) {
	/*-
	 *                   Gene = [ [2  FLIP         [1 2 4 3]         dx  dy gap ] ... ]
	 *                 groupNumber-|  |-Flip flag  |- order of lines |   |   |- Gap between the groups parts
	 *                                                               |   |- Y coord of the group
	 *                                                               |- X coord of the group
	 *
	 *                       |----------Group---------|
	 *             Groups= [ [[t1 t2 t3]  [t4 t5 null]] ... ]
	 *                        Line0        Line1
	 *                groupDim= [ Pt(dx, dy)                 ... ]
	 *             groupLineDy= [ [ dy.L0       dy.L1      ] ... ]
	 *           groupColumnDx= [ [ dx.C0       dx.C1      ] ... ]
	 */

        //        Clear design of all components (Fets, Bipolars and Cells)
        clearTestComps(design);

        //                Delete the reference for the fets
        design.getFets().clear(); //////////////// BUGG ???????????????????
        Rectangle area = design.getDesignArea();
        int deltaX = area.c1.x;

        //                ReadFlag
        boolean readFlag = true;
        int groupDy = 0;
        int groupGap = 0;
        for (int groupInd1 = 0; groupInd1 < genes.size(); groupInd1++) {
            Gene groupGene = (Gene) genes.get(groupInd1);

            //                Read gene information
            int groupInd = groupGene.getGroupNumber();
            List group = groups.lst(groupInd);
            boolean flagFlip = groupGene.getFlip();
            int groupDx = groupGene.getDx();
		/*-
		 *                Armengue para evitar que os grupos desalinhem
		 *                Apenas o valor do primeiro grupo de fets depois de um grupo
		 *                nao fet ou  o iinicio serao lidos
		 */
            if (!isGroupFet(group) || readFlag) {
                groupDy = groupGene.getDy();
                groupGap = groupGene.getGap();
                readFlag = !isGroupFet(group);
            }
            //groupDy= RTYPE(Int, groupGene[4]).toInt();
            //groupGap= RTYPE(Int, groupGene[5]).toInt();
            int groupLx = ((Pt) groupsDim.get(groupInd)).x;
            deltaX = deltaX + groupDx;

            //                Scan lines
            boolean flagFet = isGroupFet(group);
            boolean flagGap = false;
            int yCoord = area.c1.y + groupDy;
            List order = groupGene.getLinesOrder();
            List auxFetLst = Gen.newList();
            for (int aux2 = 0; aux2 < order.size(); aux2++) {

                //                Scan line
                int lineInd = ((Integer) order.get(aux2)).intValue();
                List line = group.lst(lineInd);
                int xCoord = deltaX;
                int lineDy = ((Integer) groupsLineDy.lst(groupInd).get(lineInd)).intValue();
                for (int columnInd = 0; columnInd < line.size(); columnInd++) {

                    //                Test fliping if yes take fets backwards and swapDS
                    int columnDx = ((Integer) groupsColumnDx.lst(groupInd).get(columnInd)).intValue();
                    if ((!flagFlip && (line.get(columnInd) instanceof Component)) || (flagFlip && (line.get(line.size() - 1 - columnInd) instanceof Component))) {

                        //
                        Component cmp;
                        if (!flagFlip)
                            cmp = (Component) ((Component) line.get(columnInd)).clone();
                        else
                            cmp = (Component) ((Component) line.get(line.size() - 1 - columnInd)).clone();
                        Rectangle rect = cmp.getEnvelope();
                        if (flagFlip) {
                            cmp.mirrorY();
                            cmp.translate(rect.getLx(), 0);
                        }

                        //                Calculate the gap of the fet
                        if (!flagGap && flagFet && ((Fet) cmp).tecn.equals(Fet.PMOS)) {
                            yCoord = yCoord + groupGap + pmosNmosSeparation;
                            flagGap = true;
                        }

                        //                 then translate it to the position:
                        cmp.translate(xCoord + (int) Math.ceil((columnDx - rect.getLx()) / 2.0), yCoord + (int) Math.ceil((lineDy - rect.getLy()) / 2.0));

                        //                append to the design
                        //CHECK_ERR(!design().inDesignBoundaries(*cmp), "Placer:: generate: Component out of design border");
                        NoCrash[] paths = new NoCrash[0];
                        if (design.isCrashing(paths, cmp)) {
                            //errorMessage = "Placer:: Reproduce: Crash laying down the groups";
                            return false;
                        }

                        //
                        if (cmp instanceof Fet)
                            auxFetLst.add(0, cmp);
                        else {

                            //                Take note of all components added to design to take then out later
                            // testComps.ap(cmp);
                            design.add(0, cmp);
                        }
                    }
                    xCoord = xCoord + columnDx;
                }
                yCoord = yCoord + lineDy;
			/*-
			 *                Add the gap for all other cells (This is done for Bipolars only
			 *                groups with cells have only one cell per group
			 */
                if (!flagFet)
                    yCoord = yCoord + groupGap + bipolarSeparation;
            }

            //                Append the fets, if any
            while (!auxFetLst.isEmpty()) {

                //                Take note of all components added to design to take then out later
                //testComps.ap(&auxFetLst[0]);
                design.add(0, (Component) auxFetLst.remove(0));
            }

            //                Increment the group delta
            deltaX = deltaX + groupLx;
        }
        design.refFets();
        design.initRoutingNets();
        return true;
    }

    List getBest() {
        parentsScores.remove(0);
        return (List) parents.remove(0);
    }

    /**
     * This method was created in VisualAge.
     */
    DesignCmp getDesign() {
        return (DesignCmp) design.clone();
    }

    void getGroupDimensions(List group, List groupLineDy, List groupColumnDx) {
	/*-
	 *                Gets the maximun dx and dy for each column and line of a group
	 *
	 *                Groups=  [[t1 t2 t3] [t4 t5 null]]
	 *                        Line        Line
	 *                    |----------Group--------|
	 *
	 *                Map the biggest lx in each column to be the lx of
	 *                this column.
	 */
        int columnInd, columnNum, dx, dy;
        Rectangle rect;
        for (int aux1 = 0; aux1 < group.size(); aux1++) {

            //                Scan line
            List line = group.lst(aux1);
            dy = 0;
            for (int aux2 = 0; aux2 < line.size(); aux2++) {
                if (!(line.get(aux2) instanceof Component))
                    continue;
                rect = ((Component) line.get(aux2)).getEnvelope();
                if (rect.getLy() > dy)
                    dy = rect.getLy();
            }

            //                Finds the taller fet
            //CHECK_ERR(dy==0, "EvalAgent:: getGroupEnvelope: Line all empty");
            groupLineDy.add(new Integer(dy));
        }

	/*-
	 *                Map the biggest lx in each column to be the lx of
	 *                this column.
	 */
        columnNum = group.lst(0).size();
        for (columnInd = 0; columnInd < columnNum; columnInd++) {

            //                Scan line
            dx = 0;
            for (int aux1 = 0; aux1 < group.size(); aux1++) {
                if (!(group.lst(aux1).get(columnInd) instanceof Component))
                    continue;
                rect = ((Component) group.lst(aux1).get(columnInd)).getEnvelope();
                if (rect.getLx() > dx)
                    dx = rect.getLx();
            }

            //                Finds the lengthier fet
            //CHECK_ERR(dx==0, "EvalAgent:: getGroupEnvelope: Column all empty");
            groupColumnDx.add(new Integer(dx));
        }
    }

    /**
     * This method was created in VisualAge.
     */
    Gene initNewGene(int groupNum, boolean flip, double dyFactor, double gapFactor) {

        //	Find line order
        List lineOrder = Gen.newList();
        for (int aux1 = 0; aux1 < groups.lst(groupNum).size(); aux1++) {
            lineOrder.add(new Integer(aux1));
        }
        return new Gene(
                groupNum, flip, lineOrder,
                groupsAverageDx, //dx
                (int) Math.ceil(((Integer) groupsAverageDy.get(groupNum)).intValue() * dyFactor), //dy
                (int) Math.ceil(((Integer) groupsAverageDy.get(groupNum)).intValue() * gapFactor) //gap
        );
    }

    boolean isGroupBipolar(List group) {
        for (int aux1 = 0; aux1 < group.lst(0).size(); aux1++) {
            if (group.lst(0).get(aux1) != null)
                return (group.lst(0).get(aux1) instanceof Bipolar);
        }
        throw new RuntimeException("Total Null line");
    }

    boolean isGroupFet(List group) {
        for (int aux1 = 0; aux1 < group.lst(0).size(); aux1++) {
            if (group.lst(0).get(aux1) != null)
                return (group.lst(0).get(aux1) instanceof Fet);
        }
        throw new RuntimeException("Total Null line");
    }

    List mate(List father1, List father2) {

	/*
	IF_DEBUG_ON(
	COUT( "Father1 ")
	Debug::printLst(cout, 100, father1);
	COUT( endl << "Father2 ")
	Debug::printLst(cout, 100, father2);
	COUT( endl)
	)*/

        List fathers = Gen.newList();
        fathers.add(father1);
        fathers.add(father2);
        int groupNumber = father1.size();

        //                Chose main parent
        int mainParent = rndInt(2);
        List main = fathers.lst(mainParent);

        //                match the two parents in one
        boolean cloneFlag = true;
        List son = Gen.newList();

        //                To avoid clones
        while (cloneFlag) {
            int lastInd = -1;
            son.clear();
            for (int aux1 = 0; aux1 < main.size(); aux1++) {

                //                Get group
                int ind = rndInt(2);
                if (lastInd != -1 && lastInd != ind)
                    cloneFlag = false;
                lastInd = ind;
                List father = fathers.lst(ind);
                int group = ((Gene) main.get(aux1)).getGroupNumber();
                int aux2;
                for (aux2 = 0; aux2 < father.size() && group != ((Gene) father.get(aux2)).getGroupNumber(); aux2++) ;
                //CHECK_ERR(aux2.null(), "EvalAgent:: mate: Unknown group");
                //
                son.add(new Gene((Gene) father.get(aux2)));
            }
        }

        //                Swap some groups
        for (int aux3 = groupNumber; aux3 > 0; aux3--) {
            if (rnd() <= CROSS_OVER_PROB)
                swap(son, rndInt(groupNumber), rndInt(groupNumber));
        }

        //                Mutate and cross-over Flip and the line order;
        for (int aux1 = 0; aux1 < son.size(); aux1++) {
            Gene gene = (Gene) son.get(aux1);
            if (rnd() <= CROSS_OVER_PROB) {
                gene.setFlip(!gene.getFlip());
            }
            int num1 = gene.getLinesOrder().size() - 1;
            int groupInd = gene.getGroupNumber();
            while (rnd() <= CROSS_OVER_PROB) {
                int ind1 = rndInt(num1 + 1);
                int ind2 = rndInt(num1 + 1);
                List sonGroup = gene.getLinesOrder();
                if (ind1 != ind2 && areSwapable(groups.lst(groupInd).lst(ind1), groups.lst(groupInd).lst(ind2)))
                    swap(sonGroup, ind1, ind2);
            }
        }

        //                Crossover and mutation on the groups distances
        for (int aux3 = groupNumber - 1; aux3 >= 0; aux3--) {
            Gene gene = (Gene) son.get(aux3);
            //                Interchange distances between groups
            if (rnd() <= MUTATION_PROB) {
                int ind1 = rndInt(groupNumber);
                int auxInt = gene.getDx();
                gene.setDx(((Gene) son.get(ind1)).getDx());
                ((Gene) son.get(ind1)).setDx(auxInt);
            }

            //                Change distance between groups
            if (rnd() <= MUTATION_PROB)
                gene.setDx((int) Math.ceil(gene.getDx() * (1 + 0.5 * (rnd() - rnd()))));

            //                Change groups Y distances (dy and gap)
            if (rnd() <= MUTATION_PROB) {
                if (rnd() > 0.5)
                    gene.setDy((int) Math.ceil(gene.getDy() * (1 + 0.5 * (rnd() - rnd()))));
                else
                    gene.setGap((int) Math.ceil(gene.getGap() * (1 + 0.5 * (rnd() - rnd()))));
            }
        }
	/*
	IF_DEBUG_ON(
	COUT( "NEQ  -- ")
	Debug::printLst(cout, 100, son);
	COUT( endl)
	)*/

        equalizeGroupsDist(son);
        return son;
	/*
	IF_DEBUG_ON(
	COUT( "SON  -- ")
	Debug::printLst(cout, 100, son);
	COUT( endl)
	)*/
    }

    boolean mateGroup(List seeds) {

        //CHECK_ERR(seeds.number()<2, "EvalAgent:: mateGroup: Seeds too small");
        int mate1 = randomSmallMoreProbable(seeds.size() - 1);
        int mate2 = randomSmallMoreProbable(seeds.size() - 1);

        //
        Dsp.println("Mating " + mate1 + " " + mate2);
        for (int numTry = 0; numTry < NUM_TRY_GENERATE; numTry++) {
            while (mate1 == mate2)
                mate2 = randomSmallMoreProbable(seeds.size() - 1);
            List son = mate(seeds.lst(mate1), seeds.lst(mate2));
            if (parents.contains(son))
                continue;
            if (!generate(son)) {
                Dsp.println("CAN'T GENERATE ");// + errorMessage);
                continue;
            }
            reclassify(evalCircuit(), son, parents, parentsScores);
            return true;
        }
        return false;
    }

    int randomBigMoreProbable(long range) {
        /**
         *                The number probability is greatter as bigger is the number
         *                range-1 is more probable
         *                0-0.999 is 0, 1-1.999 is 1, ..., range-range.999 is range
         *
         *                rand()/(RAND_MAX + 1.0) gives a value from 0-0.999
         */
        int a;
        do {
            a = (int) Math.floor((1 - gauss()) * (range + 1.0));
        } while (a > range || a < 0);
        return a;
    }

    int randomSmallMoreProbable(int range) {
        /**
         *                The number probability is greatter as smaller is the number
         *                0 is more probable
         *                0-0.999 is 0, 1-1.999 is 1, ..., range-range.999 is range
         *
         *                rand()/(RAND_MAX + 1.0) gives a value from 0-0.999
         */
        int a;
        do {
            a = (int) Math.floor(gauss() * (range + 1.0));
        } while (a > range);
        return a;
    }

    void reclassify(EvalReport sonScore, List son, List parents, List parentsScores) {
	/*-
	 *                Score and reclassify circuits
	 *
	 *                report = [ 345 76 ]  : The scores of son
	 *                parents = [ [genes1] [genes2] ... ]  : The parents genes
	 *                parentsScores = [ [ 330 34]  320 33 ] : First all scores of the best, then the
	 *                                                        scores of the best by category
	 */

        int aux1;
        for (aux1 = 0; aux1 < parentsScores.size(); aux1++) {
            EvalReport parentScore = (EvalReport) parentsScores.get(aux1);
            if (sonScore.isBetterThan(parentScore)) {
                parentsScores.add(aux1, sonScore);
                parents.add(aux1, son);
                break;
            }
        }

        //                If the new one is the last
        if (aux1 >= parents.size()) {
            parentsScores.add(sonScore);
            parents.add(son);
        }

	/*-
	 *                If the population is too big kill half of individuals
	 *                depending on number
	 */
        if (parents.size() >= MAX_PARENTS) {
            while (parents.size() >= MAX_PARENTS / 2) {
                int ind = randomBigMoreProbable(parents.size() - 1);
                parentsScores.remove(ind);
                parents.remove(ind);
            }
        }
    }

    void resetPopulation() {

        //                Init random numbers generator
        random = new Random();

	/*-
	 *                Create seeds
	 */
        List seeds = Gen.newList();
        for (int aux1 = 0; aux1 < SEEDS_SIZE; aux1++) {
            seeds.add(0, Gen.newList());
            int[] chosen = new int[groups.size()];
            for (int num1 = 0; num1 < groups.size(); num1++) {
                seeds.lst(0).add(0, initNewGene(rndInd(chosen), rndBoolean(), rnd() * MAX_DY_FACTOR, rnd() * MAX_GAP_FACTOR));
            }
        }

	/*
	/*-
	 *                First seed
	 *
	seeds.add(0, Gen.newList());
	for (num1 = groups.size() - 1; num1 >= 0; num1--) {
	seeds.lst(0).add(0, initNewGene(num1, false, 1.5, 0.0));
	}
	
	/*-
	 *                Second seed
	 *
	seeds.add(0, Gen.newList());
	for (num1 = 0; num1 < groups.size(); num1++) {
	seeds.lst(0).add(0, initNewGene(num1, true, 1.2, 0.4));
	}
	
	/*-
	 *                Third seed
	 *
	seeds.add(0, Gen.newList());
	for (num1 = 0; num1 < groups.size(); num1++) {
	seeds.lst(0).add(0, initNewGene(num1, rndBoolean(), 1.3, 0.2));
	}
	
	/*-
	 *                Fourth seed
	 *
	seeds.add(0, Gen.newList());
	for (num1 = 0; num1 < groups.size(); num1++) {
	seeds.lst(0).add(0, initNewGene(num1, rndBoolean(), 1.2, 0.4));
	}
	*/
        seeds.invert();

	/*-
	 *                Create parents
	 *
	 *                The parents are not classifiedy by the groups as they should be,
	 *                they are just made and put in place without testing to know if they are
	 *                actualy the best in each class  [NOT VALID ANYMORE]
	 *
	 *
	 *                report = [ 345 76 ]  : The scores of son
	 *                parents = [ [genes1] [genes2] ... ]  : The parents genes
	 *                parentsScores = [ [ 330 34]  [320 33] ... ] : First all scores of the best, then the
	 *                                                              scores of the best by category
	 */
        EvalReport report;
        parents = Gen.newList();
        parentsScores = Gen.newList();
        for (int aux1 = 0; aux1 < seeds.size(); aux1++) {
            if (generate(seeds.lst(aux1))) {
                report = evalCircuit();
                reclassify(report, seeds.lst(aux1), parents, parentsScores);
            }
        }

	/*-
	 *                If some of the seeds didn't generate try to generate
	 *                new ones
	 */
        while (parents.size() < seeds.size()) {
            if (!mateGroup(seeds)) {
                throw new RuntimeException("Placer:: generateParents: Can't generate a parent from the seeds");
            }
        }

	/*For Debugging
	Dsp.println("Showing the gauss dist: ");
	int tot, aux;
	tot = 0;
	for (int au1 = 2000; au1 > 0; au1--) {
	aux = randomSmallMoreProbable(1000);
	tot = tot + aux;
	}
	tot = tot / 2000;
	Dsp.println("Media (1000 max) randomSmallMoreProbable: " + tot);
	tot = 0;
	for (int au1 = 2000; au1 > 0; au1--) {
	aux = randomBigMoreProbable(1000);
	tot = tot + aux;
	}
	tot = tot / 2000;
	Dsp.println("Media (1000 max) randomBigMoreProbable: " + tot + "\n");
	//        End of debugging 
	*/
    }

    /*-
     *                Random functions
     */
    double rnd() {
        return random.nextDouble();
    }

    /**
     * This method was created in VisualAge.
     */
    boolean rndBoolean() {
        return random.nextBoolean();
    }

    /**
     * This method was created in VisualAge.
     */
    int rndInd(int[] chosen) {
        int i, aux1;
        boolean flag = false;
        do {
            flag = false;
            i = rndInt(chosen.length) + 1;
            for (aux1 = 0; chosen[aux1] > 0; aux1++) {
                if (aux1 >= chosen.length) throw new RuntimeException("Chosen error");
                if (chosen[aux1] == i) {
                    flag = true;
                    break;
                }
            }
        } while (flag);
        chosen[aux1] = i;
        return i - 1;
    }

    int rndInt(int range) {
        return random.nextInt(range);
    }

    boolean run(int numGenerations) {
	/*-
	 *        TEST DX DY E GROUPS TO SEE IF THE CELL IS AT ALL ROUTABLE
	 *        PuT ALL TESTS TO SEE THE MESSAGES BACK ABOUT THE FAILURE
	 */

        for (int aux1 = 0; aux1 < numGenerations; aux1++) {
            //IF_DEBUG_ON( COUT( "Generation " << aux1 << endl))

            //                Chose parents
            if (!mateGroup(parents))
                return false;
        }
        generate(parents.lst(0));
        return true;
    }

    static void swap(List lst, int i1, int i2) {
        Object obj1 = lst.get(i1);
        lst.set(i1, lst.get(i2));
        lst.set(i2, obj1);
    }
}
