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
 *        Class Display1
 */

import layout.util.Gen;
import layout.util.Linea;
import layout.util.Pt;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

class Display1 extends Frame implements Display, ActionListener, AdjustmentListener {

	Button quitB;
	Button zoomIn;
	Button zoomOut;
	Button setupD;
	java.awt.List showL;
	java.awt.List fillL;
	CompPaint cp;
	LayerDialog dialog;
	int zoom = 128;
	int dx = 20;
	int dy = 20;
	layout.util.Rectangle area = null;
	ScrollPane sp = new ScrollPane();
	Dimension spSize, cpSize;
	int heightY = 0, widthX = 0;

	List layer = Gen.newList();
	java.util.List[] lst = new java.util.List[30];


	List extras;

	Display1(String comp) {
		super("Display " + comp);

		for (int aux1 = 0; aux1 < lst.length; aux1++)
			lst[aux1] = Gen.newList();
		extras = Gen.newList();

		layer.add(new LayerSetup("NWELL", Color.pink, true, false));
		layer.add(new LayerSetup("BCCD", Color.blue.brighter(), true, false));
		layer.add(new LayerSetup("PBASE", Color.magenta, true, false));
		layer.add(new LayerSetup("PDIFF", Color.green.darker(), true, false));
		layer.add(new LayerSetup("NDIFF", Color.green, true, false));
		layer.add(new LayerSetup("POLY", Color.red, true, true));
		layer.add(new LayerSetup("MET1", Color.blue, true, false));
		layer.add(new LayerSetup("CONT", Color.gray, true, false));
		layer.add(new LayerSetup("MET2", Color.cyan, true, false));
		layer.add(new LayerSetup("VIA", Color.yellow, true, false));
		layer.add(new LayerSetup("LINE", Color.black, true, false));
		layer.add(new LayerSetup("LABEL", Color.black, true, false));

		// Make the lists of layers
		showL = new java.awt.List(9, true);
		fillL = new java.awt.List(9, true);
		for (int aux1 = 0; aux1 < layer.size(); aux1++) {
			LayerSetup setup = (LayerSetup) layer.get(aux1);
			showL.add(setup.layer);
			if (setup.show) showL.select(aux1);
			fillL.add(setup.layer);
			if (setup.fill) fillL.select(aux1);
		}
		dialog = new LayerDialog(this, showL, fillL);

		//Add the text field to the applet.
		setFont(new Font("Helvetica", Font.BOLD, 12));
		Color corback = Color.lightGray;
		setBackground(new Color(255, 204, 102));

		Panel p = new Panel();

		// Zoom in button
		zoomIn = new Button(" Zoom In ");
		zoomIn.setBackground(corback);
		zoomIn.addActionListener(this);
		p.add(zoomIn);

		// Zoom out button
		zoomOut = new Button(" Zoom Out ");
		zoomOut.setBackground(corback);
		zoomOut.addActionListener(this);
		p.add(zoomOut);

		// Show dialog
		setupD = new Button(" Setup ");
		setupD.setBackground(corback);
		setupD.addActionListener(this);
		p.add(setupD);

		// Quit button
		quitB = new Button(" Quit ");
		quitB.setBackground(corback);
		quitB.addActionListener(this);
		p.add(quitB);

		//    Add panel with Buttons
		add("North", p);

		cp = new CompPaint(this);
		cp.setBackground(Color.white);

		//ScrollPane sp = new ScrollPane();

		sp.setSize(600, 600);
		sp.add(cp);

		add(sp);

		//add("Center", cp);
		//hor= new Scrollbar(Scrollbar.HORIZONTAL);
		//hor.addAdjustmentListener(this);
		//add("South", hor);
		//ver= new Scrollbar(Scrollbar.VERTICAL);
		//ver.addAdjustmentListener(this);
		//add("East", ver);

		pack();
		setVisible(true);

		//   Add delete window
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	/**
	 * Listenner of Events.
	 *
	 * @param e The event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		Dimension d = sp.getViewportSize();
		Dimension cd = cp.getSize();
		int dW = cd.width - d.width;
		int dH = cd.height - d.height;

		if (source == quitB)
			System.exit(0);
		if (source == setupD)
			dialog.setVisible(true);
		if (source == zoomOut) {
			zoom = zoom / 2;
			if (zoom < 2) zoom = 2;
			cp.width = (area.getLx() * zoom) / 122;
			cp.height = ((area.getLy() + (2 * dy)) * zoom) / 122;
			//cp.width = (widthX  * zoom) / 122;
			//cp.height =((heightY + (2 * dy)) * zoom) / 122;
			refresh();
			validate();
		}

		if (source == zoomIn) {
			zoom = zoom * 2;
			if (zoom > 2048) zoom = 2048;
			cp.width = (area.getLx() * zoom) / 122;
			cp.height = ((area.getLy() + (2 * dy)) * zoom) / 122;
			//cp.width = (widthX  * zoom) / 122;
			//cp.height =((heightY + (2 * dy)) * zoom) / 122;
			refresh();
			validate();
		}
	}

	@Override
	public void addLabel(String label, layout.util.Pt pt) {
		int aux1;
		for (aux1 = 0; aux1 < layer.size(); aux1++)
			if ("LABEL".equals(((LayerSetup) layer.get(aux1)).layer)) break;
		if (aux1 >= layer.size()) throw new RuntimeException("Label problem.");
		lst[aux1].add(new RegLabel(label, pt));
	}

	@Override
	public void addLine(Linea lin, Color c) {
		extras.add(new RegLine(lin, c));
	}

	@Override
	public void addRect(layout.util.Rectangle rec, Color c) {
		extras.add(new RegRectangle(rec, c));
	}

	@Override
	public void addRect(layout.util.Rectangle rec, String l) {
		int aux1;

		for (aux1 = 0; aux1 < layer.size(); aux1++)
			if (l.equals(((LayerSetup) layer.get(aux1)).layer)) break;
		if (aux1 >= layer.size())
			throw new RuntimeException("addRect: Unknown layer");

		setMaxProject(rec);
		//rec.c2.y = rec.c2.y * -1;
		lst[aux1].add(new RegRectangle(rec));
	}

	@Override
	public void addVector(Linea vect, Color c) {
		Pt aux = vect.getPointOfT(20);
		extras.add(new RegVector(vect.c1.x, vect.c1.y, aux.x, aux.y, c));
		refresh();
	}

	@Override
	public void addWire(layout.comp.Wire w) {
		w.drawOut(this);
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		Object obj = e.getAdjustable();

		//if (obj==hor) dx=20-e.getValue();
		//   else dy=20-e.getValue();

		cpSize = cp.getSize();
		spSize = sp.getViewportSize();
	}

	//addRect
	@Override
	public void clear() {
		for (List element : lst)
			element.clear();
		extras.clear();
		refresh();
	}

	@Override
	public void clearExtras() {
		extras.clear();
		refresh();
	}

	@Override
	public void doLayout() {
		super.doLayout();
	}

	@Override
	public void refresh() {
		cp.repaint();
	}

	// Save the tamanho maximo of project
	public void setMaxProject(layout.util.Rectangle rec) {

		if (area == null) area = new layout.util.Rectangle(rec);
		if (rec.c1.x < area.c1.x) area.c1.x = rec.c1.x;
		if (rec.c1.y < area.c1.y) area.c1.y = rec.c1.y;
		if (rec.c2.x > area.c2.x) area.c2.x = rec.c2.x;
		if (rec.c2.y > area.c2.y) area.c2.y = rec.c2.y;

		widthX = area.getLx();
		heightY = area.getLy();
	}
}
