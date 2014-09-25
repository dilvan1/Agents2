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

/**
 * Insert the type's description here.
 * Creation date: (10/12/2000 10:59:34 PM)
 * @author:
 */

import layout.comp.Consultant;
import layout.comp.DesignCmp;
import layout.router.RouterExpert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Comm implements Runnable {
    ObjectOutputStream out;
    ObjectInputStream in;
    volatile boolean freeFlag;
    DesignCmp design;

    /**
     * Comm constructor comment.
     */
    Comm(String address, int port) throws IOException {
        design = null;
        freeFlag = true;
        out = null;
        in = null;

        if (address != null) {
            //
            Socket soc = new Socket(address, port);
            out = new ObjectOutputStream(soc.getOutputStream());
            in = new ObjectInputStream(soc.getInputStream());

            //	Send design rules
            out.writeObject(Consultant.db);
            try {
                if (!((String) in.readObject()).equals("OK"))
                    throw new IOException("");
            } catch (Exception e) {
                throw new IOException("Failure transmiting rules.");
            }
        }
    }

    /**
     * Insert the method's description here.
     * Creation date: (10/12/2000 11:51:54 PM)
     */
    DesignCmp getDesign() {
        return design;
    }

    /**
     * Insert the method's description here.
     * Creation date: (10/12/2000 11:36:33 PM)
     */
    boolean isFree() {
        return freeFlag;
    }

    /**
     * Insert the method's description here.
     * Creation date: (10/12/2000 11:37:03 PM)
     */
    public void run() {

        //	If no router servers
        if (out == null) {
            RouterExpert re = new RouterExpert(design);
            re.run();
            design = re.getDesign();
            freeFlag = true;
            return;
        }

        //If server OK
        try {
            out.writeObject(design);
            Object obj = in.readObject();
            if (obj instanceof DesignCmp) {
                design = (DesignCmp) obj;
                System.out.println("Design OK");
            } else {
                design = null;
                System.out.println("Design Fail:" + obj);
            }
        } catch (Exception e) {
            design = null;
            System.out.println("Communication Exception:\n" + e);
        }
        freeFlag = true;
    }

    /**
     * Insert the method's description here.
     * Creation date: (10/12/2000 11:27:13 PM)
     */
    void setDesign(DesignCmp des) {
        freeFlag = false;
        design = des;
    }
}
