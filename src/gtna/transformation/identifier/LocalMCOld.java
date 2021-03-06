/* ===========================================================
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
 * LocalMC.java
 * ---------------------------------------
 * (C) Copyright 2009-2011, by Benjamin Schiller (P2P, TU Darmstadt)
 * and Contributors 
 *
 * Original Author: Stefanie Roos;
 * Contributors:    Benjamin Schiller;
 *
 * Changes since 2011-05-17
 * ---------------------------------------
 * 2011-06-05 : changed names of variables and parameters (BS)
 *
 */
package gtna.transformation.identifier;

import gtna.graph.Graph;
import gtna.routing.node.RingNode;
import gtna.routing.node.identifier.RingID;
import gtna.transformation.Transformation;
import gtna.transformation.TransformationImpl;

import java.util.Random;

public class LocalMCOld extends TransformationImpl implements Transformation {

	/*
	 * modi 1: any distance to neighbor is allowed 2: do not allow a new
	 * location with a neighbor in distance delta 3: any distance to neighbor is
	 * allowed, but nodes that are closer than minDist are not excluded from any
	 * calculation 4: any distance to neighbor is allowed, but nodes that are
	 * closer than minDist are treated as if they had distance minDist
	 * 
	 * MAINLY USED: mode 1, sometimes mode 2 for comparison
	 */
	int mode;
	// number of iterations (per node)
	int iterations;
	// probability to select an ID adjusted to an neighbor
	// contrary to just a random ID
	double p;
	// minimal distance to neighbor
	double delta;
	// nodes try to get within distance C*minDist when adjusting
	int c;
	// flag if nodes of degree 1 are included in algorithm
	// or simply adjust to their neighbor (improved version)
	boolean includeDegree1;

	// for calculating acceptance rate and change of distance
	// NOT NEEDED FOR ACTUAL ALGORITHM
	public double[] acceptanceRate;
	public double[] distanceRate;
	public double[] acceptanceRateNode;
	public double[] distanceRateNode;
	public double[] distanceRatePur;
	public double[] distanceRatePurNode;

	public LocalMCOld(int mode, int iterations, double p, double delta, int c,
			boolean includeDegree1) {
		super("LOCALMC", new String[] { "MODE", "ITERATIONS", "P", "DELTA",
				"C", "INCLUDE_DEGREE_1" }, new String[] { "" + mode,
				"" + iterations, "" + p, "" + delta, "" + c,
				"" + includeDegree1 });
		this.mode = mode;
		this.iterations = iterations;
		this.p = p;
		this.delta = delta;
		this.c = c;
		this.includeDegree1 = includeDegree1;
	}

	public boolean applicable(Graph g) {
		return g.nodes[0] instanceof RingNode;
	}

