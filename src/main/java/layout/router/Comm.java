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
 * Insert the type's description here.
 * Creation date: (10/13/2000 5:38:20 PM)
 * @author:
 */

import layout.comp.Consultant;
import layout.comp.DesignCmp;
import layout.comp.Layer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Comm extends Thread {
    Socket soc;

    /**
     * Comm constructor comment.
     */
    public Comm(String name, Socket soc) {
        super(name);
        this.soc = soc;
    }

    /**
     * Insert the method's description here.
     * Creation date: (10/13/2000 5:39:46 PM)
     */
    public void run() {

        //	Route circuits
        try {
            ObjectInputStream in = new ObjectInputStream(soc.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream());

            //Loop to route the circuits
            while (true) {
                Object obj = in.readObject();

                //	Commands
                if (obj instanceof String) {
                    String comm = (String) obj;

                    //	Shutdown
                    if (comm.equals("SHUTDOWN")) {
                        soc.close();
                        System.exit(0);
                    }

                    //	Stop
                    if (comm.equals("STOP")) {
                        soc.close();
                        return;
                    }

                    //	Is ready?
                    if (comm.equals("READY")) {
                        if (Consultant.db == null)
                            out.writeObject("MISSING CONSULTANT");
                        else
                            out.writeObject("OK");
                        continue;
                    }
                }

                //	Consultant
                if (obj instanceof Consultant) {
                    if (Consultant.db != null) {
                        out.writeObject(new RuntimeException("Consultant already installed."));
                        continue;
                    }
                    Consultant.db = (Consultant) obj;
                    out.writeObject("OK");
                    //    Get the layers names and create them.
                    Layer.makeLayers(Consultant.db.getLayersNames());
                    System.out.println("Consultant read.");
                    continue;
                }

                //	Design to be routed
                DesignCmp design = (DesignCmp) obj;
                design = layout.placer.PlacerExpert.initDesign(design);

                //	Router the design
                RouterExpert rp = new RouterExpert(design);
                try {
                    rp.run();
                    if (rp.getDesign() == null)
                        throw new RuntimeException("NO DESIGN");
                    out.writeObject(rp.getDesign());
                    System.out.println("OK Design.");
                } catch (Exception erun) {
                    out.writeObject(erun);
                    System.out.println("No Design.");
                }
            }
        } catch (Exception e2) {
        }
    }
}
