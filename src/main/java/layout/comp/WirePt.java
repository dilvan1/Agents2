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

package layout.comp;

/*
 *        WirePt
 */

import layout.util.Pt;

public class WirePt extends Pt implements Owned, Cloneable {

	Layer layer;
	int width;
	private Object owner;

	public WirePt(int x, int y, int w, Layer lay) {
		super(x, y);
		width = w;
		layer = lay;
	}

	public WirePt(Pt p1, int w, Layer lay) {
		super(p1);
		width = w;
		layer = lay;
	}

	@Override
	public WirePt clone() {
		try {
			WirePt obj = (WirePt) super.clone();
			obj.setOwner(null);
			return obj;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported  exception");
		}
	}

	@Override
	public boolean equals(Object obj) {
		try {
			WirePt wp = (WirePt) obj;
			return (super.equals(wp) && width == wp.width && layer().equals(wp.layer()));
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Object getOwner() {
		return owner;
	}
	//        Method not used
	/*
	public void set(WirePt wir) {
		super.set(wir);
		width= wir.width;
		layer= wir.layer();
	}
	 */

	public boolean isJoinable(WirePt pt) {
		return (x == pt.x && y == pt.y);
	}

	public Layer layer() {
		return layer;
	}

	@Override
	public void setOwner(Object obj) {
		owner = obj;
	}

	public void setWidth(int wid) {
		width = wid;
	}

	@Override
	public String toString() {
		return "(" + super.toString() + " " + width + " " + layer() + ")";
	}

	public int width() {
		return width;
	}
}