	public Graph transform(Graph g) {
		if (this.mode < 0) {
			this.mode = 1;
		}
		if (this.p < 0) {
			this.p = 0.5;
		}
		if (this.delta < 0) {
			this.delta = (double) 1
					/ (double) (g.nodes.length);
		}
		if (this.c < 0) {
			this.c = 10;
		}

		// initialize variables
		int count = 0;
		RingNode cur, neighbor;
		int n = g.nodes.length;
		Random rand = new Random(System.currentTimeMillis());

		// begin: NOT NEEDED
		int pos = 0;
		int[] countsAll = new int[n];
		acceptanceRate = new double[this.iterations];
		distanceRate = new double[this.iterations];
		distanceRatePur = new double[this.iterations];
		acceptanceRateNode = new double[n];
		distanceRateNode = new double[n];
		distanceRatePurNode = new double[n];
		// end: NOT NEEDED

		while (count < this.iterations * g.nodes.length) {
			cur = (RingNode) g.nodes[rand.nextInt(n)];
			countsAll[cur.index()]++; // NOT NEEDED
			if (cur.out().length == 1 && !includeDegree1) {
				continue;
			}
			count++;

			// calculate product before
			double before = 1;
			for (int i = 0; i < cur.out().length; i++) {
				neighbor = (RingNode) cur.out()[i];
				if (!includeDegree1 && neighbor.out().length == 1) {

				} else {
					before = before * cur.dist(neighbor.getID());
				}
			}
			double oldLoc = cur.getID().pos;

			// decide which side of neighbor to take
			int sign;
			if (rand.nextBoolean()) {
				sign = 1;
			} else {
				sign = -1;
			}

			neighbor = (RingNode) cur.out()[rand.nextInt(cur.out().length)];
			if (rand.nextDouble() < p) {
				// adjust to neighbor
				cur.getID().pos = neighbor.getID().pos + sign * delta + sign
						* rand.nextDouble() * this.c * delta;
				while (cur.getID().pos > 1) {
					cur.getID().pos--;
				}
				while (cur.getID().pos < 0) {
					cur.getID().pos++;
				}
			} else {
				// choose random ID
				cur.getID().pos = rand.nextDouble();
			}

			// calculate product afterwards
			double after = 1;
			double curDist;
			for (int i = 0; i < cur.out().length; i++) {
				if (includeDegree1 || cur.out()[i].out().length > 1) {
					curDist = cur.dist(((RingNode) cur.out()[i]).getID());
					if (curDist < delta) {
						switch (this.mode) {
						case 1:
							after = after * curDist;
							break;
						case 2:
							cur.getID().pos = oldLoc;
							break;
						case 3:
							break;
						case 4:
							after = after * Math.max(delta, curDist);
							break;
						default:
							throw new IllegalArgumentException("Modus unknown");
						}
					} else {
						after = after * curDist;
					}
				}
			}

			// switch back, if new ID not better
			if (rand.nextDouble() > before / after) {
				cur.getID().pos = oldLoc;
			} else {
				// begin NOT NEEDED
				acceptanceRate[pos]++;
				RingID cid = new RingID(oldLoc);
				distanceRate[pos] = distanceRate[pos] + cur.dist(cid);
				distanceRatePur[pos] = distanceRatePur[pos] + cur.dist(cid);
				acceptanceRateNode[cur.index()]++;
				distanceRateNode[cur.index()] = distanceRateNode[cur.index()]
						+ cur.dist(cid);
				distanceRatePurNode[cur.index()] = distanceRatePurNode[cur
						.index()]
						+ cur.dist(cid);
				// end: NOT NEEDED
			}

			// begin NOT NEEDED
			if (count % g.nodes.length == 0) {
				distanceRate[pos] = (double) distanceRate[pos] / g.nodes.length;
				if (acceptanceRate[pos] > 0)
					distanceRatePur[pos] = (double) distanceRatePur[pos]
							/ acceptanceRate[pos];
				acceptanceRate[pos] = (double) acceptanceRate[pos]
						/ g.nodes.length;
				pos++;
			}
			// end NOT NEEDED

		}

		// begin NOT NEEDED
		for (int i = 0; i < g.nodes.length; i++) {
			if (countsAll[i] > 0) {
				distanceRateNode[i] = (double) distanceRateNode[i]
						/ countsAll[i];
				if (acceptanceRateNode[i] > 0)
					distanceRatePurNode[i] = (double) distanceRatePurNode[i]
							/ acceptanceRateNode[i];
				acceptanceRateNode[i] = (double) acceptanceRateNode[i]
						/ countsAll[i];
			}
		}
		// end NOT NEEDED

		// adjust degree 1 nodes
		if (!includeDegree1) {
			for (int i = 0; i < g.nodes.length; i++) {
				cur = (RingNode) g.nodes[i];
				if (cur.out().length == 1) {
					neighbor = (RingNode) cur.out()[0];
					cur.getID().pos = neighbor.getID().pos + delta
							* rand.nextDouble();
					while (cur.getID().pos > 1) {
						cur.getID().pos--;
					}
					while (cur.getID().pos < 0) {
						cur.getID().pos++;
					}
				}
			}
		}
		return g;
	}

}
