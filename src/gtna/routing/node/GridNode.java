/*
 * ===========================================================
 * GTNA : Graph-Theoretic Network Analyzer
 * ===========================================================
 * 
 * (C) Copyright 2009-2011, by Benjamin Schiller (P2P, TU Darmstadt)
 * and Contributors
 * 
 * Project Info:  http://www.p2p.tu-darmstadt.de/research/gtna/
 * 
 * GTNA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GTNA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * ---------------------------------------
 * GridNode.java
 * ---------------------------------------
 * (C) Copyright 2009-2011, by Benjamin Schiller (P2P, TU Darmstadt)
 * and Contributors 
 * 
 * Original Author: Benjamin Schiller;
 * Contributors:    -;
 * 
 * Changes since 2011-05-17
 * ---------------------------------------
*/
package gtna.routing.node;

import gtna.graph.NodeImpl;
import gtna.routing.node.identifier.GridID;
import gtna.routing.node.identifier.Identifier;

import java.util.Random;

/**
 * @deprecated
 */
public class GridNode extends NodeImpl implements IDNode {
	public GridID id;

	public GridNode(int index, GridID id) {
		super(index);
		this.id = id;
	}

	public boolean contains(Identifier id) {
		return this.id.equals((GridID) id);
	}

	public double dist(Identifier id) {
		return this.id.dist((GridID) id);
	}

	public Identifier randomID(Random rand, NodeImpl[] nodes) {
		return ((GridNode) nodes[rand.nextInt(nodes.length)]).id;
	}

	public double dist(IDNode node) {
		return ((GridNode) node).id.dist(this.id);
	}

	public String toString() {
		return this.id.toString();
	}
}
