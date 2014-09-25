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
 *  LayerDialog
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class LayerDialog extends Dialog implements ActionListener {

    Display1 dsp;

    public LayerDialog(Display1 d, java.awt.List showL, java.awt.List fillL) {
        super(d, "Choose Layers");
        dsp = d;
        Panel p = new Panel();
        p.add(new Label("Layers Showing"));
        p.add(new Label("Filled Layers"));
        add("North", p);

        p = new Panel();
        p.add(showL);
        p.add(fillL);
        add("Center", p);

        Button b = new Button("OK");
        b.addActionListener(this);
        p = new Panel();
        p.add(b);
        add("South", p);

        pack();
    }

    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dsp.cp.repaint();
    }
}
